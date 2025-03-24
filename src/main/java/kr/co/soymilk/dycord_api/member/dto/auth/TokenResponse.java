package kr.co.soymilk.dycord_api.member.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenResponse {

    private Boolean isSignedIn;
    private String accessToken;
    private String refreshToken;

}
