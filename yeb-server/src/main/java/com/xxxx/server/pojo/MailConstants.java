package com.xxxx.server.pojo;

public class MailConstants {
    public static final Integer DELIVERING = 0;
    public static final Integer SUCCESS = 1;
    public static final Integer FAILURE = 2;
    public static final Integer MAX_TRY_COUNT = 3;
    public static final Integer MSG_TIMEOUT = 1;
    public static final String MAIL_QUEUE_NAME = "mail.queue";
    public static final String MAIL_EXCHANGE_NAME = "mail.exchange";
    public static final String MAIL_ROUTING_KEY_NAME = "mail.routing.key";
}
