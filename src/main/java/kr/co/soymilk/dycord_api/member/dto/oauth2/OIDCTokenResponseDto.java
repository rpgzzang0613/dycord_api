package kr.co.soymilk.dycord_api.member.dto.oauth2;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OIDCTokenResponseDto extends OAuth2TokenResponseDto {

    private String id_token;

}
