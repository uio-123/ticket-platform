CREATE TABLE IF NOT EXISTS ticket_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, user_id BIGINT NOT NULL, session_id BIGINT NOT NULL, audience_id BIGINT,
  total_amount DECIMAL(10,2) NOT NULL, status TINYINT NOT NULL COMMENT '1-pending,2-paid,3-cancelled',
  create_time DATETIME NOT NULL, pay_time DATETIME, close_time DATETIME,
  INDEX idx_user_session (user_id, session_id), INDEX idx_pending_time (status, create_time)
) COMMENT='票务订单';
CREATE TABLE IF NOT EXISTS ticket_order_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, order_id BIGINT NOT NULL, tier_id BIGINT NOT NULL,
  tier_name VARCHAR(64) NOT NULL, unit_price DECIMAL(10,2) NOT NULL, quantity INT NOT NULL,
  INDEX idx_order_id (order_id)
) COMMENT='票务订单明细';
CREATE TABLE IF NOT EXISTS ticket (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, order_id BIGINT NOT NULL, audience_id BIGINT,
  token VARCHAR(64) NOT NULL UNIQUE, status TINYINT NOT NULL COMMENT '1-issued,2-verified', verified_time DATETIME,
  INDEX idx_order_id (order_id)
) COMMENT='电子票';
