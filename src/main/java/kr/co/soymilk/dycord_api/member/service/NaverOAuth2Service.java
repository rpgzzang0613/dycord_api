package kr.co.soymilk.dycord_api.member.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.soymilk.dycord_api.member.dto.oauth2.OAuth2ErrorResponseDto;
import kr.co.soymilk.dycord_api.member.dto.oauth2.naver.NaverProfileResponseDto;
import kr.co.soymilk.dycord_api.member.dto.oauth2.naver.NaverTokenResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class NaverOAuth2Service {

    private final RestClient restClient;
    private final Environment env;

    public NaverTokenResponseDto requestNaverTokenByCode(String code, String state) {
        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.add("grant_type", "authorization_code");
        requestMap.add("client_id", env.getProperty("social.naver.client_id"));
        requestMap.add("client_secret", env.getProperty("social.naver.client_secret"));
        requestMap.add("code", code);
        requestMap.add("state", state);

        return restClient.post()
                .uri("https://nid.naver.com/oauth2.0/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(requestMap)
                .exchange((request, response) -> {
                    HttpStatusCode httpStatusCode = response.getStatusCode();
                    ObjectMapper objectMapper = new ObjectMapper();
                    NaverTokenResponseDto naverTokenResDto = objectMapper.readValue(response.getBody(), new TypeReference<>() {});

                    if (httpStatusCode.isError()) {
                        if (naverTokenResDto != null && naverTokenResDto.getError() != null) {
                            // 네이버에서 에러를 반환한 경우
                            OAuth2ErrorResponseDto errResDto = OAuth2ErrorResponseDto.builder()
                                    .error(naverTokenResDto.getError())
                                    .error_description(naverTokenResDto.getError_description())
                                    .build();

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
                        OAuth2ErrorResponseDto errResDto = OAuth2ErrorResponseDto.builder()
                                .error(naverTokenResDto.getError())
                                .error_description(naverTokenResDto.getError_description())
                                .build();

                        throw new HttpClientErrorException(httpStatusCode, errResDto.toJsonString());
                    }

                    return naverTokenResDto;
                });
    }

    public NaverProfileResponseDto requestNaverProfileByToken(String accessToken) {
        return restClient.get()
                .uri("https://openapi.naver.com/v1/nid/me")
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .exchange((request, response) -> {
                    HttpStatusCode httpStatusCode = response.getStatusCode();
                    ObjectMapper objectMapper = new ObjectMapper();
                    NaverProfileResponseDto naverProfileResDto = objectMapper.readValue(response.getBody(), new TypeReference<>() {});

                    if (httpStatusCode.isError()) {
                        if (naverProfileResDto != null && !"00".equals(naverProfileResDto.getResultcode())) {
                            // 네이버에서 에러를 반환한 경우
                            OAuth2ErrorResponseDto errResDto = OAuth2ErrorResponseDto.builder()
                                    .error(naverProfileResDto.getResultcode())
                                    .error_description(naverProfileResDto.getMessage())
                                    .build();

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
