import callApi from '@/api/call'
import wsManager, { WsMessageType } from './websocket'

/**
 * WebRTC 配置
 */
const WEBRTC_CONFIG = {
  iceServers: []
}

/**
 * 加载 WebRTC 配置
 */
export async function loadWebRTCConfig() {
  try {
    const res = await callApi.getWebRTCConfig()
    if (res.code === 0) {
      WEBRTC_CONFIG.iceServers = res.data.iceServers || []
      console.log('WebRTC 配置已加载:', WEBRTC_CONFIG)
      return WEBRTC_CONFIG
    }
  } catch (error) {
    console.error('加载 WebRTC 配置失败:', error)
  }
  return WEBRTC_CONFIG
}

/**
 * WebRTC 管理器
 */
class WebRTCManager {
  constructor() {
    this.peerConnection = null
    this.localStream = null
    this.remoteStream = null
    this.callId = null
    this.isInitiator = false
    this.onStateChange = null
    this.onRemoteStream = null
  }

  /**
   * 创建 PeerConnection
   */
  async createPeerConnection(iceServers) {
    const config = {
      iceServers: iceServers || WEBRTC_CONFIG.iceServers
    }

    this.peerConnection = new RTCPeerConnection(config)

    // 处理 ICE 候选
    this.peerConnection.onicecandidate = (event) => {
      if (event.candidate) {
        console.log('发送 ICE 候选:', event.candidate)
        this.sendIceCandidate(event.candidate)
      }
    }

    // 处理远程流
    this.peerConnection.ontrack = (event) => {
      console.log('收到远程流:', event.streams[0])
      this.remoteStream = event.streams[0]
      if (this.onRemoteStream) {
        this.onRemoteStream(event.streams[0])
      }
    }

    // 处理连接状态
    this.peerConnection.onconnectionstatechange = () => {
      console.log('连接状态:', this.peerConnection.connectionState)
      if (this.onStateChange) {
        this.onStateChange(this.peerConnection.connectionState)
      }
    }

    // 处理 ICE 连接状态
    this.peerConnection.oniceconnectionstatechange = () => {
      console.log('ICE 状态:', this.peerConnection.iceConnectionState)
    }

    return this.peerConnection
  }

  /**
   * 初始化呼叫（作为呼叫方）
   */
  async initCall(targetId, type = 'AUDIO') {
    try {
      this.isInitiator = true
      this.callId = 'call_' + Date.now()

      // 获取本地媒体流
      await this.getLocalMedia(type)

      // 创建 PeerConnection
      await this.createPeerConnection()

      // 添加本地流
      this.localStream.getTracks().forEach(track => {
        this.peerConnection.addTrack(track, this.localStream)
      })

      // 创建 Offer
      const offer = await this.peerConnection.createOffer()
      await this.peerConnection.setLocalDescription(offer)

      console.log('创建 Offer:', offer)

      // 发送 Offer 给对方
      this.sendOffer(offer, targetId)

      return { callId: this.callId, offer }
    } catch (error) {
      console.error('初始化呼叫失败:', error)
      throw error
    }
  }

  /**
   * 接听呼叫（作为接收方）
   */
  async answerCall(callData, type = 'AUDIO') {
    try {
      this.isInitiator = false
      this.callId = callData.callId

      // 获取本地媒体流
      await this.getLocalMedia(type)

      // 创建 PeerConnection
      await this.createPeerConnection()

      // 添加本地流
      this.localStream.getTracks().forEach(track => {
        this.peerConnection.addTrack(track, this.localStream)
      })

      return { callId: this.callId }
    } catch (error) {
      console.error('接听呼叫失败:', error)
      throw error
    }
  }

  /**
   * 处理收到的 Offer
   */
  async handleOffer(offer) {
    try {
      console.log('收到 Offer:', offer)

      if (!this.peerConnection) {
        await this.createPeerConnection()
      }

      await this.peerConnection.setRemoteDescription(new RTCSessionDescription(offer))

      // 创建 Answer
      const answer = await this.peerConnection.createAnswer()
      await this.peerConnection.setLocalDescription(answer)

      console.log('创建 Answer:', answer)

      return answer
    } catch (error) {
      console.error('处理 Offer 失败:', error)
      throw error
    }
  }

  /**
   * 处理收到的 Answer
   */
  async handleAnswer(answer) {
    try {
      console.log('收到 Answer:', answer)
      await this.peerConnection.setRemoteDescription(new RTCSessionDescription(answer))
    } catch (error) {
      console.error('处理 Answer 失败:', error)
      throw error
    }
  }

  /**
   * 处理收到的 ICE 候选
   */
  async handleIceCandidate(candidate) {
    try {
      console.log('收到 ICE 候选:', candidate)
      if (this.peerConnection) {
        await this.peerConnection.addIceCandidate(new RTCIceCandidate(candidate))
      }
    } catch (error) {
      console.error('处理 ICE 候选失败:', error)
    }
  }

  /**
   * 获取本地媒体流
   */
  async getLocalMedia(type = 'AUDIO') {
    const constraints = {
      audio: true,
      video: type === 'VIDEO'
    }

    try {
      const stream = await navigator.mediaDevices.getUserMedia(constraints)
      this.localStream = stream
      return stream
    } catch (error) {
      console.error('获取媒体流失败:', error)
      throw new Error('无法访问摄像头或麦克风')
    }
  }

  /**
   * 发送 Offer
   */
  sendOffer(offer, targetId) {
    wsManager.send(WsMessageType.OFFER, {
      callId: this.callId,
      offer: {
        type: offer.type,
        sdp: offer.sdp
      },
      targetId
    })
  }

  /**
   * 发送 Answer
   */
  sendAnswer(answer) {
    wsManager.send(WsMessageType.ANSWER, {
      callId: this.callId,
      answer: {
        type: answer.type,
        sdp: answer.sdp
      }
    })
  }

  /**
   * 发送 ICE 候选
   */
  sendIceCandidate(candidate) {
    wsManager.send(WsMessageType.ICE_CANDIDATE, {
      callId: this.callId,
      candidate
    })
  }

  /**
   * 切换静音
   */
  toggleMute() {
    if (this.localStream) {
      const audioTrack = this.localStream.getAudioTracks()[0]
      if (audioTrack) {
        audioTrack.enabled = !audioTrack.enabled
        return audioTrack.enabled
      }
    }
    return false
  }

  /**
   * 切换视频
   */
  toggleVideo() {
    if (this.localStream) {
      const videoTrack = this.localStream.getVideoTracks()[0]
      if (videoTrack) {
        videoTrack.enabled = !videoTrack.enabled
        return videoTrack.enabled
      }
    }
    return false
  }

  /**
   * 结束通话
   */
  endCall() {
    // 停止本地流
    if (this.localStream) {
      this.localStream.getTracks().forEach(track => track.stop())
      this.localStream = null
    }

    // 关闭 PeerConnection
    if (this.peerConnection) {
      this.peerConnection.close()
      this.peerConnection = null
    }

    this.remoteStream = null
    this.callId = null
  }

  /**
   * 获取本地流
   */
  getLocalStream() {
    return this.localStream
  }

  /**
   * 获取远程流
   */
  getRemoteStream() {
    return this.remoteStream
  }
}

// 创建单例
const webrtcManager = new WebRTCManager()

export default webrtcManager
