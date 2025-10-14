package kr.co.soymilk.dycord_api.member.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import kr.co.soymilk.dycord_api.member.dto.oauth2.OAuth2RestDto;
import kr.co.soymilk.dycord_api.member.dto.oauth2.oidc.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class OIDCUtil {

    private final RestClient restClient;
    private final SocialInfoProvider socialInfoProvider;

    private final ConcurrentMap<String, Set<Jwk>> jwksCache = new ConcurrentHashMap<>();

    public Jwk getFilteredJwk(String idToken, OAuth2RestDto.TokenRequest request) {
        String[] tokenArr = idToken.split("\\.");
        String header = tokenArr[0];
        String payload = tokenArr[1];

        // 서명 검증 전 페이로드 부분만 따로 유효성 검사
        boolean isValidPayload = checkUnsignedPayload(payload, request);
        if (!isValidPayload) {
            throw new IllegalStateException("유효하지 않은 id token Payload");
        }

        // OIDC 프로바이더로부터 jwks_uri 조회
        String jwksUri = requestJwksUri(request.getPlatform());

        // jwk 필터링 시도 횟수
        int retryFilterCnt = 0;

        // 조회한 jwks_uri로부터 jwks 조회 (캐시에 있으면 그걸로 꺼내옴)
        Set<Jwk> jwks = getJwks(jwksUri, CacheMode.CACHE);

        // jwks와 헤더의 kid를 비교하여 사용할 jwk 추출
        Jwk filteredJwk = filterJwk(header, jwks, retryFilterCnt);
        if (filteredJwk == null) {
            retryFilterCnt++;

            // OIDC 프로바이더가 jwks를 갱신한 경우이므로 캐시없이 jwks_uri로부터 jwks 재조회
            jwks = getJwks(jwksUri, CacheMode.NO_CACHE);

            // jwks와 헤더의 kid를 비교하여 사용할 jwk 추출
            filteredJwk = filterJwk(header, jwks, retryFilterCnt);
        }

        return filteredJwk;
    }

    private String requestJwksUri(String platform) {
        // 메타데이터 정보를 요청할 url 세팅
        String uri = socialInfoProvider.getOidcMetaUri(platform);

        // 요청 및 메타데이터 응답 반환
        OIDCRestDto.MetaDataResponse metaRes = restClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatusCode::isError, ((request, response) -> {
                    throw new HttpServerErrorException(response.getStatusCode(), "Cannot get jwks_uri");
                }))
                .body(OIDCRestDto.MetaDataResponse.class);

        if (metaRes == null) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot get jwks_uri");
        }

        // 메타데이터에서 jwks_uri 추출 후 반환
        return metaRes.getJwks_uri();
    }

    private Set<Jwk> getJwks(String jwksUri, CacheMode mode) {
        if (mode == CacheMode.NO_CACHE) {
            // 캐시없이 바로 url에 요청 후 리턴
            return requestJwks(jwksUri);
        }

        // 캐시에 있으면 있는 값 리턴, 없으면 url에 요청 후 캐시에 저장하고 리턴
        return jwksCache.computeIfAbsent(jwksUri, this::requestJwks);
    }

    private Set<Jwk> requestJwks(String jwksUri) {
        // 요청 및 jwks 응답 반환
        OIDCRestDto.JwksResponse oidcResDto = restClient.get()
                .uri(jwksUri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    // Http 응답 코드가 2xx가 아닐 경우 실행되는 함수
                    HttpStatusCode httpStatusCode = response.getStatusCode();

                    String bodyStr = new String(response.getBody().readAllBytes());

                    if (httpStatusCode.is4xxClientError()) {
                        throw new HttpClientErrorException(httpStatusCode, bodyStr.isEmpty() ? response.getStatusText() : bodyStr);
                    } else {
                        throw new HttpServerErrorException(httpStatusCode, bodyStr.isEmpty() ? response.getStatusText() : bodyStr);
                    }
                })
                .body(OIDCRestDto.JwksResponse.class);

        if (oidcResDto == null || oidcResDto.getKeys() == null) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "OIDC Keys 조회 실패");
        }

        // jwks 반환
        return oidcResDto.getKeys();
    }

    private boolean checkUnsignedPayload(String payload, OAuth2RestDto.TokenRequest request) {
        byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
        ObjectMapper objectMapper = new ObjectMapper();
        IdTokenDto.Payload idTokenPayload;
        try {
            idTokenPayload = objectMapper.readValue(decodedBytes, IdTokenDto.Payload.class);
        } catch (IOException e) {
            throw new IllegalStateException("id token Payload 파싱 실패");
        }

        String iss = socialInfoProvider.getOidcIss(request.getPlatform());
        String aud = socialInfoProvider.getClientId(request.getPlatform());

        long curSec = System.currentTimeMillis() / 1000;

        boolean isValidIss = idTokenPayload.getIss().equals(iss);
        boolean isValidAud = idTokenPayload.getAud().equals(aud);
        boolean isValidExp = idTokenPayload.getExp() > curSec;
        boolean isValidIat = false;

        if (idTokenPayload.getIat() != null) {
            final long MAX_AGE_SEC = 600;  // 발급 10분 지나면 무효
            long tokenAge = curSec - idTokenPayload.getIat();
            isValidIat = tokenAge >= 0 && tokenAge < MAX_AGE_SEC;
        }

        boolean isValidNonce = true;
        if (!request.getPlatform().equals("naver")) {
            isValidNonce = idTokenPayload.getNonce().equals(request.getNonce());
        }

        return isValidIss && isValidAud && isValidExp && isValidIat && isValidNonce;
    }

    private Jwk filterJwk(String header, Set<Jwk> jwks, int retryCnt) {
        // id token의 헤더 파싱
        IdTokenDto.Header idTokenHeader = Optional.of(parseIdTokenHeader(header))
                .orElseThrow(() -> new IllegalStateException("id token의 헤더 파싱 실패"));

        // 헤더의 kid, alg와 일치하는 jwk 추출
        return jwks.stream()
                .filter(key -> key.getKid().equals(idTokenHeader.getKid()))
                .findFirst()
                .orElseGet(() -> {
                    if (retryCnt == 0) {
                        // 첫시도인 경우 null 리턴
                        return null;
                    }

                    // 재시도인 경우 예외
                    throw new IllegalStateException("jwks 갱신 후 재조회 시도했으나 매칭되는 jwk 조회 실패");
                });
    }

    private IdTokenDto.Header parseIdTokenHeader(String header) {
        byte[] decodedHeaderBytes = Base64.getUrlDecoder().decode(header);

        IdTokenDto.Header idTokenHeader;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            idTokenHeader = objectMapper.readValue(decodedHeaderBytes, IdTokenDto.Header.class);
        } catch (IOException e) {
            idTokenHeader = null;
        }

        return idTokenHeader;
    }

    public Claims validateAndExtractIdToken(String idToken, Jwk jwk) {
        PublicKey publicKey = generatePublicKey(jwk);

        Claims payload;
        try {
            payload = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(idToken)
                    .getPayload();
        } catch (Exception e) {
            throw new IllegalStateException("id token 검증 및 파싱 실패");
        }

        return payload;
    }

    private PublicKey generatePublicKey(Jwk publicKey) {
        byte[] nBytes = Base64.getUrlDecoder().decode(publicKey.getN());
        byte[] eBytes = Base64.getUrlDecoder().decode(publicKey.getE());

        RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(new BigInteger(1, nBytes), new BigInteger(1, eBytes));

        try {
            return KeyFactory.getInstance(publicKey.getKty()).generatePublic(rsaPublicKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            return null;
        }
    }

}
