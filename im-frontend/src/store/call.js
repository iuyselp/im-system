import { defineStore } from 'pinia'
import { ref } from 'vue'
import callApi from '@/api/call'
import { ElNotification } from 'element-plus'

export const useCallStore = defineStore('call', () => {
  // 通话状态
  const isCalling = ref(false)
  const isInCall = ref(false)
  const callType = ref('AUDIO') // AUDIO | VIDEO
  const callStatus = ref('idle') // idle | calling | incoming | connected
  const currentCall = ref(null)
  const callerInfo = ref(null)
  
  // 媒体状态
  const isMuted = ref(false)
  const isVideoOff = ref(false)
  const duration = ref(0)
  
  // WebRTC 相关
  const localStream = ref(null)
  const remoteStream = ref(null)
  const peerConnection = ref(null)
  
  let durationTimer = null
  let callTimeout = null

  // 发起通话
  async function startCall(targetId, type = 'AUDIO', groupId = null) {
    try {
      const userStore = useUserStore()
      const callId = 'call_' + Date.now()
      
      currentCall.value = {
        callId,
        targetId,
        groupId,
        type
      }
      
      callType.value = type
      callStatus.value = 'calling'
      isCalling.value = true
      
      // 调用后端 API
      await callApi.startCall({
        callerId: userStore.userInfo?.id,
        receiverId: groupId ? null : targetId,
        groupId: groupId || null,
        type,
        callId
      })
      
      // 设置超时（60 秒无人接听）
      callTimeout = setTimeout(() => {
        if (callStatus.value === 'calling') {
          endCall()
          ElNotification.warning({
            title: '通话超时',
            message: '对方未接听'
          })
        }
      }, 60000)
      
      return { success: true, callId }
    } catch (error) {
      console.error('发起通话失败:', error)
      ElNotification.error({
        title: '发起通话失败',
        message: error.message || '请稍后重试'
      })
      return { success: false, error }
    }
  }

  // 接听通话
  async function answerCall() {
    try {
      if (!currentCall.value?.callId) return
      
      await callApi.answerCall(currentCall.value.callId)
      
      callStatus.value = 'connected'
      isInCall.value = true
      isCalling.value = false
      
      // 清除超时
      if (callTimeout) {
        clearTimeout(callTimeout)
        callTimeout = null
      }
      
      // 开始计时
      startDurationTimer()
      
      // TODO: 初始化 WebRTC 媒体流
      await initMediaStream()
      
      return { success: true }
    } catch (error) {
      console.error('接听通话失败:', error)
      return { success: false, error }
    }
  }

  // 拒接通话
  async function rejectCall() {
    try {
      if (!currentCall.value?.callId) return
      
      await callApi.rejectCall(currentCall.value.callId)
      
      resetCallState()
      
      return { success: true }
    } catch (error) {
      console.error('拒接通话失败:', error)
      return { success: false, error }
    }
  }

  // 结束通话
  async function endCall() {
    try {
      if (!currentCall.value?.callId) {
        resetCallState()
        return
      }
      
      await callApi.endCall(currentCall.value.callId, duration.value)
      
      // 释放媒体流
      releaseMediaStream()
      
      resetCallState()
      
      return { success: true }
    } catch (error) {
      console.error('结束通话失败:', error)
      return { success: false, error }
    }
  }

  // 切换静音
  function toggleMute() {
    isMuted.value = !isMuted.value
    if (localStream.value) {
      const audioTrack = localStream.value.getAudioTracks()[0]
      if (audioTrack) {
        audioTrack.enabled = !isMuted.value
      }
    }
  }

  // 切换视频
  function toggleVideo() {
    isVideoOff.value = !isVideoOff.value
    if (localStream.value) {
      const videoTrack = localStream.value.getVideoTracks()[0]
      if (videoTrack) {
        videoTrack.enabled = !isVideoOff.value
      }
    }
  }

  // 开始计时
  function startDurationTimer() {
    duration.value = 0
    durationTimer = setInterval(() => {
      duration.value++
    }, 1000)
  }

  // 停止计时
  function stopDurationTimer() {
    if (durationTimer) {
      clearInterval(durationTimer)
      durationTimer = null
    }
  }

  // 重置通话状态
  function resetCallState() {
    stopDurationTimer()
    
    if (callTimeout) {
      clearTimeout(callTimeout)
      callTimeout = null
    }
    
    isCalling.value = false
    isInCall.value = false
    callStatus.value = 'idle'
    callType.value = 'AUDIO'
    currentCall.value = null
    callerInfo.value = null
    isMuted.value = false
    isVideoOff.value = false
    duration.value = 0
  }

  // 初始化媒体流
  async function initMediaStream() {
    try {
      const constraints = {
        audio: true,
        video: callType.value === 'VIDEO'
      }
      
      const stream = await navigator.mediaDevices.getUserMedia(constraints)
      localStream.value = stream
      
      return stream
    } catch (error) {
      console.error('获取媒体流失败:', error)
      ElNotification.error({
        title: '媒体流错误',
        message: '无法访问摄像头或麦克风'
      })
      throw error
    }
  }

  // 释放媒体流
  function releaseMediaStream() {
    if (localStream.value) {
      localStream.value.getTracks().forEach(track => track.stop())
      localStream.value = null
    }
    
    if (peerConnection.value) {
      peerConnection.value.close()
      peerConnection.value = null
    }
    
    remoteStream.value = null
  }

  // 处理来电
  function handleIncomingCall(callData) {
    currentCall.value = callData
    callStatus.value = 'incoming'
    callType.value = callData.type || 'AUDIO'
    
    ElNotification.info({
      title: '来电提醒',
      message: `${callData.callerName} 请求${callData.type === 'VIDEO' ? '视频' : '语音'}通话`,
      duration: 0, // 不自动关闭
      onClick: () => {
        // 点击通知时可以在前端显示通话弹窗
      }
    })
  }

  return {
    // 状态
    isCalling,
    isInCall,
    callType,
    callStatus,
    currentCall,
    callerInfo,
    isMuted,
    isVideoOff,
    duration,
    localStream,
    remoteStream,
    peerConnection,
    
    // 方法
    startCall,
    answerCall,
    rejectCall,
    endCall,
    toggleMute,
    toggleVideo,
    resetCallState,
    handleIncomingCall,
    initMediaStream,
    releaseMediaStream
  }
})

// 需要导入 useUserStore
import { useUserStore } from './user'
