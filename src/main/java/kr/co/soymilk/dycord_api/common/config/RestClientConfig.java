package kr.co.soymilk.dycord_api.common.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
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

    private static final int CONNECTION_TIMEOUT_SEC = 10;
    private static final int SOCKET_TIMEOUT_SEC = 10;
    private static final int IDLE_SEC = 15;

    private static final int MAX_CONN_TOTAL = 100;
    private static final int MAX_CONN_PER_ROUTE = 20;

    @Bean
    public RestClient restClient() {
        // SimpleClientRequestFactory를 사용할 경우 HttpStatus가 200이 아닐때 response body를 못읽음
        return RestClient.builder()
                .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient()))
                .build();
    }

    private HttpClient httpClient() {
        return HttpClientBuilder.create()
                .setConnectionManager(connectionManager())
                .evictIdleConnections(TimeValue.of(IDLE_SEC, TimeUnit.SECONDS))
                .build();
    }

    private HttpClientConnectionManager connectionManager() {
        return PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(connectionConfig())
                .setMaxConnTotal(MAX_CONN_TOTAL)
                .setMaxConnPerRoute(MAX_CONN_PER_ROUTE)
                .build();
    }

    private ConnectionConfig connectionConfig() {
        return ConnectionConfig.custom()
                .setConnectTimeout(CONNECTION_TIMEOUT_SEC, TimeUnit.SECONDS)
                .setSocketTimeout(SOCKET_TIMEOUT_SEC, TimeUnit.SECONDS)
                .build();
    }

}
