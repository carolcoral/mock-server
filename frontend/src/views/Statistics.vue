<template>
  <div class="statistics-page">
    <div class="page-header">
      <h1>{{ $t('statistics.title') }}</h1>
    </div>

    <!-- IOPS 实时概览卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="8">
        <el-card class="iops-card" shadow="hover">
          <div class="iops-card-inner">
            <div class="iops-label">{{ $t('statistics.currentIops') }}</div>
            <div class="iops-value">{{ currentIops }}</div>
            <div class="iops-unit">req/s</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="iops-card" shadow="hover">
          <div class="iops-card-inner">
            <div class="iops-label">{{ $t('statistics.avgIops') }}</div>
            <div class="iops-value">{{ avgIops }}</div>
            <div class="iops-unit">req/s</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="iops-card" shadow="hover">
          <div class="iops-card-inner">
            <div class="iops-label">{{ $t('statistics.peakIops') }}</div>
            <div class="iops-value">{{ peakIops }}</div>
            <div class="iops-unit">req/s</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- IOPS 趋势图 -->
    <el-card class="chart-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>{{ $t('statistics.iopsTrend') }}</span>
          <el-radio-group v-model="iopsMinutes" size="small" @change="fetchIops">
            <el-radio-button :value="30">30min</el-radio-button>
            <el-radio-button :value="60">1h</el-radio-button>
            <el-radio-button :value="180">3h</el-radio-button>
            <el-radio-button :value="360">6h</el-radio-button>
            <el-radio-button :value="720">12h</el-radio-button>
            <el-radio-button :value="1440">24h</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div ref="iopsChart" class="chart-container"></div>
    </el-card>

    <!-- 请求频率统计 -->
    <el-card class="chart-card" shadow="hover" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>{{ $t('statistics.requestFrequency') }}</span>
          <el-radio-group v-model="freqGranularity" size="small" @change="fetchRequestFrequency">
            <el-radio-button value="daily">{{ $t('statistics.daily') }}</el-radio-button>
            <el-radio-button value="hourly">{{ $t('statistics.hourly') }}</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div ref="requestFreqChart" class="chart-container"></div>
    </el-card>

    <!-- 来源IP统计 -->
    <el-card class="chart-card" shadow="hover" style="margin-top: 20px;">
      <template #header>
        <span>{{ $t('statistics.sourceIps') }}</span>
      </template>
      <div ref="sourceIpChart" class="chart-container"></div>
    </el-card>

    <!-- 新增趋势统计 -->
    <el-card class="chart-card" shadow="hover" style="margin-top: 20px;">
      <template #header>
        <span>{{ $t('statistics.creationTrend') }}</span>
      </template>
      <div ref="creationTrendChart" class="chart-container"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import request from '@/utils/request'
import * as echarts from 'echarts'

const { t } = useI18n()

const freqGranularity = ref('daily')
const iopsMinutes = ref(60)
const currentIops = ref('--')
const avgIops = ref('--')
const peakIops = ref('--')

const iopsChart = ref(null)
const requestFreqChart = ref(null)
const sourceIpChart = ref(null)
const creationTrendChart = ref(null)

let iopsChartInstance = null
let freqChartInstance = null
let ipChartInstance = null
let trendChartInstance = null

// ========== IOPS 统计 ==========

const fetchIops = async () => {
  try {
    const response = await request.get('/statistics/iops', {
      params: { minutes: iopsMinutes.value }
    })
    if (response.code === 200 && response.data) {
      const vals = response.data.values || []
      if (vals.length > 0) {
        currentIops.value = vals[vals.length - 1].toFixed(2)
        avgIops.value = (vals.reduce((a, b) => a + b, 0) / vals.length).toFixed(2)
        peakIops.value = Math.max(...vals).toFixed(2)
      } else {
        currentIops.value = '0.00'
        avgIops.value = '0.00'
        peakIops.value = '0.00'
      }
      renderIopsChart(response.data)
    }
  } catch (error) {
    console.error('获取IOPS统计失败:', error)
  }
}

