<template>
  <div class="users">
    <el-card>
      <div class="card-title">用户列表（管理员）</div>
      <div class="helper-text">支持查看用户详情，编辑操作需后端开启对应接口。</div>
      <el-table :data="users" style="width: 100%" class="mt8">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.enabled ? 'success' : 'info'">{{ scope.row.enabled ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="roles" label="角色">
          <template #default="scope">
            <el-tag v-for="r in scope.row.roles" :key="r.code" class="mr4">{{ r.code }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" @click="openDetail(scope.row)">查看</el-button>
            <el-button size="small" type="primary" plain @click="editUser(scope.row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-drawer v-model="drawerVisible" title="用户详情" :with-header="true" size="400px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="ID">{{ currentUser.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentUser.enabled ? 'success' : 'info'">{{ currentUser.enabled ? '启用' : '停用' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag v-for="r in currentUser.roles || []" :key="r.code" class="mr4">{{ r.code }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <div class="helper-text mt12">需要修改用户信息时，请在后端开放更新接口，前端已预留入口。</div>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listUsers } from '../api/users'

const users = ref([])
const drawerVisible = ref(false)
const currentUser = ref({})

async function load() {
  try {
    users.value = await listUsers()
  } catch (e) {
    ElMessage.error('需要管理员权限')
  }
}

onMounted(load)

function openDetail(row) {
  currentUser.value = row
  drawerVisible.value = true
}

function editUser(row) {
  ElMessage.info('前端已打开编辑入口，需后端提供更新接口后生效')
  openDetail(row)
}
</script>

<style scoped>
.users { display: flex; flex-direction: column; gap: 8px; }
.card-title { font-weight: 700; margin-bottom: 4px; }
.mr4 { margin-right: 4px; }
.mt8 { margin-top: 8px; }
.mt12 { margin-top: 12px; }
</style>
