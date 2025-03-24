package kr.co.soymilk.dycord_api.member.service;

import kr.co.soymilk.dycord_api.member.dto.oauth2.naver.NaverAuthRequest;
import kr.co.soymilk.dycord_api.member.dto.oauth2.oidc.OIDCAuthRequest;
import kr.co.soymilk.dycord_api.member.dto.oauth2.oidc.OIDCProfile;
import kr.co.soymilk.dycord_api.member.dto.oauth2.oidc.OIDCTokenResponse;
import kr.co.soymilk.dycord_api.member.dto.oauth2.naver.NaverProfile;
import kr.co.soymilk.dycord_api.member.dto.oauth2.naver.NaverProfileResponse;
import kr.co.soymilk.dycord_api.member.dto.oauth2.naver.NaverTokenResponse;
import kr.co.soymilk.dycord_api.member.util.OAuth2ProfileProvider;
import kr.co.soymilk.dycord_api.member.util.OAuth2TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final OAuth2TokenProvider oAuth2TokenProvider;
    private final OAuth2ProfileProvider oAuth2ProfileProvider;

    public OIDCProfile processOIDCAuth(OIDCAuthRequest request) {
        OIDCTokenResponse tokenRes = oAuth2TokenProvider.requestOAuth2TokenByCode(request);
        OIDCProfile profile = oAuth2ProfileProvider.getProfileFromIdToken(tokenRes.getId_token(), request.getNonce(), request.getPlatform());

        if (profile == null) {
            return null;
        }

        String socialUid = profile.getId();

        // TODO DB에서 social id 조회해서 가입유무 확인 후 가입했으면 dycord 토큰 발급하도록 수정하기
        return profile;
    }

    public NaverProfile processNaverAuth(NaverAuthRequest request) {
        NaverTokenResponse tokenRes = oAuth2TokenProvider.requestNaverTokenByCode(request);
        NaverProfileResponse profileRes = oAuth2ProfileProvider.requestNaverProfileByToken(tokenRes.getAccess_token());

        String naverUid = profileRes.getResponse().getId();

        // TODO DB에서 social id 조회해서 가입유무 확인 후 가입했으면 dycord 토큰 발급하도록 수정하기
        return profileRes.getResponse();
    }

}
