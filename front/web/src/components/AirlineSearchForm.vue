<template>
  <form @submit.prevent="submitSearch" class="search-form-container">
    <div class="form-group">
      <label for="airline">选择航空公司</label>
      <el-select 
        id="airline"
        v-model="form.airlineCode" 
        placeholder="请选择或搜索航空公司" 
        filterable 
        style="width: 100%;"
      >
        <el-option
          v-for="airline in airlines"
          :key="airline.airlineCode"
          :label="airline.airlineName + ' (' + airline.airlineCode + ')'"
          :value="airline.airlineCode">
        </el-option>
      </el-select>
    </div>
    
    <div class="form-group">
      <label for="date-airline">查询日期</label>
      <el-date-picker
        id="date-airline"
        v-model="form.flightDate"
        type="date"
        placeholder="选择日期"
        format="yyyy-MM-dd"
        value-format="yyyy-MM-dd"
        style="width: 100%;">
      </el-date-picker>
    </div>
    
    <button type="submit" class="btn search-btn" :disabled="isLoading">
      <i class="fas fa-search"></i> {{ isLoading ? '查询航班' : '查询航班' }}
    </button>
  </form>
</template>

<script>
export default {
  name: 'AirlineSearchForm',
  props: {
    isLoading: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      form: {
        airlineCode: 'CA', // 默认选择国航
        flightDate: new Date().toISOString().split('T')[0],
      },
      // 航空公司列表。在实际应用中，这个列表应该从后端API获取。
      // 为方便演示，这里先硬编码。
      airlines: [
        { airlineCode: 'CA', airlineName: '中国国际航空' },
        { airlineCode: 'MU', airlineName: '中国东方航空' },
        { airlineCode: 'CZ', airlineName: '中国南方航空' },
        { airlineCode: 'HU', airlineName: '海南航空' },
        { airlineCode: '3U', airlineName: '四川航空' },
        { airlineCode: 'MF', airlineName: '厦门航空' },
        { airlineCode: 'ZH', airlineName: '深圳航空' },
        { airlineCode: 'SC', airlineName: '山东航空' },
      ],
    };
  },
  methods: {
    submitSearch() {
      if (!this.form.airlineCode || !this.form.flightDate) {
        this.$message.warning('请选择航空公司和日期！');
        return;
      }
      // 触发 search 事件，将表单数据传递给父组件 HomeView
      // HomeView 会根据 searchMode 来适配成后端需要的格式
      this.$emit('search', { ...this.form });
    },
  },
};
</script>

<style scoped>
.search-form-container {
  display: grid;
  grid-template-columns: 1fr 1fr; /* 两列布局 */
  gap: 20px;
  align-items: end; /* 使元素底部对齐 */
}

.form-group { 
  margin-bottom: 0; 
  display: flex;
  flex-direction: column;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #fff; /* 假设在深色背景下，标签为白色 */
}

/* 适配 Element UI 的选择框和日期选择器 */
::v-deep .el-select, ::v-deep .el-date-editor {
  width: 100% !important;
}

.search-btn {
  grid-column: 1 / -1; /* 按钮横跨两列 */
  background-color: #ff9800;
  color: white;
  padding: 15px;
  font-size: 18px;
  font-weight: bold;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.search-btn:hover {
  background-color: #f57c00;
}

.search-btn:disabled {
    background-color: var(--gray);
    cursor: not-allowed;
}
</style>