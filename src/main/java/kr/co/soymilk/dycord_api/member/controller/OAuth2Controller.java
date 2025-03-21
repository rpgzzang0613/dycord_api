package kr.co.soymilk.dycord_api.member.controller;

import kr.co.soymilk.dycord_api.member.dto.oauth2.OAuth2ProfileDto;
import kr.co.soymilk.dycord_api.member.dto.oauth2.naver.NaverProfileDto;
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
    public ResponseEntity<OAuth2ProfileDto> requestGoogleAuth(@RequestBody HashMap<String, String> body) {
        String code = body.get("code");
        String nonce = body.get("nonce");
        String platform = body.get("platform");

        OAuth2ProfileDto profileDto = OAuth2Service.processOIDCAuth(code, nonce, platform);

        return ResponseEntity.ok(profileDto);
    }

    @PostMapping("/naver")
    public ResponseEntity<NaverProfileDto> requestNaverAuth(@RequestBody HashMap<String, String> body) {
        String code = body.get("code");
        String state = body.get("state");

        NaverProfileDto naverProfileDto = OAuth2Service.processNaverAuth(code, state);

        return ResponseEntity.ok(naverProfileDto);
    }

}
