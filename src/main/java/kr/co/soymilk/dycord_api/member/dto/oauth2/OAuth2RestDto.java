package kr.co.soymilk.dycord_api.member.dto.oauth2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OAuth2RestDto {

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TokenRequest {

        // 모든 소셜 공통
        private String code;
        private String platform;
        private String codeVerifier;

        // 카카오, 구글 공통
        private String nonce;

        // 네이버
        private String state;

    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TokenResponse {

        // 모든 소셜 공통
        private String token_type;
        private String access_token;
        private Long expires_in;
        private String refresh_token;

        // OIDC 지원 소셜 공통
        private String id_token;

        // 카카오, 구글 공통
        private String scope;

        // 카카오
        private Long refresh_token_expires_in;

        // 네이버
        private String error;
        private String error_description;

    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorResponse {

        private String error;
        private String error_code;
        private String error_description;

        public String toJsonString() {
            try {
                return new ObjectMapper().writeValueAsString(this);
            } catch (JsonProcessingException e) {
                return super.toString();
            }
        }

    }

}
