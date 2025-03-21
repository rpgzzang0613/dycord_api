package kr.co.soymilk.dycord_api.member.dto.oauth2;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OIDCKeysResponseDto {

    private List<OIDCPublicKey> keys;

}
