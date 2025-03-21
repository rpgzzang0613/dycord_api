package kr.co.soymilk.dycord_api.member.service;

import kr.co.soymilk.dycord_api.member.dto.oauth2.OAuth2ProfileDto;
import kr.co.soymilk.dycord_api.member.dto.oauth2.OAuth2TokenResponseDto;
import kr.co.soymilk.dycord_api.member.dto.oauth2.OIDCTokenResponseDto;
import kr.co.soymilk.dycord_api.member.dto.oauth2.google.GoogleTokenResponseDto;
import kr.co.soymilk.dycord_api.member.dto.oauth2.kakao.KakaoTokenResponseDto;
import kr.co.soymilk.dycord_api.member.dto.oauth2.naver.NaverProfileDto;
import kr.co.soymilk.dycord_api.member.dto.oauth2.naver.NaverProfileResponseDto;
import kr.co.soymilk.dycord_api.member.dto.oauth2.naver.NaverTokenResponseDto;
import kr.co.soymilk.dycord_api.member.util.OIDCUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final GoogleOAuth2Service googleOAuth2Service;
    private final KakaoOAuth2Service kakaoOAuth2Service;
    private final NaverOAuth2Service naverOAuth2Service;
    private final OIDCUtil oidcUtil;

    public OAuth2ProfileDto processOIDCAuth(String code, String nonce, String platform) {
        OIDCTokenResponseDto tokenResDto = platform.equals("kakao") ? kakaoOAuth2Service.requestKakaoTokenByCode(code) : googleOAuth2Service.requestGoogleTokenByCode(code);
        OAuth2ProfileDto profileDto = oidcUtil.getPayloadFromIdToken(tokenResDto.getId_token(), nonce, platform);

        if (profileDto == null) {
            return null;
        }

        String socialUid = profileDto.getId();

        // TODO DB에서 social id 조회해서 가입유무 확인 후 가입했으면 dycord 토큰 발급하도록 수정하기
        return profileDto;
    }

    public NaverProfileDto processNaverAuth(String code, String state) {
        NaverTokenResponseDto naverTokenResDto = naverOAuth2Service.requestNaverTokenByCode(code, state);
        NaverProfileResponseDto naverProfileResDto = naverOAuth2Service.requestNaverProfileByToken(naverTokenResDto.getAccess_token());

        String naverUid = naverProfileResDto.getResponse().getId();

        // TODO DB에서 social id 조회해서 가입유무 확인 후 가입했으면 dycord 토큰 발급하도록 수정하기
        return naverProfileResDto.getResponse();
    }

}
