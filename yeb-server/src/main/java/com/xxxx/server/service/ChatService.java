package com.xxxx.server.service;

import com.xxxx.server.pojo.Admin;
import com.xxxx.server.pojo.ChatMsg;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {
    private final JdbcTemplate jdbc;
    private final IAdminService admins;
    public ChatService(JdbcTemplate jdbc, IAdminService admins) { this.jdbc = jdbc; this.admins = admins; }

    public ChatMsg send(Admin sender, ChatMsg request) {
        if (request == null || !StringUtils.hasText(request.getContent()) || request.getContent().length() > 2000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "消息正文不能为空，且不能超过 2000 字");
        }
        Admin recipient = recipient(request.getTo());
        if (sender.getUsername().equals(recipient.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请选择其他操作员");
        }
        LocalDateTime now = LocalDateTime.now();
        KeyHolder key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO t_chat_message(sender,recipient,content,created_at) VALUES(?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, sender.getUsername()); statement.setString(2, recipient.getUsername());
            statement.setString(3, request.getContent()); statement.setTimestamp(4, Timestamp.valueOf(now));
            return statement;
        }, key);
        return new ChatMsg().setId(key.getKey().longValue()).setFrom(sender.getUsername()).setTo(recipient.getUsername())
            .setContent(request.getContent()).setDate(now).setFromNickName(sender.getName()).setRead(false);
    }

    public List<ChatMsg> history(String current, String peer, Long beforeId, int size) {
        String username = recipient(peer).getUsername();
        int limit = Math.min(100, Math.max(1, size));
        long before = beforeId == null ? Long.MAX_VALUE : beforeId;
        List<ChatMsg> messages = jdbc.query(
            "SELECT m.*,a.name AS sender_name FROM t_chat_message m LEFT JOIN t_admin a ON a.username=m.sender " +
            "WHERE ((m.sender=? AND m.recipient=?) OR (m.sender=? AND m.recipient=?)) AND m.id<? ORDER BY m.id DESC LIMIT ?",
            (rs, row) -> new ChatMsg().setId(rs.getLong("id")).setFrom(rs.getString("sender")).setTo(rs.getString("recipient"))
                .setContent(rs.getString("content")).setDate(rs.getTimestamp("created_at").toLocalDateTime())
                .setFromNickName(rs.getString("sender_name")).setRead(rs.getTimestamp("read_at") != null),
            current, username, username, current, before, limit);
        Collections.reverse(messages);
        return messages;
    }

    public Map<String, Long> unread(String current) {
        Map<String, Long> result = new LinkedHashMap<>();
        jdbc.query("SELECT sender,COUNT(*) AS unread_count FROM t_chat_message WHERE recipient=? AND read_at IS NULL GROUP BY sender",
            rs -> { result.put(rs.getString("sender"), rs.getLong("unread_count")); }, current);
        return result;
    }

    public void markRead(String current, String peer) {
        jdbc.update("UPDATE t_chat_message SET read_at=? WHERE sender=? AND recipient=? AND read_at IS NULL",
            Timestamp.valueOf(LocalDateTime.now()), recipient(peer).getUsername(), current);
    }

    private Admin recipient(String username) {
        Admin recipient = StringUtils.hasText(username) ? admins.getAdminByUserName(username) : null;
        if (recipient == null || !recipient.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "接收操作员不存在或已停用");
        }
        return recipient;
    }
}
