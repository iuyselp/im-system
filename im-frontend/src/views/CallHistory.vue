<template>
  <div class="call-history-container">
    <div class="page-header">
      <h2>通话记录</h2>
      <el-button @click="refresh" :loading="loading">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>
    
    <!-- 筛选器 -->
    <div class="filters">
      <el-radio-group v-model="filterType" @change="loadCallHistory">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="AUDIO">语音</el-radio-button>
        <el-radio-button label="VIDEO">视频</el-radio-button>
      </el-radio-group>
      
      <el-radio-group v-model="filterStatus" @change="loadCallHistory">
        <el-radio-button label="">全部状态</el-radio-button>
        <el-radio-button label="ANSWERED">已接听</el-radio-button>
        <el-radio-button label="MISSED">未接</el-radio-button>
        <el-radio-button label="REJECTED">拒接</el-radio-button>
      </el-radio-group>
    </div>
    
    <!-- 通话列表 -->
    <div class="call-list">
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      
      <el-empty v-else-if="calls.length === 0" description="暂无通话记录" />
      
      <div v-else class="list">
        <div
          v-for="call in calls"
          :key="call.id"
          class="call-item"
          :class="call.status"
        >
          <div class="call-icon">
            <el-icon :size="24" :color="call.type === 'VIDEO' ? '#409eff' : '#67c23a'">
              <component :is="call.type === 'VIDEO' ? 'VideoCamera' : 'Phone'" />
            </el-icon>
          </div>
          
          <div class="call-info">
            <div class="call-header">
              <span class="call-type">
                {{ call.type === 'VIDEO' ? '视频通话' : '语音通话' }}
              </span>
              <span class="call-status" :class="call.status">
                {{ formatStatus(call.status) }}
              </span>
            </div>
            
            <div class="call-details">
              <span class="call-time">{{ formatTime(call.createdAt) }}</span>
              <span v-if="call.duration > 0" class="call-duration">
                时长：{{ formatDuration(call.duration) }}
              </span>
            </div>
            
            <div class="call-counterpart">
              <el-icon><User /></el-icon>
              <span>{{ call.callerId === userId ? '对方' : '我' }}</span>
            </div>
          </div>
          
          <div class="call-actions">
            <el-button
              v-if="call.status !== 'MISSED'"
              type="primary"
              size="small"
              @click="redial(call)"
            >
              回拨
            </el-button>
            <el-button
              v-if="call.status === 'MISSED'"
              type="danger"
              size="small"
              @click="redial(call)"
            >
              回拨
            </el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 分页 -->
    <div class="pagination" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadCallHistory"
      />
    </div>
    
    <!-- 通话弹窗 -->
    <CallModal
      v-model="showCallModal"
      :call-type="callModalData.type"
      :caller-id="userId"
      :caller-name="userStore.userInfo?.nickname"
      :caller-avatar="userStore.userInfo?.avatar"
      :receiver-id="callModalData.receiverId"
      @end="handleCallEnd"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Loading, VideoCamera, Phone, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import callApi from '@/api/call'
import CallModal from '@/components/CallModal.vue'

const userStore = useUserStore()
const userId = computed(() => userStore.userInfo?.id)

const loading = ref(false)
const calls = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

const filterType = ref('')
const filterStatus = ref('')

const showCallModal = ref(false)
const callModalData = ref({ type: 'AUDIO', receiverId: null })

// 加载通话历史
const loadCallHistory = async () => {
  loading.value = true
  try {
    const res = await callApi.getCallHistory(userId.value, currentPage.value, pageSize.value)
    if (res.code === 0) {
      let data = res.data || []
      
      // 前端过滤（实际应该在后端实现）
      if (filterType.value) {
        data = data.filter(call => call.type === filterType.value)
      }
      if (filterStatus.value) {
        data = data.filter(call => call.status === filterStatus.value)
      }
      
      calls.value = data
      total.value = res.data?.length || 0
    }
  } catch (error) {
    console.error('加载通话记录失败', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 刷新
const refresh = () => {
  currentPage.value = 1
  loadCallHistory()
}

// 回拨
const redial = (call) => {
  const targetId = call.callerId === userId.value ? call.receiverId : call.callerId
  if (!targetId) {
    ElMessage.warning('群聊通话暂不支持回拨')
    return
  }
  
  callModalData.value = {
    type: call.type,
    receiverId: targetId
  }
  showCallModal.value = true
}

// 通话结束
const handleCallEnd = () => {
  loadCallHistory()
}

// 格式化状态
const formatStatus = (status) => {
  const statusMap = {
    'INITIATED': '已发起',
    'ANSWERED': '已接听',
    'COMPLETED': '已完成',
    'MISSED': '未接',
    'REJECTED': '拒接',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

// 格式化时间
const formatTime = (time) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  // 今天
  if (diff < 24 * 60 * 60 * 1000 && date.getDate() === now.getDate()) {
    return `今天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
  }
  
  // 昨天
  if (diff < 48 * 60 * 60 * 1000) {
    return `昨天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
  }
  
  // 更早
  return date.toLocaleDateString('zh-CN', { 
    month: 'short', 
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化时长
const formatDuration = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}分${secs}秒`
}

onMounted(() => {
  loadCallHistory()
})
</script>

<style scoped>
.call-history-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.filters {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.call-list {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  color: #909399;
  gap: 10px;
}

.list {
  padding: 10px;
}

.call-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.2s;
}

.call-item:last-child {
  border-bottom: none;
}

.call-item:hover {
  background: #f5f7fa;
}

.call-item.MISSED {
  background: #fef0f0;
}

.call-item.MISSED:hover {
  background: #fecece;
}

.call-icon {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 50%;
  margin-right: 16px;
}

.call-info {
  flex: 1;
}

.call-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.call-type {
  font-weight: 500;
  color: #303133;
}

.call-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
}

.call-status.ANSWERED,
.call-status.COMPLETED {
  background: #f0f9eb;
  color: #67c23a;
}

.call-status.MISSED {
  background: #fef0f0;
  color: #f56c6c;
}

.call-status.REJECTED,
.call-status.CANCELLED {
  background: #f4f4f5;
  color: #909399;
}

.call-details {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.call-counterpart {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #606266;
}

.call-actions {
  display: flex;
  gap: 8px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
