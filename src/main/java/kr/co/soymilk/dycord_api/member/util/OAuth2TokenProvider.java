package kr.co.soymilk.dycord_api.member.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.soymilk.dycord_api.member.dto.oauth2.OAuth2RestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2TokenProvider {

    private final RestClient restClient;
    private final SocialInfoProvider socialInfoProvider;

    public OAuth2RestDto.TokenResponse requestOAuth2TokenByCode(OAuth2RestDto.TokenRequest authRequest) {
        // body 세팅 (content-type: x-www-form-urlencoded 이므로 MultiValueMap에 담아 전달)
        String grantType = "authorization_code";
        String clientId = socialInfoProvider.getClientId(authRequest.getPlatform());
        String clientSecret = socialInfoProvider.getClientSecret(authRequest.getPlatform());
        String redirectUri = socialInfoProvider.getRedirectUri(authRequest.getPlatform());

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.add("grant_type", grantType);
        requestMap.add("client_id", clientId);
        requestMap.add("client_secret", clientSecret);
        requestMap.add("code", authRequest.getCode());
        requestMap.add("redirect_uri", redirectUri);
        requestMap.add("code_verifier", authRequest.getCodeVerifier());

        if (authRequest.getPlatform().equals("naver")) {
            requestMap.add("state", authRequest.getState());
        }

        // 토큰을 요청할 주소 세팅
        String requestUri = socialInfoProvider.getTokenUri(authRequest.getPlatform());

        // 요청 및 결과 반환
        return restClient.post()
                .uri(requestUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(requestMap)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    // Http 응답 코드가 2xx가 아닐 경우 실행되는 함수
                    HttpStatusCode httpStatusCode = response.getStatusCode();

                    ObjectMapper objectMapper = new ObjectMapper();
                    OAuth2RestDto.ErrorResponse errResDto = objectMapper.readValue(response.getBody(), new TypeReference<>() {});

                    if (httpStatusCode.is4xxClientError()) {
                        throw new HttpClientErrorException(httpStatusCode, errResDto.toJsonString());
                    }

                    throw new HttpServerErrorException(httpStatusCode, errResDto.toJsonString());
                })
                .body(OAuth2RestDto.TokenResponse.class);
    }

}
