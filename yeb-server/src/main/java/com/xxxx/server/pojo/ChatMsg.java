package com.xxxx.server.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode
@Accessors(chain = true)
public class ChatMsg {
    private String from;
    private String to;
    private LocalDateTime date;
    private String forNickName;
}
