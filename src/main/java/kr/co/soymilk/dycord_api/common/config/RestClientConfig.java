package kr.co.soymilk.dycord_api.common.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class RestClientConfig {

    private static final int CONNECTION_TIMEOUT_SEC = 10;
    private static final int RESPONSE_TIMEOUT_SEC = 10;

    @Bean
    public RestClient restClient(HttpClient httpClient) {
        return RestClient.builder()
                .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient))
                .build();
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig())
                .build();
    }

    private RequestConfig requestConfig() {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(CONNECTION_TIMEOUT_SEC, TimeUnit.SECONDS)
                .setResponseTimeout(RESPONSE_TIMEOUT_SEC, TimeUnit.SECONDS)
                .build();
    }

}
