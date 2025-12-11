# 📡 API 文档

## 基础信息

- **Base URL**: `http://localhost:8081`
- **认证方式**: JWT Bearer Token
- **响应格式**: JSON

### 统一响应格式

```json
{
  "code": 200,           // 状态码：200=成功，其他=失败
  "message": "SUCCESS",  // 消息
  "obj": {}             // 数据对象
}
```

### 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 500 | 服务端异常 |
| 500210 | 用户名或密码不正确 |
| 500211 | 手机号码格式不正确 |
| 500212 | 参数校验异常 |
| 500500 | 库存不足 |
| 500501 | 该商品每人限购一件 |
| 500502 | 请求非法，请重新尝试 |
| 500503 | 验证码错误，请重新输入 |

---

## 🔐 认证模块

### 1. 用户登录

**接口**: `POST /auth/login`

**请求参数**:
```
userId: Long (Query参数)
```

**请求示例**:
```bash
POST /auth/login?userId=1001
```

**响应示例**:
```json
{
  "code": 200,
  "message": "SUCCESS",
  "obj": {
    "token": "eyJhbGciOiJIUzUxMiJ9..."
  }
}
```

**说明**: 
- 使用模拟登录，输入任意数字ID即可
- 返回的 token 需要在后续请求的 Header 中携带：`Authorization: Bearer {token}`

---

## 📋 活动模块

### 1. 获取活动列表

**接口**: `GET /activity/list`

**认证**: 需要 Token

**响应示例**:
```json
{
  "code": 200,
  "message": "SUCCESS",
  "obj": [
    {
      "id": 1,
      "name": "iPhone 15 Pro",
      "productId": 1001,
      "originalPrice": 9999.00,
      "seckillPrice": 4999.00,
      "initialStock": 100,
      "availableStock": 100,
      "startTime": "2025-12-10T14:00:00",
      "endTime": "2025-12-11T14:00:00",
      "createTime": "2025-12-10T13:00:00"
    }
  ]
}
```

### 2. 获取活动详情

**接口**: `GET /activity/{activityId}`

**认证**: 需要 Token

**路径参数**:
- `activityId`: 活动ID

**响应示例**: 同活动列表中的单个活动对象

### 3. 获取服务器时间

**接口**: `GET /activity/serverTime`

**认证**: 需要 Token

**响应示例**:
```json
{
  "code": 200,
  "message": "SUCCESS",
  "obj": {
    "serverTime": "2025-12-10T13:56:13",
    "timestamp": 1733831773000
  }
}
```

---

## ⚡ 秒杀模块

### 1. 获取验证码

**接口**: `GET /seckill/captcha`

**认证**: 需要 Token

**请求参数**:
```
activityId: Long (Query参数)
```

**响应**: 图片 Blob（Content-Type: image/png）

**请求示例**:
```bash
GET /seckill/captcha?activityId=1
```

### 2. 获取秒杀Token（验证码校验 + 获取Token）

**接口**: `GET /seckill/token/{activityId}`

**认证**: 需要 Token

**路径参数**:
- `activityId`: 活动ID

**请求参数**:
```
verifyCode: String (Query参数) - 验证码
```

**响应示例**:
```json
{
  "code": 200,
  "message": "SUCCESS",
  "obj": "423cdd96605347d7b898eda77f582e8e"  // 秒杀Token
}
```

**错误响应**:
```json
{
  "code": 500503,
  "message": "验证码错误，请重新输入",
  "obj": null
}
```

### 3. 执行秒杀

**接口**: `POST /seckill/doSeckill/{activityId}/{token}`

**认证**: 需要 Token

**路径参数**:
- `activityId`: 活动ID
- `token`: 秒杀Token（从获取秒杀Token接口获得）

**响应示例**:
```json
{
  "code": 200,
  "message": "SUCCESS",
  "obj": "排队中"
}
```

**错误响应**:
```json
{
  "code": 500501,
  "message": "该商品每人限购一件",
  "obj": null
}
```

或

```json
{
  "code": 500500,
  "message": "库存不足",
  "obj": null
}
```

---

## 📦 订单模块

### 1. 查询订单结果

**接口**: `GET /order/result/{activityId}`

**认证**: 需要 Token

**路径参数**:
- `activityId`: 活动ID

**响应示例**（排队中）:
```json
{
  "code": 200,
  "message": "SUCCESS",
  "obj": {
    "status": "waiting",
    "message": "排队中，请稍候..."
  }
}
```

**响应示例**（秒杀成功）:
```json
{
  "code": 200,
  "message": "SUCCESS",
  "obj": {
    "status": "success",
    "orderId": 12345,
    "orderPrice": 4999.00,
    "orderStatus": 0,
    "createTime": "2025-12-10T14:00:00"
  }
}
```

**说明**: 
- 前端需要轮询此接口，直到 `status` 变为 `success`
- 建议轮询间隔：1秒

---

## 🔒 认证说明

所有需要认证的接口都需要在请求头中携带 Token：

```
Authorization: Bearer {token}
```

**获取 Token**：
1. 调用 `/auth/login` 接口
2. 从响应中获取 `obj.token`
3. 存储到 localStorage 或 sessionStorage
4. 后续请求自动携带（前端已实现）

---

## 📊 Swagger UI

启动后端服务后，可以通过 Swagger UI 查看完整的 API 文档：

**访问地址**: http://localhost:8081/swagger-ui.html

Swagger UI 提供了：
- 所有接口的详细说明
- 请求参数和响应格式
- 在线测试功能
- 认证配置

---

## 🧪 测试建议

### 使用 Postman 或 curl 测试

1. **登录获取 Token**:
```bash
curl -X POST "http://localhost:8081/auth/login?userId=1001"
```

2. **获取活动列表**:
```bash
curl -X GET "http://localhost:8081/activity/list" \
  -H "Authorization: Bearer {your_token}"
```

3. **获取验证码**（保存为图片）:
```bash
curl -X GET "http://localhost:8081/seckill/captcha?activityId=1" \
  -H "Authorization: Bearer {your_token}" \
  -o captcha.png
```

4. **执行秒杀流程**:
```bash
# 1. 获取秒杀Token
curl -X GET "http://localhost:8081/seckill/token/1?verifyCode=1234" \
  -H "Authorization: Bearer {your_token}"

# 2. 执行秒杀（使用上一步获得的token）
curl -X POST "http://localhost:8081/seckill/doSeckill/1/{seckill_token}" \
  -H "Authorization: Bearer {your_token}"

# 3. 查询订单结果
curl -X GET "http://localhost:8081/order/result/1" \
  -H "Authorization: Bearer {your_token}"
```

---

## ⚠️ 注意事项

1. **Token 有效期**: 默认 24 小时，过期后需要重新登录
2. **限流**: 秒杀接口有访问频率限制（5秒内最多5次）
3. **验证码**: 验证码有时效性，建议及时使用
4. **秒杀Token**: 秒杀Token有时效性，获取后应尽快使用
5. **轮询频率**: 订单结果查询建议1秒轮询一次，避免过于频繁

---

**更多详细信息请查看 Swagger UI 文档**
