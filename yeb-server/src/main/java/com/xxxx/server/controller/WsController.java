package com.xxxx.server.controller;

import com.xxxx.server.pojo.Admin;
import com.xxxx.server.pojo.ChatMsg;
import com.xxxx.server.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
import java.util.Collections;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WsController {
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private ChatService chatService;
    @MessageMapping("/ws/chat")
    public void handleMsg(Authentication authentication, ChatMsg chatMsg){
        if (authentication == null || !(authentication.getPrincipal() instanceof Admin)) throw new AccessDeniedException("请先登录聊天服务");
        ChatMsg saved = chatService.send((Admin)authentication.getPrincipal(), chatMsg);
        simpMessagingTemplate.convertAndSendToUser(saved.getTo(), "/queue/chat", saved);
        simpMessagingTemplate.convertAndSendToUser(saved.getFrom(), "/queue/chat", saved);
    }
    @MessageExceptionHandler({ResponseStatusException.class, AccessDeniedException.class})
    @SendToUser(value = "/queue/errors", broadcast = false)
    public Map<String,String> chatError(RuntimeException exception) {
        String text = exception instanceof ResponseStatusException ? ((ResponseStatusException)exception).getReason() : exception.getMessage();
        return Collections.singletonMap("message", text);
    }
}
