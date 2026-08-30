CREATE DATABASE IF NOT EXISTS ticket_event DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS ticket_order DEFAULT CHARACTER SET utf8mb4;

USE ticket_event;
CREATE TABLE IF NOT EXISTS `show` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, title VARCHAR(128) NOT NULL, category VARCHAR(32) NOT NULL,
  poster VARCHAR(512), description VARCHAR(2000), status TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS show_session (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, show_id BIGINT NOT NULL, venue VARCHAR(128) NOT NULL,
  sale_start_time DATETIME NOT NULL, sale_end_time DATETIME NOT NULL, show_time DATETIME NOT NULL,
  status TINYINT NOT NULL DEFAULT 1, INDEX idx_show_id (show_id)
);
CREATE TABLE IF NOT EXISTS ticket_tier (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, session_id BIGINT NOT NULL, name VARCHAR(64) NOT NULL,
  price DECIMAL(10,2) NOT NULL, total_stock INT NOT NULL, available_stock INT NOT NULL,
  purchase_limit INT NOT NULL DEFAULT 2, status TINYINT NOT NULL DEFAULT 1, INDEX idx_session_id (session_id)
);

USE ticket_order;
CREATE TABLE IF NOT EXISTS ticket_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, user_id BIGINT NOT NULL, session_id BIGINT NOT NULL, audience_id BIGINT,
  total_amount DECIMAL(10,2) NOT NULL, status TINYINT NOT NULL, create_time DATETIME NOT NULL,
  pay_time DATETIME, close_time DATETIME, INDEX idx_user_session (user_id, session_id), INDEX idx_pending_time (status, create_time)
);
CREATE TABLE IF NOT EXISTS ticket_order_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, order_id BIGINT NOT NULL, tier_id BIGINT NOT NULL, tier_name VARCHAR(64) NOT NULL,
  unit_price DECIMAL(10,2) NOT NULL, quantity INT NOT NULL, INDEX idx_order_id (order_id)
);
CREATE TABLE IF NOT EXISTS ticket (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, order_id BIGINT NOT NULL, audience_id BIGINT, token VARCHAR(64) NOT NULL UNIQUE,
  status TINYINT NOT NULL, verified_time DATETIME, INDEX idx_order_id (order_id)
);
