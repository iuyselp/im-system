# IM 通信系统 - WebSocket & WebRTC 集成指南

**完成日期**: 2026-03-14  
**版本**: V1.5  

---

## 📡 WebSocket 实时通信

### 架构设计

```
┌──────────────┐                      ┌──────────────┐
│   Frontend   │                      │   Backend    │
│              │                      │              │
│  wsManager   │◀────WebSocket──────▶│ WebSocketHandler │
│              │    (JSON 消息)        │              │
│              │                      │              │
│  callStore   │◀────信令事件─────────│ CallSignalingService │
│              │                      │              │
│  webrtc      │◀────WebRTC 信令──────│              │
└──────────────┘                      └──────────────┘
```

### 连接配置

**后端端点**:
- `/ws/text` - 文本 WebSocket（JWT 认证）
- `/ws` - STOMP WebSocket

**前端 URL**:
```javascript
const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
const url = `${protocol}//${host}/ws/text?token=${jwtToken}`
```

### 消息类型

```javascript
// 连接相关
CONNECTED, DISCONNECTED, ERROR

// 心跳
PING, PONG

// 消息相关
NEW_MESSAGE, MESSAGE_ACK, MESSAGE_READ, MESSAGE_REVOKE

// 通话相关
CALL_INVITE, CALL_ANSWER, CALL_REJECT, CALL_END, CALL_CANCEL

// WebRTC 信令
ICE_CANDIDATE, OFFER, ANSWER
```

### 使用示例

#### 前端连接 WebSocket

```javascript
import wsManager, { WsMessageType } from '@/utils/websocket'

// 连接
wsManager.connect()

// 监听消息
wsManager.on(WsMessageType.NEW_MESSAGE, (data) => {
  console.log('收到新消息:', data)
})

// 监听通话邀请
wsManager.on(WsMessageType.CALL_INVITE, (data) => {
  console.log('收到通话邀请:', data)
})

// 发送消息
wsManager.send(WsMessageType.PING, null)

// 断开
wsManager.disconnect()
```

#### 后端发送消息

```java
@Autowired
private WebSocketHandler webSocketHandler;

// 发送给单个用户
webSocketHandler.sendMessage(userId, new WsMessage("NEW_MESSAGE", data));

// 广播给多个用户
webSocketHandler.broadcastMessage(userIds, new WsMessage("NEW_MESSAGE", data));
```

---

## 📞 通话信令流程

### 1. 发起通话

```
主叫方                              被叫方
  │                                   │
  │───CALL_INVITE (WebSocket)────────▶│
  │                                   │
  │                                   │ 弹出通话对话框
  │                                   │
```

### 2. 接听通话

```
主叫方                              被叫方
  │                                   │
  │◀───CALL_ANSWER (WebSocket)───────│
  │                                   │
  │ 开始 WebRTC 连接                    │
  │                                   │
```

### 3. WebRTC 连接建立

```
主叫方                              被叫方
  │                                   │
  │───OFFER (SDP)───────────────────▶│
  │                                   │
  │◀───ANSWER (SDP)──────────────────│
  │                                   │
  │◀───ICE_CANDIDATE────────────────▶│
  │      (多次交换)                    │
  │                                   │
  │  连接建立，开始音视频传输            │
  │                                   │
```

### 4. 结束通话

```
主叫方                              被叫方
  │                                   │
  │───CALL_END──────────────────────▶│
  │                                   │
  │  释放媒体流，关闭连接               │
  │                                   │
```

---

## 🌐 WebRTC 配置

### STUN/TURN 服务器

**application.yml**:
```yaml
webrtc:
  stun-servers: |
    stun:stun.l.google.com:19302,
    stun:stun1.l.google.com:19302
  turn-servers: |
    turn:your-turn-server.com:3478?transport=udp
```

**前端配置**:
```javascript
const config = {
  iceServers: [
    { urls: 'stun:stun.l.google.com:19302' },
    { 
      urls: 'turn:your-turn-server.com:3478',
      username: 'user',
      credential: 'password'
    }
  ]
}
```

### 媒体流约束

```javascript
// 语音通话
const audioConstraints = {
  audio: true,
  video: false
}