const renderIopsChart = (data) => {
  if (!iopsChart.value) return
  if (!iopsChartInstance) {
    iopsChartInstance = echarts.init(iopsChart.value)
  }

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const p = params[0]
        return `${p.axisValue}<br/>${t('statistics.iops')}: ${p.value} req/s`
      }
    },
    grid: { left: '3%', right: '8%', bottom: '15%', top: '10px', containLabel: true },
    xAxis: {
      type: 'category',
      data: data.labels || [],
      axisLabel: {
        rotate: 45,
        fontSize: 10,
        formatter: (val) => val.substring(11)
      }
    },
    yAxis: {
      type: 'value',
      name: 'req/s',
      minInterval: 0.01
    },
    dataZoom: [
      { type: 'slider', start: 0, end: 100, height: 20, bottom: 0 },
      { type: 'inside', start: 0, end: 100 }
    ],
    series: [{
      name: 'IOPS',
      type: 'line',
      data: data.values || [],
      smooth: true,
      symbol: 'none',
      lineStyle: { color: '#30cfd0', width: 2 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(48,207,208,0.4)' },
          { offset: 1, color: 'rgba(48,207,208,0.02)' }
        ])
      },
      markLine: {
        silent: true,
        data: data.values && data.values.length > 0 ? [{
          type: 'average',
          name: 'Avg',
          lineStyle: { color: '#f5576c', type: 'dashed' }
        }] : []
      }
    }]
  }
  iopsChartInstance.setOption(option, true)
}

// ========== 请求频率统计 ==========

const fetchRequestFrequency = async () => {
  try {
    const days = freqGranularity.value === 'hourly' ? 1 : 7
    const response = await request.get('/statistics/request-frequency', {
      params: { days, granularity: freqGranularity.value }
    })
    if (response.code === 200 && response.data) {
      renderRequestFreqChart(response.data)
    }
  } catch (error) {
    console.error('获取请求频率统计失败:', error)
  }
}

const renderRequestFreqChart = (data) => {
  if (!requestFreqChart.value) return
  if (!freqChartInstance) {
    freqChartInstance = echarts.init(requestFreqChart.value)
  }

  const labels = data.labels || []
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: { left: '3%', right: '4%', bottom: freqGranularity.value === 'hourly' ? '18%' : '10%', top: '10px', containLabel: true },
    xAxis: {
      type: 'category',
      data: labels,
      axisLabel: { rotate: freqGranularity.value === 'hourly' ? 45 : 0, fontSize: 11 }
    },
    yAxis: { type: 'value', minInterval: 1, name: t('statistics.requestCount') },
    dataZoom: labels.length > 10 ? [
      { type: 'slider', start: 0, end: labels.length <= 24 ? 100 : Math.min(100, (24 / labels.length) * 100), height: 20, bottom: freqGranularity.value === 'hourly' ? 2 : 0 },
      { type: 'inside' }
    ] : [],
    series: [{
      name: t('statistics.requestCount'),
      type: 'bar',
      data: data.values || [],
      itemStyle: {
        borderRadius: [4, 4, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#667eea' }, { offset: 1, color: '#764ba2' }
        ])
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#f093fb' }, { offset: 1, color: '#f5576c' }
          ])
        }
      }
    }]
  }
  freqChartInstance.setOption(option, true)
}

// ========== 来源IP统计 ==========

const fetchSourceIps = async () => {
  try {
    const response = await request.get('/statistics/source-ips', { params: { days: 7 } })
    if (response.code === 200 && response.data) {
      renderSourceIpChart(response.data)
    }
  } catch (error) {
    console.error('获取来源IP统计失败:', error)
  }
}

