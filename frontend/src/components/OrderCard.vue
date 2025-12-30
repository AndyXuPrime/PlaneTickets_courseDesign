<template>
  <div class="order-card">
    <!-- 1. 卡片头部：显示航班号和日期 (LogicalOrderVO 没有统一状态，状态在 Ticket 上) -->
    <div class="order-card-header">
      <span class="order-number">
        <i class="el-icon-s-ticket"></i> 订单组: {{ order.logicalOrderId }}
      </span>
      <!-- 显示航班号 -->
      <span class="flight-no">{{ order.flightNumber }}</span>
    </div>

    <!-- 2. 卡片主体：机票列表 -->
    <div class="order-card-body">
      <!-- 遍历订单内的每一张机票 -->
      <div v-for="ticket in order.tickets" :key="ticket.ticketId" class="ticket-info">

        <!-- 左侧：乘机人与舱位 -->
        <div class="passenger-info">
          <div class="name"><i class="fas fa-user"></i> {{ ticket.passengerName }}</div>
          <div class="cabin-tag">{{ ticket.cabinClass }}</div>
        </div>

        <!-- 中间：航线与日期 -->
        <div class="flight-info">
          <div class="flight-route">
            {{ order.departureAirport }} <i class="el-icon-right"></i> {{ order.arrivalAirport }}
          </div>
          <div class="flight-date">{{ order.flightDate }}</div>
        </div>

        <!-- 右侧：价格、状态与操作 -->
        <div class="action-info">
          <div class="price">¥{{ ticket.finalPrice.toFixed(2) }}</div>

          <!-- 状态显示 (适配后端中文枚举) -->
          <el-tag size="small" :type="getStatusType(ticket.status)" class="status-tag">
            {{ ticket.status }}
          </el-tag>

          <el-button
              v-if="ticket.status === '已支付'"
              type="text"
              class="refund-btn"
              @click="handleRefund(ticket.ticketId)"
          >
            退票
          </el-button>
        </div>
      </div>
    </div>

    <!-- 3. 卡片脚部：总金额 -->
    <div class="order-card-footer">
      <div class="total-amount">
        <span>订单总额: </span>
        <span class="amount-value">¥{{ order.totalAmount.toFixed(2) }}</span>
      </div>
      <!-- 这里的查看详情可以保留，或者去掉，看你需求 -->
    </div>
  </div>
</template>

<script>
export default {
  name: 'OrderCard',
  props: {
    order: {
      type: Object,
      required: true,
    }
  },
  methods: {
    // 适配后端的中文状态，返回对应的 ElementUI Tag 类型
    getStatusType(status) {
      const map = {
        '已支付': 'success',
        '已取消': 'info',
        '已使用': 'warning',
        '已预订': 'primary'
      };
      return map[status] || 'info';
    },

    // 处理退票
    handleRefund(ticketId) {
      this.$confirm('确定要退订这张机票吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 向父组件触发事件，传递具体的 ticketId
        this.$emit('refund-ticket', ticketId);
      }).catch(() => {});
    }
  }
}
</script>

<style scoped>
.order-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  margin-bottom: 20px;
  transition: all 0.3s;
}
.order-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.1); }

/* 头部样式 */
.order-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f5f7fa;
  padding: 12px 20px;
  border-bottom: 1px solid #ebeef5;
}
.order-number { font-weight: bold; color: #606266; font-size: 14px; }
.flight-no { font-weight: 800; color: #409EFF; font-size: 16px; }

/* 主体样式 */
.order-card-body { padding: 0 20px; }
.ticket-info {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px dashed #eee;
}
.ticket-info:last-child { border-bottom: none; }

.passenger-info { flex: 1; }
.passenger-info .name { font-weight: bold; font-size: 15px; color: #303133; }
.passenger-info .cabin-tag { font-size: 12px; color: #909399; margin-top: 4px; }

.flight-info { flex: 2; text-align: center; }
.flight-route { font-weight: bold; font-size: 15px; color: #303133; }
.flight-date { color: #909399; font-size: 13px; margin-top: 4px; }

.action-info { flex: 1; text-align: right; display: flex; flex-direction: column; align-items: flex-end; gap: 5px; }
.price { font-size: 16px; font-weight: 800; color: #F56C6C; }
.refund-btn { color: #F56C6C; padding: 0; }

/* 底部样式 */
.order-card-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 12px 20px;
  background: #fcfcfc;
  border-top: 1px solid #ebeef5;
}
.total-amount span { color: #606266; font-size: 14px; }
.total-amount .amount-value { font-size: 20px; color: #F56C6C; margin-left: 5px; }
</style>