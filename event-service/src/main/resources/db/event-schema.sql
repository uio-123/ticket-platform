CREATE TABLE IF NOT EXISTS `show` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(128) NOT NULL, category VARCHAR(32) NOT NULL, poster VARCHAR(512), description VARCHAR(2000),
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1-published, 0-hidden',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='演出';

CREATE TABLE IF NOT EXISTS show_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT, show_id BIGINT NOT NULL, venue VARCHAR(128) NOT NULL,
    sale_start_time DATETIME NOT NULL, sale_end_time DATETIME NOT NULL, show_time DATETIME NOT NULL,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1-on sale, 0-closed',
    INDEX idx_show_id (show_id), INDEX idx_sale_time (sale_start_time, sale_end_time)
) COMMENT='演出场次';

CREATE TABLE IF NOT EXISTS ticket_tier (
    id BIGINT PRIMARY KEY AUTO_INCREMENT, session_id BIGINT NOT NULL, name VARCHAR(64) NOT NULL,
    price DECIMAL(10,2) NOT NULL, total_stock INT NOT NULL, available_stock INT NOT NULL,
    purchase_limit INT NOT NULL DEFAULT 2, status TINYINT NOT NULL DEFAULT 1 COMMENT '1-on sale, 0-off sale',
    CONSTRAINT chk_ticket_tier_stock CHECK (available_stock >= 0 AND available_stock <= total_stock),
    INDEX idx_session_id (session_id)
) COMMENT='场次票档';
