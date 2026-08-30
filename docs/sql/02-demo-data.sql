USE ticket_event;
INSERT INTO `show` (id, title, category, poster, description, status) VALUES
  (1001, '城市音乐节', '音乐演出', '', '用于本地开发的固定场次', 1)
ON DUPLICATE KEY UPDATE title = VALUES(title), status = VALUES(status);
INSERT INTO show_session (id, show_id, venue, sale_start_time, sale_end_time, show_time, status) VALUES
  (2001, 1001, '城市体育中心', '2026-01-01 10:00:00', '2030-12-31 23:59:59', '2030-01-01 19:30:00', 1)
ON DUPLICATE KEY UPDATE status = VALUES(status);
INSERT INTO ticket_tier (id, session_id, name, price, total_stock, available_stock, purchase_limit, status) VALUES
  (3001, 2001, '看台票', 380.00, 100, 100, 2, 1),
  (3002, 2001, '内场票', 680.00, 50, 50, 2, 1),
  (3003, 2001, 'VIP 票', 1080.00, 20, 20, 2, 1)
ON DUPLICATE KEY UPDATE available_stock = VALUES(available_stock), status = VALUES(status);
