package com.xxxx.server.config;

import com.xxxx.server.config.security.component.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatAuthenticationInterceptorTest {
    private ChatAuthenticationInterceptor interceptor;
    private UserDetails user;
    @BeforeEach
    void setup() {
        JwtTokenUtil tokens=mock(JwtTokenUtil.class);UserDetailsService users=mock(UserDetailsService.class);
        user=User.withUsername("alice").password("unused").roles("USER").build();
        when(tokens.getUserNameFromToken("valid")).thenReturn("alice");when(tokens.validateToken("valid",user)).thenReturn(true);
        when(users.loadUserByUsername("alice")).thenReturn(user);
        interceptor=new ChatAuthenticationInterceptor(tokens,users,"Bearer");
    }
    private Message<byte[]> frame(StompCommand command,String token,String destination,boolean authenticated) {
        StompHeaderAccessor accessor=StompHeaderAccessor.create(command);
        if(token!=null) accessor.addNativeHeader("Auth-Token",token);
        if(destination!=null) accessor.setDestination(destination);
        if(authenticated) accessor.setUser(new UsernamePasswordAuthenticationToken(user,null,user.getAuthorities()));
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0],accessor.getMessageHeaders());
    }
    @Test
    void requiresAValidPrefixedToken() {
        assertThrows(AccessDeniedException.class,()->interceptor.preSend(frame(StompCommand.CONNECT,null,null,false),null));
        assertThrows(AccessDeniedException.class,()->interceptor.preSend(frame(StompCommand.CONNECT,"Bearer",null,false),null));
        assertThrows(AccessDeniedException.class,()->interceptor.preSend(frame(StompCommand.CONNECT,"Bearer expired",null,false),null));
        Message<?> valid=frame(StompCommand.CONNECT,"Bearer valid",null,false);
        interceptor.preSend(valid,null);
        assertEquals("alice",StompHeaderAccessor.getAccessor(valid,StompHeaderAccessor.class).getUser().getName());
    }
    @Test
    void blocksAnonymousSendsAndOtherUsersPhysicalQueues() {
        assertThrows(AccessDeniedException.class,()->interceptor.preSend(frame(StompCommand.SEND,null,"/ws/chat",false),null));
        assertThrows(AccessDeniedException.class,()->interceptor.preSend(frame(StompCommand.SUBSCRIBE,null,"/queue/chat-user-bob",true),null));
        assertThrows(AccessDeniedException.class,()->interceptor.preSend(frame(StompCommand.SEND,null,"/queue/chat",true),null));
        assertDoesNotThrow(()->interceptor.preSend(frame(StompCommand.SUBSCRIBE,null,"/user/queue/chat",true),null));
    }
}
