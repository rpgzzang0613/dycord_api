package kr.co.soymilk.dycord_api.member.dto.oauth2.oidc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Jwk {

    private String kid;
    private String kty;
    private String alg;
    private String use;
    private String n;
    private String e;

}
