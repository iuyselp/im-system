<template>
  <el-dialog
    v-model="visible"
    :title="callType === 'VIDEO' ? '视频通话' : '语音通话'"
    :width="callType === 'VIDEO' && callStatus === 'connected' ? '800px' : '400px'"
    :close-on-click-modal="false"
    :show-close="false"
    class="call-modal"
    fullscreen
  >
    <div class="call-content">
      <!-- 视频通话 - 通话中 -->
      <div v-if="callType === 'VIDEO' && callStatus === 'connected'" class="video-container">
        <!-- 远程视频 -->
        <video
          ref="remoteVideoRef"
          class="remote-video"
          autoplay
          playsinline
        />
        
        <!-- 本地视频（小窗口） -->
        <div class="local-video-wrapper">
          <video
            ref="localVideoRef"
            class="local-video"
            autoplay
            playsinline
            muted
          />
          <div class="local-video-info">
            <span>{{ userStore.userInfo?.nickname || '我' }}</span>
          </div>
        </div>
        
        <!-- 通话控制栏 -->
        <div class="video-controls">
          <div class="control-info">
            <div class="caller-name">{{ callerName }}</div>
            <div class="call-duration">{{ formatDuration(duration) }}</div>
          </div>
          <div class="control-buttons">
            <el-button
              :type="muted ? 'warning' : 'default'"
              circle
              size="large"
              @click="toggleMute"
            >
              <el-icon><Microphone /></el-icon>
            </el-button>
            <el-button
              :type="videoOff ? 'warning' : 'default'"
              circle
              size="large"
              @click="toggleVideo"
            >
              <el-icon><VideoCamera /></el-icon>
            </el-button>
            <el-button type="danger" circle size="large" @click="endCall">
              <el-icon><PhoneClosed /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <!-- 语音通话 / 呼叫中 / 来电 -->
      <div v-else class="audio-content">
        <!-- 呼叫中 -->
        <div v-if="callStatus === 'calling'" class="call-status">
          <div class="avatar">
            <el-avatar :size="100" :src="callerAvatar" />
          </div>
          <div class="caller-name">{{ callerName }}</div>
          <div class="call-action">正在呼叫...</div>
          <div class="call-status-indicator">
            <el-icon class="is-pulse"><Loading /></el-icon>
          </div>
          <div class="call-buttons">
            <el-button type="danger" size="large" circle @click="endCall">
              <el-icon><PhoneClosed /></el-icon>
            </el-button>
          </div>
        </div>

        <!-- 来电提醒 -->
        <div v-else-if="callStatus === 'incoming'" class="call-status incoming">
          <div class="avatar">
            <el-avatar :size="100" :src="callerAvatar" />
          </div>
          <div class="caller-name">{{ callerName }}</div>
          <div class="call-action">
            {{ callType === 'VIDEO' ? '请求与你视频通话' : '请求与你语音通话' }}
          </div>
          <div class="call-buttons">
            <el-button type="success" size="large" circle @click="answerCall">
              <el-icon><Phone /></el-icon>
            </el-button>
            <el-button type="danger" size="large" circle @click="rejectCall">
              <el-icon><PhoneClosed /></el-icon>
            </el-button>
          </div>
        </div>

        <!-- 通话中（语音） -->
        <div v-else-if="callStatus === 'connected'" class="call-status">
          <div class="avatar">
            <el-avatar :size="100" :src="callerAvatar" />
          </div>
          <div class="caller-name">{{ callerName }}</div>
          <div class="call-duration">{{ formatDuration(duration) }}</div>
          <div class="call-status-text">通话中...</div>
          <div class="call-controls">
            <el-button
              :type="muted ? 'warning' : 'default'"
              circle
              size="large"
              @click="toggleMute"
            >
              <el-icon><Microphone /></el-icon>
            </el-button>
            <el-button
              v-if="callType === 'VIDEO'"
              :type="videoOff ? 'warning' : 'default'"
              circle
              size="large"
              @click="toggleVideo"
            >
              <el-icon><VideoCamera /></el-icon>
            </el-button>
            <el-button type="danger" circle size="large" @click="endCall">
              <el-icon><PhoneClosed /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { Phone, PhoneClosed, Microphone, VideoCamera, Loading } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import callApi from '@/api/call'
import webrtc from '@/utils/webrtc'

const props = defineProps({
  modelValue: Boolean,
  callType: {
    type: String,
    default: 'AUDIO'
  },
  callerId: Number,
  callerName: String,
  callerAvatar: String,
  receiverId: Number,
  groupId: Number
})

const emit = defineEmits(['update:modelValue', 'end', 'answer', 'reject'])

const userStore = useUserStore()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const callStatus = ref('calling')
const callId = ref('')
const duration = ref(0)
const muted = ref(false)
const videoOff = ref(false)

const localVideoRef = ref(null)
const remoteVideoRef = ref(null)
let durationTimer = null

// 发起通话
const startCall = async () => {
  callId.value = 'call_' + Date.now()
  try {
    // 如果是视频通话，先获取媒体流
    if (props.callType === 'VIDEO') {
      await initWebRTC('initiate')
    }
    
    emit('answer', callId.value)
    callStatus.value = 'calling'
  } catch (error) {
    console.error('发起通话失败:', error)
  }
}

// 接听通话
const answerCall = async () => {
  try {
    emit('answer', callId.value)
    
    // 初始化 WebRTC
    if (props.callType === 'VIDEO') {
      await initWebRTC('answer')
    }
    
    callStatus.value = 'connected'
    startDurationTimer()
  } catch (error) {
    console.error('接听通话失败:', error)
  }
}

