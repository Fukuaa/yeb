package com.xxxx.server.task;

import com.xxxx.server.service.MailOutboxService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name="app.mail.dispatch-enabled",havingValue="true",matchIfMissing=true)
public class MailTask {
    private final MailOutboxService outbox;
    public MailTask(MailOutboxService outbox) { this.outbox=outbox; }
    @Scheduled(fixedDelayString="${app.mail.dispatch-delay:10000}")
    public void mailTask() { outbox.dispatchDue(); }
}