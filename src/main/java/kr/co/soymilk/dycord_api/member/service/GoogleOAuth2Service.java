package kr.co.soymilk.dycord_api.member.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.soymilk.dycord_api.member.dto.oauth2.OAuth2ErrorResponseDto;
import kr.co.soymilk.dycord_api.member.dto.oauth2.OIDCTokenResponseDto;
import kr.co.soymilk.dycord_api.member.dto.oauth2.google.GoogleTokenResponseDto;
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
public class GoogleOAuth2Service {

    private final RestClient restClient;
    private final Environment env;

    public OIDCTokenResponseDto requestGoogleTokenByCode(String code) {
        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.add("grant_type", "authorization_code");
        requestMap.add("client_id", env.getProperty("social.google.client_id"));
        requestMap.add("client_secret", env.getProperty("social.google.client_secret"));
        requestMap.add("redirect_uri", env.getProperty("social.google.redirect_uri"));
        requestMap.add("code", code);

        return restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(requestMap)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    HttpStatusCode httpStatusCode = response.getStatusCode();

                    ObjectMapper objectMapper = new ObjectMapper();
                    OAuth2ErrorResponseDto errResDto = objectMapper.readValue(response.getBody(), new TypeReference<>() {});

                    if (httpStatusCode.is4xxClientError()) {
                        throw new HttpClientErrorException(httpStatusCode, errResDto.toJsonString());
                    }

                    throw new HttpServerErrorException(httpStatusCode, errResDto.toJsonString());
                })
                .body(GoogleTokenResponseDto.class);
    }
}
