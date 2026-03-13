<template>
  <div class="settings-container">
    <div class="settings-box">
      <h1>个人设置</h1>
      
      <el-form :model="form" label-width="100px">
        <el-form-item label="头像">
          <el-avatar :size="80" :src="form.avatar" />
          <el-button size="small" style="margin-left: 15px">更换头像</el-button>
        </el-form-item>
        
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" style="width: 300px" />
        </el-form-item>
        
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :label="0">未知</el-radio>
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="个性签名">
          <el-input
            v-model="form.signature"
            type="textarea"
            :rows="3"
            style="width: 300px"
            placeholder="请输入个性签名"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSave">保存</el-button>
        </el-form-item>
      </el-form>
      
      <el-divider />
      
      <h2>修改密码</h2>
      <el-form :model="passwordForm" label-width="100px">
        <el-form-item label="原密码">
          <el-input v-model="passwordForm.oldPassword" type="password" style="width: 300px" />
        </el-form-item>
        
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" style="width: 300px" />
        </el-form-item>
        
        <el-form-item label="确认密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" style="width: 300px" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="warning" @click="handleChangePassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { updateUserInfo, changePassword } from '@/api/user'

const userStore = useUserStore()

const form = reactive({
  avatar: '',
  nickname: '',
  gender: 0,
  signature: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

onMounted(() => {
  if (userStore.userInfo) {
    form.avatar = userStore.userInfo.avatar
    form.nickname = userStore.userInfo.nickname
    form.gender = userStore.userInfo.gender
    form.signature = userStore.userInfo.signature
  }
})

const handleSave = async () => {
  try {
    const res = await updateUserInfo(form)
    if (res.code === 200) {
      userStore.setUserInfo(res.data)
      ElMessage.success('保存成功')
    }
  } catch (error) {
    console.error('保存失败', error)
  }
}

const handleChangePassword = async () => {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  
  try {
    await changePassword(passwordForm.oldPassword, passwordForm.newPassword)
    ElMessage.success('密码修改成功，请重新登录')
    userStore.clearToken()
    setTimeout(() => {
      window.location.href = '/login'
    }, 1500)
  } catch (error) {
    console.error('修改密码失败', error)
  }
}
</script>

<style scoped>
.settings-container {
  height: 100%;
  background: #f5f5f5;
  padding: 30px;
  overflow-y: auto;
}

.settings-box {
  max-width: 600px;
  margin: 0 auto;
  background: white;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

h1 {
  font-size: 24px;
  margin-bottom: 30px;
  color: #333;
}

h2 {
  font-size: 18px;
  margin: 20px 0 15px;
  color: #333;
}
</style>
