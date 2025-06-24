<template>
  <div>
    <!-- 卡片1: 航班搜索表单 -->
    <div class="card search-section">
      <div class="card-body" style="padding: 0;">
        <!-- 搜索模式切换 -->
        <el-tabs v-model="searchMode" type="card" class="search-mode-tabs">
          <el-tab-pane label="按航线查询" name="byRoute"></el-tab-pane>
          <el-tab-pane label="按航空公司查询" name="byAirline"></el-tab-pane>
        </el-tabs>
        <div class="search-form-container">
          <flight-search-form 
            v-if="searchMode === 'byRoute'"
            @search="handleSearch" 
            :is-loading="isLoading" 
          />
          <airline-search-form 
            v-if="searchMode === 'byAirline'"
            @search="handleSearch" 
            :is-loading="isLoading" 
          />
        </div>
      </div>
    </div>

    <!-- 卡片2: 航班结果展示 -->
    <div class="card">
      <div class="card-header">
        <h3 class="card-title">{{ resultTitle }}</h3>
        <span v-if="lastSearchParams" class="text-muted">{{ searchSummary }}</span>
      </div>
      <div class="card-body">
        <div v-if="isLoading" class="loading-state">
          <i class="el-icon-loading"></i><p>正在加载航班信息...</p>
        </div>
        <div v-else-if="!flights.length" class="no-results-state">
          <i class="el-icon-info"></i><p>抱歉，未找到符合条件的航班。</p>
        </div>
        <!-- 无论默认加载还是搜索结果，都使用 FlightList 组件展示 -->
        <flight-list v-else :flights="flights" :search-date="displayDate" />
      </div>
    </div>
  </div>
</template>

<script>
import FlightSearchForm from '../components/FlightSearchForm.vue';
import AirlineSearchForm from '../components/AirlineSearchForm.vue';
import FlightList from '../components/FlightList.vue';
import api from '../api';

export default {
  name: 'HomeView',
  components: { FlightSearchForm, AirlineSearchForm, FlightList },
  data() {
    return {
      searchMode: 'byRoute',
      isLoading: false,
      flights: [],
      lastSearchParams: null, // 存储上一次的搜索参数
      isInitialLoad: true, // 标记是否为初始加载
    };
  },
  computed: {
    // 动态显示结果区的标题
    resultTitle() {
      return this.isInitialLoad ? '今日推荐航班' : '查询结果';
    },
    // 显示的日期，优先使用搜索日期，否则为今天
    displayDate() {
      return this.lastSearchParams?.flightDate || new Date().toISOString().split('T')[0];
    },
    // 动态生成搜索摘要
    searchSummary() {
      if (!this.lastSearchParams) return `日期: ${this.displayDate}`;
      if (this.lastSearchParams.searchType === 'byRoute') {
        const [dep, arr] = this.lastSearchParams.value.split('-');
        return `${dep} → ${arr} (${this.lastSearchParams.flightDate})`;
      }
      if (this.lastSearchParams.searchType === 'byAirline') {
        return `航空公司: ${this.lastSearchParams.value} (${this.lastSearchParams.flightDate})`;
      }
      return '';
    }
  },
  created() {
    // 组件创建时，执行初始航班加载
    this.loadInitialFlights();
  },
  methods: {
    // 加载所有航班作为默认显示
    async loadInitialFlights() {
      this.isLoading = true;
      this.isInitialLoad = true;
      this.lastSearchParams = null;
      const today = new Date().toISOString().split('T')[0];
      try {
        const response = await api.getAllFlights(today);
        this.flights = response.data;
      } catch (error) {
        console.error("加载初始航班失败:", error);
      } finally {
        this.isLoading = false;
      }
    },
    // 处理搜索事件
    async handleSearch(searchParams) {
      this.isLoading = true;
      this.isInitialLoad = false; // 用户已执行搜索
      this.flights = [];
      
      let apiParams;
      if (searchParams.departureAirport) {
        apiParams = {
          searchType: 'byRoute',
          value: `${searchParams.departureAirport}-${searchParams.arrivalAirport}`,
          flightDate: searchParams.flightDate
        };
      } else {
        apiParams = {
          searchType: 'byAirline',
          value: searchParams.airlineCode,
          flightDate: searchParams.flightDate
        };
      }
      this.lastSearchParams = apiParams;

      try {
        const response = await api.searchFlights(apiParams);
        this.flights = response.data;
      } catch (error) {
        console.error("航班搜索失败:", error);
      } finally {
        this.isLoading = false;
      }
    },
  },
};
</script>

<style scoped>
/* 这些样式之前已经定义过，这里是回顾 */
.loading-state, .no-results-state {
  text-align: center;
  padding: 40px 20px;
  color: var(--gray);
}
.loading-state .el-icon-loading, .no-results-state .el-icon-info {
  font-size: 48px;
  margin-bottom: 20px;
}
.no-results-state span {
  font-size: 14px;
}
.text-muted {
  color: var(--gray);
  font-size: 14px;
}
</style>