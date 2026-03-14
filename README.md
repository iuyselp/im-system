# IM 通信系统

一个生产级的即时通信系统，支持单聊、群聊、音视频通话、客服系统等功能。

## 🚀 技术栈

### 后端
- **框架**: Spring Boot 2.7.x
- **ORM**: MyBatis Plus
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **消息队列**: RabbitMQ (可选)
- **对象存储**: MinIO / OSS / S3
- **WebSocket**: Spring WebSocket + STOMP
- **认证**: JWT
- **文档**: Swagger

### 前端
- **框架**: Vue 3 + Vite
- **UI 库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP**: Axios
- **WebSocket**: 原生 WebSocket

## 📦 功能特性

### 核心功能
- ✅ 用户注册/登录/个人信息管理
- ✅ 单聊（文本、图片、语音、视频、文件）
- ✅ 群聊（创建、管理、权限控制）
- ✅ 消息撤回、删除、转发
- ✅ 离线消息推送
- ✅ WebSocket 实时通信

### 高级功能（V1.5）
- ✅ 音视频通话（WebRTC 信令）
- ✅ 消息已读回执
- ✅ 聊天记录搜索
- ⏳ 多人会议
- ⏳ 客服工单系统
- ⏳ 消息队列削峰
- ⏳ 高可用部署

## 🛠️ 快速开始

### 环境要求
- JDK 1.8+
- Node.js 16+
- MySQL 8.0+
- Redis
- MinIO (可选)

### 1. 数据库初始化

```bash
mysql -u root -p < im-backend/src/main/resources/schema.sql
```

### 2. 启动后端

```bash
cd im-backend
mvn spring-boot:run
```

访问 Swagger 文档：http://localhost:8080/api/swagger-ui.html

### 3. 启动前端

```bash
cd im-frontend
npm install
npm run dev
```

访问：http://localhost:5173

## 📁 项目结构

```
im-system/
├── docs/                    # 文档
│   ├── 01-需求文档.md
│   ├── 02-架构设计.md
│   └── 03-数据库设计.md
├── im-backend/              # 后端项目
│   ├── src/main/java/com/im/
│   │   ├── config/          # 配置类
│   │   ├── controller/      # 控制器
│   │   ├── entity/          # 实体类
│   │   ├── mapper/          # Mapper
│   │   ├── service/         # 服务层
│   │   ├── websocket/       # WebSocket
│   │   └── util/            # 工具类
│   └── src/main/resources/
│       ├── application.yml  # 配置文件
│       └── schema.sql       # 数据库脚本
└── im-frontend/             # 前端项目
    └── src/
        ├── api/             # API 接口
        ├── components/      # 组件
        ├── views/           # 页面
        ├── store/           # 状态管理
        └── router/          # 路由
```

## 🔧 配置说明

### 后端配置 (application.yml)

```yaml
# 数据库
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/im_system
    username: root
    password: your_password

# Redis
  redis:
    host: localhost
    port: 6379

# MinIO
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
```

## 📖 API 文档

启动后端后访问：http://localhost:8080/api/swagger-ui.html

### 主要接口

#### 认证
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出

#### 用户
- `GET /api/user/info` - 获取用户信息
- `PUT /api/user/info` - 更新用户信息

#### 消息
- `POST /api/message/send` - 发送消息
- `GET /api/message/list` - 获取消息列表
- `POST /api/message/revoke` - 撤回消息

#### 群组
- `POST /api/group/create` - 创建群组
- `GET /api/group/info` - 获取群信息
- `POST /api/group/invite` - 邀请成员

## 🔒 安全说明

- 所有密码使用 BCrypt 加密
- JWT Token 认证，7 天有效期
- WebSocket 连接需要 Token 验证
- 支持 HTTPS/WSS 加密传输

## 📝 开发计划

### V1.0 (MVP) - 已完成
- [x] 用户系统
- [x] 单聊功能
- [x] 群聊基础
- [x] WebSocket 通信
- [x] 文件上传

### V1.5 - 已完成 🎉
- [x] 音视频通话（信令服务 + WebRTC 媒体流）
- [x] 消息已读回执
- [x] 聊天记录搜索
- [x] WebSocket 实时消息推送
- [x] 通话历史管理

### V2.0
- [ ] 多人音视频会议（SFU 架构）
- [ ] 客服工单系统
- [ ] 消息队列（RabbitMQ）
- [ ] 高可用部署
- [ ] 屏幕共享
- [ ] 通话录制

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

MIT License

---

**开发团队**: IM Team
**创建日期**: 2026-03-13
