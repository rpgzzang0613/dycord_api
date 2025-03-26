package kr.co.soymilk.dycord_api.member.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class OIDCUtil {

    private final RestClient restClient;
    private final SocialInfoProvider socialInfoProvider;

    private final ConcurrentMap<String, List<Jwk>> jwksCache = new ConcurrentHashMap<>();

    public String requestJwksUri(String platform) {
        String uri = socialInfoProvider.getOidcMetaUri(platform);

        OIDCMetaData metaDto = restClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatusCode::isError, ((request, response) -> {
                    throw new HttpServerErrorException(response.getStatusCode(), "Cannot get jwks_uri");
                }))
                .body(OIDCMetaData.class);

        if (metaDto == null) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot get jwks_uri");
        }

        return metaDto.getJwks_uri();
    }

    public List<Jwk> getJwksWithCache(String jwksUri) {
        if (jwksCache.containsKey(jwksUri)) {
            return jwksCache.get(jwksUri);
        }

        return requestJwks(jwksUri);
    }

    public List<Jwk> getJwksWithoutCache(String jwksUri) {
        return requestJwks(jwksUri);
    }

    private List<Jwk> requestJwks(String jwksUri) {
        JwkResponse oidcResDto = restClient.get()
                .uri(jwksUri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    HttpStatusCode httpStatusCode = response.getStatusCode();

                    String bodyStr = new String(response.getBody().readAllBytes());

                    if (httpStatusCode.is4xxClientError()) {
                        throw new HttpClientErrorException(httpStatusCode, bodyStr.isEmpty() ? response.getStatusText() : bodyStr);
                    } else {
                        throw new HttpServerErrorException(httpStatusCode, bodyStr.isEmpty() ? response.getStatusText() : bodyStr);
                    }
                })
                .body(JwkResponse.class);

        if (oidcResDto == null || oidcResDto.getKeys() == null) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "OIDC Keys 조회 실패");
        }

        jwksCache.put(jwksUri, oidcResDto.getKeys());

        return oidcResDto.getKeys();
    }

    public boolean validateUnsignedPayload(String payload, String nonce, String platform) {
        byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
        ObjectMapper objectMapper = new ObjectMapper();
        IdTokenPayload idTokenPayload;
        try {
            idTokenPayload = objectMapper.readValue(decodedBytes, IdTokenPayload.class);
        } catch (IOException e) {
            idTokenPayload = null;
        }

        if (idTokenPayload == null) {
            return false;
        }

        String iss = socialInfoProvider.getOidcIss(platform);
        String aud = socialInfoProvider.getClientId(platform);

        boolean isValidIss = idTokenPayload.getIss().equals(iss);
        boolean isValidAud = idTokenPayload.getAud().equals(aud);
        boolean isValidExp = idTokenPayload.getExp() > System.currentTimeMillis() / 1000;
        boolean isValidNonce = idTokenPayload.getNonce().equals(nonce);

        return isValidIss && isValidAud && isValidExp && isValidNonce;
    }

    public FilteredJwkResult filterJwk(String header, List<Jwk> jwks) {

        IdTokenHeader idTokenHeader = parseIdTokenHeader(header);
        if (idTokenHeader == null) {
            return null;
        }

        Jwk filteredJwk = jwks.stream()
                .filter(key -> key.getKid().equals(idTokenHeader.getKid()) && key.getAlg().equals(idTokenHeader.getAlg()))
                .findFirst()
                .orElse(null);

        FilteredJwkResult jwkFilterRes = new FilteredJwkResult();
        jwkFilterRes.setJwk(filteredJwk);

        return jwkFilterRes;
    }

    private IdTokenHeader parseIdTokenHeader(String header) {
        byte[] decodedHeaderBytes = Base64.getUrlDecoder().decode(header);

        IdTokenHeader idTokenHeader;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            idTokenHeader = objectMapper.readValue(decodedHeaderBytes, IdTokenHeader.class);
        } catch (IOException e) {
            idTokenHeader = null;
        }

        return idTokenHeader;
    }

    public Claims parsePayloadFromVerifiedIdToken(String idToken, Jwk jwk) {
        PublicKey publicKey = generatePublicKey(jwk);

        Claims payload;
        try {
            payload = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(idToken)
                    .getPayload();
        } catch (Exception e) {
            payload = null;
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
