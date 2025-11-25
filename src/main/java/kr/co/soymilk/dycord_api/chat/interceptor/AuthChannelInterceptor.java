package kr.co.soymilk.dycord_api.chat.interceptor;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

/**
 * 웹소켓 연결 후 주고받는 메시지 단계에서 동작하는 인터셉터
 */
@Component
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);

        // TODO 토큰 검증 생기고 핸드쉐이크 인터셉터에서 유저정보 추가되면 이후로직 추가
//        Object user = headerAccessor.getSessionAttributes().get("user");

        return message;
    }
}
