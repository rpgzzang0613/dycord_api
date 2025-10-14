package kr.co.soymilk.dycord_api.member.controller;

import kr.co.soymilk.dycord_api.member.dto.oauth2.OAuth2RestDto;
import kr.co.soymilk.dycord_api.member.dto.oauth2.oidc.OIDCProfile;
import kr.co.soymilk.dycord_api.member.service.OAuth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final OAuth2Service OAuth2Service;

    @PostMapping("/oidc")
    public ResponseEntity<OIDCProfile> processAuthByOIDC(@RequestBody OAuth2RestDto.TokenRequest body) {
        OIDCProfile profile = OAuth2Service.processAuthByOIDC(body);

        return ResponseEntity.ok(profile);
    }

}
