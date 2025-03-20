package kr.co.soymilk.dycord_api.member.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NaverTokenDto {

    private String token_type;
    private String access_token;
    private Long expires_in;
    private String refresh_token;
    private String error;
    private String error_description;

}
