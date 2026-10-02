-- Apply against the existing yeb database; existing data is preserved.
CREATE TABLE IF NOT EXISTS t_chat_message (
    id BIGINT NOT NULL AUTO_INCREMENT,
    sender VARCHAR(64) NOT NULL,
    recipient VARCHAR(64) NOT NULL,
    content VARCHAR(2000) NOT NULL,
    created_at DATETIME(3) NOT NULL,
    read_at DATETIME(3) NULL,
    PRIMARY KEY (id),
    INDEX idx_chat_conversation (sender, recipient, id),
    INDEX idx_chat_inbox (recipient, read_at, sender, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
