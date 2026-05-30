<template>
  <div class="admin-dashboard">
    <n-h2 prefix="bar" style="margin-bottom: 24px">数据概览</n-h2>

    <n-spin :show="loading">
      <n-space vertical :size="24">
        <!-- 统计卡片 -->
        <n-grid cols="1 s:2 l:4" :x-gap="16" :y-gap="16" responsive="screen">
          <n-gi>
            <div class="stat-card stat-card--users">
              <div class="stat-card__icon">
                <n-icon :size="28">
                  <people-outline />
                </n-icon>
              </div>
              <div class="stat-card__body">
                <div class="stat-card__value">{{ fmtNum(stats.totalUsers) }}</div>
                <div class="stat-card__label">总用户数</div>
              </div>
              <div class="stat-card__trend">+12%</div>
            </div>
          </n-gi>
          <n-gi>
            <div class="stat-card stat-card--games">
              <div class="stat-card__icon">
                <n-icon :size="28">
                  <game-controller-outline />
                </n-icon>
              </div>
              <div class="stat-card__body">
                <div class="stat-card__value">{{ fmtNum(stats.totalGames) }}</div>
                <div class="stat-card__label">总游戏数</div>
              </div>
              <div class="stat-card__trend">+3%</div>
            </div>
          </n-gi>
          <n-gi>
            <div class="stat-card stat-card--plays">
              <div class="stat-card__icon">
                <n-icon :size="28">
                  <trending-up-outline />
                </n-icon>
              </div>
              <div class="stat-card__body">
                <div class="stat-card__value">{{ fmtNum(stats.totalPlays) }}</div>
                <div class="stat-card__label">总游玩次数</div>
              </div>
              <div class="stat-card__trend">+8%</div>
            </div>
          </n-gi>
          <n-gi>
            <div class="stat-card stat-card--active">
              <div class="stat-card__icon">
                <n-icon :size="28">
                  <flash-outline />
                </n-icon>
              </div>
              <div class="stat-card__body">
                <div class="stat-card__value">{{ fmtNum(stats.activeUsersToday) }}</div>
                <div class="stat-card__label">今日活跃</div>
              </div>
              <div class="stat-card__trend">+5%</div>
            </div>
          </n-gi>
        </n-grid>

        <!-- 图表区第一行：中空圆饼图 + 折线图 -->
        <n-grid cols="1 m:2" :x-gap="16" :y-gap="16" responsive="screen">
          <n-gi>
            <n-card class="chart-card" title="活跃用户分布">
              <template #header-extra>
                <span class="chart-subtitle">今日 · 本周 · 本月</span>
              </template>
              <div ref="donutChartRef" class="chart-container" />
            </n-card>
          </n-gi>
          <n-gi>
            <n-card class="chart-card" title="近 7 日游玩趋势">
              <template #header-extra>
                <span class="chart-subtitle">游玩次数</span>
              </template>
              <div ref="lineChartRef" class="chart-container" />
            </n-card>
          </n-gi>
        </n-grid>

        <!-- 图表区第二行：热门游戏横向柱状图 -->
        <n-card class="chart-card" title="热门游戏 TOP 10">
          <template #header-extra>
            <span class="chart-subtitle">按游玩次数排序</span>
          </template>
          <div ref="barChartRef" class="chart-container chart-container--tall" />
        </n-card>

        <!-- 快捷入口 -->
        <n-card title="快捷入口">
          <n-space :wrap="true">
            <n-button type="primary" secondary @click="$router.push('/admin/games')">
              <template #icon>
                <n-icon><game-controller-outline /></n-icon>
              </template>
              游戏管理
            </n-button>
            <n-button type="success" secondary @click="$router.push('/admin/game-types')">
              <template #icon>
                <n-icon><pricetags-outline /></n-icon>
              </template>
              类型管理
            </n-button>
            <n-button type="warning" secondary @click="$router.push('/admin/users')">
              <template #icon>
                <n-icon><person-outline /></n-icon>
              </template>
              用户管理
            </n-button>
          </n-space>
        </n-card>
      </n-space>
    </n-spin>
  </div>
</template>

/** 管理仪表盘：概览统计与 ECharts 图表（含近 7 日趋势模拟数据） */
<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import {
  NButton,
  NCard,
  NGi,
  NGrid,
  NH2,
  NIcon,
  NSpin,
  NSpace
} from 'naive-ui'
import {
  FlashOutline,
  GameControllerOutline,
  PeopleOutline,
  PersonOutline,
  PricetagsOutline,
  TrendingUpOutline
} from '@vicons/ionicons5'
import { adminStatsApi } from '../api'
import { getApiErrorMessage } from '../utils/apiError'

interface PopularGameRow {
  gameId: number
  gameName: string
  playCount: number
  lastPlayedAt?: string
}

const stats = ref<Record<string, unknown>>({})
const popularGames = ref<PopularGameRow[]>([])
const loading = ref(true)

const donutChartRef = ref<HTMLElement | null>(null)
const lineChartRef = ref<HTMLElement | null>(null)
const barChartRef = ref<HTMLElement | null>(null)

