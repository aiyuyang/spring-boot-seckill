-- KEYS[1]: Rate limit key (e.g., seckill:limit:1001)
-- ARGV[1]: Rate limit count (limit)
-- ARGV[2]: Rate limit time (seconds)

local key = KEYS[1]
local limit = tonumber(ARGV[1])
local expireTime = tonumber(ARFV[2])

local current = redis.call('incr', key)

if current == 1 then
    redis.call('expire', key, expireTime)
end

if current > limit then
    return 0
end

return 1