# IM 通信系统 - WebRTC 完整集成指南

**完成日期**: 2026-03-14  
**版本**: V1.5  

---

## 🎯 概述

本指南介绍如何在 IM 系统中实现完整的 WebRTC 音视频通话功能，包括：

- ✅ WebSocket 信令服务器
- ✅ WebRTC P2P 连接建立
- ✅ 媒体流处理（音频/视频）
- ✅ 通话控制（静音、开关摄像头）
- ✅ 视频渲染（本地/远程）

---

## 📋 前置条件

### 浏览器支持

| 浏览器 | 最低版本 | WebRTC 支持 |
|--------|---------|-----------|
| Chrome | 50+ | ✅ 完整 |
| Firefox | 50+ | ✅ 完整 |
| Safari | 11+ | ✅ 完整 |
| Edge | 79+ | ✅ 完整 |

### HTTPS 要求

**生产环境必须使用 HTTPS**：
- WebSocket: `wss://`
- WebRTC: 需要安全上下文获取媒体设备

### STUN/TURN 服务器

**STUN 服务器**（公网 IP 发现）:
```
stun:stun.l.google.com:19302
stun:stun1.l.google.com:19302
```

**TURN 服务器**（NAT 穿透，可选但推荐）:
- 推荐使用 [coturn](https://github.com/coturn/coturn)
- 配置见下文

---

## 🏗️ 架构设计

### 信令流程

```
┌─────────────┐                          ┌─────────────┐
│   Caller    │                          │   Callee    │
│  (主叫方)    │                          │  (被叫方)    │
└──────┬──────┘                          └──────┬──────┘
       │                                        │
       │─── 1. CALL_INVITE (WebSocket) ────────▶│
       │                                        │
       │                                        │ 显示来电弹窗
       │                                        │
       │◀─── 2. CALL_ANSWER (WebSocket) ────────│
       │                                        │
       │─── 3. OFFER (SDP) (WebSocket) ────────▶│
       │                                        │
       │◀─── 4. ANSWER (SDP) (WebSocket) ───────│
       │                                        │
       │◀─── 5. ICE_CANDIDATE (WebSocket) ─────▶│
       │         (多次交换)                      │
       │                                        │
       │        [WebRTC P2P 连接建立]              │
       │        [媒体流直接传输]                  │
       │                                        │
       │─── 6. CALL_END (WebSocket) ───────────▶│
       │                                        │
```

### 组件关系

```
CallModal.vue (UI 组件)
    │
    ├─ callStore (状态管理)
    │   └─ WebSocket 监听
    │
    └─ webrtc.js (WebRTC 管理器)
        ├─ PeerConnection
        ├─ LocalStream
        └─ RemoteStream
```

---

## 🔧 实现步骤

### 1. 后端配置

#### application.yml

```yaml
webrtc:
  # STUN 服务器列表
  stun-servers: |
    stun:stun.l.google.com:19302,
    stun:stun1.l.google.com:19302
  
  # TURN 服务器（可选）
  turn-servers: |
    turn:your-turn-server.com:3478?transport=udp
  
  # TURN 认证（如果启用）
  turn-username: turn_user
  turn-password: turn_password
```

#### WebSocket 配置

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue", "/user");
        registry.setApplicationDestinationPrefixes("/app");
    }
}
```

### 2. 前端实现

#### WebRTC 管理器 (webrtc.js)

```javascript
class WebRTCManager {
  constructor() {
    this.peerConnection = null
    this.localStream = null
    this.remoteStream = null
  }

  // 创建 PeerConnection
  async createPeerConnection(iceServers) {
    const config = { iceServers }
    this.peerConnection = new RTCPeerConnection(config)
    
    // ICE 候选
    this.peerConnection.onicecandidate = (e) => {
      if (e.candidate) {
        this.sendIceCandidate(e.candidate)
      }
    }
    
    // 远程流
    this.peerConnection.ontrack = (e) => {
      this.remoteStream = e.streams[0]
      this.onRemoteStream?.(e.streams[0])
    }
  }

