package kr.co.soymilk.dycord_api.member.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.soymilk.dycord_api.common.util.JwtOIDCUtil;
import kr.co.soymilk.dycord_api.member.dto.auth.social.kakao.KakaoErrorResponseDto;
import kr.co.soymilk.dycord_api.member.dto.auth.social.kakao.KakaoTokenResponseDto;
import kr.co.soymilk.dycord_api.member.dto.auth.social.naver.NaverErrorResponseDto;
import kr.co.soymilk.dycord_api.member.dto.auth.social.naver.NaverProfileDto;
import kr.co.soymilk.dycord_api.member.dto.auth.social.naver.NaverProfileResponseDto;
import kr.co.soymilk.dycord_api.member.dto.auth.social.naver.NaverTokenResponseDto;
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
public class SocialService {

    private final RestClient restClient;
    private final JwtOIDCUtil jwtOIDCUtil;
    private final Environment env;
    private final ProfileService profileService;

    public void processKakaoAuth(String code, String nonce) {
        KakaoTokenResponseDto kakaoTokenResDto = requestKakaoTokenByCode(code);

        // TODO id_token 검증 후 uid 뽑아내기
    }

    private KakaoTokenResponseDto requestKakaoTokenByCode(String code) {
        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.add("grant_type", "authorization_code");
        requestMap.add("client_id", env.getProperty("social.kakao.client_id"));
        requestMap.add("client_secret", env.getProperty("social.kakao.client_secret"));
        requestMap.add("redirect_uri", env.getProperty("social.kakao.redirect_uri"));
        requestMap.add("code", code);

        return restClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(requestMap)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    HttpStatusCode httpStatus = response.getStatusCode();

                    ObjectMapper objectMapper = new ObjectMapper();
                    KakaoErrorResponseDto kakaoErrResDto = objectMapper.readValue(response.getBody(), new TypeReference<>() {});

                    if (httpStatus.is4xxClientError()) {
                        throw new HttpClientErrorException(httpStatus, kakaoErrResDto.toJsonString());
                    }

                    throw new HttpServerErrorException(httpStatus, kakaoErrResDto.toJsonString());
                })
                .body(KakaoTokenResponseDto.class);
    }

    public NaverProfileDto processNaverAuth(String code, String state) {
        NaverTokenResponseDto naverTokenResDto = requestNaverTokenByCode(code, state);
        NaverProfileResponseDto naverProfileResDto = requestNaverProfileByToken(naverTokenResDto.getAccess_token());

        String naverUid = naverProfileResDto.getResponse().getId();

        // TODO DB에서 socialId 조회해서 가입유무 확인 후 가입 했으면 dycord 토큰 발급하도록 수정하기
        return naverProfileResDto.getResponse();
    }

    private NaverTokenResponseDto requestNaverTokenByCode(String code, String state) {
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
                    HttpStatusCode httpStatus = response.getStatusCode();
                    ObjectMapper objectMapper = new ObjectMapper();
                    NaverTokenResponseDto naverTokenResDto = objectMapper.readValue(response.getBody(), new TypeReference<>() {});

                    if (httpStatus.isError()) {
                        if (naverTokenResDto != null && naverTokenResDto.getError() != null) {
                            // 네이버에서 에러를 반환한 경우
                            NaverErrorResponseDto naverErrResDto = new NaverErrorResponseDto();
                            naverErrResDto.setError(naverTokenResDto.getError());
                            naverErrResDto.setError_description(naverTokenResDto.getError_description());

                            if (httpStatus.is4xxClientError()) {
                                throw new HttpClientErrorException(httpStatus, naverErrResDto.toJsonString());
                            } else {
                                throw new HttpServerErrorException(httpStatus, naverErrResDto.toJsonString());
                            }

                        } else {
                            // 예기치 못한 에러일때
                            if (httpStatus.is4xxClientError()) {
                                throw new HttpClientErrorException(httpStatus, response.getStatusText());
                            } else {
                                throw new HttpServerErrorException(httpStatus, response.getStatusText());
                            }
                        }
                    }

                    if (naverTokenResDto.getError() != null) {
                        // HttpStatus는 200이지만 네이버에서 에러를 반환한 경우
                        NaverErrorResponseDto naverErrResDto = new NaverErrorResponseDto();
                        naverErrResDto.setError(naverTokenResDto.getError());
                        naverErrResDto.setError_description(naverTokenResDto.getError_description());
                        throw new HttpClientErrorException(httpStatus, naverErrResDto.toJsonString());
                    }

                    return naverTokenResDto;
                });
    }

    private NaverProfileResponseDto requestNaverProfileByToken(String accessToken) {
        return restClient.get()
                .uri("https://openapi.naver.com/v1/nid/me")
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .exchange((request, response) -> {
                    HttpStatusCode httpStatus = response.getStatusCode();
                    ObjectMapper objectMapper = new ObjectMapper();
                    NaverProfileResponseDto naverProfileResDto = objectMapper.readValue(response.getBody(), new TypeReference<>() {});

                    if (httpStatus.isError()) {
                        if (naverProfileResDto != null && !"00".equals(naverProfileResDto.getResultcode())) {
                            // 네이버에서 에러를 반환한 경우
                            NaverErrorResponseDto naverErrResDto = new NaverErrorResponseDto();
                            naverErrResDto.setError(naverProfileResDto.getResultcode());
                            naverErrResDto.setError_description(naverProfileResDto.getMessage());

                            if (httpStatus.is4xxClientError()) {
                                throw new HttpClientErrorException(httpStatus, naverErrResDto.toJsonString());
                            } else {
                                throw new HttpServerErrorException(httpStatus, naverErrResDto.toJsonString());
                            }

                        } else {
                            // 예기치 못한 에러일때
                            if (httpStatus.is4xxClientError()) {
                                throw new HttpClientErrorException(httpStatus, response.getStatusText());
                            } else {
                                throw new HttpServerErrorException(httpStatus, response.getStatusText());
                            }
                        }
                    }

                    return naverProfileResDto;
                });
    }

}
