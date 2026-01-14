<template>
  <div class="section">
    <el-card>
      <div class="card-title">创建商品（管理员）</div>
      <div class="helper-text">仅管理员可新增商品。普通用户可查看商品列表。</div>
      <div v-if="isAdmin" class="mt12">
        <el-form :inline="true">
          <el-form-item label="商品名称">
            <el-input v-model="newProduct.name" placeholder="如：ThinkPad X1" style="width: 220px" />
          </el-form-item>
          <el-form-item label="单价">
            <el-input-number v-model.number="newProduct.price" :min="0" :step="1" />
          </el-form-item>
          <el-form-item label="图片 URL">
            <el-input v-model="newProduct.imageUrl" placeholder="https://..." style="width: 260px" />
          </el-form-item>
          <el-button type="primary" @click="addProduct">保存商品</el-button>
        </el-form>
      </div>
      <el-table :data="products" size="small" class="mt12">
        <el-table-column prop="name" label="商品" />
        <el-table-column prop="price" label="单价" width="120" />
        <el-table-column label="图片" width="140">
          <template #default="scope">
            <el-image v-if="scope.row.imageUrl" :src="scope.row.imageUrl" fit="cover" style="width:64px;height:64px" />
            <span v-else class="helper-text">暂无</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listProducts, createProduct } from '../api/products'
import { listUsers } from '../api/users'

const products = ref([])
const isAdmin = ref(false)
const newProduct = ref({ name: '', price: 0, imageUrl: '' })

async function loadProducts() {
  products.value = await listProducts()
}

async function detectAdmin() {
  try {
    await listUsers()
    isAdmin.value = true
  } catch (e) {
    isAdmin.value = false
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
    newProduct.value = { name: '', price: 0, imageUrl: '' }
    loadProducts()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '创建失败')
  }
}

onMounted(async () => {
  await Promise.all([detectAdmin(), loadProducts()])
})
</script>

<style scoped>
.section { display: flex; flex-direction: column; gap: 12px; }
.card-title { font-weight: 700; margin-bottom: 4px; font-size: 16px; }
.mt12 { margin-top: 12px; }
.helper-text { color: #999; font-size: 13px; }
</style>
