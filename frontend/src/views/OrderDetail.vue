<template>
  <div class="detail">
    <el-card class="summary-card">
      <div class="card-title">订单 {{ id }}</div>
      <div class="helper-text">普通用户仅能调整状态，管理员可在后端开启数量/价格编辑。</div>
      <el-row :gutter="12" class="mt8">
        <el-col :span="6">
          <div class="label">状态</div>
          <div class="stat-pill">{{ order.status || '—' }}</div>
        </el-col>
        <el-col :span="6">
          <div class="label">金额</div>
          <div class="metric">{{ order.totalAmount ?? '—' }}</div>
        </el-col>
        <el-col :span="6">
          <div class="label">创建时间</div>
          <div class="metric">{{ order.createdAt || '—' }}</div>
        </el-col>
        <el-col :span="6">
          <div class="label">下单人</div>
          <div class="metric">{{ order.user?.username || '—' }}</div>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="mt12">
      <div class="card-title">订单明细</div>
      <el-table :data="order.items || []" style="width:100%">
        <el-table-column prop="productName" label="商品" />
        <el-table-column prop="quantity" label="数量" width="120" />
        <el-table-column prop="price" label="单价" width="120" />
      </el-table>
    </el-card>

    <el-card class="mt12">
      <div class="card-title">状态更新</div>
      <el-form :inline="true">
        <el-form-item label="更新状态">
          <el-select v-model="status" placeholder="选择状态" style="width: 200px">
            <el-option label="CREATED" value="CREATED" />
            <el-option label="PAID" value="PAID" />
            <el-option label="SHIPPED" value="SHIPPED" />
            <el-option label="COMPLETED" value="COMPLETED" />
            <el-option label="CANCELLED" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-button type="primary" @click="onUpdate">更新状态</el-button>
        <el-button plain @click="status = order.status">重置</el-button>
      </el-form>
    </el-card>

    <el-card class="mt12">
      <div class="card-title">操作记录</div>
      <el-timeline>
        <el-timeline-item v-for="log in logs" :key="log.id" :timestamp="log.createdAt">
          {{ log.action }} - {{ log.operator }} - {{ log.remark }}
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { allOrders, fetchLogs, myOrders, updateStatus } from '../api/orders'

const route = useRoute()
const id = route.params.id
const logs = ref([])
const order = ref({})
const status = ref('CREATED')

async function load() {
  await Promise.all([loadOrder(), loadLogs()])
  status.value = order.value.status || 'CREATED'
}

async function loadLogs() {
  logs.value = await fetchLogs(id)
}

async function loadOrder() {
  // 优先管理员接口，否则回退到个人订单
  const page = await allOrders(0, 200).catch(() => null)
  let found = page?.content?.find((o) => String(o.id) === String(id))
  if (!found) {
    const mine = await myOrders(0, 200)
    found = mine?.content?.find((o) => String(o.id) === String(id))
  }
  order.value = found || {}
}

async function onUpdate() {
  try {
    await updateStatus(id, status.value)
    ElMessage.success('更新成功')
    load()
  } catch (e) {
    ElMessage.error('更新失败，需管理员或拥有写权限的用户')
  }
}

onMounted(load)
</script>

<style scoped>
.card-title { font-weight: 700; margin-bottom: 8px; font-size: 16px; }
.label { color: #6b7280; margin-bottom: 4px; }
.metric { font-weight: 700; font-size: 18px; }
.mt12 { margin-top: 12px; }
.detail { display: flex; flex-direction: column; gap: 8px; }
.summary-card { background: #f9fbff; }
</style>
