-- 参数说明：
-- KEYS[1]: 用户名单 Set Key (seckill:success_users:{activityId})
-- KEYS[2]: 库存 String Key (seckill:stock:{activityId})
-- ARGV[1]: 用户 ID

-- 1. 校验：用户是否已存在（重复下单）
if redis.call('sismember', KEYS[1], ARGV[1]) == 1 then
    return -1 -- 返回 -1 代表重复下单
end

-- 2. 校验：检查库存
local stock = tonumber(redis.call('get', KEYS[2]))

if stock == nil or stock <= 0 then
    return 0 -- 返回 0 代表库存不足
end

-- 3. 执行：扣减库存 & 加入名单
redis.call('decr', KEYS[2])
redis.call('sadd', KEYS[1], ARGV[1])

return 1 -- 返回 1 代表抢购成功