<template>
  <div class="chat-container">
    <!-- 左侧边栏 -->
    <div class="sidebar">
      <div class="user-info">
        <el-avatar :size="40" :src="userStore.userInfo?.avatar" />
        <span class="nickname">{{ userStore.userInfo?.nickname }}</span>
        <div class="user-actions">
          <el-button link @click="$router.push('/calls')" title="通话记录">
            <el-icon><Phone /></el-icon>
          </el-button>
          <el-button link @click="logout" class="logout-btn" title="退出">
            <el-icon><SwitchButton /></el-icon>
          </el-button>
        </div>
      </div>
      
      <!-- 搜索框 -->
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索聊天..."
          clearable
          prefix-icon="Search"
          @keyup.enter="openSearchDialog"
        >
          <template #append>
            <el-button @click="openSearchDialog">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>
      
      <div class="conversation-list">
        <div
          v-for="item in conversations"
          :key="item.id"
          class="conversation-item"
          :class="{ active: currentTargetId === item.targetId }"
          @click="selectConversation(item)"
        >
          <el-avatar :size="40" :src="item.avatar" />
          <div class="conversation-info">
            <div class="conversation-name">
              {{ item.name }}
              <el-tag v-if="item.isGroup" size="small" type="info">群</el-tag>
            </div>
            <div class="last-message">{{ item.lastMessage }}</div>
          </div>
          <div v-if="item.unreadCount > 0" class="unread-badge">
            {{ item.unreadCount }}
          </div>
        </div>
      </div>
    </div>
    
    <!-- 聊天区域 -->
    <div class="chat-area">
      <div v-if="currentTargetId" class="chat-header">
        <div class="header-left">
          <el-avatar :size="36" :src="currentAvatar" />
          <span class="header-title">{{ currentTitle }}</span>
        </div>
        <div class="header-actions">
          <el-button link @click="openSearchDialog" title="搜索聊天记录">
            <el-icon><Search /></el-icon>
          </el-button>
          <el-dropdown @command="handleCallCommand">
            <el-button link>
              <el-icon><Phone /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="audio">
                  <el-icon><Phone /></el-icon> 语音通话
                </el-dropdown-item>
                <el-dropdown-item command="video">
                  <el-icon><VideoCamera /></el-icon> 视频通话
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button link @click="showInfoPanel = !showInfoPanel" title="聊天信息">
            <el-icon><InfoFilled /></el-icon>
          </el-button>
        </div>
      </div>
      
      <!-- 消息列表 -->
      <div class="message-list" ref="messageListRef">
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="message-item"
          :class="{ 'message-self': msg.fromId === userStore.userInfo?.id }"
        >
          <el-avatar :size="40" :src="msg.avatar" />
          <div class="message-content">
            <div class="message-info">
              <span class="message-name">{{ msg.senderName }}</span>
              <span class="message-time">{{ msg.time }}</span>
            </div>
            <div class="message-bubble" :class="msg.type">
              <!-- 文本消息 -->
              <template v-if="msg.type === 'TEXT'">
                {{ msg.content }}
              </template>
              <!-- 图片消息 -->
              <template v-else-if="msg.type === 'IMAGE'">
                <el-image
                  :src="msg.content"
                  fit="cover"
                  style="width: 200px; height: 150px"
                  :preview-src-list="[msg.content]"
                />
              </template>
              <!-- 其他消息类型 -->
              <template v-else>
                {{ msg.content }}
              </template>
            </div>
            <!-- 已读状态 -->
            <div v-if="msg.fromId === userStore.userInfo?.id" class="read-status">
              <el-icon v-if="msg.isRead" color="#67c23a"><CircleCheck /></el-icon>
              <el-icon v-else color="#909399"><CircleClose /></el-icon>
            </div>
          </div>
        </div>
        
        <!-- 加载中 -->
        <div v-if="loadingMessages" class="loading-more">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
      </div>
      
      <!-- 消息输入 -->
      <div class="message-input">
        <div class="input-toolbar">
          <el-button link @click="selectImage" title="发送图片">
            <el-icon><Picture /></el-icon>
          </el-button>
          <el-button link @click="selectFile" title="发送文件">
            <el-icon><Document /></el-icon>
          </el-button>
          <input
            ref="fileInputRef"
            type="file"
            accept="image/*"
            style="display: none"
            @change="handleImageSelected"
          />
        </div>
        <el-input
          v-model="inputMessage"
          placeholder="输入消息... (Enter 发送，Shift+Enter 换行)"
          @keydown.enter.exact="sendMessage"
          :autosize="{ minRows: 1, maxRows: 4 }"
          type="textarea"
          resize="none"
        />
        <el-button type="primary" @click="sendMessage" :disabled="!inputMessage.trim()">
          发送
        </el-button>
      </div>
    </div>
    
    <!-- 右侧信息面板 -->
    <div class="info-panel" v-if="showInfoPanel && currentTargetId">
      <div class="panel-header">
        <span>聊天信息</span>
        <el-button link @click="showInfoPanel = false">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
      <div class="panel-content">
        <el-avatar :size="80" :src="currentAvatar" />
        <div class="panel-name">{{ currentTitle }}</div>
        
        <!-- 搜索聊天记录 -->
        <el-button @click="openSearchDialog" style="width: 100%; margin-top: 20px">
          <el-icon><Search /></el-icon>
          搜索聊天记录
        </el-button>
        
        <!-- 通话记录 -->
        <div class="panel-section">
          <div class="section-title">最近通话</div>
          <div v-if="recentCalls.length > 0" class="call-list">
            <div
              v-for="call in recentCalls"
              :key="call.id"
              class="call-item"
              @click="redialCall(call)"
            >
              <el-icon :color="call.type === 'VIDEO' ? '#409eff' : '#67c23a'">
                <component :is="call.type === 'VIDEO' ? 'VideoCamera' : 'Phone'" />
              </el-icon>
              <div class="call-info">
                <div class="call-time">{{ formatCallTime(call.createdAt) }}</div>
                <div class="call-status" :class="call.status">
                  {{ formatCallStatus(call.status) }}
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无通话记录" :image-size="60" />
        </div>
        
        <!-- 消息已读 -->
        <div class="panel-section">
          <div class="section-title">消息已读</div>
          <el-button @click="markAllAsRead" style="width: 100%">
            标记全部为已读
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 通话弹窗 -->
    <CallModal
      v-model="showCallModal"
      :call-type="callModalData.type"
      :caller-id="userStore.userInfo?.id"
      :caller-name="userStore.userInfo?.nickname"
      :caller-avatar="userStore.userInfo?.avatar"
      :receiver-id="currentTargetId"
      @end="handleCallEnd"
      @answer="handleCallAnswer"
      @reject="handleCallReject"
    />
    
    <!-- 搜索对话框 -->
    <MessageSearch
      v-model="showSearchDialog"
      :user-id="userStore.userInfo?.id"
      :conversation-id="currentTargetId"
      @jump="handleJumpToMessage"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import wsManager, { WsMessageType } from '@/utils/websocket'
