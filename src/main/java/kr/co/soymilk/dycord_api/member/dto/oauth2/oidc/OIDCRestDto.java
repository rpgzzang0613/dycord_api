package kr.co.soymilk.dycord_api.member.dto.oauth2.oidc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OIDCRestDto {

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MetaDataResponse {

        private String issuer;
        private String authorization_endpoint;
        private String token_endpoint;
        private String device_authorization_endpoint;
        private String userinfo_endpoint;
        private String revocation_endpoint;
        private String jwks_uri;
        private List<String> token_endpoint_auth_methods_supported;
        private List<String> subject_types_supported;
        private List<String> id_token_signing_alg_values_supported;
        Boolean request_uri_parameter_supported;
        private List<String> response_types_supported;
        private List<String> response_modes_supported;
        private List<String> grant_types_supported;
        private List<String> code_challenge_methods_supported;
        private List<String> claims_supported;
        private List<String> scopes_supported;

    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class JwksResponse {
        private Set<Jwk> keys;
    }

}
