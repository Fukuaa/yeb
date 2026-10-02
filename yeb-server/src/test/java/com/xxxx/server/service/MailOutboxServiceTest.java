package com.xxxx.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MailOutboxServiceTest {
    private JdbcTemplate jdbc;
    private RabbitTemplate rabbit;
    private MailOutboxService outbox;
    @BeforeEach
    void setup() {
        JdbcDataSource database=new JdbcDataSource();database.setURL("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DB_CLOSE_DELAY=-1");jdbc=new JdbcTemplate(database);
        jdbc.execute("CREATE TABLE t_employee(id INT PRIMARY KEY,email VARCHAR(100))");jdbc.update("INSERT INTO t_employee VALUES(1,'recipient@example.com')");
        jdbc.execute("CREATE TABLE t_mail_log(msgId VARCHAR(64) UNIQUE,eid INT,status INT,routeKey VARCHAR(30),`exchange` VARCHAR(30),`count` INT,tryTime TIMESTAMP,createTime TIMESTAMP,updateTime TIMESTAMP)");
        rabbit=mock(RabbitTemplate.class);outbox=new MailOutboxService(jdbc,rabbit,new ObjectMapper(),"mail.exchange","mail.routing.key");
    }
    @Test
    void persistsBeforePublicationAndDoesNotTreatBrokerAcceptanceAsDelivered() {
        String id=outbox.enqueueWelcome(1);verifyNoInteractions(rabbit);
        outbox.dispatchDue();
        verify(rabbit).send(eq("mail.exchange"),eq("mail.routing.key"),argThat(message->id.equals(message.getMessageProperties().getMessageId())),any(CorrelationData.class));
        assertEquals(0,jdbc.queryForObject("SELECT status FROM t_mail_log WHERE msgId=?",Integer.class,id));
        assertEquals(1,jdbc.queryForObject("SELECT `count` FROM t_mail_log WHERE msgId=?",Integer.class,id));
    }
    @Test
    void stopsAtThreeAttemptsAndAllowsAnExplicitRetry() {
        String id=outbox.enqueueWelcome(1);
        doThrow(new AmqpException("broker unavailable")).when(rabbit).send(anyString(),anyString(),any(Message.class),any(CorrelationData.class));
        for(int i=0;i<4;i++) { jdbc.update("UPDATE t_mail_log SET tryTime=DATEADD('MINUTE',-1,CURRENT_TIMESTAMP())");outbox.dispatchDue(); }
        verify(rabbit,times(3)).send(anyString(),anyString(),any(Message.class),any(CorrelationData.class));
        assertEquals(2,jdbc.queryForObject("SELECT status FROM t_mail_log",Integer.class));
        outbox.retry(id);assertEquals(0,jdbc.queryForObject("SELECT `count` FROM t_mail_log",Integer.class));
        assertEquals(0,jdbc.queryForObject("SELECT status FROM t_mail_log",Integer.class));
    }
}
