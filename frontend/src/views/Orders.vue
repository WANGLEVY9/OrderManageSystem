<template>
  <div class="orders">
    <el-card class="mb16">
      <div class="card-title">新建订单</div>
      <div class="helper-text">先补齐商品名称/数量/单价，提交后可在详情页继续调整。</div>
      <el-form class="mt8" label-position="top">
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="商品名称">
              <el-input v-model="newItem.productName" placeholder="输入商品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="数量">
              <el-input-number v-model.number="newItem.quantity" :min="1" class="full" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="单价">
              <el-input-number v-model.number="newItem.price" :min="0" :step="1" class="full" />
            </el-form-item>
          </el-col>
        </el-row>
        <div class="form-actions">
          <el-button type="primary" @click="addItem">添加明细</el-button>
          <div class="helper-text">已添加 {{ items.length }} 条明细</div>
        </div>
      </el-form>
      <el-table :data="items" class="mt8" style="width: 100%">
        <el-table-column prop="productName" label="商品" />
        <el-table-column prop="quantity" label="数量" width="120" />
        <el-table-column prop="price" label="单价" width="120" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button size="small" type="danger" @click="remove(scope.$index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="submit-row">
        <div class="stat-pill">总金额：{{ totalAmount }}</div>
        <el-button type="success" @click="submitOrder" :disabled="items.length === 0">提交订单</el-button>
      </div>
    </el-card>

    <el-card>
      <div class="card-title">我的订单</div>
      <el-table :data="orders" style="width:100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="商品摘要">
          <template #default="scope">
            {{ formatItems(scope.row.items) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="totalAmount" label="金额" width="120" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" type="primary" plain @click="goDetail(scope.row.id)">详情/更新</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { createOrder, myOrders } from '../api/orders'
import { useRouter } from 'vue-router'

const newItem = ref({ productName: '示例商品', quantity: 1, price: 100 })
const items = ref([])
const orders = ref([])
const router = useRouter()

function addItem() {
  items.value.push({ ...newItem.value })
}
function remove(index) {
  items.value.splice(index, 1)
}

async function submitOrder() {
  try {
    await createOrder(items.value)
    ElMessage.success('创建成功')
    items.value = []
    loadOrders()
  } catch (e) {
    ElMessage.error('创建失败')
  }
}

async function loadOrders() {
  const page = await myOrders(0, 50)
  orders.value = page.content || []
}

function goDetail(row) {
  router.push(`/orders/${row}`)
}

const totalAmount = computed(() => items.value.reduce((sum, item) => sum + (item.quantity || 0) * (item.price || 0), 0))

function formatItems(list = []) {
  if (!list.length) return '—'
  const names = list.map((i) => i.productName).filter(Boolean)
  return names.join('、') || '—'
}

onMounted(loadOrders)
</script>

<style scoped>
.orders {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.mb16 { margin-bottom: 16px; }
.mt8 { margin-top: 8px; }
.card-title { font-weight: 700; margin-bottom: 4px; font-size: 16px; }
.full { width: 100%; }
.form-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
.submit-row {
  margin-top: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
