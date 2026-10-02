package com.xxxx.mail;

import com.xxxx.server.pojo.MailJob;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MailDeliveryServiceTest {
    private JdbcTemplate jdbc;
    private JavaMailSender sender;
    private MailDeliveryService delivery;
    private MimeMessage message;
    private MailJob job;
    @BeforeEach
    void setup() {
        JdbcDataSource database=new JdbcDataSource();database.setURL("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DB_CLOSE_DELAY=-1");jdbc=new JdbcTemplate(database);
        jdbc.execute("CREATE TABLE t_mail_log(msgId VARCHAR(64) UNIQUE,eid INT,status INT,`count` INT,tryTime TIMESTAMP,updateTime TIMESTAMP)");
        jdbc.execute("CREATE TABLE t_employee(id INT PRIMARY KEY,name VARCHAR(64),email VARCHAR(100),posId INT,jobLevelId INT,departmentId INT)");
        for(String table:new String[]{"t_position","t_joblevel","t_department"}) { jdbc.execute("CREATE TABLE "+table+"(id INT,name VARCHAR(64))");jdbc.update("INSERT INTO "+table+" VALUES(1,'示例部门')"); }
        jdbc.update("INSERT INTO t_employee VALUES(1,'测试员工','recipient@example.com',1,1,1)");
        jdbc.update("INSERT INTO t_mail_log VALUES('job-1',1,0,1,CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP())");
        job=new MailJob().setMsgId("job-1").setEid(1);
        sender=mock(JavaMailSender.class);message=new MimeMessage(Session.getInstance(new Properties()));when(sender.createMimeMessage()).thenReturn(message);
        ClassLoaderTemplateResolver resolver=new ClassLoaderTemplateResolver();resolver.setPrefix("templates/");resolver.setSuffix(".html");resolver.setCharacterEncoding("UTF-8");
        TemplateEngine templates=new TemplateEngine();templates.setTemplateResolver(resolver);
        MailProperties properties=new MailProperties();properties.setUsername("sender@example.com");
        delivery=new MailDeliveryService(jdbc,sender,templates,properties,"");
    }
    @Test
    void sendsUtf8WelcomeAndDeduplicatesAnAlreadyCompletedJob() throws Exception {
        delivery.deliver(job);delivery.deliver(job);
        message.saveChanges();
        verify(sender,times(1)).send(any(MimeMessage.class));
        assertEquals(1,jdbc.queryForObject("SELECT status FROM t_mail_log",Integer.class));
        assertEquals("入职欢迎邮件",message.getSubject());assertTrue(message.getContent().toString().contains("测试员工"));
        assertTrue(message.getContentType().toLowerCase().contains("utf-8"));
    }
    @Test
    void recordsSmtpFailureAndStopsRetryingAfterTheThirdAttempt() {
        doThrow(new MailSendException("SMTP unavailable")).when(sender).send(any(MimeMessage.class));
        for(int attempt=1;attempt<=3;attempt++) {
            jdbc.update("UPDATE t_mail_log SET `count`=?",attempt);delivery.deliver(job);
            assertEquals(attempt==3?2:0,jdbc.queryForObject("SELECT status FROM t_mail_log",Integer.class));
        }
        delivery.deliver(job);verify(sender,times(3)).send(any(MimeMessage.class));
    }
    @Test
    void doesNotResendAJobBeingHandledByAnotherWorker() {
        jdbc.update("UPDATE t_mail_log SET status=3");delivery.deliver(job);verifyNoInteractions(sender);
    }
}
