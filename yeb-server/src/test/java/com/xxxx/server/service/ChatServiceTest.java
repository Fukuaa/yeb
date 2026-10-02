package com.xxxx.server.service;

import com.xxxx.server.pojo.Admin;
import com.xxxx.server.pojo.ChatMsg;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatServiceTest {
    private ChatService chats;
    private Admin alice, bob, carol;
    private JdbcTemplate jdbc;
    @BeforeEach
    void setup() {
        JdbcDataSource database=new JdbcDataSource();database.setURL("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DB_CLOSE_DELAY=-1");
        jdbc=new JdbcTemplate(database);
        jdbc.execute("CREATE TABLE t_admin(username VARCHAR(64),name VARCHAR(64))");
        jdbc.execute("CREATE TABLE t_chat_message(id BIGINT AUTO_INCREMENT PRIMARY KEY,sender VARCHAR(64),recipient VARCHAR(64),content VARCHAR(2000),created_at TIMESTAMP,read_at TIMESTAMP)");
        IAdminService admins=mock(IAdminService.class);
        alice=admin("alice");bob=admin("bob");carol=admin("carol");
        for(Admin admin:new Admin[]{alice,bob,carol}) {
            when(admins.getAdminByUserName(admin.getUsername())).thenReturn(admin);
            jdbc.update("INSERT INTO t_admin VALUES(?,?)",admin.getUsername(),admin.getName());
        }
        chats=new ChatService(jdbc,admins);
    }
    private Admin admin(String username) { Admin value=new Admin();value.setUsername(username);value.setName(username+" name");value.setEnabled(true);return value; }
    private ChatMsg send(Admin sender,String peer,String text) { return chats.send(sender,new ChatMsg().setTo(peer).setContent(text)); }
    @Test
    void storesContentAndOverridesSpoofedSenderAndTimestamp() {
        ChatMsg result=chats.send(alice,new ChatMsg().setFrom("mallory").setTo("bob").setContent("你好 😀").setDate(LocalDateTime.of(2000,1,1,0,0)));
        assertEquals("alice",result.getFrom());assertEquals("alice name",result.getFromNickName());
        assertEquals("你好 😀",chats.history("bob","alice",null,50).get(0).getContent());
        assertTrue(result.getDate().getYear()>2000);assertNotNull(result.getId());
    }
    @Test
    void keepsHistoryPrivateAndPaginatesInChronologicalOrder() {
        ChatMsg first=send(alice,"bob","one");ChatMsg second=send(bob,"alice","two");send(alice,"carol","private");
        assertEquals(2,chats.history("alice","bob",null,50).size());
        assertTrue(chats.history("mallory","alice",null,50).isEmpty());
        assertEquals(second.getId(),chats.history("alice","bob",null,1).get(0).getId());
        assertEquals(first.getId(),chats.history("alice","bob",second.getId(),50).get(0).getId());
    }
    @Test
    void retainsOfflineUnreadAndOnlyMarksTheCurrentRecipientsConversation() {
        send(alice,"bob","one");send(alice,"bob","two");send(alice,"carol","other");
        assertEquals(2L,chats.unread("bob").get("alice"));
        chats.markRead("bob","alice");assertTrue(chats.unread("bob").isEmpty());
        assertEquals(1L,chats.unread("carol").get("alice"));
    }
    @Test
    void rejectsEmptyOversizedUnknownAndSelfMessages() {
        assertThrows(ResponseStatusException.class,()->send(alice,"bob","  "));
        assertThrows(ResponseStatusException.class,()->send(alice,"bob",new String(new char[2001]).replace('\0','a')));
        assertThrows(ResponseStatusException.class,()->send(alice,"missing","hello"));
        assertThrows(ResponseStatusException.class,()->send(alice,"alice","hello"));
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM t_chat_message",Integer.class));
    }
}
