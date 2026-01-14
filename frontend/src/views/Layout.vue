<template>
  <div class="layout">
    <el-container>
      <el-aside width="240px" class="aside">
        <div class="logo">OMS</div>
        <el-menu :default-active="active" router class="nav" :default-openeds="defaultOpeneds">
          <el-menu-item index="/">仪表盘</el-menu-item>
          <el-sub-menu index="orders-group">
            <template #title>订单管理</template>
            <el-menu-item index="/orders/products">商品管理</el-menu-item>
            <el-menu-item index="/orders/create">新建订单</el-menu-item>
            <el-menu-item index="/orders/list">订单列表</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/stats">接口统计</el-menu-item>
          <el-menu-item index="/users">用户管理</el-menu-item>
          <el-menu-item @click="logout">退出登录</el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="header">订单管理系统</el-header>
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const active = computed(() => route.path)
const defaultOpeneds = computed(() => (route.path.startsWith('/orders') ? ['orders-group'] : []))

function logout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  background: var(--bg-soft);
}
.aside {
  background: #0f172a;
  color: #fff;
  position: sticky;
  top: 0;
  height: 100vh;
  box-shadow: 10px 0 30px rgba(15, 23, 42, 0.25);
}
.logo {
  padding: 20px 18px;
  font-weight: 800;
  letter-spacing: 2px;
  color: #fff;
  font-size: 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.nav {
  border-right: none;
}
.nav :deep(.el-menu-item) {
  height: 52px;
  font-size: 15px;
}
.nav :deep(.el-menu-item.is-active) {
  background: #15203a;
  box-shadow: inset 4px 0 0 var(--brand);
}
.nav :deep(.el-menu-item:hover) {
  background: #1b2642;
}
.header {
  background: #fff;
  border-bottom: 1px solid #eee;
  font-size: 17px;
  letter-spacing: 0.5px;
  height: 72px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.06);
}
.main {
  padding: 20px;
}
</style>
