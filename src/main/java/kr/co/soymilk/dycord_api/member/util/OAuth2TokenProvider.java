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
        MultiValueMap<String, String> requestMap = getCommonRequestBody(authRequest.getCode(), authRequest.getPlatform());
        String redirectUri = socialInfoProvider.getRedirectUri(authRequest.getPlatform());
        requestMap.add("redirect_uri", redirectUri);

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

    public OAuth2RestDto.TokenResponse requestNaverTokenByCode(OAuth2RestDto.TokenRequest authRequest) {
        // body 세팅 (content-type: x-www-form-urlencoded 이므로 MultiValueMap에 담아 전달)
        MultiValueMap<String, String> requestMap = getCommonRequestBody(authRequest.getCode(), authRequest.getPlatform());
        requestMap.add("state", authRequest.getState());

        // 토큰을 요청할 주소 세팅
        String requestUri = socialInfoProvider.getTokenUri(authRequest.getPlatform());

        // 요청 및 결과 반환
        return restClient.post()
                .uri(requestUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(requestMap)
                .exchange((request, response) -> {
                    // Http 응답 코드와 관계 없이 실행되는 함수

                    HttpStatusCode httpStatusCode = response.getStatusCode();
                    ObjectMapper objectMapper = new ObjectMapper();
                    OAuth2RestDto.TokenResponse naverTokenResDto = objectMapper.readValue(response.getBody(), new TypeReference<>() {});

                    if (httpStatusCode.isError()) {
                        if (naverTokenResDto != null && naverTokenResDto.getError() != null) {
                            // 네이버에서 에러를 반환한 경우
                            OAuth2RestDto.ErrorResponse errResDto = new OAuth2RestDto.ErrorResponse();
                            errResDto.setError(naverTokenResDto.getError());
                            errResDto.setError_description(naverTokenResDto.getError_description());

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

                    if (naverTokenResDto.getError() != null) {
                        // HttpStatus는 200이지만 네이버에서 에러를 반환한 경우
                        OAuth2RestDto.ErrorResponse errResDto = new OAuth2RestDto.ErrorResponse();
                        errResDto.setError(naverTokenResDto.getError());
                        errResDto.setError_description(naverTokenResDto.getError_description());

                        throw new HttpClientErrorException(httpStatusCode, errResDto.toJsonString());
                    }

                    return naverTokenResDto;
                });
    }

    private MultiValueMap<String, String> getCommonRequestBody(String code, String platform) {
        String grantType = "authorization_code";
        String clientId = socialInfoProvider.getClientId(platform);
        String clientSecret = socialInfoProvider.getClientSecret(platform);

        MultiValueMap<String, String> bodyMap = new LinkedMultiValueMap<>();
        bodyMap.add("grant_type", grantType);
        bodyMap.add("client_id", clientId);
        bodyMap.add("client_secret", clientSecret);
        bodyMap.add("code", code);

        return bodyMap;
    }

}
