package com.xxxx.server.config;

import org.springframework.amqp.core.*;

public final class MailTopology {
    private MailTopology() { }
    public static Declarables create(String queueName,String exchangeName,String routingKey) {
        DirectExchange exchange=new DirectExchange(exchangeName);
        DirectExchange deadExchange=new DirectExchange(exchangeName+".dead");
        Queue deadQueue=QueueBuilder.durable(queueName+".dead").build();
        Queue queue=QueueBuilder.durable(queueName).withArgument("x-dead-letter-exchange",deadExchange.getName())
            .withArgument("x-dead-letter-routing-key",routingKey).build();
        return new Declarables(queue,exchange,BindingBuilder.bind(queue).to(exchange).with(routingKey),
            deadQueue,deadExchange,BindingBuilder.bind(deadQueue).to(deadExchange).with(routingKey));
    }
}
