package kr.co.soymilk.dycord_api.member.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.soymilk.dycord_api.common.exception.ErrorFromNaverException;
import kr.co.soymilk.dycord_api.common.util.JwtOIDCUtil;
import kr.co.soymilk.dycord_api.member.dto.auth.KakaoErrorDto;
import kr.co.soymilk.dycord_api.common.exception.ErrorFromKakaoException;
import kr.co.soymilk.dycord_api.member.dto.auth.KakaoTokenDto;
import kr.co.soymilk.dycord_api.member.dto.auth.NaverErrorDto;
import kr.co.soymilk.dycord_api.member.dto.auth.NaverTokenDto;
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

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class SocialService {

    private final RestClient restClient;
    private final JwtOIDCUtil jwtOIDCUtil;
    private final Environment env;

    public void processKakaoAuth(String code, String nonce) {
        KakaoTokenDto kakaoTokenDto = requestKakaoTokenByCode(code);

        // TODO id_token 검증 후 uid 뽑아내기
    }

    private KakaoTokenDto requestKakaoTokenByCode(String code) {
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
                    // 카카오는 자기네 에러코드 던질때 HttpStatus도 바꿔서 던지므로, HttpStatus가 200이라면 문제가 없다는 뜻
                    HttpStatusCode httpStatus = response.getStatusCode();

                    ObjectMapper objectMapper = new ObjectMapper();
                    KakaoErrorDto kakaoErrorDto = objectMapper.readValue(response.getBody(), new TypeReference<>() {});

                    // response body에 에러코드가 있다면 카카오와의 통신 자체는 문제가 없다는 의미
                    boolean isErrorFromKakao = kakaoErrorDto.getError() != null && kakaoErrorDto.getError_code() != null && kakaoErrorDto.getError_description() != null;
                    if (isErrorFromKakao) {
                        throw new ErrorFromKakaoException(httpStatus.value(), kakaoErrorDto);
                    }

                    if (httpStatus.is4xxClientError()) {
                        throw new HttpClientErrorException(httpStatus, response.getStatusText());
                    }

                    throw new HttpServerErrorException(httpStatus, response.getStatusText());
                })
                .body(KakaoTokenDto.class);
    }

    public void processNaverAuth(String code, String state) {
        NaverTokenDto naverTokenDto = requestNaverTokenByCode(code, state);

        int a = 0;
    }

    private NaverTokenDto requestNaverTokenByCode(String code, String state) {
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
                    // 네이버는 자기네 에러코드 던질때 HttpStatus 200으로 던지므로, HttpStatus가 200이어도 에러코드에 따라 처리를 따로 해줘야함

                    HttpStatusCode httpStatus = response.getStatusCode();

                    if (httpStatus.is4xxClientError()) {
                        throw new HttpClientErrorException(httpStatus, response.getStatusText());
                    }

                    if (httpStatus.is5xxServerError()) {
                        throw new HttpServerErrorException(httpStatus, response.getStatusText());
                    }

                    ObjectMapper objectMapper = new ObjectMapper();
                    NaverTokenDto tokenDto = objectMapper.readValue(response.getBody(), new TypeReference<>() {});

                    if (tokenDto.getError() != null) {
                        NaverErrorDto errorDto = new NaverErrorDto();
                        errorDto.setError(tokenDto.getError());
                        errorDto.setError_description(tokenDto.getError_description());

                        throw new ErrorFromNaverException(httpStatus.value(), errorDto);
                    }

                    return tokenDto;
                });
    }

}
