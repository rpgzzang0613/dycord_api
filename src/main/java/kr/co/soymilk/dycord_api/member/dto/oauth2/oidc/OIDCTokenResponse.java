package kr.co.soymilk.dycord_api.member.dto.oauth2.oidc;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.co.soymilk.dycord_api.member.dto.oauth2.OAuth2TokenResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OIDCTokenResponse extends OAuth2TokenResponse {

    private String id_token;

}