import {
  Search, Phone, VideoCamera, InfoFilled, Picture, Document,
  Loading, CircleCheck, CircleClose, Close, SwitchButton
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { useCallStore } from '@/store/call'
import { getMessageList, sendMessage as sendMsg, searchMessages } from '@/api/message'
import messageReadApi from '@/api/messageRead'
import callApi from '@/api/call'
import CallModal from '@/components/CallModal.vue'
import MessageSearch from '@/components/MessageSearch.vue'

const route = useRoute()
const userStore = useUserStore()
const callStore = useCallStore()

// 状态
const conversations = ref([
  { id: 1, targetId: 2, name: '测试用户', avatar: '/avatar/default.png', lastMessage: '你好', unreadCount: 1, isGroup: false }
])

const messages = ref([
  { id: 1, fromId: 2, senderName: '测试用户', content: '你好！', time: '12:00', avatar: '/avatar/default.png', type: 'TEXT', isRead: false }
])

const currentTargetId = ref(route.params.targetId || null)
const inputMessage = ref('')
const messageListRef = ref(null)
const fileInputRef = ref(null)
const loadingMessages = ref(false)
const showInfoPanel = ref(true)
const searchKeyword = ref('')

// 通话相关
const showCallModal = ref(false)
const callModalData = ref({ type: 'AUDIO' })
const recentCalls = ref([])

// 搜索相关
const showSearchDialog = ref(false)

const currentTitle = computed(() => {
  const conv = conversations.value.find(c => c.targetId === currentTargetId.value)
  return conv?.name || '聊天'
})

const currentAvatar = computed(() => {
  const conv = conversations.value.find(c => c.targetId === currentTargetId.value)
  return conv?.avatar || '/avatar/default.png'
})

// 选择会话
const selectConversation = async (item) => {
  currentTargetId.value = item.targetId
  
  // 加载消息列表
  await loadMessages()
  
  // 标记为已读
  await markConversationAsRead(item.targetId)
  
  // 重置未读数
  item.unreadCount = 0
}

// 加载消息列表
const loadMessages = async () => {
  if (!currentTargetId.value) return
  
  loadingMessages.value = true
  try {
    const res = await getMessageList({
      targetId: currentTargetId.value,
      pageNum: 1,
      pageSize: 50
    })
    
    if (res.code === 0) {
      messages.value = (res.data.records || []).map(msg => ({
        id: msg.id,
        fromId: msg.fromId,
        senderName: msg.fromId === userStore.userInfo?.id ? '我' : '对方',
        content: msg.content,
        time: new Date(msg.createdAt).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
        avatar: '/avatar/default.png',
        type: msg.type || 'TEXT',
        isRead: msg.isRead || false
      }))
      
      // 滚动到底部
      await nextTick()
      scrollToBottom()
    }
  } catch (error) {
    console.error('加载消息失败', error)
  } finally {
    loadingMessages.value = false
  }
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || !currentTargetId.value) return
  
  try {
    const res = await sendMsg({
      toId: currentTargetId.value,
      type: 'TEXT',
      content: inputMessage.value
    })
    
    if (res.code === 0) {
      messages.value.push({
        id: res.data.id,
        fromId: userStore.userInfo?.id,
        senderName: '我',
        content: inputMessage.value,
        time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
        avatar: userStore.userInfo?.avatar,
        type: 'TEXT',
        isRead: false
      })
      
      inputMessage.value = ''
      scrollToBottom()
    }
  } catch (error) {
    console.error('发送消息失败', error)
    ElMessage.error('发送失败，请重试')
  }
}

