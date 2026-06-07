## Context

校园闲置物品交换平台是一个全新的前后端分离Web项目，面向高校学生群体提供校内二手物品交易服务。项目从零搭建，无历史技术债务。技术栈选型已在上游确定：前端 Vue 3 + Naive UI，后端 Spring Boot 3 + Java 21。需要在一个开发周期内完成完整的数据库设计、后端API、前端页面和核心业务逻辑。

约束条件：
- 开发环境需要 MySQL 8、Redis、MinIO 三个基础设施服务
- 不接入AI推荐、AI识图、风控系统等外部服务（预留扩展接口）
- 前后端均为独立项目，通过 HTTP RESTful API + WebSocket 通信
- 目标产出是可运行的完整源码，不是Demo

## Goals / Non-Goals

**Goals:**
- 建立完整的前后端分离架构，支持用户端和后台管理两套前端
- 实现16张以上数据库表的完整设计，包含索引、外键、逻辑删除、审计字段
- 实现13个核心业务模块：用户认证、用户资料、商品管理、商品浏览、商品搜索、社交互动、即时聊天、订单交易、用户评价、举报系统、通知系统、后台管理、文件存储
- 统一的RESTful接口规范、统一的响应体格式、统一的异常处理
- JWT无状态认证 + Spring Security角色权限控制
- WebSocket实现实时私聊通信
- Redis缓存热点数据提升性能
- MinIO对象存储管理图片文件

**Non-Goals:**
- 不实现AI推荐算法、AI识图、风控系统（预留扩展接口）
- 不实现支付系统（订单为线下交易确认模式）
- 不实现第三方登录（仅用户名密码注册登录）
- 不实现Docker容器化部署配置
- 不实现CI/CD流水线
- 不实现移动端原生App（仅响应式Web）

## Decisions

### 1. 前后端分离架构

**决定**: 前端和后端为两个独立项目，通过 HTTP/HTTPS + WebSocket 通信。

**理由**: 前后端分离是当前企业级Web应用的标准架构，支持独立开发、独立部署、技术栈解耦。前端通过 Nginx 反向代理或 Vite proxy 解决开发环境跨域问题。

**备选方案**: 单体模板渲染 (Thymeleaf) — 不采用，前后端耦合度高，不利于团队协作和前端工程化。

### 2. 前端状态管理方案

**决定**: 使用 Pinia 进行全局状态管理，按模块拆分 Store (userStore, goodsStore, chatStore, notificationStore)。

**理由**: Pinia 是 Vue 3 官方推荐的状态管理库，TypeScript 支持良好，模块化设计天然支持按业务拆分。相比 Vuex 4 更轻量且 API 更简洁。

### 3. 认证方案

**决定**: Spring Security 6 + JWT 无状态认证。前端存储 token 在 localStorage，每次请求通过 Axios 拦截器自动携带。Token 有效期 24h，支持 refresh token 续期。

**理由**: JWT 无状态认证适合前后端分离架构，无需服务端 Session 存储，水平扩展友好。Spring Security 6 是 Spring Boot 3 的标准安全框架。

**备选方案**: OAuth2 + Session — 不采用，Session 需要服务端存储状态，不利于扩展，且对于校园内部平台过于复杂。

### 4. 数据库ORM方案

**决定**: MyBatis Plus + Lambda QueryWrapper。

**理由**: MyBatis Plus 在 MyBatis 基础上提供开箱即用的 CRUD、分页、逻辑删除、自动填充等功能，Lambda 表达式方式避免字段名硬编码。相比 JPA/Hibernate 更灵活，SQL 可控性更强。

### 5. 文件存储方案

**决定**: MinIO 对象存储，通过预签名 URL 访问文件。

**理由**: MinIO 兼容 S3 API，部署简单，适合本地开发环境。预签名 URL 避免文件直接暴露，支持访问过期控制。预留接口可切换至云存储（阿里云OSS、AWS S3等）。

### 6. 即时通信方案

**决定**: Spring WebSocket + STOMP 协议实现私聊。

**理由**: WebSocket 是浏览器原生支持的实时双向通信协议，Spring 对 STOMP 有完善支持。相比 Socket.IO 不需要额外的前端库，相比轮询方案更节省资源。

### 7. 缓存策略

**决定**: Redis 缓存热门商品列表（ZSET排序）、商品详情（String）、用户信息（Hash）。缓存过期时间：热门商品 30min，商品详情 1h，用户信息 2h。采用 Cache-Aside 模式，先查缓存，未命中查数据库并回写缓存。

**理由**: Cache-Aside 是最常见的缓存模式，实现简单，适合读多写少的商品浏览场景。Redis 相比本地缓存支持分布式部署。

### 8. 前端UI组件库

**决定**: Naive UI。

**理由**: Naive UI 是专为 Vue 3 设计的组件库，TypeScript 支持完善，Tree-shaking 友好，组件设计风格现代化，适合校园风格的年轻化设计。相比 Element Plus 更轻量且视觉风格更现代。

### 9. 分页方案

**决定**: 后端 MyBatis Plus Page 分页插件，前端无限滚动 (Intersection Observer API)。

**理由**: 商品列表采用瀑布流/无限滚动更适合移动端浏览体验，后端分页保证查询性能。MyBatis Plus 分页插件自动拦截 SQL 拼接 LIMIT。

### 10. 项目结构约定

**决定**: 
- 前端: `api/` (接口层) → `stores/` (状态层) → `views/` (视图层)，`components/` (通用组件)，`composables/` (可复用逻辑)
- 后端: `controller` → `service` → `service.impl` → `mapper`，`dto` (入参) / `vo` (出参) 分离

**理由**: 分层架构是 Spring Boot 项目的标准实践，DTO/VO 分离避免实体类直接暴露，接口数据与业务数据解耦。

## Risks / Trade-offs

- **WebSocket连接管理**: 大量并发WebSocket连接可能导致服务器资源紧张 → 配置心跳检测和最大连接数限制，前端断线自动重连
- **图片上传性能**: 多图并发上传可能影响服务器响应 → 前端限制单次上传图片数量（最多9张），单张图片大小限制（5MB），后端异步处理
- **JWT无状态无法主动失效**: 用户被封禁后已签发的token仍然有效 → 设置较短token有效期（24h），关键操作二次验证，管理员操作实时校验用户状态
- **初始数据冷启动**: 新平台缺乏商品数据和用户 → 提供数据库初始化脚本，预置分类数据和测试数据
- **MinIO单点故障**: 开发环境单节点MinIO不可用影响图片访问 → 前端图片加载失败时显示占位图，生产环境建议MinIO集群部署

## Open Questions

- 实名认证是否需要对接学校教务系统？当前方案为手动上传学生证图片 + 管理员人工审核
- 是否需要邮箱验证码注册？当前方案为用户名+密码直接注册，可后续扩展短信/邮箱验证