  // 发起呼叫
  async initCall(targetId, type) {
    await this.getLocalMedia(type)
    await this.createPeerConnection()
    
    this.localStream.getTracks().forEach(track => {
      this.peerConnection.addTrack(track, this.localStream)
    })
    
    const offer = await this.peerConnection.createOffer()
    await this.peerConnection.setLocalDescription(offer)
    
    this.sendOffer(offer, targetId)
  }

  // 接听呼叫
  async answerCall(callData, type) {
    await this.getLocalMedia(type)
    await this.createPeerConnection()
    
    this.localStream.getTracks().forEach(track => {
      this.peerConnection.addTrack(track, this.localStream)
    })
  }

  // 获取媒体流
  async getLocalMedia(type) {
    const stream = await navigator.mediaDevices.getUserMedia({
      audio: true,
      video: type === 'VIDEO'
    })
    this.localStream = stream
    return stream
  }
}
```

#### 通话组件 (CallModal.vue)

```vue
<template>
  <el-dialog v-model="visible">
    <!-- 视频通话 -->
    <div v-if="callType === 'VIDEO' && callStatus === 'connected'" class="video-container">
      <!-- 远程视频 -->
      <video ref="remoteVideoRef" autoplay playsinline />
      
      <!-- 本地视频 -->
      <div class="local-video-wrapper">
        <video ref="localVideoRef" autoplay playsinline muted />
      </div>
      
      <!-- 控制栏 -->
      <div class="video-controls">
        <button @click="toggleMute">🎤</button>
        <button @click="toggleVideo">📹</button>
        <button @click="endCall">❌</button>
      </div>
    </div>
    
    <!-- 语音通话 -->
    <div v-else class="audio-content">
      <el-avatar :src="callerAvatar" />
      <div>{{ callerName }}</div>
      <div>{{ formatDuration(duration) }}</div>
    </div>
  </el-dialog>
</template>

<script setup>
import webrtc from '@/utils/webrtc'

const initWebRTC = async (mode) => {
  await webrtc.loadWebRTCConfig()
  
  if (mode === 'initiate') {
    await webrtc.initCall(props.receiverId, props.callType)
    attachLocalStream()
  } else {
    await webrtc.answerCall({ callId: callId.value }, props.callType)
    attachLocalStream()
  }
  
  webrtc.onRemoteStream = attachRemoteStream
}

const attachLocalStream = () => {
  const stream = webrtc.getLocalStream()
  if (stream && localVideoRef.value) {
    localVideoRef.value.srcObject = stream
  }
}

const attachRemoteStream = (stream) => {
  if (remoteVideoRef.value) {
    remoteVideoRef.value.srcObject = stream
  }
}
</script>
```

---

## 🎥 视频渲染

### HTML5 Video 元素

```html
<!-- 远程视频（全屏） -->
<video
  ref="remoteVideoRef"
  class="remote-video"
  autoplay
  playsinline
/>

<!-- 本地视频（画中画） -->
<video
  ref="localVideoRef"
  class="local-video"
  autoplay
  playsinline
  muted
/>
```

### CSS 样式

```css
.video-container {
  position: relative;
  width: 100%;
  height: 600px;
  background: #000;
}

.remote-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.local-video-wrapper {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 160px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
}

.local-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transform: scaleX(-1); /* 镜像 */
}
```

---

## 🎛️ 通话控制

### 静音/取消静音

```javascript
function toggleMute() {
  const stream = webrtc.getLocalStream()
  const audioTrack = stream.getAudioTracks()[0]
  
  if (audioTrack) {
    audioTrack.enabled = !audioTrack.enabled
    return audioTrack.enabled
  }
  return false
}
```

### 开关摄像头

```javascript
function toggleVideo() {
  const stream = webrtc.getLocalStream()
  const videoTrack = stream.getVideoTracks()[0]
  
  if (videoTrack) {
    videoTrack.enabled = !videoTrack.enabled
    return videoTrack.enabled
  }
  return false
}
```

---

## 🌐 部署 TURN 服务器

### 安装 coturn

```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install coturn

