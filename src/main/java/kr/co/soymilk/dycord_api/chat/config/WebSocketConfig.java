package kr.co.soymilk.dycord_api.chat.config;

import kr.co.soymilk.dycord_api.chat.interceptor.AuthChannelInterceptor;
import kr.co.soymilk.dycord_api.chat.interceptor.AuthHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final AuthHandshakeInterceptor authHandshakeInterceptor;
    private final AuthChannelInterceptor authChannelInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")                     // 웹소켓 연결 요청 진입점
                .addInterceptors(authHandshakeInterceptor)       // 웹소켓 연결 전 토큰 검증
                .setAllowedOrigins("http://localhost:3636", "https://dycord.co.kr")
                .setAllowedOriginPatterns(
                        "http://10.*.*.*:3636",
                        "http://172.16.*.*:3636",
                        "http://172.17.*.*:3636",
                        "http://172.18.*.*:3636",
                        "http://172.19.*.*:3636",
                        "http://172.20.*.*:3636",
                        "http://172.21.*.*:3636",
                        "http://172.22.*.*:3636",
                        "http://172.23.*.*:3636",
                        "http://172.24.*.*:3636",
                        "http://172.25.*.*:3636",
                        "http://172.26.*.*:3636",
                        "http://172.27.*.*:3636",
                        "http://172.28.*.*:3636",
                        "http://172.29.*.*:3636",
                        "http://172.30.*.*:3636",
                        "http://172.31.*.*:3636",
                        "http://192.168.*.*:3636"
                )
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authChannelInterceptor);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub");    // 클라이언트가 구독하는(=브로커가 구독자에게 메시지를 발송하는) 경로의 공통 prefix
        registry.setApplicationDestinationPrefixes("/pub");          // 클라이언트가 서버로 메시지를 발송할때 사용하는 경로의 경로의 공통 prefix
    }

}
