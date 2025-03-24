package kr.co.soymilk.dycord_api.member.property.social;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "social")
public class SocialProperties {

    private ProviderProperties google;
    private ProviderProperties kakao;
    private ProviderProperties naver;

    public ProviderProperties getProviderProperties(String platform) {
        return switch (platform) {
            case "google" -> google;
            case "kakao" -> kakao;
            case "naver" -> naver;
            default -> throw new IllegalArgumentException("Unsupported Social Provider: " + platform);
        };
    }

}

