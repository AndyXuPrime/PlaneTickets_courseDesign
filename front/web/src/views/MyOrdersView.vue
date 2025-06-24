<template>
  <div class="my-orders-page">
    <!-- 1. 页面头部，包含用户信息 -->
    <div class="page-header card">
      <div class="user-profile-summary">
        <div class="user-avatar">
          <i class="fas fa-user-circle"></i>
        </div>
        <div class="user-details">
          <!-- 假设用户信息已从 store 中获取 -->
          <h2 class="user-name">{{ user.name || user.username }}</h2>
          <p class="membership-level" v-if="user.level">
            <i :class="getMembershipIcon(user.level)"></i> {{ user.level }}
          </p>
        </div>
        <div class="user-stats">
          <div class="stat-item">
            <span class="stat-value">12,345</span>
            <span class="stat-label">可用里程</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 2. 订单列表区域 -->
    <div class="orders-list-container card">
      <div class="card-header">
        <h3 class="card-title">我的订单列表</h3>
        <!-- 可以添加筛选器 -->
        <el-tabs v-model="filterStatus" @tab-click="handleFilterChange">
          <el-tab-pane label="全部订单" name="ALL"></el-tab-pane>
          <el-tab-pane label="待支付" name="PENDING_PAYMENT"></el-tab-pane>
          <el-tab-pane label="已支付" name="PAID"></el-tab-pane>
          <el-tab-pane label="已完成" name="COMPLETED"></el-tab-pane>
          <el-tab-pane label="已取消" name="CANCELLED"></el-tab-pane>
        </el-tabs>
      </div>
      <div class="card-body">
        <!-- 加载状态 -->
        <div v-if="isLoading" class="loading-state">
          <i class="el-icon-loading"></i><p>正在加载您的订单...</p>
        </div>
        <!-- 无订单状态 -->
        <div v-else-if="filteredOrders.length === 0" class="no-results-state">
          <i class="el-icon-document-delete"></i><p>您还没有相关订单。</p>
        </div>
        <!-- 订单列表 -->
        <div v-else class="orders-list">
          <order-card 
            v-for="order in filteredOrders" 
            :key="order.orderId"
            :order="order"
            @cancel="handleCancelOrder"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import OrderCard from '../components/OrderCard.vue'; // 引入子组件
import api from '../api';
import { store } from '../store'; // 获取用户信息

export default {
  name: 'MyOrdersView',
  components: { OrderCard },
  data() {
    return {
      isLoading: false,
      orders: [], // 存储从API获取的所有订单
      filterStatus: 'ALL', // 当前筛选状态
    };
  },
  computed: {
    // 从全局 store 获取用户信息
    user() {
      // 提供一个默认对象防止 user 为 null 时报错
      return store.user || { name: '旅客', username: '未知', level: '普通' };
    },
    // 根据筛选状态计算显示的订单列表
    filteredOrders() {
      if (this.filterStatus === 'ALL') {
        return this.orders;
      }
      return this.orders.filter(order => order.status === this.filterStatus);
    }
  },
  created() {
    this.fetchMyOrders();
  },
  methods: {
    // 从API获取订单列表
    async fetchMyOrders() {
      this.isLoading = true;
      try {
        const response = await api.getMyOrders();
        this.orders = response.data;
      } catch (error) {
        console.error("获取订单列表失败:", error);
        // 错误消息已由拦截器处理
      } finally {
        this.isLoading = false;
      }
    },
    // 处理取消订单事件
    async handleCancelOrder(orderId) {
      this.$confirm('您确定要取消这个订单吗？此操作不可逆。', '确认取消', {
        confirmButtonText: '确定取消',
        cancelButtonText: '再想想',
        type: 'warning'
      }).then(async () => {
        // 用户点击了“确定”
        try {
          await api.cancelOrder(orderId);
          this.$message.success('订单取消成功！');
          // 刷新订单列表以更新状态
          this.fetchMyOrders();
        } catch (error) {
          console.error(`取消订单 ${orderId} 失败:`, error);
          // 错误消息已由拦截器处理
        }
      }).catch(() => {
        // 用户点击了“再想想”或关闭了弹窗
        this.$message.info('操作已取消');
      });
    },
    // 切换筛选标签时触发
    handleFilterChange() {
      // 目前的筛选是纯前端的，所以这个方法可以为空。
      // 如果未来要做后端分页筛选，逻辑将在这里。
    },
    // 辅助方法，用于显示会员等级图标
    getMembershipIcon(level) {
      switch (level) {
        case '白金': return 'fas fa-gem';
        case '金卡': return 'fas fa-crown';
        case '银卡': return 'fas fa-star';
        default: return 'fas fa-user';
      }
    },
  }
};
</script>

<style scoped>
.my-orders-page { max-width: 1000px; margin: 0 auto; }
.page-header { padding: 20px; margin-bottom: 20px; }
.user-profile-summary { display: flex; align-items: center; }
.user-avatar { font-size: 60px; margin-right: 20px; color: var(--primary); }
.user-details .user-name { font-size: 24px; font-weight: bold; margin: 0; }
.user-details .membership-level { font-weight: 500; color: var(--primary); }
.user-stats { margin-left: auto; text-align: right; }
.stat-item { margin-bottom: 5px; }
.stat-value { font-size: 20px; font-weight: bold; }
.stat-label { font-size: 14px; color: var(--gray); }

.orders-list-container .card-header { display: block; }
.card-header .el-tabs { margin-top: 15px; }
.orders-list { display: flex; flex-direction: column; gap: 20px; }
.loading-state, .no-results-state { text-align: center; padding: 50px; color: var(--gray); }
.loading-state i, .no-results-state i { font-size: 48px; margin-bottom: 20px; display: block; }
</style>