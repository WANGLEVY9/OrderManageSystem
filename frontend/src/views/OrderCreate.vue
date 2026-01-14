<template>
  <div class="section">
    <el-card>
      <div class="card-title">新建订单</div>
      <div class="helper-text">选择商品与用户；未选商品时可输入自定义名称与价格。</div>
      <el-form class="mt12" label-position="top">
        <el-row :gutter="12">
          <el-col :span="6" v-if="isAdmin">
            <el-form-item label="下单用户">
              <el-select v-model="selectedUserId" placeholder="选择用户" filterable>
                <el-option v-for="u in users" :key="u.id" :label="u.username" :value="u.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="选择商品">
              <el-select v-model="newItem.productId" placeholder="选择商品" filterable clearable @change="onProductChange">
                <el-option v-for="p in products" :key="p.id" :label="`${p.name}（￥${p.price}`" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="商品名称（自定义）">
              <el-input v-model="newItem.productName" placeholder="不选商品时填写" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="单价">
              <el-input-number v-model.number="newItem.price" :min="0" :step="1" class="full" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="图片 URL（可选）">
              <el-input v-model="newItem.imageUrl" placeholder="https://..." />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="6">
            <el-form-item label="数量">
              <el-input-number v-model.number="newItem.quantity" :min="1" class="full" />
            </el-form-item>
          </el-col>
          <el-col :span="18" class="form-actions">
            <el-button type="primary" @click="addItem">添加明细</el-button>
            <div class="helper-text">已添加 {{ items.length }} 条明细</div>
          </el-col>
        </el-row>
      </el-form>
      <el-table :data="items" class="mt12" style="width: 100%">
        <el-table-column prop="productName" label="商品" />
        <el-table-column prop="quantity" label="数量" width="120" />
        <el-table-column prop="price" label="单价" width="120" />
        <el-table-column label="图片" width="140">
          <template #default="scope">
            <el-image v-if="scope.row.imageUrl" :src="scope.row.imageUrl" fit="cover" style="width:60px;height:60px" />
            <span v-else class="helper-text">—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button size="small" type="danger" @click="remove(scope.$index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="submit-row">
        <div class="stat-pill">总金额：￥{{ totalAmount }}</div>
        <el-button type="success" @click="submitOrder" :disabled="items.length === 0">提交订单</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { createOrder } from '../api/orders'
import { listProducts } from '../api/products'
import { listUsers } from '../api/users'

const isAdmin = ref(false)
const products = ref([])
const users = ref([])
const selectedUserId = ref(null)
const newItem = ref({ productId: null, productName: '', price: 0, quantity: 1, imageUrl: '' })
const items = ref([])

function onProductChange() {
  const product = products.value.find((p) => p.id === newItem.value.productId)
  if (product) {
    newItem.value.productName = product.name
    newItem.value.price = Number(product.price)
    newItem.value.imageUrl = product.imageUrl || ''
  }
}

function addItem() {
  if (!newItem.value.productId && !newItem.value.productName) {
    ElMessage.warning('请选择商品或填写名称')
    return
  }
  const product = products.value.find((p) => p.id === newItem.value.productId)
  items.value.push({
    productId: newItem.value.productId || null,
    productName: product ? product.name : newItem.value.productName,
    price: product ? Number(product.price) : newItem.value.price,
    quantity: newItem.value.quantity,
    imageUrl: product ? product.imageUrl : newItem.value.imageUrl
  })
  newItem.value = { productId: null, productName: '', price: 0, quantity: 1, imageUrl: '' }
}

function remove(index) {
  items.value.splice(index, 1)
}

const totalAmount = computed(() => {
  return items.value.reduce((sum, item) => sum + Number(item.price || 0) * Number(item.quantity || 0), 0).toFixed(2)
})

async function submitOrder() {
  try {
    const payload = { userId: isAdmin.value ? selectedUserId.value : null, items: items.value }
    await createOrder(payload)
    ElMessage.success('创建成功')
    items.value = []
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '创建失败')
  }
}

async function loadUsers() {
  try {
    users.value = await listUsers()
    isAdmin.value = true
    if (!selectedUserId.value && users.value.length) {
      selectedUserId.value = users.value[0].id
    }
  } catch (e) {
    isAdmin.value = false
  }
}

async function loadProducts() {
  products.value = await listProducts()
}

onMounted(async () => {
  await Promise.all([loadUsers(), loadProducts()])
})
</script>

<style scoped>
.section { display: flex; flex-direction: column; gap: 12px; }
.card-title { font-weight: 700; margin-bottom: 4px; font-size: 16px; }
.full { width: 100%; }
.mt12 { margin-top: 12px; }
.helper-text { color: #999; font-size: 13px; }
.form-actions { display: flex; align-items: center; gap: 10px; height: 100%; }
.submit-row { margin-top: 12px; display: flex; justify-content: space-between; align-items: center; }
.stat-pill { font-weight: 700; }
</style>
