package kr.co.soymilk.dycord_api.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "endpoints")
public class EndpointProperties {

    private List<String> permitAll;

}
