package kr.co.soymilk.dycord_api.member.dto.oauth2;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IdTokenPayload {

    private String iss;
    private String aud;
    private String sub;
    private Long iat;
    private Long exp;

    private Long auth_time;

    private String nonce;
    private String azp;
    private String at_hash;
    private String nickname;
    private String picture;
    private String email;
    private Boolean email_verified;
    private String family_name;
    private String given_name;
    private String hd;
    private String locale;
    private String name;
    private String profile;

}
