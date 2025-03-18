package kr.co.soymilk.dycord_api.member.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.soymilk.dycord_api.common.util.JwtOIDCUtil;
import kr.co.soymilk.dycord_api.member.dto.auth.KakaoErrorDto;
import kr.co.soymilk.dycord_api.common.exception.ErrorFromKakaoException;
import kr.co.soymilk.dycord_api.member.dto.auth.KakaoTokenDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
        requestMap.add("redirect_uri", env.getProperty("social.kakao.redirect_uri"));
        requestMap.add("code", code);
        requestMap.add("client_secret", env.getProperty("social.kakao.client_secret"));

        return restClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(requestMap)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    // HttpStatus가 200 ok 아닐 경우
                    int httpStatusCode = response.getStatusCode().value();
                    String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);

                    ObjectMapper objectMapper = new ObjectMapper();
                    KakaoErrorDto kakaoErrorDto = objectMapper.readValue(body, KakaoErrorDto.class);

                    throw new ErrorFromKakaoException(httpStatusCode, kakaoErrorDto);
                })
                .body(KakaoTokenDto.class);
    }

}
