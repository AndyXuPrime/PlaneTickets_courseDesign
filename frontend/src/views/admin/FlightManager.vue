<template>
  <div class="manager-container">
    <div class="toolbar">
      <el-button type="primary" icon="el-icon-plus" size="small" @click="openAddDialog">发布新航班</el-button>
      <el-button icon="el-icon-refresh" size="small" @click="fetchFlights">刷新列表</el-button>
    </div>

    <!-- 航班表格 -->
    <el-table :data="flights" border stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="flightNumber" label="航班号" width="100" fixed></el-table-column>
      <el-table-column label="航空公司" width="150">
        <template slot-scope="scope">
          <el-tag size="medium">{{ scope.row.airline ? scope.row.airline.airlineName : '未知' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="起降机场" min-width="200">
        <template slot-scope="scope">
          <el-tooltip :content="scope.row.departureAirport + ' 到 ' + scope.row.arrivalAirport" placement="top">
            <span>{{ scope.row.departureAirport }} <i class="el-icon-right"></i> {{ scope.row.arrivalAirport }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column prop="departureTime" label="起飞" width="100"></el-table-column>
      <el-table-column prop="arrivalTime" label="到达" width="100"></el-table-column>
      <el-table-column label="基础票价" width="120">
        <template slot-scope="scope">
          <b style="color: #f56c6c">¥ {{ scope.row.basePrice }}</b>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template slot-scope="scope">
          <el-button type="text" size="small" @click="editPrice(scope.row)">调价</el-button>
          <el-button type="text" size="small" style="color: #f56c6c" @click="handleCancel(scope.row)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增航班弹窗 -->
    <el-dialog title="发布新航班计划" :visible.sync="dialogVisible" width="600px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="flightForm" label-width="100px" size="medium">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="航班号" prop="flightNumber">
              <el-input v-model="form.flightNumber" placeholder="如: CA1407"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="执行机型" prop="aircraftModel">
              <el-input v-model="form.aircraftModel" placeholder="如: A320"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="航空公司" prop="airlineCode" v-if="isPlatformAdmin">
          <el-select v-model="form.airlineCode" placeholder="请选择航司" style="width: 100%" filterable>
            <el-option v-for="item in airlineOptions" :key="item.airlineCode" :label="item.airlineName" :value="item.airlineCode"></el-option>
          </el-select>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="出发机场" prop="departureAirport">
              <el-select v-model="form.departureAirport" placeholder="请选择" style="width: 100%" filterable>
                <el-option v-for="city in airportOptions" :key="city" :label="city" :value="city"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="到达机场" prop="arrivalAirport">
              <el-select v-model="form.arrivalAirport" placeholder="请选择" style="width: 100%" filterable>
                <el-option v-for="city in airportOptions" :key="city" :label="city" :value="city"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="起飞时间" prop="departureTime">
              <el-time-picker v-model="form.departureTime" value-format="HH:mm:ss" style="width: 100%"></el-time-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="到达时间" prop="arrivalTime">
              <el-time-picker v-model="form.arrivalTime" value-format="HH:mm:ss" style="width: 100%"></el-time-picker>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="经济舱" prop="economySeats">
              <el-input v-model.number="form.economySeats"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="商务舱" prop="businessSeats">
              <el-input v-model.number="form.businessSeats"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="基准价" prop="basePrice">
              <el-input v-model="form.basePrice"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确认发布</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import api from '@/api';
import { store } from '@/store';

export default {
  data() {
    return {
      flights: [],
      loading: false,
      dialogVisible: false,
      submitting: false,
      airlineOptions: [],
      // 预设机场选项
      airportOptions: ['北京首都国际机场', '北京大兴国际机场', '上海虹桥国际机场', '上海浦东国际机场', '广州白云国际机场', '成都天府国际机场', '成都双流国际机场', '深圳宝安国际机场', '昆明长水国际机场', '西安咸阳国际机场', '杭州萧山国际机场'],
      form: {
        flightNumber: '', airlineCode: '', departureAirport: '', arrivalAirport: '',
        departureTime: '', arrivalTime: '', economySeats: 150, businessSeats: 12,
        basePrice: '', aircraftModel: ''
      },
      rules: {
        flightNumber: [{ required: true, message: '请输入航班号', trigger: 'blur' }],
        airlineCode: [{ required: true, message: '请选择航司', trigger: 'change' }],
        departureAirport: [{ required: true, message: '请选择出发机场', trigger: 'change' }],
        arrivalAirport: [{ required: true, message: '请选择到达机场', trigger: 'change' }],
        departureTime: [{ required: true, message: '请选择时间', trigger: 'change' }],
        arrivalTime: [{ required: true, message: '请选择时间', trigger: 'change' }],
        basePrice: [{ required: true, message: '请输入票价', trigger: 'blur' }]
      }
    };
  },
  computed: {
    isPlatformAdmin() { return store.user?.role === 'ROLE_PLATFORM_ADMIN'; }
  },
  created() { this.fetchFlights(); },
  methods: {
    async fetchFlights() {
      this.loading = true;
      try {
        const res = await api.getAdminFlights();
        if (res.code === 200) this.flights = res.data;
      } finally { this.loading = false; }
    },
    async openAddDialog() {
      this.dialogVisible = true;
      if (this.isPlatformAdmin && this.airlineOptions.length === 0) {
        const res = await api.getAllAirlines();
        this.airlineOptions = res.data;
      }
    },
    submitForm() {
      this.$refs.flightForm.validate(async (valid) => {
        if (!valid) return;
        this.submitting = true;
        try {
          // 确保价格是数字
          const submitData = { ...this.form, basePrice: parseFloat(this.form.basePrice) };
          const res = await api.createFlight(submitData);
          if (res.code === 200) {
            this.$message.success('发布成功');
            this.dialogVisible = false;
            this.fetchFlights();
          }
        } catch (e) {
          console.error(e);
        } finally {
          this.submitting = false;
        }
      });
    },
    editPrice(row) {
      this.$prompt('请输入新票价', '价格调整', { inputValue: row.basePrice }).then(async ({ value }) => {
        const res = await api.updateFlightPrice(row.flightNumber, value);
        if (res.code === 200) {
          this.$message.success('已更新');
          this.fetchFlights();
        }
      }).catch(() => {
        // === 【关键修复】加上这个 catch 块 ===
        this.$message.info('已取消修改');
      });
    },
    handleCancel(row) {
      this.$confirm(`确定取消航班 ${row.flightNumber} 吗？`, '提示', { type: 'warning' });
    },
    resetForm() { this.$refs.flightForm.resetFields(); }
  }
};
</script>

<style scoped>
.manager-container { padding: 20px; }
.toolbar { margin-bottom: 20px; }
</style>