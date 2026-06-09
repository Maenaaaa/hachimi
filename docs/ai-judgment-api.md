# AI 自动判决审核系统 API 文档

## 概述

AI 自动判决审核系统为校园交易平台提供智能审核能力，支持举报审核、订单仲裁和商品审核三个场景。

## API 端点

### 用户端 API

#### 获取 AI 判决详情
```
GET /api/ai-judgment/{id}
```

**响应:**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "sourceType": "REPORT",
    "sourceId": 100,
    "aiModel": "mimo-v2.5-pro",
    "confidence": 0.85,
    "verdict": "APPROVED",
    "reasoning": "AI推理过程...",
    "evidence": ["证据1", "证据2"],
    "autoHandled": true,
    "status": "PROCESSED",
    "createTime": "2026-06-09T10:00:00"
  }
}
```

#### 提交上诉
```
PUT /api/ai-judgment/{id}/appeal
```

**请求体:**
```json
{
  "reason": "我认为判决有误，因为..."
}
```

**响应:**
```json
{
  "code": 200,
  "message": "success"
}
```

#### 获取 AI 配置
```
GET /api/ai-judgment/config
```

**响应:**
```json
{
  "code": 200,
  "data": {
    "report_auto_review": "true",
    "dispute_auto_arbitrate": "true",
    "goods_auto_review": "false",
    "confidence_auto_threshold": "0.8",
    "confidence_escalate_threshold": "0.5"
  }
}
```

#### 更新 AI 配置
```
PUT /api/ai-judgment/config/{key}
```

**请求体:**
```json
{
  "value": "false"
}
```

### 管理员 API

#### 获取 AI 判决列表
```
GET /api/admin/ai-judgment
```

**查询参数:**
- `sourceType` (可选): 来源类型 (REPORT, DISPUTE, GOODS_REVIEW)
- `status` (可选): 状态 (PENDING, PROCESSED, APPEALED, OVERRIDDEN, ESCALATED)
- `page` (可选): 页码，默认 1
- `size` (可选): 每页数量，默认 20

**响应:**
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "sourceType": "REPORT",
      "sourceId": 100,
      "confidence": 0.85,
      "verdict": "APPROVED",
      "status": "PROCESSED",
      "autoHandled": true,
      "createTime": "2026-06-09T10:00:00"
    }
  ]
}
```

#### 获取单个判决详情
```
GET /api/admin/ai-judgment/{id}
```

#### 执行判决
```
PUT /api/admin/ai-judgment/{id}/execute
```

#### 手动处理判决
```
PUT /api/admin/ai-judgment/{id}/handle
```

**请求体:**
```json
{
  "note": "人工处理备注"
}
```

#### 处理上诉
```
PUT /api/ai-judgment/{id}/handle-appeal
```

**请求体:**
```json
{
  "override": true,
  "note": "上诉成功，推翻原判"
}
```

## 状态说明

### 判决状态 (status)
- `PENDING`: 待处理
- `PROCESSED`: 已处理
- `APPEALED`: 已上诉
- `OVERRIDDEN`: 已推翻
- `ESCALATED`: 转人工

### 判决结果 (verdict)
- `APPROVED`: 通过/支持
- `REJECTED`: 拒绝/不支持
- `ESCALATED`: 转人工

### 置信度阈值
- `≥ 0.8`: 自动执行
- `0.5 ~ 0.8`: AI建议 + 转人工确认
- `< 0.5`: 直接转人工

## 业务流程

### 举报审核流程
1. 用户提交举报
2. 系统检查 `report_auto_review` 配置
3. 如果开启，异步调用 AI 分析
4. 根据置信度自动处理或转人工
5. 通知用户审核结果

### 订单仲裁流程
1. 用户申请仲裁
2. 系统检查 `dispute_auto_arbitrate` 配置
3. 如果开启，异步调用 AI 分析
4. 根据置信度自动仲裁或转人工
5. 通知双方仲裁结果

### 商品审核流程
1. 用户发布商品
2. 系统检查 `goods_auto_review` 配置
3. 如果开启，异步调用 AI 审核
4. 根据置信度自动上架/拒绝或转人工
5. 通知用户审核结果

## 错误码

- `200`: 成功
- `400`: 请求参数错误
- `401`: 未认证
- `403`: 无权限
- `404`: 资源不存在
- `500`: 服务器内部错误
