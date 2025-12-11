-- 1. Clean up old tables
DROP TABLE IF EXISTS seckill_order;
DROP TABLE IF EXISTS order_info;
DROP TABLE IF EXISTS seckill_activity;

-- 2. Activity table
CREATE TABLE seckill_activity (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  product_id BIGINT NOT NULL,
  original_price DECIMAL(19,2) NOT NULL,
  seckill_price DECIMAL(19,2) NOT NULL,
  initial_stock INT NOT NULL,
  available_stock INT NOT NULL,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Order detail table
CREATE TABLE order_info (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  order_price DECIMAL(19,2) NOT NULL,
  order_status INT NOT NULL DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Seckill order table
CREATE TABLE seckill_order (
  id BIGINT NOT NULL AUTO_INCREMENT,
  activity_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  order_id BIGINT NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_activity_user (activity_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Initialize data
-- Activity 1: Starts in 1 hour (for testing countdown)
INSERT INTO seckill_activity (id, name, product_id, original_price, seckill_price, initial_stock, available_stock, start_time, end_time) 
VALUES (1, 'iPhone 15 Pro', 1001, 9999.00, 4999.00, 100, 100, DATE_ADD(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 25 HOUR));

-- Activity 2: Started activity (starts from current time)
INSERT INTO seckill_activity (id, name, product_id, original_price, seckill_price, initial_stock, available_stock, start_time, end_time) 
VALUES (2, '小米 14', 1002, 3999.00, 1999.00, 50, 50, NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY));

-- Activity 3: Sold out activity
INSERT INTO seckill_activity (id, name, product_id, original_price, seckill_price, initial_stock, available_stock, start_time, end_time) 
VALUES (3, 'MacBook Pro (已售罄)', 1003, 12999.00, 6999.00, 30, 0, NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY));