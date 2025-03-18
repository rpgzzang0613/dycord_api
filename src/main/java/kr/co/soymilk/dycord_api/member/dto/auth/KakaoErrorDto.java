package kr.co.soymilk.dycord_api.member.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoErrorDto {

    private String error;
    private String error_code;
    private String error_description;

}
