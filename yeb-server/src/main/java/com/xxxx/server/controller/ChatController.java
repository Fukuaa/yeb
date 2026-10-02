package com.xxxx.server.controller;

import com.xxxx.server.pojo.Admin;
import com.xxxx.server.service.IAdminService;
import com.xxxx.server.service.ChatService;
import com.xxxx.server.pojo.ChatMsg;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {
    @Autowired
    private IAdminService adminService;
    @Autowired
    private ChatService chats;
    @ApiOperation("获取所有操作员")
    @GetMapping("/admin")
    public List<Map<String,Object>> getAllAdmin(String keywords){
        return adminService.getAllAdmin(keywords).stream().filter(Admin::isEnabled).map(admin -> {
            Map<String,Object> contact = new LinkedHashMap<>();
            contact.put("id", admin.getId()); contact.put("username", admin.getUsername());
            contact.put("name", admin.getName()); contact.put("userFace", admin.getUserFace());
            return contact;
        }).collect(Collectors.toList());
    }
    @GetMapping("/history")
    public List<ChatMsg> history(Authentication authentication, @RequestParam("with") String peer,
                                @RequestParam(required=false) Long beforeId, @RequestParam(defaultValue="50") int size) {
        return chats.history(authentication.getName(), peer, beforeId, size);
    }
    @GetMapping("/unread")
    public Map<String,Long> unread(Authentication authentication) { return chats.unread(authentication.getName()); }
    @PostMapping("/read")
    public Map<String,Boolean> read(Authentication authentication, @RequestParam("with") String peer) {
        chats.markRead(authentication.getName(), peer);
        return java.util.Collections.singletonMap("success", true);
    }
}
