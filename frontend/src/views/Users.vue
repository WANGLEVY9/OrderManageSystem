<template>
  <div class="users">
    <el-card>
      <div class="card-title">用户列表（管理员）</div>
      <div class="helper-text">支持查看、修改用户名 / 状态 / 角色。</div>
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

    <el-drawer v-model="drawerVisible" title="用户详情" :with-header="true" size="420px">
      <el-form label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" active-text="启用" inactive-text="停用" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleCodes" multiple placeholder="选择角色" style="width: 100%">
            <el-option v-for="r in roles" :key="r.code" :label="r.code" :value="r.code" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="drawerVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listUsers, updateUser, listRoles } from '../api/users'

const users = ref([])
const drawerVisible = ref(false)
const currentUser = ref({})
const form = ref({ username: '', enabled: true, roleCodes: [] })
const roles = ref([])

async function load() {
  try {
    users.value = await listUsers()
    roles.value = await listRoles()
  } catch (e) {
    ElMessage.error('需要管理员权限')
  }
}

onMounted(load)

function openDetail(row) {
  currentUser.value = row
  form.value = {
    username: row.username,
    enabled: row.enabled,
    roleCodes: (row.roles || []).map((r) => r.code)
  }
  drawerVisible.value = true
}

function editUser(row) {
  openDetail(row)
}

async function save() {
  try {
    await updateUser(currentUser.value.id, form.value)
    ElMessage.success('已更新')
    drawerVisible.value = false
    load()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '更新失败')
  }
}
</script>

<style scoped>
.users { display: flex; flex-direction: column; gap: 8px; }
.card-title { font-weight: 700; margin-bottom: 4px; }
.mr4 { margin-right: 4px; }
.mt8 { margin-top: 8px; }
.mt12 { margin-top: 12px; }
</style>
