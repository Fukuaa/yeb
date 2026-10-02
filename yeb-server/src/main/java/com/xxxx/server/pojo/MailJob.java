package com.xxxx.server.pojo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MailJob {
    private String msgId;
    private Integer eid;
}
