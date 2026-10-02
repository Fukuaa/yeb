package com.xxxx.server.controller;

import com.xxxx.server.pojo.RespBean;
import com.xxxx.server.service.MailOutboxService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mail-log")
@PreAuthorize("hasRole('admin')")
public class MailLogController {
    private final JdbcTemplate jdbc;
    private final MailOutboxService outbox;
    public MailLogController(JdbcTemplate jdbc,MailOutboxService outbox) { this.jdbc=jdbc;this.outbox=outbox; }
    @GetMapping
    public Map<String,Object> list(@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size) {
        int limit=Math.min(100,Math.max(1,size));
        int offset=(Math.max(1,Math.min(page,100000))-1)*limit;
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT m.*,e.name AS employeeName,e.email FROM t_mail_log m LEFT JOIN t_employee e ON e.id=m.eid ORDER BY m.createTime DESC,m.msgId DESC LIMIT ? OFFSET ?",limit,offset);
        Map<String,Object> result=new LinkedHashMap<>();
        result.put("total",jdbc.queryForObject("SELECT COUNT(*) FROM t_mail_log",Long.class));result.put("data",rows);
        return result;
    }
    @PostMapping("/{messageId}/retry")
    public RespBean retry(@PathVariable String messageId) { outbox.retry(messageId);return RespBean.success("已安排重试"); }
    @PostMapping("/welcome/{employeeId}")
    public RespBean welcome(@PathVariable int employeeId) { return RespBean.success("欢迎邮件已加入发送队列",outbox.enqueueWelcome(employeeId)); }
}