// 标记已读
const markConversationAsRead = async (targetId) => {
  try {
    await messageReadApi.markConversationAsRead(userStore.userInfo?.id, String(targetId))
  } catch (error) {
    console.error('标记已读失败', error)
  }
}

// 标记全部为已读
const markAllAsRead = async () => {
  if (!currentTargetId.value) return
  
  try {
    const messageIds = messages.value
      .filter(msg => msg.fromId !== userStore.userInfo?.id && !msg.isRead)
      .map(msg => String(msg.id))
    
    if (messageIds.length > 0) {
      await messageReadApi.batchMarkAsRead(userStore.userInfo?.id, messageIds)
      
      // 更新本地状态
      messages.value.forEach(msg => {
        if (msg.fromId !== userStore.userInfo?.id) {
          msg.isRead = true
        }
      })
      
      ElMessage.success('已标记全部为已读')
    }
  } catch (error) {
    console.error('标记已读失败', error)
  }
}

// 通话相关方法
const handleCallCommand = (command) => {
  if (!currentTargetId.value) {
    ElMessage.warning('请先选择聊天对象')
    return
  }
  
  callModalData.value = { type: command.toUpperCase() }
  showCallModal.value = true
}

const handleCallEnd = async ({ callId, duration }) => {
  console.log('通话结束:', callId, '时长:', duration, '秒')
  // 刷新通话记录
  await loadRecentCalls()
}

const handleCallAnswer = (callId) => {
  console.log('接听通话:', callId)
}

const handleCallReject = (callId) => {
  console.log('拒接通话:', callId)
}

// 加载最近通话
const loadRecentCalls = async () => {
  try {
    const res = await callApi.getCallHistory(userStore.userInfo?.id, 1, 10)
    if (res.code === 0) {
      recentCalls.value = res.data || []
    }
  } catch (error) {
    console.error('加载通话记录失败', error)
  }
}

// 重拨
const redialCall = (call) => {
  callModalData.value = { type: call.type }
  showCallModal.value = true
}

// 搜索相关
const openSearchDialog = () => {
  showSearchDialog.value = true
}

const handleJumpToMessage = (msg) => {
  console.log('跳转到消息:', msg)
  // TODO: 实现消息定位
}

// 选择图片
const selectImage = () => {
  fileInputRef.value?.click()
}

const handleImageSelected = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // TODO: 上传图片并发送
  ElMessage.info('图片上传功能待实现')
}

const selectFile = () => {
  ElMessage.info('文件发送功能待实现')
}

// 工具方法
const scrollToBottom = () => {
  setTimeout(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  }, 100)
}

const formatCallTime = (time) => {
  return new Date(time).toLocaleString('zh-CN')
}

