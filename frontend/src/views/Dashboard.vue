<template>
  <div class="dashboard">
    <el-row :gutter="16" class="hero">
      <el-col :span="18">
        <div>
          <div class="hero-title">接口运行概览</div>
          <div class="helper-text">快速查看调用量、热点接口，并在右侧找到操作指引</div>
        </div>
      </el-col>
      <el-col :span="6" class="hero-actions">
        <el-button type="primary" plain @click="refresh">刷新数据</el-button>
        <div class="timestamp">更新于 {{ lastUpdated || '加载中...' }}</div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="metric-row">
      <el-col :span="6" v-for="card in metricCards" :key="card.label">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-label">{{ card.label }}</div>
          <div class="metric-value">{{ card.value }}</div>
          <div class="metric-sub">{{ card.desc }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="content">
      <el-col :span="16">
        <el-card shadow="hover" class="chart-card">
          <div class="section-title">Top 接口</div>
          <div ref="rankRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="section-title">操作指引</div>
          <ul class="guide">
            <li>订单创建后可在“订单管理”查看并补充明细。</li>
            <li>普通用户仅能调整订单状态，管理员可修改数量/价格。</li>
            <li>统计报表的柱状图和表格已按同一数据源同步。</li>
            <li>接口异常时请在“接口统计”查看具体接口排名。</li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { fetchCounters, fetchRank } from '../api/stats'

const counters = ref({})
const metricCards = ref([])
const lastUpdated = ref('')
const rankRef = ref(null)

onMounted(() => {
  refresh()
})

async function refresh() {
  counters.value = await fetchCounters()
  const rank = await fetchRank(5)
  buildMetricCards()
  renderChart(rank)
  lastUpdated.value = new Date().toLocaleString()
}

function buildMetricCards() {
  const totalCalls = Object.values(counters.value || {}).reduce((a, b) => a + b, 0)
  metricCards.value = [
    { label: '总调用量', value: totalCalls || 0, desc: '接口总体承载' },
    { label: '接口数量', value: Object.keys(counters.value || {}).length, desc: '纳入监控的接口数' },
    { label: '最高单接口', value: topEndpoint(), desc: '排名第一的接口' },
    { label: '最近刷新', value: lastUpdated.value || '—', desc: '数据拉取时间' }
  ]
}

function topEndpoint() {
  const entries = Object.entries(counters.value || {})
  if (!entries.length) return '暂无'
  const [path, count] = entries.sort((a, b) => b[1] - a[1])[0]
  return `${path} (${count})`
}

function renderChart(rank) {
  if (!rankRef.value) return
  const chart = echarts.init(rankRef.value)
  const labels = Object.keys(rank)
  const data = Object.values(rank)
  chart.setOption({
    grid: { left: 40, right: 10, top: 30, bottom: 30 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: labels, axisTick: { show: false } },
    yAxis: { type: 'value' },
    series: [{ data, type: 'bar', itemStyle: { color: '#409eff' }, barWidth: 26 }]
  })
}
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.hero {
  align-items: center;
}
.hero-title {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 0.5px;
}
.hero-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  font-size: 13px;
}
.timestamp {
  color: #6b7280;
}
.metric-row .metric-card {
  min-height: 110px;
}
.metric-label {
  color: #6b7280;
  margin-bottom: 6px;
}
.metric-value {
  font-size: 24px;
  font-weight: 800;
}
.metric-sub {
  color: #6b7280;
}
.chart-card .chart {
  height: 320px;
}
.guide {
  margin: 0;
  padding-left: 18px;
  color: #4b5563;
  line-height: 1.7;
}
.guide li + li {
  margin-top: 6px;
}
</style>