# 启用服务
sudo systemctl enable coturn
sudo systemctl start coturn
```

### 配置 /etc/turnserver.conf

```conf
# 监听端口
listening-port=3478
tls-listening-port=5349

# 监听 IP
listening-ip=0.0.0.0

# 外部 IP（公网 IP）
external-ip=your.public.ip.address

# Realm
realm=your-domain.com
server-name=turn.your-domain.com

# 认证
lt-cred-mech
user=turn_user:turn_password

# SSL 证书（如果使用 TLS）
cert=/etc/ssl/certs/turnserver.crt
pkey=/etc/ssl/private/turnserver.key

# 日志
verbose
```

### 防火墙配置

```bash
# 开放端口
sudo ufw allow 3478/tcp
sudo ufw allow 3478/udp
sudo ufw allow 5349/tcp
sudo ufw allow 5349/udp

# TURN 中继端口范围
sudo ufw allow 49152:65535/tcp
sudo ufw allow 49152:65535/udp
```

---

## 🐛 常见问题

### 1. 无法获取媒体流

**错误**: `NotAllowedError: Permission denied`

**解决**:
- 确保使用 HTTPS
- 检查浏览器权限设置
- 用户必须手动授予权限

### 2. WebRTC 连接失败

**错误**: `ICE connection state: failed`

**解决**:
- 检查 STUN 服务器可达性
- 部署 TURN 服务器（对称 NAT 环境）
- 检查防火墙规则

### 3. 视频黑屏

**可能原因**:
- 摄像头被其他应用占用
- 视频轨道未添加
- CSS 样式问题

**解决**:
```javascript
// 确保添加视频轨道
localStream.getVideoTracks().forEach(track => {
  peerConnection.addTrack(track, localStream)
})
```

### 4. 回声/噪音

**解决**:
- 启用回声消除（AEC）
- 使用耳机而非扬声器
- 调整麦克风增益

---

## 📊 性能优化

### 1. 视频质量调整

```javascript
const constraints = {
  video: {
    width: { ideal: 1280, max: 1920 },
    height: { ideal: 720, max: 1080 },
    frameRate: { ideal: 30, max: 60 }
  }
}
```

### 2. 带宽管理

```javascript
const sender = peerConnection.getSenders()[0]
sender.setParameters({
  encodings: [{
    maxBitrate: 1000000 // 1 Mbps
  }]
})
```

### 3. 网络质量监测

```javascript
const stats = await peerConnection.getStats()
stats.forEach(report => {
  if (report.type === 'inbound-rtp') {
    console.log('接收码率:', report.bytesReceived)
  }
})
```

---

## 🔒 安全考虑

### 1. 媒体加密

- WebRTC 默认使用 DTLS-SRTP 加密
- 所有媒体流都是端到端加密的

### 2. 信令认证

- WebSocket 连接需要 JWT Token
- 验证通话双方身份

### 3. 权限控制

```javascript
// 检查用户是否有权通话
if (!hasPermission(callerId, receiverId)) {
  rejectCall()
}
```

---

## 📝 测试清单

- [ ] 语音通话（一对一）
- [ ] 视频通话（一对一）
- [ ] 静音/取消静音
- [ ] 开关摄像头
- [ ] 网络切换（WiFi → 4G）
- [ ] 通话超时处理
- [ ] 拒接/取消通话
- [ ] 通话历史记录
- [ ] 不同浏览器兼容性
- [ ] 移动端适配

---

## 🚀 下一步

### V2.0 功能

1. **多人会议**
   - 使用 SFU 架构（mediasoup）
   - 支持 3-9 人同时通话

2. **屏幕共享**
   ```javascript
   const screenStream = await navigator.mediaDevices.getDisplayMedia({
     video: true
   })
   ```

3. **通话录制**
   - 使用 MediaRecorder API
   - 云端录制存储

4. **美颜滤镜**
   - WebGL 实时处理
   - AI 美颜算法

---

**开发团队**: IM Team  
**文档版本**: 1.0  
**最后更新**: 2026-03-14
