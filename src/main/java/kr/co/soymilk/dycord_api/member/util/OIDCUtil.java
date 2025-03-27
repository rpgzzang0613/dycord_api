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

    public String requestJwksUri(String platform) {
        String uri = socialInfoProvider.getOidcMetaUri(platform);

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

        return metaRes.getJwks_uri();
    }

    public Set<Jwk> getJwksWithCache(String jwksUri) {
        if (jwksCache.containsKey(jwksUri)) {
            return jwksCache.get(jwksUri);
        }

        return requestJwks(jwksUri);
    }

    public Set<Jwk> getJwksWithoutCache(String jwksUri) {
        return requestJwks(jwksUri);
    }

    private Set<Jwk> requestJwks(String jwksUri) {
        OIDCRestDto.JwksResponse oidcResDto = restClient.get()
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
                .body(OIDCRestDto.JwksResponse.class);

        if (oidcResDto == null || oidcResDto.getKeys() == null) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "OIDC Keys 조회 실패");
        }

        jwksCache.put(jwksUri, oidcResDto.getKeys());

        return oidcResDto.getKeys();
    }

    public boolean validateUnsignedPayload(String payload, String nonce, String platform) {
        byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
        ObjectMapper objectMapper = new ObjectMapper();
        IdTokenDto.Payload idTokenPayload;
        try {
            idTokenPayload = objectMapper.readValue(decodedBytes, IdTokenDto.Payload.class);
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

    public JwkFilterResult filterJwk(String header, Set<Jwk> jwks) {

        IdTokenDto.Header idTokenHeader = parseIdTokenHeader(header);
        if (idTokenHeader == null) {
            return null;
        }

        Jwk filteredJwk = jwks.stream()
                .filter(key -> key.getKid().equals(idTokenHeader.getKid()) && key.getAlg().equals(idTokenHeader.getAlg()))
                .findFirst()
                .orElse(null);

        JwkFilterResult jwkFilterRes = new JwkFilterResult();
        jwkFilterRes.setJwk(filteredJwk);

        return jwkFilterRes;
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
