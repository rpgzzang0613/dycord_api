package kr.co.soymilk.dycord_api.member.dto.oauth2.naver;

import kr.co.soymilk.dycord_api.member.dto.oauth2.OAuth2AuthRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NaverAuthRequest extends OAuth2AuthRequest {

    private String state;

}
