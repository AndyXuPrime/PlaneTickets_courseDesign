<template>
  <div class="dashboard-container structural-theme">
    <!-- 顶部装饰条 -->
    <div class="top-bar-wrapper">
      <div class="top-bar-struct">
        <div class="brand-block">
          <div class="logo-square"></div>
          <span class="brand-text">ADMIN_CONSOLE // V3.0</span>
        </div>
        <div class="status-block">
          <span class="indicator-light"></span>
          <span>SYSTEM ONLINE</span>
        </div>
      </div>
    </div>

    <!-- 第一行：核心指标卡片 -->
    <el-row :gutter="24">
      <el-col :span="6" v-for="(item, index) in topStats" :key="index">
        <div class="struct-card stat-card">
          <!-- 装饰：顶部连接线 -->
          <div class="connector-line"></div>
          <!-- 装饰：四角螺丝钉 -->
          <div class="screw top-left"></div>
          <div class="screw top-right"></div>
          <div class="screw bottom-left"></div>
          <div class="screw bottom-right"></div>

          <div class="stat-content">
            <div class="stat-header">
              <span class="stat-label">{{ item.label.toUpperCase() }}</span>
              <div class="icon-square" :style="{ color: item.color, borderColor: '#1a1a1a' }">
                <i :class="item.icon"></i>
              </div>
            </div>
            <div class="stat-body">
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-footer">
                <span class="trend-tag" :class="item.trend > 0 ? 'up' : 'down'">
                  <i :class="item.trend > 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
                  {{ Math.abs(item.trend) }}%
                </span>
                <span class="trend-label">VS YESTERDAY</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 第二行：趋势图与分布图 -->
    <el-row :gutter="24" class="m-t-24">
      <el-col :span="16">
        <div class="struct-card">
          <div class="panel-header">
            <div class="header-left">
              <span class="block-mark"></span>
              <span class="title-text">业务增长趋势 / GROWTH_TREND</span>
            </div>
            <div class="header-right">
              <i class="el-icon-data-line" style="font-size: 18px;"></i>
            </div>
          </div>
          <div class="panel-body">
            <div id="trendChart" style="height: 350px;"></div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="struct-card">
          <div class="panel-header">
            <div class="header-left">
              <span class="block-mark"></span>
              <span class="title-text">会员分布 / USER_DIST</span>
            </div>
          </div>
          <div class="panel-body">
            <div id="pieChart" style="height: 350px;"></div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 第三行：排行与公告 -->
    <el-row :gutter="24" class="m-t-24">
      <el-col :span="12">
        <div class="struct-card">
          <div class="panel-header">
            <div class="header-left">
              <span class="block-mark"></span>
              <span class="title-text">热门航线 / TOP_ROUTES</span>
            </div>
          </div>
          <div class="panel-body">
            <div id="barChart" style="height: 350px;"></div>
          </div>
        </div>
      </el-col>

      <el-col :span="12">
        <div class="struct-card">
          <div class="panel-header">
            <div class="header-left">
              <span class="block-mark"></span>
              <span class="title-text">系统公告 / SYSTEM_LOGS</span>
            </div>
            <el-button
                v-if="isPlatformAdmin"
                class="struct-btn"
                size="mini"
                icon="el-icon-plus"
                @click="$router.push('/admin/messages')"
            >
              发布消息
            </el-button>
          </div>

          <div class="notice-container" v-loading="loadingMessages">
            <el-empty v-if="messages.length === 0" description="NO DATA" :image-size="60"></el-empty>

            <div v-else class="struct-timeline">
              <div v-for="msg in messages" :key="msg.msgId" class="timeline-row">
                <div class="time-box">
                  <div class="date">{{ formatTime(msg.createTime).split(' ')[0] }}</div>
                  <div class="time">{{ formatTime(msg.createTime).split(' ')[1] }}</div>
                </div>
                <div class="content-box">
                  <div class="msg-title">{{ msg.title }}</div>
                  <div class="msg-desc">{{ msg.content }}</div>
                  <div class="msg-meta">
                    <span>BY: {{ msg.publisher }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import api from '@/api';
import { store } from '@/store';

export default {
  data() {
    return {
      // 核心指标数据
      topStats: [
        { label: 'Revenue', value: '¥ 12,840', icon: 'el-icon-money', color: '#005f87', trend: 12.5 }, // 深蓝
        { label: 'New Orders', value: '42', icon: 'el-icon-s-order', color: '#2e7d32', trend: -5.2 }, // 深绿
        { label: 'Active Users', value: '1,205', icon: 'el-icon-user', color: '#ef6c00', trend: 8.1 }, // 深橙
        { label: 'On-Time Rate', value: '98.5%', icon: 'el-icon-time', color: '#c62828', trend: 0.4 } // 深红
      ],
      hotRoutes: [],
      membershipStats: [],
      messages: [],
      loadingMessages: false,
      charts: { trend: null, pie: null, bar: null }
    };
  },
  computed: {
    isPlatformAdmin() {
      return store.user && store.user.role === 'ROLE_PLATFORM_ADMIN';
    }
  },
  mounted() {
    // 确保 DOM 渲染完成后再初始化图表
    this.$nextTick(() => {
      this.initCharts();
      this.fetchData();
      this.fetchMessages();
    });
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
    // 销毁图表实例防止内存泄漏
    Object.values(this.charts).forEach(chart => chart && chart.dispose());
  },
  methods: {
    fetchData() {
      api.getDashboardStats().then(res => {
        if (res.code === 200) {
          this.hotRoutes = res.data.hotRoutes || [];
          this.membershipStats = res.data.membershipStats || [];
          this.updateBarChart();
          this.updatePieChart();
        }
      });
    },
    async fetchMessages() {
      this.loadingMessages = true;
      try {
        const res = await api.getSystemMessages();
        if (res.code === 200) {
          this.messages = res.data.slice(0, 5);
        }
      } catch (e) {
        console.error(e);
      } finally {
        this.loadingMessages = false;
      }
    },
    formatTime(time) {
      if (!time) return '';
      const date = new Date(time);
      return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`;
    },

    // --- 图表配置区域 ---

    initCharts() {
      this.charts.trend = echarts.init(document.getElementById('trendChart'));
      this.charts.pie = echarts.init(document.getElementById('pieChart'));
      this.charts.bar = echarts.init(document.getElementById('barChart'));

      // 1. 趋势图：双 Y 轴优化 + 结构主义配色
      this.charts.trend.setOption({
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'axis',
          backgroundColor: '#fff',
          borderColor: '#1a1a1a',
          borderWidth: 2,
          textStyle: { color: '#000', fontFamily: 'Consolas' },
          axisPointer: { type: 'cross', label: { backgroundColor: '#1a1a1a' } }
        },
        legend: {
          data: ['营收额', '订单数'],
          top: 0,
          right: 10,
          textStyle: { color: '#1a1a1a', fontWeight: 'bold' }
        },
        grid: {
          left: '2%', right: '2%', bottom: '5%', top: '15%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN'],
          axisLine: { lineStyle: { color: '#1a1a1a', width: 2 } },
          axisLabel: { color: '#000', fontFamily: 'Consolas', fontWeight: 'bold' },
          axisTick: { show: true, alignWithLabel: true }
        },
        yAxis: [
          // 左轴：营收
          {
            type: 'value',
            name: 'REVENUE (¥)',
            nameTextStyle: { fontWeight: 'bold', align: 'left', padding: [0, 0, 0, 10] },
            position: 'left',
            splitLine: { lineStyle: { color: '#e0e0e0', type: 'dashed' } },
            axisLine: { show: true, lineStyle: { color: '#005f87' } },
            axisLabel: { color: '#005f87', fontFamily: 'Consolas', fontWeight: 'bold' }
          },
          // 右轴：订单
          {
            type: 'value',
            name: 'ORDERS',
            nameTextStyle: { fontWeight: 'bold', align: 'right', padding: [0, 10, 0, 0] },
            position: 'right',
            splitLine: { show: false },
            axisLine: { show: true, lineStyle: { color: '#2e7d32' } },
            axisLabel: { color: '#2e7d32', fontFamily: 'Consolas', fontWeight: 'bold' }
          }
        ],
        series: [
          {
            name: '营收额', type: 'line', smooth: false,
            yAxisIndex: 0,
            symbol: 'square', symbolSize: 8,
            data: [8000, 12000, 10000, 15000, 13000, 18000, 12840],
            color: '#005f87',
            lineStyle: { width: 3 },
            areaStyle: { opacity: 0.15, color: '#005f87' }
          },
          {
            name: '订单数', type: 'line', smooth: false,
            yAxisIndex: 1,
            symbol: 'circle', symbolSize: 8,
            data: [30, 45, 38, 55, 48, 62, 42],
            color: '#2e7d32',
            lineStyle: { type: 'dashed', width: 2 }
          }
        ]
      });
    },

    updatePieChart() {
      // 2. 饼图：缩小半径，修复标签截断
      this.charts.pie.setOption({
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'item',
          backgroundColor: '#fff',
          borderColor: '#1a1a1a',
          borderWidth: 2,
          textStyle: { color: '#000' },
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          bottom: '0%',
          icon: 'rect', // 方形图例
          itemWidth: 12, itemHeight: 12,
          textStyle: { color: '#333', fontSize: 11, fontWeight: 'bold' }
        },
        series: [
          {
            name: '用户分布',
            type: 'pie',
            // 关键修复：缩小半径，留出外部标签空间
            radius: ['35%', '55%'],
            center: ['50%', '45%'],
            avoidLabelOverlap: true,
            itemStyle: {
              borderRadius: 0,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: true,
              formatter: '{b}\n{c}人', // 换行显示
              color: '#1a1a1a',
              fontSize: 12,
              fontWeight: 'bold',
              lineHeight: 16
            },
            labelLine: {
              show: true,
              length: 15,
              length2: 15,
              lineStyle: { color: '#666' }
            },
            emphasis: {
              label: { show: true, fontSize: '14', fontWeight: 'bold' },
              scale: true,
              scaleSize: 5
            },
            data: [
              { value: 18, name: '普通会员', itemStyle: { color: '#b3e5fc' } },
              { value: 7, name: '银卡会员', itemStyle: { color: '#4fc3f7' } },
              { value: 5, name: '金卡会员', itemStyle: { color: '#0288d1' } },
              { value: 4, name: '白金会员', itemStyle: { color: '#01579b' } }
            ]
          }
        ]
      });
    },

    updateBarChart() {
      if (!this.hotRoutes || this.hotRoutes.length === 0) return;
      const names = this.hotRoutes.map(item => item[0]);
      const values = this.hotRoutes.map(item => item[1]);

      this.charts.bar.setOption({
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          backgroundColor: '#fff',
          borderColor: '#1a1a1a',
          borderWidth: 2
        },
        grid: {
          left: '3%', right: '6%', bottom: '3%', top: '5%',
          containLabel: true
        },
        xAxis: {
          type: 'value',
          boundaryGap: [0, 0.01],
          splitLine: { lineStyle: { color: '#e0e0e0', type: 'dashed' } },
          axisLabel: { color: '#666' },
          axisLine: { show: true, lineStyle: { color: '#1a1a1a' } }
        },
        yAxis: {
          type: 'category',
          data: names.reverse(),
          axisLabel: { color: '#000', fontFamily: 'Consolas', fontWeight: 'bold' },
          axisTick: { show: false },
          axisLine: { show: true, lineStyle: { color: '#1a1a1a' } }
        },
        series: [{
          name: '销量',
          type: 'bar',
          data: values.reverse(),
          itemStyle: {
            color: '#005f87',
            borderRadius: 0,
            borderWidth: 1,
            borderColor: '#1a1a1a' // 增加描边
          },
          barWidth: '50%',
          label: {
            show: true,
            position: 'right',
            color: '#000',
            fontFamily: 'Consolas',
            fontWeight: 'bold'
          }
        }]
      });
    },
    handleResize() {
      Object.values(this.charts).forEach(chart => chart && chart.resize());
    }
  }
};
</script>

<style scoped>
/* ========== 结构主义主题 (Structural Theme) ========== */
.structural-theme {
  padding: 24px;
  background-color: #f2f4f6; /* 冷灰背景 */
  min-height: 100vh;
  font-family: 'Helvetica Neue', 'Roboto', Arial, sans-serif;
  color: #1a1a1a;
  /* 极淡的网格背景，增加工程感 */
  background-image:
      linear-gradient(#e6e8eb 1px, transparent 1px),
      linear-gradient(90deg, #e6e8eb 1px, transparent 1px);
  background-size: 20px 20px;
}

.m-t-24 { margin-top: 24px; }

/* 顶部栏 */
.top-bar-wrapper {
  margin-bottom: 24px;
}
.top-bar-struct {
  background: #fff;
  border: 2px solid #1a1a1a; /* 粗黑边框 */
  padding: 15px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 4px 4px 0 rgba(0,0,0,0.15); /* 实色阴影 */
}
.brand-block {
  display: flex;
  align-items: center;
  gap: 12px;
}
.logo-square {
  width: 24px; height: 24px;
  background: #005f87;
  border: 2px solid #1a1a1a;
}
.brand-text {
  font-family: 'Impact', 'Arial Black', sans-serif;
  font-size: 20px;
  letter-spacing: 1px;
  color: #1a1a1a;
}
.status-block {
  font-family: 'Consolas', monospace;
  font-weight: bold;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f0f0f0;
  padding: 4px 10px;
  border: 1px solid #1a1a1a;
}
.indicator-light {
  width: 10px; height: 10px;
  background-color: #67C23A;
  border-radius: 50%;
  border: 1px solid #1a1a1a;
  box-shadow: 0 0 4px #67C23A;
}

/* 通用结构卡片 */
.struct-card {
  background: #fff;
  border: 2px solid #1a1a1a; /* 核心：粗边框 */
  height: 100%;
  position: relative;
  transition: transform 0.2s;
}
.struct-card:hover {
  transform: translate(-2px, -2px);
  box-shadow: 6px 6px 0 rgba(0, 95, 135, 0.2); /* 悬停效果 */
}

/* 装饰：螺丝钉 */
.screw {
  position: absolute;
  width: 6px; height: 6px;
  border-radius: 50%;
  border: 1px solid #666;
  background: #ddd;
  z-index: 2;
}
.screw::after { content: ''; position: absolute; top: 2px; left: 0; width: 4px; height: 1px; background: #666; transform: rotate(45deg); }
.top-left { top: 6px; left: 6px; }
.top-right { top: 6px; right: 6px; }
.bottom-left { bottom: 6px; left: 6px; }
.bottom-right { bottom: 6px; right: 6px; }

/* 统计卡片 */
.stat-card {
  padding: 0;
  overflow: hidden;
}
.connector-line {
  height: 6px;
  background: #1a1a1a;
  width: 100%;
  border-bottom: 1px solid #fff;
}
.stat-content {
  padding: 24px 20px 20px;
}
.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}
.stat-label {
  font-size: 13px;
  font-weight: 800;
  color: #666;
  letter-spacing: 0.5px;
}
.icon-square {
  width: 42px; height: 42px;
  border: 2px solid;
  display: flex; align-items: center; justify-content: center;
  font-size: 22px;
  background: #f9f9f9;
  box-shadow: 2px 2px 0 rgba(0,0,0,0.1);
}
.stat-value {
  font-size: 30px;
  font-weight: 900; /* 极粗字体 */
  color: #1a1a1a;
  font-family: 'Arial', sans-serif;
  letter-spacing: -1px;
  line-height: 1.1;
  margin-bottom: 12px;
}
.stat-footer {
  display: flex;
  align-items: center;
  font-size: 12px;
  font-family: 'Consolas', monospace;
}
.trend-tag {
  border: 2px solid #1a1a1a;
  padding: 2px 6px;
  font-weight: bold;
  margin-right: 8px;
  background: #fff;
  box-shadow: 2px 2px 0 rgba(0,0,0,0.1);
}
.trend-tag.up { color: #2e7d32; }
.trend-tag.down { color: #c62828; }
.trend-label { color: #1a1a1a; font-weight: 700; text-transform: uppercase; font-size: 10px; }

/* 面板头部 */
.panel-header {
  background: #f0f2f5;
  border-bottom: 2px solid #1a1a1a;
  padding: 12px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.block-mark {
  width: 14px; height: 14px;
  background: #005f87;
  border: 2px solid #1a1a1a;
}
.title-text {
  font-weight: 900;
  font-size: 15px;
  letter-spacing: 0.5px;
  color: #1a1a1a;
  text-transform: uppercase;
}
.panel-body {
  padding: 20px;
  background: #fff;
}

/* 公告列表 (Timeline) */
.notice-container {
  height: 350px;
  overflow-y: auto;
  padding: 10px 20px;
}
/* 滚动条 */
.notice-container::-webkit-scrollbar { width: 8px; }
.notice-container::-webkit-scrollbar-thumb { background: #1a1a1a; border: 1px solid #fff; }
.notice-container::-webkit-scrollbar-track { background: #f0f0f0; }

.struct-timeline {
  display: flex;
  flex-direction: column;
}
.timeline-row {
  display: flex;
  border-bottom: 1px dashed #999;
  padding: 16px 0;
}
.timeline-row:last-child { border-bottom: none; }

.time-box {
  width: 90px;
  font-family: 'Consolas', monospace;
  text-align: right;
  padding-right: 15px;
  border-right: 3px solid #005f87;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.time-box .date { font-weight: 900; color: #1a1a1a; font-size: 14px; }
.time-box .time { color: #555; font-size: 12px; font-weight: bold; }

.content-box {
  padding-left: 15px;
  flex: 1;
}
.msg-title {
  font-weight: 800;
  font-size: 15px;
  color: #005f87;
  margin-bottom: 6px;
}
.msg-desc {
  font-size: 13px;
  color: #1a1a1a;
  line-height: 1.5;
  margin-bottom: 8px;
  font-weight: 500;
}
.msg-meta {
  font-size: 11px;
  font-weight: 800;
  color: #1a1a1a;
  text-transform: uppercase;
  background: #e0e0e0;
  display: inline-block;
  padding: 3px 6px;
  border: 1px solid #1a1a1a;
}

/* 按钮 */
.struct-btn {
  background: #fff;
  border: 2px solid #1a1a1a;
  color: #1a1a1a;
  font-weight: 800;
  border-radius: 0;
  box-shadow: 3px 3px 0 #1a1a1a;
  transition: all 0.1s;
}
.struct-btn:hover {
  transform: translate(1px, 1px);
  box-shadow: 2px 2px 0 #1a1a1a;
  background: #005f87;
  color: #fff;
  border-color: #1a1a1a;
}
.struct-btn:active {
  transform: translate(3px, 3px);
  box-shadow: none;
}
</style>