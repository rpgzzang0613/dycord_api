package kr.co.soymilk.dycord_api.member.dto.oauth2.naver;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NaverRestDto {

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProfileResponse {

        private String resultcode;
        private String message;
        private NaverProfile response;

    }

}
