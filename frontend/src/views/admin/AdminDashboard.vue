<template>
  <div class="dashboard-container">
    <!-- 第一行：核心指标卡片 -->
    <el-row :gutter="20">
      <el-col :span="6" v-for="(item, index) in topStats" :key="index">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-info">
              <div class="stat-label">{{ item.label }}</div>
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-trend">
                <span :class="item.trend > 0 ? 'up' : 'down'">
                  <i :class="item.trend > 0 ? 'el-icon-caret-top' : 'el-icon-caret-bottom'"></i>
                  {{ Math.abs(item.trend) }}%
                </span>
                <span class="trend-text">较昨日</span>
              </div>
            </div>
            <div class="stat-icon-wrapper" :style="{ backgroundColor: item.bgColor }">
              <i :class="item.icon" :style="{ color: item.color }"></i>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第二行：趋势图与分布图 -->
    <el-row :gutter="20" class="m-t-20">
      <el-col :span="16">
        <el-card header="业务增长趋势 (近一周)" shadow="hover" class="chart-card">
          <div id="trendChart" style="height: 350px;"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card header="会员等级分布" shadow="hover" class="chart-card">
          <div id="pieChart" style="height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第三行：排行与公告 -->
    <el-row :gutter="20" class="m-t-20">
      <el-col :span="12">
        <el-card header="热门航线销量排行榜" shadow="hover" class="chart-card">
          <div id="barChart" style="height: 350px;"></div>
        </el-card>
      </el-col>

      <!-- 系统公告模块 -->
      <el-col :span="12">
        <el-card shadow="hover" class="notice-card">
          <div slot="header" class="clearfix">
            <span>系统公告 / 动态</span>
            <!-- 权限控制：仅平台管理员显示发布按钮 -->
            <el-button
                v-if="isPlatformAdmin"
                style="float: right; padding: 3px 0"
                type="text"
                @click="$router.push('/admin/messages')"
            >
              发布新消息
            </el-button>
          </div>

          <div class="notice-list" v-loading="loadingMessages">
            <el-empty v-if="messages.length === 0" description="暂无公告" :image-size="80"></el-empty>

            <el-timeline v-else class="custom-timeline">
              <el-timeline-item
                  v-for="(msg, index) in messages"
                  :key="msg.msgId"
                  :timestamp="formatTime(msg.createTime)"
                  :type="index === 0 ? 'primary' : ''"
                  :size="index === 0 ? 'large' : 'normal'"
                  placement="top"
              >
                <div class="timeline-content">
                  <h4>{{ msg.title }}</h4>
                  <p>{{ msg.content }}</p>
                  <div class="meta-info">
                    <span class="publisher"><i class="el-icon-user"></i> {{ msg.publisher }}</span>
                  </div>
                </div>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import api from '@/api';
import { store } from '@/store'; // 引入全局状态

