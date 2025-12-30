<template>
  <div class="my-orders-container">
    <!-- 1. 用户信息概览 -->
    <div class="user-header" v-if="currentUser">
      <div class="user-info">
        <el-avatar :size="60" icon="el-icon-user-solid" class="avatar"></el-avatar>
        <div class="info-text">
          <h2>{{ currentUser.name }}</h2>
          <div class="badges">
            <el-tag effect="dark" size="small" type="warning">
              <i class="el-icon-medal-1"></i> {{ currentUser.membershipLevel }}
            </el-tag>
            <span class="phone">{{ currentUser.username }}</span>
          </div>
        </div>
      </div>
      <div class="header-actions">
        <el-button type="primary" icon="el-icon-refresh" circle @click="fetchMyOrders" title="刷新订单"></el-button>
      </div>
    </div>

    <el-divider content-position="left">我的行程单</el-divider>

    <!-- 2. 状态处理 (加载中/错误/空) -->
    <div v-if="isLoading" class="state-box loading">
      <i class="el-icon-loading"></i>
      <p>正在同步最新的行程信息...</p>
    </div>

    <div v-else-if="error" class="state-box error">
      <i class="el-icon-warning-outline"></i>
      <p>{{ error }}</p>
      <el-button type="primary" plain size="small" @click="fetchMyOrders">重试</el-button>
    </div>

    <div v-else-if="!logicalOrders.length" class="state-box empty">
      <img src="https://gw.alipayobjects.com/zos/antfincdn/ZHrcdLPrvN/empty.svg" alt="empty" width="120">
      <p>暂无行程，开启一段新的旅程吧</p>
      <el-button type="primary" @click="$router.push('/')">去预订机票</el-button>
    </div>

    <!-- 3. 订单列表 -->
    <div v-else class="order-list">
      <el-card
          v-for="order in logicalOrders"
          :key="order.logicalOrderId"
          class="order-card"
          shadow="hover"
          :body-style="{ padding: '0px' }"
      >
        <!-- 卡片头部：航班摘要 -->
        <div class="order-header">
          <div class="flight-main">
            <span class="flight-no">{{ order.flightNumber }}</span>
            <span class="route">
              {{ order.departureAirport }} <i class="el-icon-right"></i> {{ order.arrivalAirport }}
            </span>
            <el-tag size="small" type="info" effect="plain" class="date-tag">
              <i class="el-icon-date"></i> {{ order.flightDate }}
            </el-tag>
          </div>
          <div class="order-meta">
            <span class="label">订单总额</span>
            <span class="amount">¥{{ order.totalAmount.toFixed(2) }}</span>
          </div>
        </div>

        <!-- 卡片主体：乘机人列表 -->
        <el-table :data="order.tickets" style="width: 100%" :show-header="true">
          <el-table-column prop="passengerName" label="乘机人" width="140">
            <template slot-scope="scope">
              <i class="el-icon-user"></i> {{ scope.row.passengerName }}
            </template>
          </el-table-column>

          <el-table-column prop="cabinClass" label="舱位" width="120">
            <template slot-scope="scope">
              <span :class="scope.row.cabinClass === '商务舱' ? 'text-gold' : 'text-blue'">
                {{ scope.row.cabinClass }}
              </span>
            </template>
          </el-table-column>

          <el-table-column label="票价" width="120">
            <template slot-scope="scope">¥{{ scope.row.finalPrice.toFixed(2) }}</template>
          </el-table-column>

          <el-table-column label="状态" align="center">
            <template slot-scope="scope">
              <el-tag :type="getStatusType(scope.row.status)" size="small" effect="light">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="120" align="right">
            <template slot-scope="scope">
              <el-button
                  v-if="scope.row.status === '已支付'"
                  type="text"
                  class="refund-btn"
                  @click="handleRefund(scope.row.ticketId)"
              >
                退订机票
              </el-button>
              <span v-else class="text-disabled">-</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script>
import api from '@/api';
import { store } from '@/store';

export default {
  name: 'MyTicketsView',
  data() {
    return {
      logicalOrders: [],
      isLoading: true,
      error: null,
    };
  },
  computed: {
    currentUser() { return store.user; }
  },
  created() {
    this.fetchMyOrders();
  },
  methods: {
    async fetchMyOrders() {
      this.isLoading = true;
      this.error = null;
      try {
        const res = await api.getMyOrders();
        // 按日期降序排列
        this.logicalOrders = (res.data || []).sort((a, b) =>
            new Date(b.flightDate) - new Date(a.flightDate)
        );
      } catch (err) {
        this.error = err.message || '加载失败，请稍后重试';
      } finally {
        this.isLoading = false;
      }
    },

    handleRefund(ticketId) {
      this.$confirm('确定要退订这张机票吗？退订后无法恢复。', '退票确认', {
        confirmButtonText: '确认退订',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }).then(async () => {
        try {
          const res = await api.refundTicket(ticketId);
          if (res.code === 200) {
            this.$message.success('退订成功');
            this.fetchMyOrders(); // 刷新列表
          }
        } catch (e) {
          // 错误已由拦截器处理，此处可忽略
        }
      }).catch(() => {});
    },

    getStatusType(status) {
      const map = {
        '已支付': 'success',
        '已取消': 'info',
        '已使用': 'warning',
        '已预订': 'primary'
      };
      return map[status] || '';
    }
  }
};
</script>

<style scoped>
.my-orders-container {
  max-width: 1000px;
  margin: 30px auto;
  padding: 0 20px;
  min-height: 80vh;
}

/* 用户头部 */
.user-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  margin-bottom: 25px;
}
.user-info { display: flex; align-items: center; gap: 15px; }
.info-text h2 { margin: 0 0 5px 0; font-size: 18px; color: #303133; }
.badges { display: flex; align-items: center; gap: 10px; }
.phone { font-size: 13px; color: #909399; }

/* 状态盒子 */
.state-box {
  text-align: center;
  padding: 60px 0;
  color: #909399;
}
.state-box i { font-size: 32px; margin-bottom: 10px; }
.state-box.error i { color: #F56C6C; }

/* 订单卡片 */
.order-card {
  margin-bottom: 20px;
  border: none;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.2s;
}
.order-card:hover { transform: translateY(-2px); box-shadow: 0 8px 20px rgba(0,0,0,0.08); }

.order-header {
  background: #f8fbfd;
  padding: 15px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #ebeef5;
}

.flight-main { display: flex; align-items: center; gap: 15px; }
.flight-no { font-weight: 800; font-size: 18px; color: #303133; }
.route { font-size: 15px; color: #606266; font-weight: 500; }
.date-tag { border: none; background: #eef1f6; color: #606266; }

.order-meta { display: flex; flex-direction: column; align-items: flex-end; }
.order-meta .label { font-size: 12px; color: #909399; }
.order-meta .amount { font-size: 20px; font-weight: 800; color: #F56C6C; font-family: DIN, sans-serif; }

/* 表格样式微调 */
.text-gold { color: #b88230; font-weight: bold; }
.text-blue { color: #409EFF; }
.text-disabled { color: #C0C4CC; }
.refund-btn { color: #F56C6C; padding: 0; }
.refund-btn:hover { color: #f78989; text-decoration: underline; }
</style>