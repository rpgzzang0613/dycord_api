package kr.co.soymilk.dycord_api.member.dto.auth.social.naver;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NaverTokenResponseDto {

    private String token_type;
    private String access_token;
    private Long expires_in;
    private String refresh_token;
    private String error;
    private String error_description;

    public String toJsonString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return super.toString();
        }
    }

}
