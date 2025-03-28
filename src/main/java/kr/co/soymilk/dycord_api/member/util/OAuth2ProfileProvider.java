package kr.co.soymilk.dycord_api.member.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import kr.co.soymilk.dycord_api.member.dto.oauth2.OAuth2RestDto;
import kr.co.soymilk.dycord_api.member.dto.oauth2.naver.NaverRestDto;
import kr.co.soymilk.dycord_api.member.dto.oauth2.oidc.OIDCProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2ProfileProvider {

    private final RestClient restClient;
    private final SocialInfoProvider socialInfoProvider;

    public OIDCProfile getProfileFromIdToken(Claims payload) {
        return OIDCProfile.builder()
                .id(payload.getSubject())
                .email(payload.get("email").toString())
                .build();
    }

    public NaverRestDto.ProfileResponse requestNaverProfileByToken(String accessToken) {
        // 프로필을 요청할 네이버 주소 세팅
        String uri = socialInfoProvider.getProfileUri("naver");

        // 요청 및 결과 반환
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
