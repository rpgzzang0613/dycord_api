package kr.co.soymilk.dycord_api.member.dto.oauth2.kakao;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.co.soymilk.dycord_api.member.dto.oauth2.OIDCTokenResponseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KakaoTokenResponseDto extends OIDCTokenResponseDto {

    private Long refresh_token_expires_in;
    private String scope;

}
