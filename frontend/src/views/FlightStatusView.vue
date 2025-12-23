<template>
  <div class="flight-status-page">
    <div class="hero-banner">
      <div class="hero-content">
        <h1>关注航班动态，轻松安排出行</h1>
      </div>
    </div>

    <div class="container search-container">
      <div class="card search-card">
        <div class="tab-content">
          <flight-status-search-form
              @search="handleStatusSearch"
              :is-loading="isLoading"
          />
        </div>
      </div>
    </div>

    <div class="container results-container" v-if="searchPerformed">
      <div v-if="isLoading" class="loading-state">
        <i class="el-icon-loading"></i><p>正在查询航班动态...</p>
      </div>
      <!-- 【修改】flightResults 数组为空时显示 -->
      <div v-else-if="!flightResults || flightResults.length === 0" class="no-results-state">
        <i class="el-icon-info"></i><p>未找到该航班信息，请检查航班号是否正确。</p>
      </div>
      <!-- 【修改】遍历 flightResults 数组，渲染多张卡片 -->
      <div v-else>
        <flight-status-detail
            v-for="flight in flightResults"
            :key="flight.flightNumber"
            :flight="flight"
            class="mb-3"
        />
      </div>
    </div>
  </div>
</template>

<script>
import FlightStatusSearchForm from '../components/FlightStatusSearchForm.vue';
import FlightStatusDetail from '../components/FlightStatusDetail.vue';
import api from '../api';

export default {
  name: 'FlightStatusView',
  components: { FlightStatusSearchForm, FlightStatusDetail },
  data() {
    return {
      isLoading: false,
      searchPerformed: false,
      flightResults: [], // 【修改】这里从单个对象改为数组
    };
  },
  methods: {
    /**
     * @param {string} flightNumber
     */
    async handleStatusSearch(flightNumber) {
      this.isLoading = true;
      this.searchPerformed = true;
      this.flightResults = []; // 清空数组

      try {
        const response = await api.getFlightStatus(flightNumber);
        // 【修改】后端现在返回的是 List，直接赋值
        this.flightResults = response.data;
      } catch (error) {
        console.error("航班动态查询失败:", error);
        this.flightResults = [];
      } finally {
        this.isLoading = false;
      }
    }
  }
};
</script>

<style scoped>
.hero-banner{background:linear-gradient(rgba(20,80,180,.7),rgba(10,40,100,.8)),url(https://images.unsplash.com/photo-1570715722755-58f86a5a55a5?auto=format&fit=crop&w=1920&q=80);background-size:cover;background-position:center;color:#fff;padding:80px 20px;text-align:center}
.hero-content h1{font-size:42px;font-weight:700;margin-bottom:15px}
.search-container{margin-top:-60px;position:relative;z-index:10}
.search-card{padding:0}
.tab-content{padding:30px;background-color:#fff;border-radius:8px}
.results-container{margin-top:30px}
.loading-state,.no-results-state{text-align:center;padding:40px;color:var(--gray)}
.loading-state i,.no-results-state i{font-size:48px;margin-bottom:20px;display:block}
.mb-3 { margin-bottom: 1.5rem; } /* 新增间距样式 */
</style>