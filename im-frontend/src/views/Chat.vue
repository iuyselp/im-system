<template>
  <div class="chat-container">
    <!-- 左侧边栏 -->
    <div class="sidebar">
      <div class="user-info">
        <el-avatar :size="40" :src="userStore.userInfo?.avatar" />
        <span class="nickname">{{ userStore.userInfo?.nickname }}</span>
        <el-button link @click="logout" class="logout-btn">退出</el-button>
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
            <div class="conversation-name">{{ item.name }}</div>
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
        <span>{{ currentTitle }}</span>
      </div>
      
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
            <div class="message-bubble">
              {{ msg.content }}
            </div>
          </div>
        </div>
      </div>
      
      <div class="message-input">
        <el-input
          v-model="inputMessage"
          placeholder="输入消息..."
          @keyup.enter="sendMessage"
          :autosize="{ minRows: 1, maxRows: 4 }"
          type="textarea"
        />
        <el-button type="primary" @click="sendMessage">发送</el-button>
      </div>
    </div>
    
    <!-- 右侧信息面板 -->
    <div class="info-panel" v-if="currentTargetId">
      <div class="panel-header">聊天信息</div>
      <div class="panel-content">
        <el-avatar :size="60" :src="currentAvatar" />
        <div class="panel-name">{{ currentTitle }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getMessageList, sendMessage as sendMsg } from '@/api/message'

const route = useRoute()
const userStore = useUserStore()

const conversations = ref([
  { id: 1, targetId: 2, name: '测试用户', avatar: '/avatar/default.png', lastMessage: '你好', unreadCount: 1 }
])

const messages = ref([
  { id: 1, fromId: 2, senderName: '测试用户', content: '你好！', time: '12:00', avatar: '/avatar/default.png' }
])

const currentTargetId = ref(route.params.targetId || null)
const inputMessage = ref('')
const messageListRef = ref(null)

const currentTitle = computed(() => {
  const conv = conversations.value.find(c => c.targetId === currentTargetId.value)
  return conv?.name || '聊天'
})

const currentAvatar = computed(() => {
  const conv = conversations.value.find(c => c.targetId === currentTargetId.value)
  return conv?.avatar || '/avatar/default.png'
})

const selectConversation = (item) => {
  currentTargetId.value = item.targetId
  // TODO: 加载消息列表
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || !currentTargetId.value) return
  
  try {
    await sendMsg({
      toId: currentTargetId.value,
      type: 'TEXT',
      content: inputMessage.value
    })
    
    messages.value.push({
      id: Date.now(),
      fromId: userStore.userInfo?.id,
      senderName: userStore.userInfo?.nickname,
      content: inputMessage.value,
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
      avatar: userStore.userInfo?.avatar
    })
    
    inputMessage.value = ''
    
    // 滚动到底部
    setTimeout(() => {
      if (messageListRef.value) {
        messageListRef.value.scrollTop = messageListRef.value.scrollHeight
      }
    }, 100)
  } catch (error) {
    console.error('发送消息失败', error)
  }
}

const logout = () => {
  userStore.clearToken()
  window.location.href = '/login'
}

onMounted(() => {
  // TODO: 连接 WebSocket
})

onUnmounted(() => {
  // TODO: 断开 WebSocket
})
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

.logout-btn {
  font-size: 12px;
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
  padding: 15px 20px;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  font-weight: 500;
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
}

.message-name {
  margin-right: 8px;
}

.message-bubble {
  background: #fff;
  padding: 10px 14px;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.message-self .message-bubble {
  background: #1890ff;
  color: white;
}

.message-input {
  display: flex;
  padding: 15px;
  background: #fff;
  border-top: 1px solid #e6e6e6;
  gap: 10px;
}

.message-input :deep(.el-textarea__inner) {
  resize: none;
}

.info-panel {
  width: 280px;
  border-left: 1px solid #e6e6e6;
  background: #fff;
}

.panel-header {
  padding: 15px;
  border-bottom: 1px solid #e6e6e6;
  font-weight: 500;
}

.panel-content {
  padding: 30px;
  text-align: center;
}

.panel-name {
  margin-top: 15px;
  font-weight: 500;
  font-size: 16px;
}
</style>
