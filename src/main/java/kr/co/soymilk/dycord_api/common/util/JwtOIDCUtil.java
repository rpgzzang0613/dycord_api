package kr.co.soymilk.dycord_api.common.util;

import io.jsonwebtoken.*;
import kr.co.soymilk.dycord_api.common.exception.InvalidJwtTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtOIDCUtil {

    private final RestClient restClient;

    // TODO 매번 조회하지 말고 캐싱해두도록 변경하기
    public void getKakaoOIDCKeys() {
        HashMap body = restClient.get()
                .uri("https://kauth.kakao.com/.well-known/jwks.json")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(HashMap.class);

        int a = 0;
    }

}
