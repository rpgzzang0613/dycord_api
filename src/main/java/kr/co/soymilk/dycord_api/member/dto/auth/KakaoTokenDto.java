package kr.co.soymilk.dycord_api.member.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoTokenDto {

    private String token_type;
    private String access_token;
    private Long expires_in;
    private String refresh_token;
    private Long refresh_token_expires_in;
    private String id_token;
    private String scope;

}