// 视频通话
const videoConstraints = {
  audio: true,
  video: {
    width: { ideal: 1280 },
    height: { ideal: 720 },
    frameRate: { ideal: 30 }
  }
}
```

---

## 🔧 使用示例

### 发起通话

```javascript
import { useCallStore } from '@/store/call'

const callStore = useCallStore()

// 发起语音通话
await callStore.startCall(targetId, 'AUDIO')

// 发起视频通话
await callStore.startCall(targetId, 'VIDEO')

// 群聊通话
await callStore.startCall(null, 'VIDEO', groupId)
```

### 处理来电

```javascript
// 在 call store 中已自动处理
// 监听 CALL_INVITE 事件并弹出对话框

function handleIncomingCall(data) {
  callerInfo.value = {
    id: data.callerId,
    name: data.callerName,
    avatar: data.callerAvatar
  }
  currentCall.value = data
  callStatus.value = 'incoming'
}
```

### 使用 WebRTC 管理器

```javascript
import webrtc from '@/utils/webrtc'

// 加载配置
await webrtc.loadWebRTCConfig()

// 初始化呼叫
await webrtc.initCall(targetId, 'VIDEO')

// 接听呼叫
await webrtc.answerCall(callData, 'VIDEO')

// 处理 Offer
const answer = await webrtc.handleOffer(offer)

// 处理 ICE 候选
await webrtc.handleIceCandidate(candidate)

// 切换静音
const isMuted = webrtc.toggleMute()

// 结束通话
webrtc.endCall()
```

---

## 📊 消息格式

### CALL_INVITE

```json
{
  "type": "CALL_INVITE",
  "data": {
    "callId": "call_1234567890",
    "callerId": 1,
    "receiverId": 2,
    "groupId": null,
    "type": "VIDEO",
    "timestamp": 1710403200000
  },
  "timestamp": 1710403200000
}
```

### OFFER (SDP)

```json
{
  "type": "OFFER",
  "data": {
    "callId": "call_1234567890",
    "offer": {
      "type": "offer",
      "sdp": "v=0\r\no=- 123456..."
    },
    "targetId": 2
  }
}
```

### ICE_CANDIDATE

```json
{
  "type": "ICE_CANDIDATE",
  "data": {
    "callId": "call_1234567890",
    "candidate": {
      "candidate": "candidate:123456...",
      "sdpMid": "0",
      "sdpMLineIndex": 0
    }
  }
}
```

---

## ⚠️ 注意事项

### 1. 网络连接

- WebSocket 支持自动重连（指数退避）
- 心跳检测（30 秒间隔）
- 断线后需要重新初始化通话状态

### 2. 浏览器兼容性

- Chrome 50+
- Firefox 50+
- Safari 11+
- Edge 79+

### 3. HTTPS 要求

- 生产环境必须使用 HTTPS/WSS
- 媒体设备权限需要安全上下文
- WebRTC 在某些浏览器要求 HTTPS

### 4. 防火墙/NAT

- 需要 STUN 服务器穿透 NAT
- 对称 NAT 需要 TURN 服务器中继
- 建议部署 coturn 服务器

---

## 🚀 部署 TURN 服务器

### 安装 coturn

```bash
# Ubuntu/Debian
sudo apt-get install coturn

# 编辑配置
sudo vim /etc/turnserver.conf
```

### 配置示例

```conf
listening-port=3478
tls-listening-port=5349
listening-ip=0.0.0.0
external-ip=your.public.ip

realm=your-domain.com
server-name=turn.your-domain.com

lt-cred-mech
user=user:password

cert=/etc/ssl/certs/turnserver.crt
pkey=/etc/ssl/private/turnserver.key
```

---

## 📝 待完成功能

### 高优先级
- [ ] 完整的 WebRTC 媒体流处理
- [ ] 多人通话支持
- [ ] 网络质量监测
- [ ] 通话录制

### 中优先级
- [ ] 屏幕共享
- [ ] 美颜滤镜
- [ ] 背景虚化
- [ ] 回声消除优化

---

**开发团队**: IM Team  
**文档版本**: 1.0