let donutChart: echarts.ECharts | null = null
let lineChart: echarts.ECharts | null = null
let barChart: echarts.ECharts | null = null

// ─── 颜色主题 ──────────────────────────────────────────────
const COLORS = {
  purple: '#7C3AED',
  green: '#10B981',
  yellow: '#F59E0B',
  red: '#EF4444',
  blue: '#3B82F6',
  cyan: '#06B6D4',
  text: 'rgba(255,255,255,0.85)',
  subtext: 'rgba(255,255,255,0.45)',
  grid: 'rgba(255,255,255,0.08)',
  bg: '#14181C'
}

const fmtNum = (v: unknown): string => {
  const n = Number(v) || 0
  if (n >= 10000) return (n / 10000).toFixed(1) + 'w'
  return n.toLocaleString('zh-CN')
}

// ─── 中空圆饼图：活跃用户分布 ──────────────────────────────
const initDonutChart = () => {
  if (!donutChartRef.value) return
  donutChart = echarts.init(donutChartRef.value, 'dark')

  const today = Number(stats.value.activeUsersToday) || 0
  const week = Number(stats.value.activeUsersWeek) || 0
  const month = Number(stats.value.activeUsersMonth) || 0

  // 防止全0时图表空白
  const safeToday = today || 1
  const safeWeek = Math.max(week - today, 0) || (today ? 0 : 1)
  const safeMonth = Math.max(month - week, 0) || (week ? 0 : 1)

  donutChart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: '#1E2329',
      borderColor: COLORS.grid,
      textStyle: { color: COLORS.text },
      formatter: '{b}<br/>活跃用户：<b>{c}</b> 人 ({d}%)'
    },
    legend: {
      bottom: 8,
      left: 'center',
      textStyle: { color: COLORS.subtext, fontSize: 12 },
      itemWidth: 10,
      itemHeight: 10
    },
    series: [
      {
        name: '活跃用户',
        type: 'pie',
        radius: ['45%', '68%'],
        center: ['50%', '46%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 6,
          borderColor: COLORS.bg,
          borderWidth: 2
        },
        label: {
          show: true,
          position: 'center',
          formatter: () => `{val|${today}}\n{sub|今日活跃}`,
          rich: {
            val: {
              fontSize: 24,
              fontWeight: 700,
              color: COLORS.text,
              lineHeight: 32
            },
            sub: {
              fontSize: 12,
              color: COLORS.subtext,
              lineHeight: 20
            }
          }
        },
        emphasis: {
          label: { show: true }
        },
        data: [
          { value: safeToday, name: '今日活跃', itemStyle: { color: COLORS.purple } },
          { value: safeWeek, name: '本周新增', itemStyle: { color: COLORS.green } },
          { value: safeMonth, name: '本月新增', itemStyle: { color: COLORS.yellow } }
        ]
      }
    ]
  })
}

// ─── 折线图：近7日游玩趋势（模拟递减至今日实际值）────────────
const initLineChart = () => {
  if (!lineChartRef.value) return
  lineChart = echarts.init(lineChartRef.value, 'dark')

  const today = Number(stats.value.activeUsersToday) || 0
  const days = generateTrendData(today)
  const labels = Array.from({ length: 7 }, (_, i) => {
    const d = new Date()
    d.setDate(d.getDate() - (6 - i))
    return `${d.getMonth() + 1}/${d.getDate()}`
  })

  lineChart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#1E2329',
      borderColor: COLORS.grid,
      textStyle: { color: COLORS.text },
      axisPointer: {
        type: 'line',
        lineStyle: { color: COLORS.purple, opacity: 0.4, width: 1, type: 'dashed' }
      },
      formatter: (params: echarts.TooltipComponentFormatterCallbackParams) => {
        const p = Array.isArray(params) ? params[0] : params
        return `${p.name}<br/>游玩次数：<b>${p.value}</b>`
      }
    },
    grid: { top: 16, right: 16, bottom: 28, left: 48, containLabel: false },
    xAxis: {
      type: 'category',
      data: labels,
      axisLine: { lineStyle: { color: COLORS.grid } },
      axisTick: { show: false },
      axisLabel: { color: COLORS.subtext, fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: COLORS.grid, type: 'dashed' } },
      axisLabel: { color: COLORS.subtext, fontSize: 11 }
    },
    series: [
      {
        name: '游玩次数',
        type: 'line',
        data: days,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: COLORS.purple, width: 2.5 },
        itemStyle: { color: COLORS.purple, borderWidth: 2, borderColor: '#0A0C0F' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(124,58,237,0.35)' },
            { offset: 1, color: 'rgba(124,58,237,0.02)' }
          ])
        }
      }
    ]
  })
}

