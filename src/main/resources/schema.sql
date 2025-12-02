-- 1. 清理旧表
DROP TABLE IF EXISTS seckill_order;
DROP TABLE IF EXISTS order_info;
DROP TABLE IF EXISTS seckill_activity;

-- 2. 活动表
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

-- 3. 订单明细表
CREATE TABLE order_info (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  order_price DECIMAL(19,2) NOT NULL,
  order_status INT NOT NULL DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 秒杀成功表 (这张表就是你缺少的！)
CREATE TABLE seckill_order (
  id BIGINT NOT NULL AUTO_INCREMENT,
  activity_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  order_id BIGINT NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_activity_user (activity_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 初始化数据
INSERT INTO seckill_activity (id, name, product_id, original_price, seckill_price, initial_stock, available_stock, start_time, end_time) 
VALUES (1, 'iPhone 15 Pro', 1001, 9999.00, 4999.00, 100, 100, NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY));
-- 插入一个库存已经卖完的活动 (ID=2)
-- 用于测试库存不足的情况
INSERT INTO `seckill_activity` 
(id, name, product_id, original_price, seckill_price, initial_stock, available_stock, start_time, end_time) 
VALUES 
(2, '小米 14 (已售罄)', 1002, 3999.00, 1999.00, 50, 0, NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY));