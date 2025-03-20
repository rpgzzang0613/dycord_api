package kr.co.soymilk.dycord_api.member.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NaverErrorDto {

    private String error;
    private String error_description;

}
