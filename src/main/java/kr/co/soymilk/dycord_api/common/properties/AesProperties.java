package kr.co.soymilk.dycord_api.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "aes")
public class AesProperties {

    private String secretKey;

}
