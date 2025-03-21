package kr.co.soymilk.dycord_api.member.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import kr.co.soymilk.dycord_api.member.dto.oauth2.*;
import kr.co.soymilk.dycord_api.member.dto.oauth2.OIDCKeysResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class OIDCUtil {

    private final RestClient restClient;
    private final Environment env;

    public String getJwksUri(String platform) {
        String path = "/.well-known/openid-configuration";

        String uri = switch (platform) {
            case "kakao" -> "https://kauth.kakao.com" + path;
            case "google" -> "https://accounts.google.com" + path;
            default -> throw new IllegalStateException("Unexpected platform: " + platform);
        };

        OIDCMetaDataDto metaDto = restClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatusCode::isError, ((request, response) -> {
                    throw new HttpServerErrorException(response.getStatusCode(), "Cannot get jwks_uri");
                }))
                .body(OIDCMetaDataDto.class);

        if (metaDto == null) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot get jwks_uri");
        }

        return metaDto.getJwks_uri();
    }

    // TODO 매번 조회하지 말고 캐싱해두도록 변경하기
    public List<OIDCPublicKey> getOIDCPublicKeys(String jwksUri) {
        OIDCKeysResponseDto oidcResDto = restClient.get()
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
                .body(OIDCKeysResponseDto.class);

        if (oidcResDto == null || oidcResDto.getKeys() == null) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "OIDC Keys 조회 실패");
        }

        return oidcResDto.getKeys();
    }

    public OAuth2ProfileDto getPayloadFromIdToken(String idToken, String nonce, String platform) {
        String[] tokenArr = idToken.split("\\.");
        String header = tokenArr[0];
        String payload = tokenArr[1];

        boolean isValidPayload = validatePayload(payload, nonce, platform);
        if (!isValidPayload) {
            return null;
        }

        String jwksUri = getJwksUri(platform);

        List<OIDCPublicKey> publicKeys = getOIDCPublicKeys(jwksUri);

        OIDCPublicKey oidcPublicKey = filterOIDCKey(header, publicKeys);
        if (oidcPublicKey == null) {
            return null;
        }

        Claims verifiedPayload = parsePayloadFromVerifiedIdToken(idToken, oidcPublicKey);

        if (verifiedPayload == null || verifiedPayload.getSubject() == null) {
            return null;
        }

        return OAuth2ProfileDto.builder()
                .id(verifiedPayload.getSubject())
                .build();
    }

    public boolean validatePayload(String payload, String nonce, String platform) {
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

        String iss = switch (platform) {
            case "kakao" -> "https://kauth.kakao.com";
            case "google" -> "https://accounts.google.com";
            default -> "";
        };

        String aud = switch (platform) {
            case "kakao" -> env.getProperty("social.kakao.client_id");
            case "google" -> env.getProperty("social.google.client_id");
            default -> "";
        };

        boolean isValidIss = idTokenPayload.getIss().equals(iss);
        boolean isValidAud = idTokenPayload.getAud().equals(aud);
        boolean isValidExp = idTokenPayload.getExp() > System.currentTimeMillis() / 1000;
        boolean isValidNonce = idTokenPayload.getNonce().equals(nonce);

        return isValidIss && isValidAud && isValidExp && isValidNonce;
    }

    public OIDCPublicKey filterOIDCKey(String header, List<OIDCPublicKey> oidcKeys) {

        IdTokenHeader idTokenHeader = parseIdTokenHeader(header);
        if (idTokenHeader == null) {
            return null;
        }

        return oidcKeys.stream()
                .filter(key -> key.getKid().equals(idTokenHeader.getKid()) && key.getAlg().equals(idTokenHeader.getAlg()))
                .findFirst()
                .orElse(null);
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

    public Claims parsePayloadFromVerifiedIdToken(String idToken, OIDCPublicKey oidcPublicKey) {
        PublicKey publicKey = generatePublicKey(oidcPublicKey);

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

    private PublicKey generatePublicKey(OIDCPublicKey publicKey) {
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
