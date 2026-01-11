<template>
  <div>
    <el-form :inline="true" size="small" class="search-bar">
      <el-form-item label="航班号">
        <el-input v-model="query.flightNo" placeholder="如 CA123" clearable></el-input>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable>
          <el-option label="已支付" value="已支付"></el-option>
          <el-option label="已使用" value="已使用"></el-option>
          <el-option label="已取消" value="已取消"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="fetchOrders">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="orders" border v-loading="loading">
      <el-table-column prop="ticketId" label="票号ID" width="80"></el-table-column>
      <el-table-column prop="passengerName" label="乘机人" width="120"></el-table-column>
      <el-table-column prop="flightNumber" label="航班" width="100"></el-table-column>
      <el-table-column prop="flightDate" label="日期" width="120"></el-table-column>
      <el-table-column prop="cabinClass" label="舱位" width="100"></el-table-column>
      <el-table-column prop="price" label="实付金额">
        <template slot-scope="scope">¥ {{ scope.row.price }}</template>
      </el-table-column>
      <el-table-column label="状态" align="center">
        <template slot-scope="scope">
          <el-tag :type="statusMap[scope.row.status]">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template slot-scope="scope">
          <el-button
              v-if="scope.row.status === '已支付'"
              type="success"
              size="mini"
              @click="checkIn(scope.row)"
          >
            确认登机
          </el-button>
          <el-button type="text" size="mini" @click="showLogs(scope.row)">审计日志</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 审计日志弹窗 -->
    <el-dialog title="订单状态变更日志" :visible.sync="logVisible" width="500px">
      <el-timeline>
        <el-timeline-item
            v-for="(log, index) in currentLogs"
            :key="index"
            :timestamp="formatTime(log.changeTime)"
            placement="top"
        >
          <el-card shadow="hover">
            <h4>{{ log.oldStatus || '无' }} <i class="el-icon-right"></i> {{ log.newStatus }}</h4>
            <p>操作人: {{ log.changedBy || '系统' }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </div>
</template>

<script>
import api from '@/api';

export default {
  data() {
    return {
      query: { flightNo: '', status: '' },
      orders: [],
      loading: false,
      statusMap: { '已支付': 'success', '已使用': 'info', '已取消': 'danger', '已预订': 'warning' },

      logVisible: false,
      currentLogs: []
    };
  },
  created() { this.fetchOrders(); },
  methods: {
    async fetchOrders() {
      this.loading = true;
      try {
        const params = {};
        if (this.query.flightNo) params.flightNumber = this.query.flightNo;
        if (this.query.status) params.status = this.query.status;

        const res = await api.getAllTickets(params);
        if (res.code === 200) {
          this.orders = res.data;
        }
      } catch (e) {
        this.$message.error('获取订单列表失败');
      } finally {
        this.loading = false;
      }
    },
    checkIn(row) {
      this.$confirm(`确认旅客 ${row.passengerName} 已办理登机？`, '核销确认', {
        confirmButtonText: '确认核销',
        type: 'success'
      }).then(async () => {
        try {
          await api.checkInTicket(row.ticketId);
          this.$message.success('核销成功');
          this.fetchOrders(); // 刷新列表
        } catch (e) {
          this.$message.error('操作失败');
        }
      }).catch(() => {});
    },
    async showLogs(row) {
      try {
        const res = await api.getTicketLogs(row.ticketId);
        if (res.code === 200) {
          this.currentLogs = res.data;
          this.logVisible = true;
        }
      } catch (e) {
        this.$message.error('获取日志失败');
      }
    },
    formatTime(time) {
      if (!time) return '';

      // 判断是否为数组格式 [2026, 1, 9, 19, 50, 50]
      if (Array.isArray(time)) {
        const year = time[0];
        const month = time[1].toString().padStart(2, '0');
        const day = time[2].toString().padStart(2, '0');
        const hour = (time[3] || 0).toString().padStart(2, '0');
        const minute = (time[4] || 0).toString().padStart(2, '0');
        const second = (time[5] || 0).toString().padStart(2, '0');
        return `${year}-${month}-${day} ${hour}:${minute}:${second}`;
      }

      return new Date(time).toLocaleString();
    }
  }
};
</script>
<style scoped> .search-bar { background: #f4f4f5; padding: 15px; margin-bottom: 20px; border-radius: 4px; } </style>