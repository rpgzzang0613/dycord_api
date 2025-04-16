package kr.co.soymilk.dycord_api.common.config;

import kr.co.soymilk.dycord_api.common.util.OSType;
import kr.co.soymilk.dycord_api.common.util.ServerUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.concurrent.TimeUnit;

/**
 * Http Rest 요청을 위한 객체인 RestClient 설정 (RestTemplate 과 같은 용도)
 */
@Slf4j
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient() {
        // SimpleClientRequestFactory를 사용할 경우 HttpStatus가 200이 아닐때 response body를 못읽음
        return RestClient.builder()
                .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient()))
                .build();
    }

    private HttpClient httpClient() {
        HttpClientBuilder builder = HttpClientBuilder.create()
                .setConnectionManager(connectionManager())
                .setDefaultRequestConfig(requestConfig())
                .evictExpiredConnections();

        if (ServerUtil.getOsType() == OSType.LINUX) {
            builder.evictIdleConnections(TimeValue.of(20, TimeUnit.SECONDS));
        }

        return builder.build();
    }

    private RequestConfig requestConfig() {
        RequestConfig.Builder builder = RequestConfig.custom();

        if (ServerUtil.getOsType() == OSType.LINUX) {
            builder.setConnectionRequestTimeout(20, TimeUnit.SECONDS)
                    .setResponseTimeout(20, TimeUnit.SECONDS);
        }

        return builder.build();
    }

    private HttpClientConnectionManager connectionManager() {
        return PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(connectionConfig())
                .setMaxConnTotal(50)
                .setMaxConnPerRoute(15)
                .build();
    }

    private ConnectionConfig connectionConfig() {
        ConnectionConfig.Builder builder = ConnectionConfig.custom();

        if (ServerUtil.getOsType() == OSType.LINUX) {
            builder.setConnectTimeout(20, TimeUnit.SECONDS)
                    .setSocketTimeout(20, TimeUnit.SECONDS);
        }

        return builder.build();
    }

}
