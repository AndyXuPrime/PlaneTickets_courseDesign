<template>
  <div>
    <el-form :inline="true" size="small" class="search-bar">
      <el-form-item label="航班号">
        <el-input v-model="query.flightNo" placeholder="如 CA123"></el-input>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部">
          <el-option label="已支付" value="已支付"></el-option>
          <el-option label="已使用" value="已使用"></el-option>
          <el-option label="已取消" value="已取消"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="orders" border>
      <el-table-column prop="ticketId" label="订单号" width="100"></el-table-column>
      <el-table-column prop="passengerName" label="乘机人" width="120"></el-table-column>
      <el-table-column prop="flightNumber" label="航班" width="100"></el-table-column>
      <el-table-column prop="cabinClass" label="舱位" width="100"></el-table-column>
      <el-table-column prop="finalPrice" label="实付金额">
        <template slot-scope="scope">¥ {{ scope.row.finalPrice }}</template>
      </el-table-column>
      <el-table-column label="状态">
        <template slot-scope="scope">
          <el-tag :type="statusMap[scope.row.status]">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template slot-scope="scope">
          <el-button v-if="scope.row.status === '已支付'" type="success" size="mini" @click="checkIn(scope.row)">确认登机</el-button>
          <el-button type="text" size="mini" @click="showLogs(scope.row)">审计日志</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  data() {
    return {
      query: { flightNo: '', status: '' },
      orders: [],
      statusMap: { '已支付': 'success', '已使用': 'info', '已取消': 'danger' }
    };
  },
  created() { this.fetchOrders(); },
  methods: {
    fetchOrders() {
      // 演示数据，实际应调用 order-service 接口
      this.orders = [
        { ticketId: 1001, passengerName: '张三', flightNumber: 'CA1886', cabinClass: '经济舱', finalPrice: 1200, status: '已支付' },
        { ticketId: 1002, passengerName: '李四', flightNumber: 'MU5101', cabinClass: '商务舱', finalPrice: 3500, status: '已使用' }
      ];
    },
    checkIn(row) {
      this.$confirm('确认该旅客已办理登机？', '核销确认').then(() => {
        row.status = '已使用';
        this.$message.success('核销成功');
      });
    },
    showLogs(row) {
      this.$alert('2025-01-01 10:00:00 - 用户下单<br>2025-01-01 10:05:00 - 支付成功', '订单审计 - ' + row.ticketId, {
        dangerouslyUseHTMLString: true
      });
    }
  }
};
</script>
<style scoped> .search-bar { background: #f4f4f5; padding: 15px; margin-bottom: 20px; } </style>