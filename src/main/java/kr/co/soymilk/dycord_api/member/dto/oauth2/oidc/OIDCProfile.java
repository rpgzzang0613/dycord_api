package kr.co.soymilk.dycord_api.member.dto.oauth2.oidc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OIDCProfile {

    private String id; // sub
    private String name;
    private String given_name;
    private String family_name;
    private String middle_name;
    private String nickname;
    private String preferred_username;
    private String profile;
    private String picture;
    private String website;
    private String email;
    private Boolean email_verified;
    private String gender;
    private String birthdate;
    private String zoneinfo;
    private String locale;
    private String phone_number;
    private Boolean phone_number_verified;
    private HashMap<String, String> address;
    private Long updated_at;

}
