<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6" v-for="(item, index) in topStats" :key="index">
        <el-card shadow="hover" :body-style="{ padding: '20px' }">
          <div class="stat-item">
            <div class="stat-icon" :style="{ color: item.color }">
              <i :class="item.icon"></i>
            </div>
            <div class="stat-info">
              <div class="stat-label">{{ item.label }}</div>
              <div class="stat-value">{{ item.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card header="热门航线销量 TOP 5">
          <el-table :data="hotRoutes" height="300">
            <el-table-column prop="0" label="航班号"></el-table-column>
            <el-table-column label="销售热度">
              <template slot-scope="scope">
                <!-- 【修复点】使用了 scope.row[1] 来计算百分比，解决了 ESLint 报错 -->
                <el-progress
                    :percentage="Math.min(scope.row[1] * 10, 100)"
                    :status="scope.row[1] > 5 ? 'exception' : 'success'">
                </el-progress>
              </template>
            </el-table-column>
            <el-table-column prop="1" label="销量" width="100"></el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="系统公告 / 提醒">
          <el-timeline>
            <el-timeline-item timestamp="2025/01/01" color="#0bbd87">系统成功重构为微服务架构</el-timeline-item>
            <el-timeline-item timestamp="2025/01/02">新增 3 名航司管理员待审核</el-timeline-item>
            <el-timeline-item timestamp="2025/01/03">今日预计客流量上涨 20%</el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import api from '@/api';
export default {
  data() {
    return {
      topStats: [
        { label: '今日营收', value: '¥ 12,840', icon: 'el-icon-money', color: '#409EFF' },
        { label: '新增订单', value: '42', icon: 'el-icon-s-order', color: '#67C23A' },
        { label: '活跃用户', value: '1,205', icon: 'el-icon-user', color: '#E6A23C' },
        { label: '航班准点率', value: '98.5%', icon: 'el-icon-time', color: '#F56C6C' }
      ],
      hotRoutes: []
    };
  },
  created() {
    api.getDashboardStats().then(res => {
      if (res.code === 200) this.hotRoutes = res.data.hotRoutes;
    });
  }
};
</script>

<style scoped>
.stat-item { display: flex; align-items: center; }
.stat-icon { font-size: 40px; margin-right: 20px; }
.stat-label { font-size: 14px; color: #909399; }
.stat-value { font-size: 24px; font-weight: bold; color: #303133; margin-top: 5px; }
</style>