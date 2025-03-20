package kr.co.soymilk.dycord_api.member.dto.auth.social;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IdTokenHeader {

    private String alg;
    private String typ;
    private String kid;

}
