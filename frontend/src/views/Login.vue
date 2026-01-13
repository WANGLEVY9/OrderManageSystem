<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2>欢迎使用 OMS</h2>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="登录" name="login">
          <el-form :model="form" @submit.prevent>
            <el-form-item label="用户名">
              <el-input v-model="form.username" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input type="password" v-model="form.password" />
            </el-form-item>
            <el-button type="primary" @click="onLogin" :loading="loading">登录</el-button>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册普通用户" name="register">
          <el-form :model="reg" @submit.prevent>
            <el-form-item label="用户名">
              <el-input v-model="reg.username" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input type="password" v-model="reg.password" />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input type="password" v-model="reg.confirm" />
            </el-form-item>
            <div class="helper-text">注册仅创建普通用户。管理员用户需由 DB 手动添加。</div>
            <el-button type="success" @click="onRegister" :loading="loading">注册并登录</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { ElMessage } from 'element-plus'
import { registerApi } from '../api/auth'

const form = reactive({ username: 'admin', password: 'admin123' })
const reg = reactive({ username: '', password: '', confirm: '' })
const loading = ref(false)
const activeTab = ref('login')
const router = useRouter()
const auth = useAuthStore()

async function onLogin() {
  loading.value = true
  try {
    await auth.login(form.username, form.password)
    router.push('/')
  } catch (e) {
    ElMessage.error('登录失败')
  } finally {
    loading.value = false
  }
}

async function onRegister() {
  if (!reg.username || !reg.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  if (reg.password !== reg.confirm) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  loading.value = true
  try {
    await registerApi(reg.username, reg.password)
    await auth.login(reg.username, reg.password)
    ElMessage.success('注册成功，已自动登录')
    router.push('/')
  } catch (e) {
    ElMessage.error('注册失败，仅可创建普通用户')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(120deg, #f0f4ff, #f9fafc);
}
.login-card {
  width: 360px;
}
.helper-text {
  color: #6b7280;
  font-size: 13px;
  margin-bottom: 8px;
}
</style>
