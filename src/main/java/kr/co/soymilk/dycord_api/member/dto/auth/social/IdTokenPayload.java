package kr.co.soymilk.dycord_api.member.dto.auth.social;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IdTokenPayload {

    private String iss;
    private String aud;
    private String sub;
    private Long iat;
    private Long exp;
    private Long auth_time;

    private String nonce;
    private String nickname;
    private String picture;
    private String email;

}
