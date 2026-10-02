package com.xxxx.mail;

import com.xxxx.server.pojo.MailJob;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class MailDeliveryService {
    private static final Logger LOG=LoggerFactory.getLogger(MailDeliveryService.class);
    private final JdbcTemplate jdbc;
    private final JavaMailSender sender;
    private final TemplateEngine templates;
    private final MailProperties properties;
    private final String configuredFrom;

    public MailDeliveryService(JdbcTemplate jdbc,JavaMailSender sender,TemplateEngine templates,MailProperties properties,
            @Value("${app.mail.from:}") String configuredFrom) {
        this.jdbc=jdbc;this.sender=sender;this.templates=templates;this.properties=properties;this.configuredFrom=configuredFrom;
    }

    public void deliver(MailJob job) {
        if(job==null || job.getMsgId()==null || job.getEid()==null) throw new IllegalArgumentException("Invalid mail job");
        // Compare-and-set prevents duplicate queue deliveries from sending the same job concurrently.
        int claimed=jdbc.update("UPDATE t_mail_log SET status=3,updateTime=? WHERE msgId=? AND eid=? AND status=0",
            Timestamp.valueOf(LocalDateTime.now()),job.getMsgId(),job.getEid());
        if(claimed==0) return;
        boolean terminal=false;
        try {
            List<Map<String,Object>> employees=jdbc.queryForList(
                "SELECT e.name,e.email,p.name AS positionName,j.name AS joblevelName,d.name AS departmentName FROM t_employee e " +
                "LEFT JOIN t_position p ON e.posId=p.id LEFT JOIN t_joblevel j ON e.jobLevelId=j.id LEFT JOIN t_department d ON e.departmentId=d.id WHERE e.id=?",job.getEid());
            if(employees.isEmpty()) { terminal=true;throw new IllegalArgumentException("Employee no longer exists"); }
            Map<String,Object> employee=employees.get(0);
            String address=String.valueOf(employee.get("email"));
            try { new InternetAddress(address,true).validate(); }
            catch(Exception exception) { terminal=true;throw new IllegalArgumentException("Invalid employee email"); }
            MimeMessage message=sender.createMimeMessage();
            MimeMessageHelper helper=new MimeMessageHelper(message,false,"UTF-8");
            helper.setFrom(configuredFrom.isEmpty()?properties.getUsername():configuredFrom);
            helper.setTo(address); helper.setSubject("入职欢迎邮件");
            message.setHeader("X-Yeb-Message-Id",job.getMsgId());
            Context context=new Context();
            context.setVariable("name",employee.get("name")); context.setVariable("posName",employee.get("positionName"));
            context.setVariable("joblevelName",employee.get("joblevelName")); context.setVariable("departmentName",employee.get("departmentName"));
            helper.setText(templates.process("mail",context),true);
            sender.send(message);
            jdbc.update("UPDATE t_mail_log SET status=1,updateTime=? WHERE msgId=? AND status=3",Timestamp.valueOf(LocalDateTime.now()),job.getMsgId());
        } catch(Exception exception) {
            LOG.warn("Mail delivery failed for {} ({})",job.getMsgId(),exception.getClass().getSimpleName());
            jdbc.update("UPDATE t_mail_log SET status=CASE WHEN `count`>=3 OR ? THEN 2 ELSE 0 END,tryTime=?,updateTime=? WHERE msgId=? AND status=3",
                terminal,Timestamp.valueOf(LocalDateTime.now().plusMinutes(1)),Timestamp.valueOf(LocalDateTime.now()),job.getMsgId());
        }
    }
}
