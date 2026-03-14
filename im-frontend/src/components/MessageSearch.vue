<template>
  <el-dialog
    v-model="visible"
    title="搜索聊天记录"
    width="600px"
    :close-on-click-modal="false"
  >
    <div class="search-container">
      <!-- 搜索框 -->
      <div class="search-box">
        <el-input
          v-model="keyword"
          placeholder="搜索聊天内容..."
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>

      <!-- 高级筛选 -->
      <div class="search-filters">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 240px"
        />
        <el-select v-model="conversationType" placeholder="消息类型" clearable style="width: 120px">
          <el-option label="全部" value="" />
          <el-option label="文本" value="TEXT" />
          <el-option label="图片" value="IMAGE" />
          <el-option label="文件" value="FILE" />
        </el-select>
      </div>

      <!-- 搜索结果 -->
      <div class="search-results">
        <div v-if="loading" class="loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>搜索中...</span>
        </div>

        <div v-else-if="searchResults.length === 0 && searched" class="empty">
          <el-empty description="未找到相关消息" />
        </div>

        <div v-else class="result-list">
          <div
            v-for="msg in searchResults"
            :key="msg.id"
            class="result-item"
            @click="jumpToMessage(msg)"
          >
            <div class="result-header">
              <span class="sender">{{ getSenderName(msg) }}</span>
              <span class="time">{{ formatTime(msg.createdAt) }}</span>
            </div>
            <div class="result-content" v-html="highlightKeyword(msg.content)"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <template #footer>
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="handleSearch"
        />
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Search, Loading } from '@element-plus/icons-vue'
import { searchMessages } from '@/api/message'
import dayjs from 'dayjs'

const props = defineProps({
  modelValue: Boolean,
  userId: Number,
  conversationId: [Number, String]
})

const emit = defineEmits(['update:modelValue', 'jump'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const keyword = ref('')
const dateRange = ref([])
const conversationType = ref('')
const loading = ref(false)
const searchResults = ref([])
const searched = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 执行搜索
const handleSearch = async () => {
  if (!keyword.value.trim()) {
    return
  }

  loading.value = true
  searched.value = true

  try {
    const params = {
      userId: props.userId,
      keyword: keyword.value.trim(),
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }

    if (props.conversationId) {
      params.conversationId = String(props.conversationId)
    }

    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0] + 'T00:00:00'
      params.endTime = dateRange.value[1] + 'T23:59:59'
    }

    const res = await searchMessages(params)
    if (res.code === 0) {
      searchResults.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('搜索失败:', error)
  } finally {
    loading.value = false
  }
}

// 跳转到消息
const jumpToMessage = (msg) => {
  emit('jump', msg)
  visible.value = false
}

// 获取发送者名称
const getSenderName = (msg) => {
  return msg.fromId === props.userId ? '我' : `用户${msg.fromId}`
}

// 格式化时间
const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

// 高亮关键词
const highlightKeyword = (content) => {
  if (!keyword.value) return content
  const regex = new RegExp(`(${keyword.value})`, 'gi')
  return content.replace(regex, '<span class="highlight">$1</span>')
}

// 监听对话框打开
watch(() => props.modelValue, (val) => {
  if (val) {
    // 重置搜索状态
    searchResults.value = []
    searched.value = false
    currentPage.value = 1
  }
})
</script>

<style scoped>
.search-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-box {
  margin-bottom: 8px;
}

.search-filters {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.search-results {
  min-height: 300px;
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #909399;
  padding: 40px;
}

.empty {
  padding: 40px;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-item {
  padding: 12px;
  border-radius: 4px;
  background: #f5f7fa;
  cursor: pointer;
  transition: background 0.2s;
}

.result-item:hover {
  background: #ecf5ff;
}

.result-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 12px;
}

.sender {
  color: #409eff;
  font-weight: 500;
}

.time {
  color: #909399;
}

.result-content {
  color: #606266;
  line-height: 1.5;
}

:deep(.highlight) {
  color: #f56c6c;
  font-weight: bold;
}

.pagination {
  display: flex;
  justify-content: center;
}
</style>
