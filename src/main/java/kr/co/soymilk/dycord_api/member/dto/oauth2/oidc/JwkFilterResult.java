package kr.co.soymilk.dycord_api.member.dto.oauth2.oidc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JwkFilterResult {

    Jwk jwk;

    public boolean isExpired() {
        return this.jwk == null;
    }

}
