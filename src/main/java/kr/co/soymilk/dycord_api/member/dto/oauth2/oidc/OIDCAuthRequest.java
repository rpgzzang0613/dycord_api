package kr.co.soymilk.dycord_api.member.dto.oauth2.oidc;

import kr.co.soymilk.dycord_api.member.dto.oauth2.OAuth2AuthRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OIDCAuthRequest extends OAuth2AuthRequest {

    private String nonce;

}
