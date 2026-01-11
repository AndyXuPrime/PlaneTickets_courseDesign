<template>
  <form @submit.prevent="submitSearch" class="search-form">
    <div class="form-group">
      <label for="departure">出发城市机场</label>
      <el-input id="departure" v-model.trim="form.departureAirport" placeholder="例如: 北京" clearable></el-input>
    </div>

    <div class="form-group">
      <label for="arrival">到达城市机场</label>
      <el-input id="arrival" v-model.trim="form.arrivalAirport" placeholder="例如: 上海" clearable></el-input>
    </div>

    <div class="form-group">
      <label for="date">出发日期</label>
      <el-date-picker id="date" v-model="form.flightDate" type="date" placeholder="选择日期" format="yyyy-MM-dd"
        value-format="yyyy-MM-dd" :picker-options="pickerOptions" style="width: 100%;">
      </el-date-picker>
    </div>

    <button type="submit" class="btn search-btn" :disabled="isLoading">
      <i class="fas fa-search"></i> {{ isLoading ? '正在搜索...' : '搜索航班' }}
    </button>
  </form>
</template>

<script>
export default {
  name: 'FlightSearchForm',
  props: {
    isLoading: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    // 获取今天的日期，格式为 YYYY-MM-DD
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    return {
      form: {
        departureAirport: '北京',
        arrivalAirport: '上海',
        flightDate: today.toISOString().split('T')[0],
      },
      pickerOptions: {
        // 禁止选择今天之前的日期
        disabledDate(time) {
          return time.getTime() < today.getTime();
        },
      },
    };
  },
  methods: {
    submitSearch() {
    
      if (!this.form.departureAirport || !this.form.arrivalAirport || !this.form.flightDate) {
        this.$message.warning('请填写完整的出发地、目的地和日期！');
        return;
      }
      
      this.$emit('search', { ...this.form });
    },
  },
};
</script>