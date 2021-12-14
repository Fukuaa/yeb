package com.xxxx.server.config;


import com.xxxx.server.config.security.component.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Value("{tokenHead}")
    private String tokenHead;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private UserDetailsService userDetailsService;
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/ep").setAllowedOrigins("*").withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
       registration.interceptors(new ChannelInterceptor() {
           @Override
           public Message<?> preSend(Message<?> message, MessageChannel channel) {
               StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,StompHeaderAccessor.class);
               if (StompCommand.COMMIT.equals(accessor.getCommand())){
                   String auth_token = accessor.getFirstNativeHeader("Auth_Token");
                   if (!StringUtils.hasText(auth_token)){
                       String authToken = auth_token.substring(tokenHead.length());
                       String username = jwtTokenUtil.getUserNameFromToken(authToken);
                       if(!StringUtils.hasText(username)){
                           UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                           if (jwtTokenUtil.validateToken(authToken,userDetails)){
                               UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                               SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                           }
                       }
                   }
                   return message;
               }
               return null;
           }
       });
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/queue");
    }

}
