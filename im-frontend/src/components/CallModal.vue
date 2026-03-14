<template>
  <el-dialog
    v-model="visible"
    :title="callType === 'VIDEO' ? '视频通话' : '语音通话'"
    width="400px"
    :close-on-click-modal="false"
    :show-close="false"
    class="call-modal"
  >
    <div class="call-content">
      <!-- 呼叫中 -->
      <div v-if="callStatus === 'calling'" class="call-status">
        <div class="avatar">
          <el-avatar :size="80" :src="callerAvatar" />
        </div>
        <div class="caller-name">{{ callerName }}</div>
        <div class="call-action">正在呼叫...</div>
        <div class="call-buttons">
          <el-button type="danger" size="large" circle @click="endCall">
            <el-icon><Phone /></el-icon>
          </el-button>
        </div>
      </div>

      <!-- 来电提醒 -->
      <div v-else-if="callStatus === 'incoming'" class="call-status">
        <div class="avatar">
          <el-avatar :size="80" :src="callerAvatar" />
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

      <!-- 通话中 -->
      <div v-else-if="callStatus === 'connected'" class="call-status">
        <div class="avatar">
          <el-avatar :size="80" :src="callerAvatar" />
        </div>
        <div class="caller-name">{{ callerName }}</div>
        <div class="call-duration">{{ formatDuration(duration) }}</div>
        <div class="call-controls">
          <el-button :type="muted ? 'warning' : 'default'" circle @click="toggleMute">
            <el-icon v-if="muted"><Microphone /></el-icon>
            <el-icon v-else><Microphone /></el-icon>
          </el-button>
          <el-button v-if="callType === 'VIDEO'" :type="videoOff ? 'warning' : 'default'" circle @click="toggleVideo">
            <el-icon v-if="videoOff"><VideoCamera /></el-icon>
            <el-icon v-else><VideoCamera /></el-icon>
          </el-button>
          <el-button type="danger" circle @click="endCall">
            <el-icon><PhoneClosed /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Phone, PhoneClosed, Microphone, VideoCamera } from '@element-plus/icons-vue'
import callApi from '@/api/call'

const props = defineProps({
  modelValue: Boolean,
  callType: {
    type: String,
    default: 'AUDIO' // AUDIO | VIDEO
  },
  callerId: Number,
  callerName: String,
  callerAvatar: String,
  receiverId: Number,
  groupId: Number
})

const emit = defineEmits(['update:modelValue', 'end', 'answer', 'reject'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const callStatus = ref('calling') // calling | incoming | connected | ended
const callId = ref('')
const duration = ref(0)
const muted = ref(false)
const videoOff = ref(false)
let durationTimer = null

// 发起通话
const startCall = async () => {
  callId.value = 'call_' + Date.now()
  try {
    await callApi.startCall({
      callerId: props.callerId,
      receiverId: props.receiverId,
      groupId: props.groupId,
      type: props.callType,
      callId: callId.value
    })
    callStatus.value = 'calling'
    // TODO: 初始化 WebRTC
  } catch (error) {
    console.error('发起通话失败:', error)
  }
}

// 接听通话
const answerCall = async () => {
  try {
    await callApi.answerCall(callId.value)
    callStatus.value = 'connected'
    startDurationTimer()
    emit('answer', callId.value)
    // TODO: 初始化 WebRTC
  } catch (error) {
    console.error('接听通话失败:', error)
  }
}

// 拒接通话
const rejectCall = async () => {
  try {
    await callApi.rejectCall(callId.value)
    callStatus.value = 'ended'
    visible.value = false
    emit('reject', callId.value)
  } catch (error) {
    console.error('拒接通话失败:', error)
  }
}

// 结束通话
const endCall = async () => {
  try {
    await callApi.endCall(callId.value, duration.value)
    callStatus.value = 'ended'
    visible.value = false
    stopDurationTimer()
    emit('end', { callId: callId.value, duration: duration.value })
  } catch (error) {
    console.error('结束通话失败:', error)
  }
}

// 切换静音
const toggleMute = () => {
  muted.value = !muted.value
  // TODO: 实际静音操作
}

// 切换视频
const toggleVideo = () => {
  videoOff.value = !videoOff.value
  // TODO: 实际开关摄像头
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
</script>

<style scoped>
.call-modal :deep(.el-dialog__body) {
  padding: 40px 20px;
}

.call-content {
  text-align: center;
}

.call-status {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar {
  margin-bottom: 16px;
}

.caller-name {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 8px;
}

.call-action {
  color: #909399;
  margin-bottom: 24px;
}

.call-duration {
  font-size: 24px;
  font-weight: bold;
  color: #67c23a;
  margin-bottom: 24px;
}

.call-buttons {
  display: flex;
  gap: 20px;
}

.call-controls {
  display: flex;
  gap: 16px;
}

.call-buttons :deep(.el-button),
.call-controls :deep(.el-button) {
  width: 60px;
  height: 60px;
}
</style>
