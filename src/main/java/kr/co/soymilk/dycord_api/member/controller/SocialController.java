package kr.co.soymilk.dycord_api.member.controller;

import kr.co.soymilk.dycord_api.member.service.SocialService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class SocialController {

    private final SocialService socialService;

    @PostMapping("/kakao")
    public void requestKakaoAuth(@RequestBody HashMap<String, String> body) {
        String code = body.get("code");
        String nonce = body.get("nonce");

        socialService.processKakaoAuth(code, nonce);
    }

    @PostMapping("/naver")
    public void requestNaverAuth(@RequestBody HashMap<String, String> body) {
        String code = body.get("code");
        String state = body.get("state");

        socialService.processNaverAuth(code, state);
    }

}
