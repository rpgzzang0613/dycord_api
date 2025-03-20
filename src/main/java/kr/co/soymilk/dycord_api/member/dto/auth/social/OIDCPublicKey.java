package kr.co.soymilk.dycord_api.member.dto.auth.social;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OIDCPublicKey {

    private String kid;
    private String kty;
    private String alg;
    private String use;
    private String n;
    private String e;

}
