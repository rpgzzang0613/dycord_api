package kr.co.soymilk.dycord_api.member.dto.oauth2.naver;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NaverProfileResponse {

    private String resultcode;
    private String message;
    private NaverProfile response;

}
