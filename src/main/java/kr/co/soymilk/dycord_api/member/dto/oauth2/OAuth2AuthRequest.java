package kr.co.soymilk.dycord_api.member.dto.oauth2;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuth2AuthRequest {

    private String code;
    private String platform;

}
