<template>
  <div class="my-orders-container">
    <!-- 1. 用户信息展示卡片 -->
    <el-card class="user-profile-card" shadow="hover">
      <div slot="header" class="clearfix">
        <span>我的账户信息</span>
      </div>
      <div v-if="currentUser" class="profile-details">
        <div class="profile-item">
          <i class="el-icon-user-solid"></i>
          <strong>用户名:</strong> {{ currentUser.name }}
        </div>
        
        <div class="profile-item">
          <i class="el-icon-medal-1"></i>
          <strong>会员等级:</strong> <el-tag type="warning" size="small">{{ currentUser.membershipLevel }}</el-tag>
        </div>
      </div>
    </el-card>

    <!-- 2. 订单列表 -->
    <h1 class="page-title">我的行程单</h1>

    <!-- 加载、错误、空状态处理 -->
    <div v-if="isLoading" class="state-container">
      <i class="el-icon-loading"></i>
      <p>正在努力加载您的行程单...</p>
    </div>
    <div v-else-if="error" class="state-container error">
      <i class="el-icon-error"></i>
      <p>加载订单失败: {{ error }}</p>
      <el-button @click="fetchMyOrders" type="primary" plain>点击重试</el-button>
    </div>
    <div v-else-if="!logicalOrders.length" class="state-container empty">
      <i class="el-icon-document-delete"></i>
      <p>您还没有任何行程单，快去预订吧！</p>
      <el-button type="primary" @click="$router.push('/')">立即预订</el-button>
    </div>

    <!-- 订单卡片列表 -->
    <div v-else>
      <el-card v-for="order in logicalOrders" :key="order.logicalOrderId" class="order-card">
        <div slot="header" class="order-header">
          <div>
            <span class="flight-number">航班: {{ order.flightNumber }}</span>
            <span class="flight-date">日期: {{ order.flightDate }}</span>
          </div>
          <div class="total-amount">
            订单总价: <strong>¥{{ order.totalAmount.toFixed(2) }}</strong>
          </div>
        </div>

        <!-- 机票详情表格 -->
        <el-table :data="order.tickets" style="width: 100%" stripe>
          <el-table-column prop="passengerName" label="乘机人" width="120"></el-table-column>
          <el-table-column prop="cabinClass" label="舱位" width="100"></el-table-column>
          <el-table-column label="票价" width="120">
            <template slot-scope="scope">
              ¥{{ scope.row.finalPrice.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template slot-scope="scope">
              <el-tag :type="getStatusTagType(scope.row.status)" size="medium">{{ scope.row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <template slot-scope="scope">
              <!-- 只有 "已支付" 状态的机票才能退订 -->
              <el-button
                @click="handleRefund(scope.row.ticketId)"
                type="danger"
                plain
                size="mini"
                :disabled="scope.row.status !== '已支付'"
                title="仅已支付状态的机票可退订"
              >
                退订
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script>
import api from '@/api'; // 确保api路径正确
import { store } from '@/store'; // 导入全局状态

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
    // 从全局状态安全地获取当前用户信息
    currentUser() {
      return store.user || {};
    }
  },
  created() {
    this.fetchMyOrders();
  },
  methods: {
    // 从后端获取订单数据
    async fetchMyOrders() {
      this.isLoading = true;
      this.error = null;
      try {
        const response = await api.getMyOrders();
        // 按航班日期降序排序，最新的行程在最上面
        this.logicalOrders = response.data.sort((a, b) => new Date(b.flightDate) - new Date(a.flightDate));
      } catch (error) {
        this.error = error.message || '无法连接到服务器';
        console.error('获取订单失败:', error);
      } finally {
        this.isLoading = false;
      }
    },
    // 处理退订逻辑
    handleRefund(ticketId) {
      this.$confirm('您确定要为这张机票申请退订吗？此操作不可撤销。', '退订确认', {
        confirmButtonText: '确定退订',
        cancelButtonText: '我再想想',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await api.refundTicket(ticketId);
          this.$message.success(response.message || '退票申请成功！');
          // 成功后重新加载订单列表，以更新状态
          this.fetchMyOrders();
        } catch (error) {
          this.$message.error(error.message || '退票申请失败');
        }
      }).catch(() => {
        this.$message.info('已取消退订操作');
      });
    },
    // 根据机票状态返回不同的标签颜色
    getStatusTagType(status) {
      switch (status) {
        case '已支付':
          return 'success';
        case '已使用':
          return 'info';
        case '已取消':
          return 'info';
        case '已预订':
          return 'warning';
        default:
          return 'primary';
      }
    }
  }
};
</script>

<style scoped>
.my-orders-container {
  max-width: 1200px;
  margin: 2rem auto;
  padding: 1rem;
}

.page-title {
  font-size: 2rem;
  font-weight: 500;
  margin-bottom: 1.5rem;
  color: #333;
  border-left: 5px solid #409EFF;
  padding-left: 15px;
}

.user-profile-card {
  margin-bottom: 2rem;
  background-color: #f9fafc;
}

.profile-details {
  display: flex;
  justify-content: space-around;
  align-items: center;
  flex-wrap: wrap;
}

.profile-item {
  display: flex;
  align-items: center;
  font-size: 1rem;
  color: #555;
  margin: 0.5rem;
}

.profile-item i {
  font-size: 1.2rem;
  margin-right: 8px;
  color: #409EFF;
}

.profile-item strong {
  margin-right: 5px;
  font-weight: 500;
}

.state-container {
  text-align: center;
  padding: 50px 20px;
  background-color: #fff;
  border-radius: 8px;
  color: #888;
  border: 1px dashed #dcdfe6;
}

.state-container i {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.state-container.error i {
  color: #F56C6C;
}

.state-container.empty i {
  color: #909399;
}

.order-card {
  margin-bottom: 1.5rem;
  border-radius: 8px;
  overflow: hidden;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 1rem;
}

.flight-number {
  font-weight: bold;
  color: #303133;
  margin-right: 20px;
}

.flight-date {
  color: #606266;
}

.total-amount {
  color: #F56C6C;
  font-size: 1.1rem;
}
</style>