package com.xxxx.server.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode
@Accessors(chain = true)
public class ChatMsg {
    private Long id;
    private String from;
    private String to;
    private String content;
    private LocalDateTime date;
    private String fromNickName;
    private Boolean read;
}
