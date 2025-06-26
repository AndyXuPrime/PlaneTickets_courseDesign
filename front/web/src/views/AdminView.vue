<template>
  <div class="admin-page">
    <h1>后台管理面板</h1>
    
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 航班管理 Tab -->
      <el-tab-pane label="航班管理" name="flight-management">
        <div class="toolbar">
          <el-button type="primary" icon="el-icon-plus" @click="openFlightDialog()">新增航班</el-button>
        </div>
        <el-table :data="flights" stripe v-loading="loading">
          <el-table-column prop="flightNumber" label="航班号"></el-table-column>
          <el-table-column prop="airlineName" label="航空公司"></el-table-column>
          <el-table-column prop="departureAirport" label="出发地"></el-table-column>
          <el-table-column prop="arrivalAirport" label="目的地"></el-table-column>
          <el-table-column prop="basePrice" label="基础票价"></el-table-column>
          <el-table-column label="操作">
            <template slot-scope="scope">
              <el-button size="mini" @click="openFlightDialog(scope.row)">编辑</el-button>
              <el-button size="mini" type="danger" @click="deleteFlight(scope.row.flightNumber)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 统计报表 Tab -->
      <el-tab-pane label="统计报表" name="statistics">
        <!-- 航线销售分析 -->
        <el-card class="box-card">
          <div slot="header"><span>航线销售分析</span></div>
          <el-date-picker v-model="salesDateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期"></el-date-picker>
          <el-button @click="fetchRouteSales" :loading="loadingStats">查询</el-button>
          <p v-if="salesAnalysis.suggestion"><b>打折策略建议:</b> {{ salesAnalysis.suggestion }}</p>
          <el-table :data="salesAnalysis.data" stripe v-loading="loadingStats">
            <el-table-column prop="departureAirport" label="出发地"></el-table-column>
            <el-table-column prop="arrivalAirport" label="目的地"></el-table-column>
            <el-table-column prop="ticketCount" label="售出票数"></el-table-column>
            <el-table-column prop="totalRevenue" label="总销售额"></el-table-column>
          </el-table>
        </el-card>
        <!-- 其他统计报表 -->
        <el-card class="box-card">
          <div slot="header"><span>分类统计</span></div>
          <el-select v-model="reportDimension" placeholder="选择统计维度">
            <el-option label="按航空公司" value="airline"></el-option>
            <el-option label="按机型" value="aircraftmodel"></el-option>
            <el-option label="按热门航线" value="route"></el-option>
          </el-select>
          <el-button @click="fetchGenericReport" :loading="loadingStats">查询</el-button>
          <el-table :data="genericReportData" stripe v-loading="loadingStats">
            <el-table-column prop="dimension" label="维度"></el-table-column>
            <el-table-column prop="count" label="票量"></el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 新增/编辑航班的对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible">
      <el-form :model="flightForm" ref="flightForm" label-width="100px">
        <!-- 表单项... -->
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveFlight">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
// 引入所有需要的API
import api from '../api'; 

export default {
  name: 'AdminView',
  data() {
    return {
      activeTab: 'flight-management',
      loading: false,
      loadingStats: false,
      flights: [],
      dialogVisible: false,
      dialogTitle: '',
      flightForm: {}, // 用于新增/编辑的表单数据
      
      // 统计相关
      salesDateRange: [],
      salesAnalysis: {},
      reportDimension: 'airline',
      genericReportData: [],
    };
  },
  created() {
    this.fetchFlights();
  },
  methods: {
    // --- 航班管理 ---
    async fetchFlights() {
      this.loading = true;
      try {
        const response = await api.admin.getAllFlights(); // 假设api已按模块组织
        this.flights = response.data;
      } finally {
        this.loading = false;
      }
    },
    openFlightDialog(flight = null) {
      if (flight) {
        this.dialogTitle = '编辑航班';
        this.flightForm = { ...flight };
      } else {
        this.dialogTitle = '新增航班';
        this.flightForm = {}; // 清空表单
      }
      this.dialogVisible = true;
    },
    async saveFlight() {
      // ... 表单校验 ...
      try {
        if (this.flightForm.flightNumber) { // 编辑
          await api.admin.updateFlight(this.flightForm.flightNumber, this.flightForm);
        } else { // 新增
          await api.admin.createFlight(this.flightForm);
        }
        this.$message.success('保存成功');
        this.dialogVisible = false;
        this.fetchFlights(); // 刷新列表
      } catch (error) {
        // 错误已由拦截器处理
      }
    },
    async deleteFlight(flightNumber) {
      await this.$confirm('此操作将永久删除该航班, 是否继续?', '提示');
      await api.admin.deleteFlight(flightNumber);
      this.$message.success('删除成功');
      this.fetchFlights();
    },

    // --- 统计报表 ---
    async fetchRouteSales() {
        
    },
    async fetchGenericReport() {
        
    }
  }
};
</script>

<style scoped>
.admin-page { padding: 20px; }
.toolbar { margin-bottom: 20px; }
.box-card { margin-top: 20px; }
</style>