package com.xxxx.server.config;

import com.xxxx.server.config.security.component.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ChatAuthenticationInterceptor implements ChannelInterceptor {
    private final JwtTokenUtil tokens;
    private final UserDetailsService users;
    private final String tokenHead;
    public ChatAuthenticationInterceptor(JwtTokenUtil tokens, UserDetailsService users, @Value("${jwt.tokenHead}") String tokenHead) {
        this.tokens = tokens; this.users = users; this.tokenHead = tokenHead;
    }
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) return message;
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String header = accessor.getFirstNativeHeader("Auth-Token");
            String prefix = tokenHead.trim() + " ";
            if (header == null || !header.startsWith(prefix) || !StringUtils.hasText(header.substring(prefix.length()))) {
                throw new AccessDeniedException("聊天连接需要有效的登录凭据");
            }
            try {
                String token = header.substring(prefix.length());
                String username = tokens.getUserNameFromToken(token);
                if (!StringUtils.hasText(username)) throw new AccessDeniedException("登录凭据已失效");
                UserDetails user = users.loadUserByUsername(username);
                if (!user.isEnabled() || !tokens.validateToken(token, user)) throw new AccessDeniedException("登录凭据已失效");
                accessor.setUser(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
            } catch (RuntimeException exception) {
                throw new AccessDeniedException("聊天登录凭据无效，请重新登录");
            }
        } else if (StompCommand.SEND.equals(accessor.getCommand()) || StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            if (!(accessor.getUser() instanceof Authentication) || !((Authentication)accessor.getUser()).isAuthenticated()) {
                throw new AccessDeniedException("请先登录聊天服务");
            }
            String destination = accessor.getDestination();
            boolean permitted = StompCommand.SEND.equals(accessor.getCommand()) ? "/ws/chat".equals(destination)
                : "/user/queue/chat".equals(destination) || "/user/queue/errors".equals(destination);
            if (!permitted) throw new AccessDeniedException("无法访问这个聊天频道");
        }
        return message;
    }
}
