<template>
  <div class="home-container">
    <!-- 搜索区域：背景图 + 悬浮表单 -->
    <div class="search-hero">
      <div class="search-box-card">
        <el-tabs v-model="searchMode" type="card" class="mode-tabs">
          <el-tab-pane label="按航线查询" name="byRoute"></el-tab-pane>
          <el-tab-pane label="按航空公司查询" name="byAirline"></el-tab-pane>
        </el-tabs>

        <div class="form-wrapper">
          <flight-search-form
              v-if="searchMode === 'byRoute'"
              @search="handleSearch"
              :is-loading="isLoading"
          />
          <airline-search-form
              v-else
              @search="handleSearch"
              :is-loading="isLoading"
          />
        </div>
      </div>
    </div>

    <!-- 结果展示区域 -->
    <div class="result-section">
      <div class="section-header">
        <div class="title-group">
          <i class="el-icon-medal-1 title-icon"></i>
          <h3 class="title-text">{{ resultTitle }}</h3>
        </div>
        <div class="search-info" v-if="lastSearchParams">
          <el-tag size="medium" effect="plain" type="info" class="info-tag">
            <i class="el-icon-date"></i> {{ searchSummary }}
          </el-tag>
        </div>
      </div>

      <div class="result-body">
        <div v-if="isLoading" class="state-box">
          <i class="el-icon-loading"></i><p>正在为您寻找最佳航班...</p>
        </div>

        <div v-else-if="!flights.length" class="state-box empty">
          <el-empty description="抱歉，未找到符合条件的航班" :image-size="120"></el-empty>
        </div>

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
      lastSearchParams: null,
      isInitialLoad: true,
    };
  },
  computed: {
    resultTitle() {
      return this.isInitialLoad ? '热门航线推荐' : '查询结果';
    },
    displayDate() {
      return this.lastSearchParams?.flightDate || new Date().toISOString().split('T')[0];
    },
    searchSummary() {
      if (!this.lastSearchParams) return this.displayDate;
      if (this.lastSearchParams.searchType === 'byRoute') {
        const [dep, arr] = (this.lastSearchParams.value || "").split('-');
        return `${dep || '未知'} → ${arr || '未知'} (${this.lastSearchParams.flightDate})`;
      }
      return `${this.lastSearchParams.value} (${this.lastSearchParams.flightDate})`;
    }
  },
  created() {
    this.loadInitialFlights();
  },
  methods: {
    async loadInitialFlights() {
      this.isLoading = true;
      this.isInitialLoad = true;
      const today = new Date().toISOString().split('T')[0];
      this.lastSearchParams = { searchType: 'all', flightDate: today };

      try {
        const response = await api.getAllFlights();
        this.flights = response.data || [];
      } catch (error) {
        this.flights = [];
      } finally {
        this.isLoading = false;
      }
    },
    async handleSearch(searchParams) {
      this.isLoading = true;
      this.isInitialLoad = false;

      const apiParams = searchParams.departureAirport
          ? { searchType: 'byRoute', value: `${searchParams.departureAirport}-${searchParams.arrivalAirport}`, flightDate: searchParams.flightDate }
          : { searchType: 'byAirline', value: searchParams.airlineCode, flightDate: searchParams.flightDate };

      this.lastSearchParams = apiParams;

      try {
        const response = await api.searchFlights(apiParams);
        this.flights = response.data || [];
        if (!this.flights.length) this.$message.info('暂无符合条件的航班');
      } catch (error) {
        this.flights = [];
      } finally {
        this.isLoading = false;
      }
    }
  }
};
</script>

<style scoped>
.home-container { min-height: 80vh; background-color: #f4f7f9; }

/* 头部搜索区美化 */
.search-hero {
  background: linear-gradient(rgba(0, 0, 0, 0.3), rgba(0, 0, 0, 0.3)), url('https://images.unsplash.com/photo-1436491865332-7a61a109c0f2?w=1400&q=80');
  background-size: cover;
  background-position: center;
  padding: 80px 20px 120px;
  text-align: center;
}

.search-box-card {
  max-width: 1000px;
  margin: 0 auto;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 15px 35px rgba(0,0,0,0.2);
  overflow: hidden;
}

.form-wrapper { padding: 30px 40px; }

/* 标签页样式 */
::v-deep .el-tabs--card > .el-tabs__header {
  border: none;
  background: #f8f9fa;
  margin: 0;
}
::v-deep .el-tabs__item {
  height: 50px;
  line-height: 50px;
  font-weight: 600;
}
::v-deep .el-tabs__item.is-active {
  background-color: #fff !important;
  color: #409EFF !important;
}

/* 结果区域样式 */
.result-section {
  max-width: 1100px;
  margin: -50px auto 50px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.05);
  min-height: 400px;
}

.section-header {
  padding: 25px 30px;
  border-bottom: 1px solid #f0f2f5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-group { display: flex; align-items: center; }
.title-icon { font-size: 28px; color: #f7ba2a; margin-right: 12px; }
.title-text { font-size: 20px; font-weight: 700; color: #2c3e50; margin: 0; }

.state-box {
  text-align: center;
  padding: 100px 0;
  color: #909399;
}
.state-box i { font-size: 40px; margin-bottom: 15px; }

/* 搜索按钮全局美化（通过深度选择器） */
::v-deep .search-btn {
  background: linear-gradient(90deg, #ff9800, #f57c00) !important;
  border: none !important;
  height: 50px;
  border-radius: 25px !important;
  font-size: 18px !important;
  margin-top: 10px;
}
</style>