-- Parameter description:
-- KEYS[1]: User list Set Key (seckill:success_users:{activityId})
-- KEYS[2]: Stock String Key (seckill:stock:{activityId})
-- ARGV[1]: User ID

-- 1. Validate: Check if user already exists (duplicate order)
if redis.call('sismember', KEYS[1], ARGV[1]) == 1 then
    return -1 -- Return -1 means duplicate order
end

-- 2. Validate: Check stock
local stock = tonumber(redis.call('get', KEYS[2]))

if stock == nil or stock <= 0 then
    return 0 -- Return 0 means insufficient stock
end

-- 3. Execute: Decrement stock & Add to list
redis.call('decr', KEYS[2])
redis.call('sadd', KEYS[1], ARGV[1])

return 1 -- Return 1 means seckill success