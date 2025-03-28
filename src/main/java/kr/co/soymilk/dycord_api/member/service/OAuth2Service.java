package kr.co.soymilk.dycord_api.member.service;

import io.jsonwebtoken.Claims;
import kr.co.soymilk.dycord_api.member.dto.oauth2.OAuth2RestDto;
import kr.co.soymilk.dycord_api.member.dto.oauth2.naver.NaverRestDto;
import kr.co.soymilk.dycord_api.member.dto.oauth2.oidc.Jwk;
import kr.co.soymilk.dycord_api.member.dto.oauth2.oidc.OIDCProfile;
import kr.co.soymilk.dycord_api.member.dto.oauth2.naver.NaverProfile;
import kr.co.soymilk.dycord_api.member.util.OAuth2ProfileProvider;
import kr.co.soymilk.dycord_api.member.util.OAuth2TokenProvider;
import kr.co.soymilk.dycord_api.member.util.OIDCUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final OAuth2TokenProvider oAuth2TokenProvider;
    private final OAuth2ProfileProvider oAuth2ProfileProvider;
    private final OIDCUtil oidcUtil;

    public OIDCProfile processAuthByOIDC(OAuth2RestDto.TokenRequest request) {
        // 프론트단으로부터 전달받은 code로 OIDC Provider(구글, 카카오)에 토큰 요청
        OAuth2RestDto.TokenResponse tokenRes = oAuth2TokenProvider.requestOAuth2TokenByCode(request);

        // id token 검증에 사용할 jwk 조회
        Jwk jwk = oidcUtil.getFilteredJwk(tokenRes.getId_token(), request.getNonce(), request.getPlatform());

        // id token을 검증하고 토큰의 데이터를 꺼낼 수 있는 형태로 추출
        Claims payload = oidcUtil.validateAndExtractIdToken(tokenRes.getId_token(), jwk);

        // id token의 페이로드로부터 데이터를 꺼내 프로필 객체로 리턴
        OIDCProfile profile = oAuth2ProfileProvider.getProfileFromIdToken(payload);

        String socialUid = profile.getId();

        // TODO DB에서 social id 조회해서 가입유무 확인 후 가입했으면 dycord 토큰 발급하도록 수정하기
        return profile;
    }

    public NaverProfile processAuthByNaver(OAuth2RestDto.TokenRequest request) {
        // 프론트단으로부터 전달받은 code로 네이버에 토큰 요청
        OAuth2RestDto.TokenResponse tokenRes = oAuth2TokenProvider.requestNaverTokenByCode(request);

        // 네이버는 OIDC를 지원하지 않으므로, 액세스 토큰을 통해 유저 정보 별도로 요청
        NaverRestDto.ProfileResponse profileRes = oAuth2ProfileProvider.requestNaverProfileByToken(tokenRes.getAccess_token());

        String naverUid = profileRes.getResponse().getId();

        // TODO DB에서 social id 조회해서 가입유무 확인 후 가입했으면 dycord 토큰 발급하도록 수정하기
        return profileRes.getResponse();
    }

}
