<template>
  <div class="order-card" :class="`status-${order.status.toLowerCase()}`">
    <!-- 卡片头部：订单号、状态、创建时间 -->
    <div class="order-card-header">
      <span class="order-number">订单号: {{ order.orderNumber }}</span>
      <span class="order-status badge" :class="getStatusBadgeClass(order.status)">
        {{ formatOrderStatus(order.status) }}
      </span>
      <span class="create-time">{{ formatDateTime(order.createTime) }}</span>
    </div>

    <!-- 卡片主体：航班信息 -->
    <div class="order-card-body">
      <div v-for="ticket in order.tickets" :key="ticket.ticketId" class="ticket-info">
        <div class="passenger-info">
          <i class="fas fa-user"></i>
          <span>{{ ticket.passengerName }}</span>
        </div>
        <div class="flight-info">
          <!-- 这里需要从 ticket 对象中获取航班信息，但 VO 中没有，我们先用订单号代替 -->
          <span class="flight-route">{{ getRouteFromOrder(order) }}</span>
          <span class="flight-details">航班日期: {{ getFlightDateFromOrder(order) }}</span>
        </div>
        <div class="price-info">
          <span class="final-price">¥{{ ticket.finalPrice.toFixed(2) }}</span>
        </div>
      </div>
    </div>

    <!-- 卡片脚部：总金额和操作按钮 -->
    <div class="order-card-footer">
      <div class="total-amount">
        <span>订单总额: </span>
        <span class="amount-value">¥{{ order.totalAmount.toFixed(2) }}</span>
      </div>
      <div class="actions">
        <el-button type="primary" plain size="small" @click="viewDetails">查看详情</el-button>
        <!-- 只有在特定状态下才显示“取消订单”按钮 -->
        <el-button 
          v-if="canBeCancelled(order.status)" 
          type="danger" 
          plain 
          size="small" 
          @click="$emit('cancel', order.orderId)"
        >
          取消订单
        </el-button>
      </div>
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
    // 格式化订单状态显示
    formatOrderStatus(status) {
      const statusMap = {
        PENDING_PAYMENT: '待支付',
        PAID: '已支付',
        CANCELLED: '已取消',
        COMPLETED: '已完成'
      };
      return statusMap[status] || '未知状态';
    },
    // 根据状态返回不同的徽章样式
    getStatusBadgeClass(status) {
      const classMap = {
        PENDING_PAYMENT: 'badge-warning',
        PAID: 'badge-success',
        CANCELLED: 'badge-danger',
        COMPLETED: 'badge-info'
      };
      return classMap[status] || 'badge-info';
    },
    // 格式化日期时间
    formatDateTime(dateTimeStr) {
      if (!dateTimeStr) return '';
      return new Date(dateTimeStr).toLocaleString('zh-CN');
    },
    // 判断订单是否可以被取消
    canBeCancelled(status) {
      // 根据后端逻辑，只有 PENDING_PAYMENT 和 PAID 状态可以取消
      return ['PENDING_PAYMENT', 'PAID'].includes(status);
    },
    // 跳转到订单详情页
    viewDetails() {
      this.$router.push({ name: 'OrderDetail', params: { orderId: this.order.orderId } });
    },
    // 从订单的第一个票据中获取航线信息（这是一个变通方法）
    getRouteFromOrder(order) {
        if (order.tickets && order.tickets.length > 0) {
            // 后端OrderDetailVO没有提供航班的起降地，这是一个需要后端改进的点
            // 我们暂时返回一个占位符
            return '航线信息待补充';
        }
        return '未知航线';
    },
    // 从订单的第一个票据中获取航班日期
    getFlightDateFromOrder(order) {
        if (order.tickets && order.tickets.length > 0) {
            // 后端OrderDetailVO.TicketVO没有提供flightDate，这也是一个需要改进的点
            return '日期未知';
        }
        return '日期未知';
    }
  }
}
</script>

<style scoped>
.order-card { border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden; transition: box-shadow 0.3s; }
.order-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
.order-card-header { display: flex; justify-content: space-between; align-items: center; background-color: #f5f7fa; padding: 10px 15px; font-size: 14px; }
.order-number { font-weight: 500; }
.create-time { color: var(--gray); }
.order-card-body { padding: 15px; }
.ticket-info { display: flex; align-items: center; padding: 10px 0; border-bottom: 1px dashed #eee; }
.ticket-info:last-child { border-bottom: none; }
.passenger-info { flex-basis: 150px; }
.flight-info { flex-grow: 1; }
.flight-route { font-weight: bold; }
.flight-details { font-size: 12px; color: var(--gray); }
.price-info { text-align: right; font-size: 16px; font-weight: bold; color: var(--danger); }
.order-card-footer { display: flex; justify-content: space-between; align-items: center; padding: 10px 15px; border-top: 1px solid #e0e0e0; }
.total-amount .amount-value { font-size: 18px; font-weight: bold; color: var(--danger); }
.actions .el-button { margin-left: 10px; }
</style>