const formatCallStatus = (status) => {
  const statusMap = {
    'ANSWERED': '已接听',
    'MISSED': '未接',
    'REJECTED': '拒接',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

const logout = () => {
  userStore.clearToken()
  window.location.href = '/login'
}

let wsMessageDisconnect = null

onMounted(async () => {
  // 加载用户信息
  if (!userStore.userInfo) {
    await userStore.getUserInfo()
  }
  
  // 加载通话记录
  await loadRecentCalls()
  
  // 连接 WebSocket
  wsManager.connect()
  
  // 初始化通话 store 的 WebSocket 监听
  callStore.init()
  
  // 监听新消息
  wsMessageDisconnect = wsManager.on(WsMessageType.NEW_MESSAGE, handleNewMessage)
  
  // 监听通话相关事件
  wsManager.on(WsMessageType.CALL_ANSWER, handleRemoteCallAnswer)
  wsManager.on(WsMessageType.CALL_REJECT, handleRemoteCallReject)
  wsManager.on(WsMessageType.CALL_END, handleRemoteCallEnd)
})

onUnmounted(() => {
  // 断开 WebSocket 监听
  if (wsMessageDisconnect) {
    wsMessageDisconnect()
  }
  
  // 清理通话状态
  callStore.destroy()
})

// 处理新消息
function handleNewMessage(data) {
  console.log('收到新消息:', data)
  
  // 如果是当前会话的消息，添加到列表
  if (data.fromId === currentTargetId.value || data.toId === currentTargetId.value) {
    messages.value.push({
      id: data.id,
      fromId: data.fromId,
      senderName: data.fromId === userStore.userInfo?.id ? '我' : '对方',
      content: data.content,
      time: new Date(data.timestamp).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
      avatar: '/avatar/default.png',
      type: data.type || 'TEXT',
      isRead: true
    })
    scrollToBottom()
  }
}

// 处理对方接听
function handleRemoteCallAnswer(data) {
  console.log('对方已接听:', data)
  ElMessage.success('对方已接听')
}

// 处理对方拒接
function handleRemoteCallReject(data) {
  console.log('对方拒接:', data)
  ElMessage.warning('对方拒接')
  showCallModal.value = false
}

// 处理对方结束通话
function handleRemoteCallEnd(data) {
  console.log('对方结束通话:', data)
  ElMessage.info(`通话结束，时长：${data.duration}秒`)
  showCallModal.value = false
  loadRecentCalls()
}
</script>

<style scoped>
.chat-container {
  display: flex;
  height: 100vh;
}

.sidebar {
  width: 280px;
  border-right: 1px solid #e6e6e6;
  background: #fff;
  display: flex;
  flex-direction: column;
}

.user-info {
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #e6e6e6;
  gap: 10px;
}

.nickname {
  flex: 1;
  font-weight: 500;
}

.user-actions {
  display: flex;
  gap: 4px;
}

.user-actions .el-button {
  padding: 6px;
  font-size: 18px;
}

.logout-btn {
  font-size: 12px;
}

.search-box {
  padding: 10px;
  border-bottom: 1px solid #e6e6e6;
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  align-items: center;
  padding: 12px 15px;
  cursor: pointer;
  transition: background 0.2s;
}

.conversation-item:hover {
  background: #f5f5f5;
}

.conversation-item.active {
  background: #e6f7ff;
}

.conversation-info {
  flex: 1;
  margin-left: 10px;
  overflow: hidden;
}

.conversation-name {
  font-weight: 500;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.last-message {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.unread-badge {
  background: #f56c6c;
  color: white;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.chat-header {
  padding: 12px 20px;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-title {
  font-weight: 500;
  font-size: 16px;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.message-item {
  display: flex;
  margin-bottom: 15px;
}

.message-self {
  flex-direction: row-reverse;
}

.message-content {
  max-width: 60%;
  margin: 0 10px;
  position: relative;
}

.message-self .message-content {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.message-info {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.message-name {
  font-weight: 500;
}

.message-bubble {
  background: #fff;
  padding: 10px 14px;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  word-break: break-word;
}

.message-self .message-bubble {
  background: #1890ff;
  color: white;
}

.read-status {
  font-size: 12px;
  margin-top: 4px;
}

.loading-more {
  text-align: center;
  padding: 20px;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.message-input {
  display: flex;
  flex-direction: column;
  padding: 15px;
  background: #fff;
  border-top: 1px solid #e6e6e6;
  gap: 10px;
}

.input-toolbar {
  display: flex;
  gap: 8px;
}

.input-toolbar .el-button {
  padding: 4px;
  font-size: 18px;
}

.message-input :deep(.el-textarea__inner) {
  resize: none;
}

.info-panel {
  width: 280px;
  border-left: 1px solid #e6e6e6;
  background: #fff;
  overflow-y: auto;
}

.panel-header {
  padding: 15px;
  border-bottom: 1px solid #e6e6e6;
  font-weight: 500;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-content {
  padding: 20px;
}

.panel-name {
  margin-top: 15px;
  font-weight: 500;
  font-size: 16px;
  text-align: center;
}

.panel-section {
  margin-top: 25px;
  padding-top: 15px;
  border-top: 1px solid #e6e6e6;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 12px;
  color: #606266;
}

.call-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.call-item {
  display: flex;
  align-items: center;
  padding: 10px;
  border-radius: 6px;
  background: #f5f7fa;
  cursor: pointer;
  transition: background 0.2s;
}

.call-item:hover {
  background: #ecf5ff;
}

.call-info {
  margin-left: 10px;
  flex: 1;
}

.call-time {
  font-size: 13px;
  color: #606266;
}

.call-status {
  font-size: 12px;
  margin-top: 2px;
}

.call-status.ANSWERED {
  color: #67c23a;
}

.call-status.MISSED {
  color: #f56c6c;
}

.call-status.REJECTED {
  color: #909399;
}
</style>
