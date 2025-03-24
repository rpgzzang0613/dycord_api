package kr.co.soymilk.dycord_api.member.util;

import kr.co.soymilk.dycord_api.member.property.social.OidcProperties;
import kr.co.soymilk.dycord_api.member.property.social.ProviderProperties;
import kr.co.soymilk.dycord_api.member.property.social.SocialProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocialInfoProvider {

    private final SocialProperties socialProperties;

    private ProviderProperties getProviderProperties(String platform) {
        return socialProperties.getProviderProperties(platform);
    }

    private OidcProperties getOidc(String platform) {
        ProviderProperties provider = getProviderProperties(platform);
        if (provider.getOidc() == null) {
            throw new IllegalArgumentException("Unsupported Social Provider: " + platform);
        }

        return provider.getOidc();
    }

    public String getClientId(String platform) {
        return getProviderProperties(platform).getClientId();
    }

    public String getClientSecret(String platform) {
        return getProviderProperties(platform).getClientSecret();
    }

    public String getRedirectUri(String platform) {
        return getProviderProperties(platform).getRedirectUri();
    }

    public String getTokenUri(String platform) {
        return getProviderProperties(platform).getTokenUri();
    }

    public String getProfileUri(String platform) {
        if (!"naver".equals(platform)) {
            throw new IllegalArgumentException("Unsupported platform: " + platform);
        }

        return getProviderProperties(platform).getProfileUri();
    }

    public String getOidcIss(String platform) {
        return getOidc(platform).getIss();
    }

    public String getOidcMetaUri(String platform) {
        return getOidc(platform).getMeta_uri();
    }

}
