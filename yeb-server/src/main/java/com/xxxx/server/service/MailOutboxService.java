package com.xxxx.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxxx.server.pojo.MailConstants;
import com.xxxx.server.pojo.MailJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MailOutboxService {
    private static final Logger LOG = LoggerFactory.getLogger(MailOutboxService.class);
    private final JdbcTemplate jdbc;
    private final RabbitTemplate rabbit;
    private final ObjectMapper json;
    private final String exchange;
    private final String routingKey;

    public MailOutboxService(JdbcTemplate jdbc, RabbitTemplate rabbit, ObjectMapper json,
            @Value("${app.mail.exchange:mail.exchange}") String exchange,
            @Value("${app.mail.routing-key:mail.routing.key}") String routingKey) {
        this.jdbc=jdbc; this.rabbit=rabbit; this.json=json; this.exchange=exchange; this.routingKey=routingKey;
    }

    @Transactional
    public String enqueueWelcome(int employeeId) {
        List<String> addresses = jdbc.query("SELECT email FROM t_employee WHERE id=?", (rs,row)->rs.getString(1), employeeId);
        if (addresses.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"员工不存在");
        if(addresses.get(0)==null || !addresses.get(0).matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"请先为员工填写有效邮箱");
        }
        String id=UUID.randomUUID().toString();
        Timestamp now=Timestamp.valueOf(LocalDateTime.now());
        jdbc.update("INSERT INTO t_mail_log(msgId,eid,status,routeKey,`exchange`,`count`,tryTime,createTime,updateTime) VALUES(?,?,0,?,?,0,?,?,?)",
            id, employeeId, routingKey, exchange, now, now, now);
        return id;
    }

    public void dispatchDue() {
        Timestamp now=Timestamp.valueOf(LocalDateTime.now());
        // A worker that died while sending must not leave an outbox entry locked forever.
        jdbc.update("UPDATE t_mail_log SET status=0,updateTime=? WHERE status=3 AND updateTime<?",now,
            Timestamp.valueOf(LocalDateTime.now().minusMinutes(5)));
        jdbc.update("UPDATE t_mail_log SET status=2,updateTime=? WHERE status=0 AND COALESCE(`count`,0)>=? AND tryTime<=?",
            now, MailConstants.MAX_TRY_COUNT,now);
        List<Map<String,Object>> due=jdbc.queryForList("SELECT msgId,eid FROM t_mail_log WHERE status=0 AND COALESCE(`count`,0)<? AND tryTime<=? ORDER BY createTime LIMIT 25",
            MailConstants.MAX_TRY_COUNT,now);
        for (Map<String,Object> entry:due) {
            String id=String.valueOf(entry.get("msgId"));
            int claimed=jdbc.update("UPDATE t_mail_log SET `count`=COALESCE(`count`,0)+1,tryTime=?,updateTime=? WHERE msgId=? AND status=0 AND COALESCE(`count`,0)<? AND tryTime<=?",
                Timestamp.valueOf(LocalDateTime.now().plusMinutes(MailConstants.MSG_TIMEOUT)),now,id,MailConstants.MAX_TRY_COUNT,now);
            if(claimed!=1) continue;
            try {
                MailJob job=new MailJob().setMsgId(id).setEid(((Number)entry.get("eid")).intValue());
                MessageProperties properties=new MessageProperties();
                properties.setMessageId(id); properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
                properties.setContentEncoding("UTF-8");
                rabbit.send(exchange,routingKey,new Message(json.writeValueAsBytes(job),properties),new CorrelationData(id));
            } catch(Exception exception) {
                // The durable outbox will retry after tryTime; never report broker acceptance as SMTP delivery.
                LOG.warn("Mail publication failed for {}; scheduled retry remains pending",id);
            }
        }
    }

    public void retry(String messageId) {
        int changed=jdbc.update("UPDATE t_mail_log SET status=0,`count`=0,tryTime=?,updateTime=? WHERE msgId=? AND status=2",
            Timestamp.valueOf(LocalDateTime.now()),Timestamp.valueOf(LocalDateTime.now()),messageId);
        if(changed!=1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"仅失败的邮件可以重试");
    }
}