const renderSourceIpChart = (data) => {
  if (!sourceIpChart.value) return
  if (!ipChartInstance) {
    ipChartInstance = echarts.init(sourceIpChart.value)
  }

  const option = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '10%', bottom: '3%', top: '10px', containLabel: true },
    xAxis: { type: 'value', minInterval: 1, name: t('statistics.requestCount') },
    yAxis: {
      type: 'category',
      data: (data.labels || []).slice(0, 15),
      inverse: true,
      axisLabel: { fontSize: 11 }
    },
    series: [{
      name: t('statistics.requestCount'),
      type: 'bar',
      data: (data.values || []).slice(0, 15),
      itemStyle: {
        borderRadius: [0, 4, 4, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#4facfe' }, { offset: 1, color: '#00f2fe' }
        ])
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#fa709a' }, { offset: 1, color: '#fee140' }
          ])
        }
      }
    }]
  }
  ipChartInstance.setOption(option, true)
}

// ========== 新增趋势统计 ==========

const fetchCreationTrend = async () => {
  try {
    const response = await request.get('/statistics/creation-trend', { params: { days: 30 } })
    if (response.code === 200 && response.data) {
      renderCreationTrendChart(response.data)
    }
  } catch (error) {
    console.error('获取新增趋势统计失败:', error)
  }
}

const renderCreationTrendChart = (data) => {
  if (!creationTrendChart.value) return
  if (!trendChartInstance) {
    trendChartInstance = echarts.init(creationTrendChart.value)
  }

  const labels = data.labels || []
  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: [t('statistics.newProjects'), t('statistics.newApis')], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '18%', top: '10px', containLabel: true },
    xAxis: {
      type: 'category',
      data: labels,
      axisLabel: { rotate: 45, fontSize: 10, formatter: (val) => val.substring(5) }
    },
    yAxis: { type: 'value', minInterval: 1 },
    dataZoom: [
      { type: 'slider', start: Math.max(0, ((labels.length - 14) / labels.length) * 100), end: 100, height: 20, bottom: 2 },
      { type: 'inside' }
    ],
    series: [
      {
        name: t('statistics.newProjects'),
        type: 'line',
        data: data.projectValues || [],
        smooth: true,
        lineStyle: { color: '#667eea', width: 2 },
        itemStyle: { color: '#667eea' },
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(102,126,234,0.4)' }, { offset: 1, color: 'rgba(102,126,234,0.05)' }]) }
      },
      {
        name: t('statistics.newApis'),
        type: 'line',
        data: data.apiValues || [],
        smooth: true,
        lineStyle: { color: '#f5576c', width: 2 },
        itemStyle: { color: '#f5576c' },
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(245,87,108,0.4)' }, { offset: 1, color: 'rgba(245,87,108,0.05)' }]) }
      }
    ]
  }
  trendChartInstance.setOption(option, true)
}

// ========== 生命周期 ==========

const handleResize = () => {
  iopsChartInstance?.resize()
  freqChartInstance?.resize()
  ipChartInstance?.resize()
  trendChartInstance?.resize()
}

const loadAllStats = async () => {
  await Promise.all([fetchIops(), fetchRequestFrequency(), fetchSourceIps(), fetchCreationTrend()])
}

onMounted(async () => {
  await nextTick()
  loadAllStats()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  iopsChartInstance?.dispose()
  freqChartInstance?.dispose()
  ipChartInstance?.dispose()
  trendChartInstance?.dispose()
})
</script>

<style scoped>
.statistics-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.chart-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  width: 100%;
  height: 360px;
}

/* IOPS 概览卡片 */
.iops-card {
  text-align: center;
}
.iops-card-inner {
  padding: 8px 0;
}
.iops-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}
.iops-value {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
}
.iops-unit {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 4px;
}

@media (max-width: 768px) {
  .statistics-page {
    padding: 10px;
  }
  .chart-container {
    height: 280px;
  }
  .iops-value {
    font-size: 24px;
  }
}
</style>
