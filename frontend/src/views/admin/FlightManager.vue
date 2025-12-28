<template>
  <div class="manager-container">
    <div class="toolbar">
      <el-button type="primary" icon="el-icon-plus" size="small">发布新航班</el-button>
      <el-button icon="el-icon-refresh" size="small" @click="fetchFlights">刷新列表</el-button>
    </div>

    <el-table :data="flights" border stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="flightNumber" label="航班号" width="100" fixed></el-table-column>
      <el-table-column label="航空公司" width="150">
        <template slot-scope="scope">
          <el-tag size="medium">{{ scope.row.airline ? scope.row.airline.airlineName : '未知' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="起降机场">
        <template slot-scope="scope">
          <span class="airport">{{ scope.row.departureAirport }}</span>
          <i class="el-icon-right"></i>
          <span class="airport">{{ scope.row.arrivalAirport }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="departureTime" label="起飞" width="100"></el-table-column>
      <el-table-column prop="arrivalTime" label="到达" width="100"></el-table-column>
      <el-table-column label="经济舱/商务舱" width="150">
        <template slot-scope="scope">
          {{ scope.row.economySeats }} / {{ scope.row.businessSeats }}
        </template>
      </el-table-column>
      <el-table-column label="基础票价" width="120">
        <template slot-scope="scope">
          <b style="color: #f56c6c">¥ {{ scope.row.basePrice }}</b>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template slot-scope="scope">
          <el-button type="text" size="small" @click="editPrice(scope.row)">调价</el-button>
          <el-divider direction="vertical"></el-divider>
          <el-button type="text" size="small" style="color: #f56c6c">取消航班</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import api from '@/api';
export default {
  data() { return { flights: [], loading: false }; },
  created() { this.fetchFlights(); },
  methods: {
    async fetchFlights() {
      this.loading = true;
      try {
        const res = await api.getAdminFlights();
        if (res.code === 200) this.flights = res.data;
      } finally { this.loading = false; }
    },
    // src/views/admin/FlightManager.vue 中的 methods 部分

    editPrice(row) {
      this.$prompt('请输入新的基础票价 (当前: ¥' + row.basePrice + ')', '价格调整 - ' + row.flightNumber, {
        confirmButtonText: '确定修改',
        cancelButtonText: '取消',
        inputPattern: /^\d+(\.\d{1,2})?$/,
        inputErrorMessage: '请输入有效的数字（最多两位小数）'
      }).then(async ({ value }) => {
        try {
          // 1. 调用真实后端接口
          const res = await api.updateFlightPrice(row.flightNumber, value);

          if (res.code === 200) {
            this.$message({
              type: 'success',
              message: `航班 ${row.flightNumber} 价格已成功调整为 ¥${value}`
            });
            // 2. 刷新列表显示最新数据
            this.fetchFlights();
          }
        } catch (error) {
          // 错误已由拦截器处理，此处无需额外逻辑
        }
      }).catch(() => {
        this.$message({ type: 'info', message: '已取消修改' });
      });
    }
  }
};
</script>
<style scoped>
.toolbar { margin-bottom: 20px; background: #f9f9f9; padding: 10px; border-radius: 4px; }
.airport { font-weight: bold; color: #409EFF; }
</style>