package kr.co.soymilk.dycord_api.member.dto.oauth2.oidc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JwkResponse {

    private Set<Jwk> keys;

}