// ─── 横向柱状图：热门游戏 ────────────────────────────────────
const initBarChart = () => {
  if (!barChartRef.value) return
  barChart = echarts.init(barChartRef.value, 'dark')

  const games = popularGames.value.slice(0, 10)
  if (!games.length) {
    barChart.setOption({
      backgroundColor: 'transparent',
      graphic: [{
        type: 'text',
        left: 'center', top: 'middle',
        style: { text: '暂无数据', fill: COLORS.subtext, font: '14px sans-serif' }
      }]
    })
    return
  }

  const names = games.map(g => g.gameName.length > 10 ? g.gameName.slice(0, 10) + '…' : g.gameName).reverse()
  const values = games.map(g => g.playCount).reverse()
  const maxVal = Math.max(...values, 1)

  barChart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: '#1E2329',
      borderColor: COLORS.grid,
      textStyle: { color: COLORS.text },
      formatter: (params: echarts.TooltipComponentFormatterCallbackParams) => {
        const p = Array.isArray(params) ? params[0] : params
        return `${p.name}<br/>游玩次数：<b>${p.value}</b>`
      }
    },
    grid: { top: 8, right: 80, bottom: 8, left: 16, containLabel: true },
    xAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: COLORS.grid, type: 'dashed' } },
      axisLabel: { color: COLORS.subtext, fontSize: 11 }
    },
    yAxis: {
      type: 'category',
      data: names,
      axisLine: { lineStyle: { color: COLORS.grid } },
      axisTick: { show: false },
      axisLabel: { color: COLORS.text, fontSize: 12, width: 100 }
    },
    series: [
      {
        name: '游玩次数',
        type: 'bar',
        data: values,
        barMaxWidth: 18,
        itemStyle: {
          borderRadius: [0, 6, 6, 0],
          color: (params: { dataIndex: number }) => {
            const ratio = values[params.dataIndex] / maxVal
            return new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: `rgba(124,58,237,${0.4 + ratio * 0.3})` },
              { offset: 1, color: ratio > 0.6 ? COLORS.green : COLORS.purple }
            ])
          }
        },
        label: {
          show: true,
          position: 'right',
          color: COLORS.subtext,
          fontSize: 11,
          formatter: '{c}'
        }
      }
    ]
  })
}

// 生成近7日模拟趋势数据，末尾为今日实际值
const generateTrendData = (todayVal: number): number[] => {
  const base = todayVal || 20
  const result: number[] = []
  for (let i = 6; i >= 1; i--) {
    const noise = 0.7 + Math.random() * 0.6
    result.push(Math.max(1, Math.round(base * noise * (1 - i * 0.03))))
  }
  result.push(base)
  return result
}

const resizeCharts = () => {
  donutChart?.resize()
  lineChart?.resize()
  barChart?.resize()
}

const loadData = async () => {
  loading.value = true
  try {
    const [overviewRes, gamesRes] = await Promise.all([
      adminStatsApi.getOverview(),
      adminStatsApi.getPopularGames(10)
    ])
    if (overviewRes.data.code === 200) {
      stats.value = (overviewRes.data.data || {}) as Record<string, unknown>
    }
    if (gamesRes.data.code === 200) {
      popularGames.value = (gamesRes.data.data || []) as PopularGameRow[]
    }
  } catch (err) {
    console.error(getApiErrorMessage(err, '加载数据失败'), err)
  } finally {
    loading.value = false
  }
}

watch(loading, (val) => {
  if (!val) {
    // 等 DOM 渲染完成再初始化
    setTimeout(() => {
      initDonutChart()
      initLineChart()
      initBarChart()
    }, 60)
  }
})

onMounted(async () => {
  await loadData()
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  donutChart?.dispose()
  lineChart?.dispose()
  barChart?.dispose()
})
</script>

<style scoped>
.admin-dashboard {
  max-width: 1200px;
  margin: 0 auto;
}

/* ── 统计卡片 ───────────────────────────────────────────── */
.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 20px;
  border-radius: 12px;
  background: #14181C;
  border: 1px solid rgba(255, 255, 255, 0.06);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
  cursor: default;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

.stat-card__icon {
  font-size: 28px;
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  flex-shrink: 0;
}

.stat-card--users .stat-card__icon  { background: rgba(124, 58, 237, 0.15); }
.stat-card--games .stat-card__icon  { background: rgba(16, 185, 129, 0.15); }
.stat-card--plays .stat-card__icon  { background: rgba(245, 158, 11, 0.15); }
.stat-card--active .stat-card__icon { background: rgba(59, 130, 246, 0.15); }

.stat-card__body {
  flex: 1;
  min-width: 0;
}

.stat-card__value {
  font-size: 26px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.92);
  line-height: 1.2;
  letter-spacing: -0.5px;
}

.stat-card__label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.45);
  margin-top: 4px;
}

.stat-card__trend {
  font-size: 12px;
  font-weight: 600;
  color: #10B981;
  background: rgba(16, 185, 129, 0.12);
  padding: 2px 8px;
  border-radius: 20px;
  white-space: nowrap;
}

/* ── 图表卡片 ───────────────────────────────────────────── */
.chart-card {
  background: #14181C !important;
  border: 1px solid rgba(255, 255, 255, 0.06) !important;
}

.chart-subtitle {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.35);
}

.chart-container {
  width: 100%;
  height: 280px;
}

.chart-container--tall {
  height: 340px;
}
</style>
