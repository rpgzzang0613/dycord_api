package kr.co.soymilk.dycord_api.member.controller;

import kr.co.soymilk.dycord_api.member.dto.oauth2.naver.NaverAuthRequest;
import kr.co.soymilk.dycord_api.member.dto.oauth2.oidc.OIDCAuthRequest;
import kr.co.soymilk.dycord_api.member.dto.oauth2.oidc.OIDCProfile;
import kr.co.soymilk.dycord_api.member.dto.oauth2.naver.NaverProfile;
import kr.co.soymilk.dycord_api.member.service.OAuth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final OAuth2Service OAuth2Service;

    @PostMapping("/oidc")
    public ResponseEntity<OIDCProfile> requestOIDCAuth(@RequestBody OIDCAuthRequest body) {
        OIDCProfile profile = OAuth2Service.processOIDCAuth(body);

        return ResponseEntity.ok(profile);
    }

    @PostMapping("/naver")
    public ResponseEntity<NaverProfile> requestNaverAuth(@RequestBody NaverAuthRequest body) {
        NaverProfile profile = OAuth2Service.processNaverAuth(body);

        return ResponseEntity.ok(profile);
    }

}
