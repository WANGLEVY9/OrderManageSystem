<template>
  <div class="section">
    <el-card>
      <div class="card-title">{{ isAdmin ? '全部订单' : '我的订单' }}</div>
      <div class="filter-row" v-if="isAdmin">
        <el-select v-model="selectedUserFilter" placeholder="按用户筛选" clearable @change="loadOrders">
          <el-option v-for="u in users" :key="u.id" :label="u.username" :value="u.id" />
        </el-select>
        <div class="helper-text">支持状态修改、明细编辑、删除</div>
      </div>
      <el-table :data="orderRows" style="width:100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column v-if="isAdmin" prop="user.username" label="用户" width="140" />
        <el-table-column label="商品摘要">
          <template #default="scope">{{ formatItems(scope.row.items) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="160">
          <template #default="scope">
            <el-select v-if="isAdmin" v-model="scope.row.status" size="small" @change="(v) => onStatusChange(scope.row.id, v)">
              <el-option label="CREATED" value="CREATED" />
              <el-option label="PAID" value="PAID" />
              <el-option label="SHIPPED" value="SHIPPED" />
              <el-option label="COMPLETED" value="COMPLETED" />
              <el-option label="CANCELLED" value="CANCELLED" />
            </el-select>
            <el-tag v-else>{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="金额" width="120" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="220">
          <template #default="scope">
            <el-button size="small" type="primary" plain @click="goDetail(scope.row.id)">详情</el-button>
            <el-button v-if="isAdmin" size="small" type="success" plain @click="openEdit(scope.row)">编辑明细</el-button>
            <el-button v-if="isAdmin" size="small" type="danger" plain @click="removeOrder(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="editDialog" title="编辑订单明细" width="900px">
      <el-table :data="editItems" size="small" class="mb12">
        <el-table-column label="商品">
          <template #default="scope">
            <template v-if="scope.row.locked">
              <div class="locked-name">{{ scope.row.productName || '原商品' }}</div>
            </template>
            <template v-else>
              <el-select v-model="scope.row.productId" placeholder="商品" filterable clearable style="width: 220px" @change="() => syncEditProduct(scope.row)">
                <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
              </el-select>
              <el-input v-model="scope.row.productName" placeholder="名称" class="mt6" />
            </template>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="140">
          <template #default="scope">
            <el-input-number v-model.number="scope.row.quantity" :min="1" class="num-input" />
          </template>
        </el-table-column>
        <el-table-column label="单价" width="140">
          <template #default="scope">
            <el-input-number v-model.number="scope.row.price" :min="0" class="num-input" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button type="danger" size="small" @click="editItems.splice(scope.$index,1)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-button type="primary" plain @click="addEditItem">添加行</el-button>
      <template #footer>
        <el-button @click="editDialog=false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { myOrders, allOrders, ordersByUser, updateStatus, updateItems, deleteOrder } from '../api/orders'
import { listUsers } from '../api/users'
import { listProducts } from '../api/products'
import { useRouter } from 'vue-router'

const isAdmin = ref(false)
const users = ref([])
const products = ref([])
const selectedUserFilter = ref(null)
const orders = ref([])
const router = useRouter()

const editDialog = ref(false)
const editingOrderId = ref(null)
const editItems = ref([])

const orderRows = computed(() => orders.value || [])

function formatItems(list = []) {
  if (!list.length) return '—'
  const names = list.map((i) => i.productName).filter(Boolean)
  return names.join('、') || '—'
}

async function loadUsers() {
  try {
    users.value = await listUsers()
    isAdmin.value = true
  } catch (e) {
    isAdmin.value = false
  }
}

async function loadProducts() {
  products.value = await listProducts()
}

async function loadOrders() {
  if (isAdmin.value) {
    const page = selectedUserFilter.value ? await ordersByUser(selectedUserFilter.value, 0, 200) : await allOrders(0, 200)
    orders.value = page.content || []
  } else {
    const page = await myOrders(0, 200)
    orders.value = page.content || []
  }
}

function goDetail(row) {
  router.push(`/orders/${row}`)
}

async function onStatusChange(id, status) {
  try {
    await updateStatus(id, status)
    ElMessage.success('状态已更新')
  } catch (e) {
    ElMessage.error('更新失败')
    loadOrders()
  }
}

function openEdit(order) {
  editingOrderId.value = order.id
  editItems.value = (order.items || []).map((i) => ({
    productId: i.product?.id || null,
    productName: i.productName,
    price: Number(i.price),
    quantity: i.quantity,
    imageUrl: i.imageUrl || '',
    locked: true
  }))
  editDialog.value = true
}

function addEditItem() {
  editItems.value.push({ productId: null, productName: '', price: 0, quantity: 1, imageUrl: '', locked: false })
}

function syncEditProduct(row) {
  const product = products.value.find((p) => p.id === row.productId)
  if (product) {
    row.productName = product.name
    row.price = Number(product.price)
    row.imageUrl = product.imageUrl || ''
  }
}

async function saveEdit() {
  try {
    await updateItems(editingOrderId.value, { items: editItems.value })
    ElMessage.success('已保存')
    editDialog.value = false
    loadOrders()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

async function removeOrder(id) {
  try {
    await ElMessageBox.confirm('确认删除该订单？', '提示', { type: 'warning' })
    await deleteOrder(id)
    ElMessage.success('已删除')
    loadOrders()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(async () => {
  await Promise.all([loadUsers(), loadProducts()])
  await loadOrders()
})
</script>

<style scoped>
.section { display: flex; flex-direction: column; gap: 12px; }
.card-title { font-weight: 700; margin-bottom: 4px; font-size: 16px; }
.filter-row { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.helper-text { color: #999; font-size: 13px; }
.mb12 { margin-bottom: 12px; }
.mt6 { margin-top: 6px; }
.num-input { width: 140px; }
.locked-name { padding: 6px 8px; background: #f3f4f6; border-radius: 4px; color: #374151; }
</style>
