package kr.co.soymilk.dycord_api.member.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import kr.co.soymilk.dycord_api.member.dto.oauth2.OAuth2RestDto;
import kr.co.soymilk.dycord_api.member.dto.oauth2.naver.NaverRestDto;
import kr.co.soymilk.dycord_api.member.dto.oauth2.oidc.Jwk;
import kr.co.soymilk.dycord_api.member.dto.oauth2.oidc.JwkFilterResult;
import kr.co.soymilk.dycord_api.member.dto.oauth2.oidc.OIDCProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2ProfileProvider {

    private final RestClient restClient;
    private final OIDCUtil oidcUtil;
    private final SocialInfoProvider socialInfoProvider;

    public OIDCProfile getProfileFromIdToken(String idToken, String nonce, String platform) {
        String[] tokenArr = idToken.split("\\.");
        String header = tokenArr[0];
        String payload = tokenArr[1];

        // 서명 검증 전 페이로드 부분만 따로 검증
        boolean isValidPayload = oidcUtil.validateUnsignedPayload(payload, nonce, platform);
        if (!isValidPayload) {
            return null;
        }

        // OIDC 프로바이더로부터 jwks_uri 조회
        String jwksUri = oidcUtil.requestJwksUri(platform);

        // 조회한 jwks_uri로부터 jwks 조회 (캐시에 있으면 그걸로 꺼내옴)
        Set<Jwk> jwks = oidcUtil.getJwksWithCache(jwksUri);

        // jwks와 헤더의 kid를 비교하여 사용할 jwk 추출
        JwkFilterResult filterRes = oidcUtil.filterJwk(header, jwks);
        if (filterRes == null) {
            return null;
        }

        if (filterRes.isExpired()) {
            // OIDC 프로바이더가 jwks를 갱신한 경우이므로 캐시없이 jwks_uri로부터 jwks 재조회
            jwks = oidcUtil.getJwksWithoutCache(jwksUri);

            // jwks와 헤더의 kid를 비교하여 사용할 jwk 추출
            filterRes = oidcUtil.filterJwk(header, jwks);
            if (filterRes == null) {
                return null;
            }

            if (filterRes.isExpired()) {
                throw new IllegalStateException("jwks 갱신되어 재조회 시도했으나 조회 실패");
            }
        }

        // 추출한 jwk로 퍼블릭키를 생성하여 id_token 검증 후 payload 변환
        Claims verifiedPayload = oidcUtil.parsePayloadFromVerifiedIdToken(idToken, filterRes.getJwk());

        if (verifiedPayload == null || verifiedPayload.getSubject() == null) {
            return null;
        }

        // 검증된 payload로부터 social uid 추출하여 반환
        return OIDCProfile.builder()
                .id(verifiedPayload.getSubject())
                .build();
    }

    public NaverRestDto.ProfileResponse requestNaverProfileByToken(String accessToken) {
        String uri = socialInfoProvider.getProfileUri("naver");

        return restClient.get()
                .uri(uri)
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .exchange((request, response) -> {
                    HttpStatusCode httpStatusCode = response.getStatusCode();
                    ObjectMapper objectMapper = new ObjectMapper();
                    NaverRestDto.ProfileResponse naverProfileResDto = objectMapper.readValue(response.getBody(), new TypeReference<>() {});

                    if (httpStatusCode.isError()) {
                        if (naverProfileResDto != null && !"00".equals(naverProfileResDto.getResultcode())) {
                            // 네이버에서 에러를 반환한 경우
                            OAuth2RestDto.ErrorResponse errResDto = new OAuth2RestDto.ErrorResponse();
                            errResDto.setError(naverProfileResDto.getResultcode());
                            errResDto.setError_description(naverProfileResDto.getMessage());

                            if (httpStatusCode.is4xxClientError()) {
                                throw new HttpClientErrorException(httpStatusCode, errResDto.toJsonString());
                            } else {
                                throw new HttpServerErrorException(httpStatusCode, errResDto.toJsonString());
                            }

                        } else {
                            // 예기치 못한 에러일때
                            if (httpStatusCode.is4xxClientError()) {
                                throw new HttpClientErrorException(httpStatusCode, response.getStatusText());
                            } else {
                                throw new HttpServerErrorException(httpStatusCode, response.getStatusText());
                            }
                        }
                    }

                    return naverProfileResDto;
                });
    }

}
