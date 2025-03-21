package kr.co.soymilk.dycord_api.member.dto.oauth2.google;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.co.soymilk.dycord_api.member.dto.oauth2.OIDCTokenResponseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GoogleTokenResponseDto extends OIDCTokenResponseDto {

    private String scope;

}
