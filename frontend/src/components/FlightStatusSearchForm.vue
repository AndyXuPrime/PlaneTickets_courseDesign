<template>
  <div class="flight-status-form">
    <div class="input-group">
      <el-input
        placeholder="请填写航班号进行查询，例如 CA1407"
        v-model.trim="flightNumber"
        class="flight-input"
        clearable
        @keyup.enter.native="submitSearch"
      >
        <i slot="prefix" class="el-input__icon el-icon-s-promotion"></i>
      </el-input>

      <el-button type="warning" @click="submitSearch" class="search-btn-inline" :disabled="isLoading">
        <i class="el-icon-search"></i> 搜索动态
      </el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FlightStatusSearchForm',
  props: {
    isLoading: {
      type: Boolean,
      default: false,
    }
  },
  data() {
    return {
      flightNumber: '', // 只需航班号这一个数据
    };
  },
  methods: {
    submitSearch() {
      if (!this.flightNumber) {
        this.$message.warning('请输入航班号！');
        return;
      }
      // 【核心修正】直接 emit 航班号字符串，这是最简单、最不可能出错的方式
      this.$emit('search', this.flightNumber);
    },
  },
};
</script>

<style scoped>
.input-group {
  display: flex;
  gap: 15px;
  align-items: center;
}
.flight-input {
  flex-grow: 1;
}
.search-btn-inline {
  padding: 0 30px;
  font-weight: bold;
}
::v-deep .el-input__inner, .search-btn-inline {
  height: 50px;
  line-height: 50px;
  font-size: 16px;
}
</style>