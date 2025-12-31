<template>
  <div class="my-orders-container">
    <!-- 1. 用户信息概览卡片 -->
    <div class="user-profile-card" v-if="currentUser">
      <div class="profile-main">
        <el-avatar :size="64" icon="el-icon-user-solid" class="custom-avatar"></el-avatar>
        <div class="profile-text">
          <div class="name-row">
            <span class="user-name">{{ currentUser.name }}</span>
            <div class="membership-badge">
              <i class="el-icon-medal-1"></i> {{ currentUser.membershipLevel }}会员
            </div>
          </div>
          <div class="user-meta">
            <i class="el-icon-mobile-phone"></i> {{ currentUser.username }}
          </div>
        </div>
      </div>
      <div class="profile-actions">
        <button class="refresh-circle-btn" @click="fetchMyOrders" title="同步行程">
          <i class="el-icon-refresh" :class="{ 'is-loading': isLoading }"></i>
        </button>
      </div>
    </div>

    <!-- 2. 标题装饰 -->
    <div class="section-title">
      <span class="title-text">我的行程订单 / ITINERARY</span>
      <div class="title-line"></div>
    </div>

    <!-- 状态展示区 -->
    <div v-if="isLoading" class="state-placeholder">
      <div class="loader"></div>
      <p>正在同步云端行程...</p>
    </div>

    <div v-else-if="error" class="state-placeholder error">
      <i class="el-icon-warning-outline"></i>
      <p>{{ error }}</p>
      <el-button type="primary" size="small" round @click="fetchMyOrders">重新加载</el-button>
    </div>

    <div v-else-if="!logicalOrders.length" class="state-placeholder empty">
      <img src="https://gw.alipayobjects.com/zos/antfincdn/ZHrcdLPrvN/empty.svg" alt="empty" width="140">
      <p>您的飞行清单还是空的</p>
      <el-button type="primary" round @click="$router.push('/')">开启新旅程</el-button>
    </div>

    <!-- 3. 订单列表 -->
    <div v-else class="order-list">
      <div
          v-for="order in logicalOrders"
          :key="order.logicalOrderId"
          class="itinerary-card"
      >
        <!-- 订单头部：航班简报 -->
        <div class="itinerary-header">
          <div class="flight-info">
            <div class="flight-no-box">
              <i class="el-icon-position"></i>
              <span class="no">{{ order.flightNumber }}</span>
            </div>
            <div class="route-display">
              <span class="apt">{{ order.departureAirport }}</span>
              <div class="flight-arrow">
                <div class="line"></div>
                <i class="el-icon-caret-right"></i>
              </div>
              <span class="apt">{{ order.arrivalAirport }}</span>
            </div>
            <div class="date-box">
              <i class="el-icon-date"></i> {{ order.flightDate }}
            </div>
          </div>
          <div class="order-price">
            <span class="price-label">总额</span>
            <span class="price-val">¥{{ order.totalAmount.toFixed(2) }}</span>
          </div>
        </div>

        <!-- 卡片主体：电子客票列表 -->
        <div class="itinerary-body">
          <el-table :data="order.tickets" style="width: 100%" class="custom-table">
            <el-table-column prop="passengerName" label="乘机人" min-width="120">
              <template slot-scope="scope">
                <span class="passenger-name-cell">
                  <i class="el-icon-user"></i> {{ scope.row.passengerName }}
                </span>
              </template>
            </el-table-column>

            <el-table-column prop="cabinClass" label="舱位" width="100">
              <template slot-scope="scope">
                <span :class="scope.row.cabinClass === '商务舱' ? 'cabin-business' : 'cabin-economy'">
                  {{ scope.row.cabinClass }}
                </span>
              </template>
            </el-table-column>

            <el-table-column label="实付票价" width="110">
              <template slot-scope="scope">
                <span class="ticket-price">¥{{ scope.row.finalPrice.toFixed(0) }}</span>
              </template>
            </el-table-column>

            <el-table-column label="客票状态" align="center" width="110">
              <template slot-scope="scope">
                <span class="status-dot-tag" :class="getStatusClass(scope.row.status)">
                  {{ scope.row.status }}
                </span>
              </template>
            </el-table-column>

            <el-table-column label="操作" width="110" align="right">
              <template slot-scope="scope">
                <el-button
                    v-if="scope.row.status === '已支付'"
                    type="text"
                    class="btn-action-refund"
                    @click="handleRefund(scope.row.ticketId)"
                >
                  退订机票
                </el-button>
                <span v-else class="action-none">已归档</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
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
        this.logicalOrders = (res.data || []).sort((a, b) =>
            new Date(b.flightDate) - new Date(a.flightDate)
        );
      } catch (err) {
        this.error = err.message || '行程同步失败，请检查网络连接';
      } finally {
        this.isLoading = false;
      }
    },

    handleRefund(ticketId) {
      this.$confirm('确定要退订此张客票吗？退款将按原渠道退回。', '退票确认', {
        confirmButtonText: '确认退票',
        cancelButtonText: '保留行程',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }).then(async () => {
        try {
          const res = await api.refundTicket(ticketId);
          if (res.code === 200) {
            this.$message.success('退票申请已受理');
            this.fetchMyOrders();
          }
        } catch (e) {
          // 拦截器已处理异常
        }
      }).catch(() => {});
    },

    getStatusClass(status) {
      const map = {
        '已支付': 's-success',
        '已取消': 's-info',
        '已使用': 's-warning',
        '已预订': 's-primary'
      };
      return map[status] || 's-default';
    }
  }
};
</script>

