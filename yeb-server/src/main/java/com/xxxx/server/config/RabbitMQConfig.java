package com.xxxx.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    private static final Logger LOG=LoggerFactory.getLogger(RabbitMQConfig.class);
    @Bean
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory factory) {
        RabbitTemplate template=new RabbitTemplate(factory);
        template.setMandatory(true);
        template.setConfirmCallback((data,ack,cause)-> {
            if(!ack && data!=null) LOG.warn("Mail broker rejected publication {}",data.getId());
        });
        template.setReturnCallback((message,code,text,exchange,routingKey)->
            LOG.warn("Mail publication was not routed; outbox retry remains pending"));
        return template;
    }
    @Bean
    public Declarables mailTopology(@Value("${app.mail.queue:mail.queue}") String queue,
            @Value("${app.mail.exchange:mail.exchange}") String exchange,
            @Value("${app.mail.routing-key:mail.routing.key}") String routingKey) {
        return MailTopology.create(queue,exchange,routingKey);
    }
}