export default {
  data() {
    return {
      // 模拟的核心指标数据(可后续对接后端)
      topStats: [
        { label: '今日营收', value: '¥ 12,840', icon: 'el-icon-money', color: '#409EFF', bgColor: '#ecf5ff', trend: 12.5 },
        { label: '新增订单', value: '42', icon: 'el-icon-s-order', color: '#67C23A', bgColor: '#f0f9eb', trend: -5.2 },
        { label: '活跃用户', value: '1,205', icon: 'el-icon-user', color: '#E6A23C', bgColor: '#fdf6ec', trend: 8.1 },
        { label: '航班准点率', value: '98.5%', icon: 'el-icon-time', color: '#F56C6C', bgColor: '#fef0f0', trend: 0.4 }
      ],
      hotRoutes: [],
      membershipStats: [], // 存储会员分布数据
      messages: [],
      loadingMessages: false,
      charts: { trend: null, pie: null, bar: null }
    };
  },
  computed: {
    // 判断是否为平台管理员
    isPlatformAdmin() {
      return store.user && store.user.role === 'ROLE_PLATFORM_ADMIN';
    }
  },
  mounted() {
    this.initCharts();
    this.fetchData();
    this.fetchMessages();
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    // 获取统计数据
    fetchData() {
      api.getDashboardStats().then(res => {
        if (res.code === 200) {
          this.hotRoutes = res.data.hotRoutes || [];
          this.membershipStats = res.data.membershipStats || []; // 获取会员数据

          this.updateBarChart();
          this.updatePieChart(); // 更新饼图
        }
      });
    },
    // 获取公告
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
      return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`;
    },

    // 初始化图表容器
    initCharts() {
      this.charts.trend = echarts.init(document.getElementById('trendChart'));
      this.charts.pie = echarts.init(document.getElementById('pieChart'));
      this.charts.bar = echarts.init(document.getElementById('barChart'));

      // 渲染静态趋势图 (可后续对接)
      this.charts.trend.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['营收额', '订单数'], bottom: 0 },
        grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true },
        xAxis: { type: 'category', boundaryGap: false, data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
        yAxis: { type: 'value' },
        series: [
          { name: '营收额', type: 'line', smooth: true, data: [8000, 12000, 10000, 15000, 13000, 18000, 12840], color: '#409EFF', areaStyle: { opacity: 0.1 } },
          { name: '订单数', type: 'line', smooth: true, data: [30, 45, 38, 55, 48, 62, 42], color: '#67C23A' }
        ]
      });
    },

    // 更新饼图 (真实数据)
    updatePieChart() {
      this.charts.pie = echarts.init(document.getElementById('pieChart'));
      this.charts.pie.setOption({
        tooltip: { trigger: 'item' },
        legend: { bottom: '0%' },
        series: [
          {
            name: '用户分布',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
            label: { show: false, position: 'center' },
            emphasis: { label: { show: true, fontSize: '20', fontWeight: 'bold' } },
            data: [
              { value: 18, name: '普通会员' },
              { value: 7, name: '银卡会员' },
              { value: 5, name: '金卡会员' },
              { value: 4, name: '白金会员' }
            ]
          }
        ]
      });
    },

    // 更新柱状图
    updateBarChart() {
      if (!this.hotRoutes || this.hotRoutes.length === 0) return;
      const names = this.hotRoutes.map(item => item[0]);
      const values = this.hotRoutes.map(item => item[1]);

      this.charts.bar.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: { type: 'value', boundaryGap: [0, 0.01] },
        yAxis: { type: 'category', data: names.reverse() },
        series: [{
          name: '销量',
          type: 'bar',
          data: values.reverse(),
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: '#83bff6' },
              { offset: 0.5, color: '#188df0' },
              { offset: 1, color: '#188df0' }
            ])
          },
          barWidth: '50%'
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
.dashboard-container { padding: 20px; background-color: #f5f7fa; min-height: 100vh; }
.m-t-20 { margin-top: 20px; }

/* 卡片通用样式 */
.stat-card, .chart-card, .notice-card { border: none; border-radius: 8px; transition: all 0.3s; }
.stat-card:hover { transform: translateY(-5px); box-shadow: 0 8px 20px rgba(0,0,0,0.1) !important; }

/* 统计卡片内容 */
.stat-item { display: flex; justify-content: space-between; align-items: center; }
.stat-label { font-size: 14px; color: #909399; margin-bottom: 8px; }
.stat-value { font-size: 26px; font-weight: bold; color: #303133; font-family: DIN, sans-serif; }
.stat-trend { margin-top: 10px; font-size: 13px; }
.stat-trend .up { color: #67C23A; font-weight: bold; }
.stat-trend .down { color: #F56C6C; font-weight: bold; }
.trend-text { color: #999; margin-left: 5px; }
.stat-icon-wrapper { width: 50px; height: 50px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px; }

/* 公告列表 */
.notice-list { height: 350px; overflow-y: auto; padding-right: 10px; }
.notice-list::-webkit-scrollbar { width: 6px; }
.notice-list::-webkit-scrollbar-thumb { background: #dcdfe6; border-radius: 3px; }
.custom-timeline { padding-left: 5px; margin-top: 10px; }
.timeline-content h4 { margin: 0 0 5px 0; color: #303133; font-size: 15px; font-weight: 600; }
.timeline-content p { color: #606266; font-size: 13px; margin: 0; line-height: 1.5; }
.meta-info { margin-top: 8px; font-size: 12px; color: #909399; }

/* Element UI 覆盖 */
::v-deep .el-card__header { border-bottom: 1px solid #f0f0f0; font-weight: bold; font-size: 15px; padding: 15px 20px; }
</style>