<style scoped>
.my-orders-container {
  max-width: 900px;
  margin: 40px auto;
  padding: 0 20px;
  min-height: 80vh;
  font-family: 'Inter', -apple-system, sans-serif;
}

/* 1. 用户信息卡片：参考航空商务风格 */
.user-profile-card {
  background: #ffffff;
  padding: 24px;
  border-radius: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 10px 25px rgba(0,0,0,0.05);
  border: 1px solid rgba(26, 54, 93, 0.1);
  margin-bottom: 40px;
}

.profile-main { display: flex; align-items: center; gap: 20px; }
.custom-avatar { border: 2px solid #ebf4ff; }
.user-name { font-size: 22px; font-weight: 700; color: #1a365d; }

.membership-badge {
  background: #fff8eb;
  color: #b88230;
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  border: 1px solid #fceccb;
  margin-left: 10px;
}

.name-row { display: flex; align-items: center; }
.user-meta { margin-top: 6px; font-size: 14px; color: #718096; }

.refresh-circle-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 1px solid #e2e8f0;
  background: #fff;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #4a5568;
}
.refresh-circle-btn:hover { background: #f7fafc; border-color: #3182ce; color: #3182ce; }

/* 2. 标题装饰 */
.section-title { margin-bottom: 25px; display: flex; flex-direction: column; gap: 8px; }
.title-text { font-size: 13px; font-weight: 700; color: #2d3748; letter-spacing: 1px; }
.title-line { width: 40px; height: 3px; background: #3182ce; border-radius: 2px; }

/* 3. 状态占位符 */
.state-placeholder { text-align: center; padding: 80px 0; color: #a0aec0; }
.state-placeholder i { font-size: 48px; margin-bottom: 15px; }
.loader { border: 4px solid #f3f3f3; border-top: 4px solid #3182ce; border-radius: 50%; width: 30px; height: 30px; animation: spin 1s linear infinite; margin: 0 auto 15px; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

/* 4. 行程卡片：重点优化 */
.itinerary-card {
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 25px;
  border: 1px solid rgba(26, 54, 93, 0.1);
  box-shadow:
      0 4px 6px rgba(0,0,0,0.02),
      0 10px 15px rgba(0,0,0,0.03);
  transition: transform 0.3s cubic-bezier(0.15, 0.83, 0.66, 1);
}
.itinerary-card:hover { transform: translateY(-4px); box-shadow: 0 20px 25px rgba(0,0,0,0.06); }

.itinerary-header {
  background: #fcfdfe;
  padding: 18px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #edf2f7;
}

.flight-info { display: flex; align-items: center; gap: 30px; }
.flight-no-box { display: flex; align-items: center; gap: 8px; color: #1a365d; }
.flight-no-box .no { font-weight: 800; font-size: 18px; }

.route-display { display: flex; align-items: center; gap: 15px; }
.route-display .apt { font-size: 15px; font-weight: 600; color: #2d3748; }
.flight-arrow { display: flex; align-items: center; gap: 5px; color: #cbd5e0; }
.flight-arrow .line { width: 30px; height: 1px; background: #cbd5e0; }

.date-box { font-size: 14px; color: #718096; display: flex; align-items: center; gap: 6px; }

.order-price { text-align: right; }
.price-label { font-size: 11px; color: #a0aec0; display: block; text-transform: uppercase; }
.price-val { font-size: 22px; font-weight: 800; color: #e53e3e; }

/* 5. 表格美化 */
.custom-table ::v-deep .el-table__row td { padding: 12px 0; border-bottom: 1px solid #f7fafc; }
.custom-table ::v-deep .el-table__header th { background: #fff; color: #a0aec0; font-size: 12px; font-weight: 600; text-transform: uppercase; }
.custom-table ::v-deep .el-table::before { display: none; }

.passenger-name-cell { font-weight: 600; color: #2d3748; }
.passenger-name-cell i { color: #3182ce; margin-right: 5px; }

.cabin-business { color: #b88230; font-weight: 700; background: #fff8eb; padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.cabin-economy { color: #3182ce; font-weight: 700; background: #ebf4ff; padding: 2px 8px; border-radius: 4px; font-size: 12px; }

.ticket-price { font-weight: 600; color: #4a5568; }

.status-dot-tag {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 20px;
}
.s-success { background: #f0fff4; color: #38a169; }
.s-primary { background: #ebf4ff; color: #3182ce; }
.s-warning { background: #fffaf0; color: #dd6b20; }
.s-info { background: #edf2f7; color: #718096; }

.btn-action-refund { color: #e53e3e; font-weight: 600; font-size: 13px; }
.btn-action-refund:hover { color: #c53030; text-decoration: underline; }
.action-none { color: #cbd5e0; font-size: 12px; }

@media (max-width: 768px) {
  .flight-info { gap: 15px; }
  .route-display .apt { font-size: 13px; }
  .itinerary-header { flex-direction: column; align-items: flex-start; gap: 15px; }
  .order-price { text-align: left; }
}
</style>