package kr.co.soymilk.dycord_api.member.dto.auth.social.kakao;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.co.soymilk.dycord_api.member.dto.auth.social.OIDCPublicKey;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KakaoOIDCKeysResponseDto {

    private List<OIDCPublicKey> keys;

}
