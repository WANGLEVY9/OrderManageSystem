<template>
  <el-card class="stats">
    <div class="card-title">接口统计（管理员）</div>
    <div class="helper-text">柱状图与表格共用同一数据源，保障口径一致。按次数降序展示前 10 个接口。</div>
    <el-row :gutter="16" class="mt12">
      <el-col :span="12">
        <el-card shadow="hover">
          <div id="rankChartAdmin" style="height:320px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <el-table :data="dataSource" size="small">
            <el-table-column prop="name" label="接口" />
            <el-table-column prop="count" label="次数" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </el-card>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import * as echarts from 'echarts'
import { fetchCounters, fetchRank } from '../api/stats'
import { ElMessage } from 'element-plus'

const counters = ref({})
const rank = ref({})
const dataSource = computed(() => {
  const source = Object.keys(rank.value).length ? rank.value : counters.value
  return Object.entries(source)
    .map(([name, count]) => ({ name, count }))
    .sort((a, b) => b.count - a.count)
})

onMounted(async () => {
  try {
    counters.value = await fetchCounters()
    rank.value = await fetchRank(10)
    renderChart()
  } catch (e) {
    ElMessage.error('需要管理员权限')
  }
})

function renderChart() {
  const chart = echarts.init(document.getElementById('rankChartAdmin'))
  const labels = dataSource.value.map((i) => i.name)
  const values = dataSource.value.map((i) => i.count)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: labels },
    yAxis: { type: 'value' },
    series: [{ data: values, type: 'bar', barWidth: 24, itemStyle: { color: '#67c23a' } }]
  })
}
</script>

<style scoped>
.card-title { font-weight: 700; margin-bottom: 4px; }
.stats { background: #f9fbff; }
.mt12 { margin-top: 12px; }
</style>
