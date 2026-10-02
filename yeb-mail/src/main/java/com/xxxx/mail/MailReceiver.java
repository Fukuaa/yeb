package com.xxxx.mail;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.xxxx.server.pojo.MailJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class MailReceiver {
    private static final Logger LOG=LoggerFactory.getLogger(MailReceiver.class);
    private final ObjectMapper json;
    private final MailDeliveryService delivery;
    public MailReceiver(ObjectMapper json,MailDeliveryService delivery) { this.json=json;this.delivery=delivery; }
    @RabbitListener(queues="${app.mail.queue:mail.queue}")
    public void handler(Message message,Channel channel) throws IOException {
        long tag=message.getMessageProperties().getDeliveryTag();
        try {
            MailJob job=json.readValue(message.getBody(),MailJob.class);
            String id=message.getMessageProperties().getMessageId();
            if(id==null || !id.equals(job.getMsgId())) throw new IllegalArgumentException("Invalid mail message id");
            delivery.deliver(job);
            // Failed SMTP attempts are persisted for scheduled, bounded retries, not immediate requeue loops.
            channel.basicAck(tag,false);
        } catch(Exception exception) {
            LOG.warn("Rejected malformed or unprocessable mail job ({})",exception.getClass().getSimpleName());
            channel.basicNack(tag,false,false);
        }
    }
}