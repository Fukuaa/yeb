package com.xxxx.mail;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.xxxx.server.pojo.MailJob;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import static org.mockito.Mockito.*;

class MailReceiverTest {
    @Test
    void rejectsMalformedJobsWithoutAnImmediateRequeueLoop() throws Exception {
        MailDeliveryService delivery=mock(MailDeliveryService.class);Channel channel=mock(Channel.class);
        MessageProperties properties=new MessageProperties();properties.setDeliveryTag(7);
        MailReceiver receiver=new MailReceiver(new ObjectMapper(),delivery);
        receiver.handler(new Message("invalid-json".getBytes("UTF-8"),properties),channel);
        verify(channel).basicNack(7,false,false);verifyNoInteractions(delivery);
    }
    @Test
    void acknowledgesAValidJobAfterProcessing() throws Exception {
        MailDeliveryService delivery=mock(MailDeliveryService.class);Channel channel=mock(Channel.class);
        ObjectMapper json=new ObjectMapper();MessageProperties properties=new MessageProperties();properties.setDeliveryTag(8);properties.setMessageId("job-1");
        MailJob job=new MailJob().setMsgId("job-1").setEid(1);
        new MailReceiver(json,delivery).handler(new Message(json.writeValueAsBytes(job),properties),channel);
        verify(delivery).deliver(job);verify(channel).basicAck(8,false);verify(channel,never()).basicNack(anyLong(),anyBoolean(),anyBoolean());
    }
}