// 拒接通话
const rejectCall = async () => {
  try {
    emit('reject', callId.value)
    callStatus.value = 'ended'
    visible.value = false
    cleanupWebRTC()
  } catch (error) {
    console.error('拒接通话失败:', error)
  }
}

// 结束通话
const endCall = async () => {
  try {
    emit('end', { callId: callId.value, duration: duration.value })
    callStatus.value = 'ended'
    visible.value = false
    stopDurationTimer()
    cleanupWebRTC()
  } catch (error) {
    console.error('结束通话失败:', error)
  }
}

// 初始化 WebRTC
const initWebRTC = async (mode) => {
  try {
    // 加载配置
    await webrtc.loadWebRTCConfig()
    
    if (mode === 'initiate') {
      // 作为呼叫方
      await webrtc.initCall(props.receiverId, props.callType)
      
      // 显示本地视频
      await nextTick()
      attachLocalStream()
      
      // 监听远程流
      webrtc.onRemoteStream = (stream) => {
        attachRemoteStream(stream)
      }
      
      // 监听状态变化
      webrtc.onStateChange = (state) => {
        console.log('WebRTC 状态:', state)
        if (state === 'connected') {
          callStatus.value = 'connected'
          startDurationTimer()
        }
      }
    } else if (mode === 'answer') {
      // 作为接收方
      await webrtc.answerCall({ callId: callId.value }, props.callType)
      
      // 显示本地视频
      await nextTick()
      attachLocalStream()
      
      // 监听远程流
      webrtc.onRemoteStream = (stream) => {
        attachRemoteStream(stream)
      }
      
      // 监听状态变化
      webrtc.onStateChange = (state) => {
        console.log('WebRTC 状态:', state)
        if (state === 'connected') {
          callStatus.value = 'connected'
          startDurationTimer()
        }
      }
    }
  } catch (error) {
    console.error('WebRTC 初始化失败:', error)
  }
}

// 绑定本地流到视频元素
const attachLocalStream = () => {
  const stream = webrtc.getLocalStream()
  if (stream && localVideoRef.value) {
    localVideoRef.value.srcObject = stream
  }
}

// 绑定远程流到视频元素
const attachRemoteStream = (stream) => {
  if (remoteVideoRef.value) {
    remoteVideoRef.value.srcObject = stream
  }
}

// 清理 WebRTC
const cleanupWebRTC = () => {
  webrtc.endCall()
  if (localVideoRef.value) {
    localVideoRef.value.srcObject = null
  }
  if (remoteVideoRef.value) {
    remoteVideoRef.value.srcObject = null
  }
}

// 切换静音
const toggleMute = () => {
  muted.value = webrtc.toggleMute()
}

// 切换视频
const toggleVideo = () => {
  videoOff.value = !webrtc.toggleVideo()
}

// 开始计时
const startDurationTimer = () => {
  durationTimer = setInterval(() => {
    duration.value++
  }, 1000)
}

// 停止计时
const stopDurationTimer = () => {
  if (durationTimer) {
    clearInterval(durationTimer)
    durationTimer = null
  }
}

// 格式化时长
const formatDuration = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 监听打开
watch(() => props.modelValue, (val) => {
  if (val && callStatus.value === 'calling') {
    startCall()
  }
}, { immediate: true })

// 组件卸载时清理
onUnmounted(() => {
  cleanupWebRTC()
  stopDurationTimer()
})
</script>

<style scoped>
.call-modal :deep(.el-dialog__body) {
  padding: 0;
  overflow: hidden;
}

.call-content {
  height: 100%;
  min-height: 400px;
}

/* 视频通话样式 */
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
  background: #1a1a1a;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.5);
}

.local-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transform: scaleX(-1);
}

.local-video-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 4px 8px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  font-size: 12px;
  text-align: center;
}

.video-controls {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.control-info {
  color: white;
}

.control-info .caller-name {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 4px;
}

.control-info .call-duration {
  font-size: 14px;
  color: #67c23a;
}

.control-buttons {
  display: flex;
  gap: 16px;
}

.control-buttons :deep(.el-button) {
  width: 56px;
  height: 56px;
  border: none;
}

/* 语音通话样式 */
.audio-content {
  padding: 60px 40px;
  text-align: center;
}

.call-status {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.call-status.incoming {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

.avatar {
  margin-bottom: 20px;
}

.caller-name {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 12px;
  color: #303133;
}

.call-action {
  color: #909399;
  margin-bottom: 24px;
  font-size: 16px;
}

.call-status-indicator {
  margin-bottom: 20px;
}

.call-status-indicator :deep(.el-icon) {
  font-size: 32px;
  color: #409eff;
}

.call-duration {
  font-size: 32px;
  font-weight: bold;
  color: #67c23a;
  margin-bottom: 16px;
}

.call-status-text {
  color: #909399;
  margin-bottom: 32px;
  font-size: 14px;
}

.call-buttons {
  display: flex;
  gap: 30px;
  margin-top: 20px;
}

.call-controls {
  display: flex;
  gap: 20px;
  margin-top: 20px;
}

.call-buttons :deep(.el-button),
.call-controls :deep(.el-button) {
  width: 64px;
  height: 64px;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.call-buttons :deep(.el-button.is-success) {
  background: #67c23a;
  border-color: #67c23a;
}

.call-buttons :deep(.el-button.is-danger) {
  background: #f56c6c;
  border-color: #f56c6c;
}
</style>
