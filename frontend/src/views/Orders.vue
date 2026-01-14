<template>
  <div class="orders">
    <el-card v-if="isAdmin" class="mb16">
      <div class="card-title">创建商品（管理员）</div>
      <el-form :inline="true" class="mt8">
        <el-form-item label="商品名称">
          <el-input v-model="newProduct.name" placeholder="如：ThinkPad X1" style="width: 220px" />
        </el-form-item>
        <el-form-item label="单价">
          <el-input-number v-model.number="newProduct.price" :min="0" :step="1" />
        </el-form-item>
        <el-button type="primary" @click="addProduct">保存商品</el-button>
      </el-form>
      <el-table :data="products" size="small" class="mt8">
        <el-table-column prop="name" label="商品" />
        <el-table-column prop="price" label="单价" width="120" />
      </el-table>
    </el-card>

    <el-card class="mb16">
      <div class="card-title">新建订单</div>
      <div class="helper-text">可选择商品与用户。若未选商品可输入自定义名称与价格。</div>
      <el-form class="mt8" label-position="top">
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
        <div class="stat-pill">总金额：￥{{ totalAmount }}</div>
        <el-button type="success" @click="submitOrder" :disabled="items.length === 0">提交订单</el-button>
      </div>
    </el-card>

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
          <template #default="scope">
            {{ formatItems(scope.row.items) }}
          </template>
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

    <el-dialog v-model="editDialog" title="编辑订单明细" width="720px">
      <el-table :data="editItems" size="small" class="mb12">
        <el-table-column label="商品">
          <template #default="scope">
            <el-select v-model="scope.row.productId" placeholder="商品" filterable clearable style="width: 200px" @change="() => syncEditProduct(scope.row)">
              <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
            </el-select>
            <el-input v-model="scope.row.productName" placeholder="名称" class="mt6" />
          </template>
        </el-table-column>
        <el-table-column label="数量" width="140">
          <template #default="scope">
            <el-input-number v-model.number="scope.row.quantity" :min="1" />
          </template>
        </el-table-column>
        <el-table-column label="单价" width="140">
          <template #default="scope">
            <el-input-number v-model.number="scope.row.price" :min="0" />
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
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createOrder, myOrders, allOrders, updateStatus, updateItems, deleteOrder, ordersByUser } from '../api/orders'
import { listProducts, createProduct } from '../api/products'
import { listUsers } from '../api/users'
import { useRouter } from 'vue-router'

const isAdmin = ref(false)
const products = ref([])
const users = ref([])
const selectedUserId = ref(null)
const selectedUserFilter = ref(null)
const newProduct = ref({ name: '', price: 0 })
const newItem = ref({ productId: null, productName: '', price: 0, quantity: 1 })
const items = ref([])
const orders = ref([])
const router = useRouter()

const editDialog = ref(false)
const editingOrderId = ref(null)
const editItems = ref([])

const orderRows = computed(() => orders.value || [])

function onProductChange() {
  const product = products.value.find((p) => p.id === newItem.value.productId)
  if (product) {
    newItem.value.productName = product.name
    newItem.value.price = Number(product.price)
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
    quantity: newItem.value.quantity
  })
  newItem.value = { productId: null, productName: '', price: 0, quantity: 1 }
}

function remove(index) {
  items.value.splice(index, 1)
}

const totalAmount = computed(() => {
  return items.value.reduce((sum, item) => sum + Number(item.price || 0) * Number(item.quantity || 0), 0).toFixed(2)
})

async function submitOrder() {
  try {
    const payload = {
      userId: isAdmin.value ? selectedUserId.value : null,
      items: items.value
    }
    await createOrder(payload)
    ElMessage.success('创建成功')
    items.value = []
    await loadOrders()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '创建失败')
  }
}

async function loadMyOrders() {
  const page = await myOrders(0, 200)
  orders.value = page.content || []
}

async function loadAdminOrders() {
  const page = selectedUserFilter.value
    ? await ordersByUser(selectedUserFilter.value, 0, 200)
    : await allOrders(0, 200)
  orders.value = page.content || []
}

async function loadOrders() {
  if (isAdmin.value) {
    await loadAdminOrders()
  } else {
    await loadMyOrders()
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

function goDetail(row) {
  router.push(`/orders/${row}`)
}

function formatItems(list = []) {
  if (!list.length) return '—'
  const names = list.map((i) => i.productName).filter(Boolean)
  return names.join('、') || '—'
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
    quantity: i.quantity
  }))
  editDialog.value = true
}

function addEditItem() {
  editItems.value.push({ productId: null, productName: '', price: 0, quantity: 1 })
}

function syncEditProduct(row) {
  const product = products.value.find((p) => p.id === row.productId)
  if (product) {
    row.productName = product.name
    row.price = Number(product.price)
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

async function addProduct() {
  if (!newProduct.value.name) {
    ElMessage.warning('请输入商品名称')
    return
  }
  try {
    await createProduct(newProduct.value)
    ElMessage.success('商品已创建')
    newProduct.value = { name: '', price: 0 }
    loadProducts()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '创建失败')
  }
}

onMounted(async () => {
  await Promise.all([loadUsers(), loadProducts()])
  await loadOrders()
})
</script>

<style scoped>
.orders {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.mb16 { margin-bottom: 16px; }
.mt8 { margin-top: 8px; }
.mt6 { margin-top: 6px; }
.mb12 { margin-bottom: 12px; }
.card-title { font-weight: 700; margin-bottom: 4px; font-size: 16px; }
.full { width: 100%; }
.form-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 100%;
}
.submit-row {
  margin-top: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.filter-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
.stat-pill { font-weight: 700; }
</style>
