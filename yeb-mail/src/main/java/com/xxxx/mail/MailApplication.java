package com.xxxx.mail;

import com.xxxx.server.config.MailTopology;
import org.springframework.amqp.core.Declarables;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MailApplication {
    public static void main(String[] args) { SpringApplication.run(MailApplication.class,args); }
    @Bean
    public Declarables mailTopology(@Value("${app.mail.queue:mail.queue}") String queue,
            @Value("${app.mail.exchange:mail.exchange}") String exchange,
            @Value("${app.mail.routing-key:mail.routing.key}") String routingKey) {
        return MailTopology.create(queue,exchange,routingKey);
    }
}