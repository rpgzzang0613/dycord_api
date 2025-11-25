package kr.co.soymilk.dycord_api.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    @MessageMapping("/send")    // 클라이언트에서 /pub/send로 메시지 발송시 이리로 진입
    @SendTo("/sub/messages")    // 클라이언트로부터 전달받은 메시지를 브로커가 전파할 구독 경로
    public String sendMessage(String inputMessage) {
        return inputMessage;
    }

}
