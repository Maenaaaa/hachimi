# AI 自动判决审核系统部署指南

## 前置要求

1. MySQL 8.0+
2. Redis
3. Java 21+
4. Node.js 18+
5. 小米 MiMo API Key

## 部署步骤

### 1. 数据库迁移

执行以下 SQL 脚本创建必要的表：

```bash
mysql -u root -p campus_exchange < sql/migration-ai-judgment.sql
mysql -u root -p campus_exchange < sql/migration-ai-fields.sql
```

### 2. 配置 MiMo API Key

在环境变量中设置 MiMo API Key：

```bash
export MIMO_API_KEY=your-api-key-here
```

或在 `application.yml` 中配置：

```yaml
ai:
  mimo:
    api-key: your-api-key-here
```

### 3. 后端部署

```bash
cd backend
mvn clean package -DskipTests
java -jar target/campus-exchange-1.0.0.jar
```

### 4. 前端部署

```bash
cd frontend
npm install
npm run build
```

将 `dist/` 目录部署到 Web 服务器。

## 配置说明

### AI 配置项

| 配置键 | 默认值 | 说明 |
|--------|--------|------|
| `report_auto_review` | `true` | 举报是否自动 AI 审核 |
| `dispute_auto_arbitrate` | `true` | 争议是否自动 AI 仲裁 |
| `goods_auto_review` | `false` | 商品发布是否自动 AI 审核 |
| `confidence_auto_threshold` | `0.8` | 自动执行置信度阈值 |
| `confidence_escalate_threshold` | `0.5` | 转人工置信度阈值 |

### 环境变量

| 变量名 | 说明 |
|--------|------|
| `MIMO_API_KEY` | 小米 MiMo API Key |
| `SPRING_DATASOURCE_URL` | 数据库连接 URL |
| `SPRING_DATASOURCE_USERNAME` | 数据库用户名 |
| `SPRING_DATASOURCE_PASSWORD` | 数据库密码 |

## 回滚策略

如果 AI 功能出现问题，可以快速回滚：

1. **关闭自动审核**：在管理后台将所有自动审核配置设为 `false`
2. **回滚代码**：部署上一版本的后端和前端代码
3. **数据保留**：数据库表和字段保留，不影响现有功能

## 监控建议

1. 监控 MiMo API 调用成功率
2. 监控 AI 判决的置信度分布
3. 定期抽样检查 AI 判决质量
4. 监控用户上诉率

## 常见问题

### Q: AI 服务不可用怎么办？
A: 系统会自动降级，将案件转为人工处理，不影响用户正常使用。

### Q: 如何调整置信度阈值？
A: 在管理后台的 AI 配置页面可以实时调整，无需重启服务。

### Q: 用户对 AI 判决不满意怎么办？
A: 用户可以在判决详情页点击"申请上诉"，管理员会进行人工复审。
