<template>
  <div class="flight-status-page">
    <!-- 1. 沉浸式头部 Banner -->
    <div class="hero-banner">
      <div class="hero-content">
        <div class="hero-badge">FLIGHT STATUS</div>
        <h1>关注航班动态，轻松安排出行</h1>
        <p class="hero-subtitle">实时同步全球空管数据，精准掌握起降时间</p>
      </div>
    </div>

    <!-- 2. 搜索区域 -->
    <div class="container search-container">
      <div class="search-card-wrapper">
        <div class="search-header-tabs">
          <span class="active-tab"><i class="el-icon-search"></i> 航班号查询</span>
        </div>
        <div class="search-body">
          <flight-status-search-form
              @search="handleStatusSearch"
              :is-loading="isLoading"
          />
        </div>
      </div>
    </div>

    <!-- 3. 查询结果展示区 -->
    <div class="container results-container" v-if="searchPerformed">
      <!-- 加载状态 -->
      <div v-if="isLoading" class="state-box loading-state">
        <div class="loader-spinner"></div>
        <p>正在同步最新的飞行数据...</p>
      </div>

      <!-- 无结果状态 -->
      <div v-else-if="!flightResults || flightResults.length === 0" class="state-box no-results-state">
        <div class="empty-icon-wrapper">
          <i class="el-icon-warning-outline"></i>
        </div>
        <h3>未找到相关航班</h3>
        <p>请检查航班号（如 CA1234）是否输入正确，或尝试查询其他日期。</p>
      </div>

      <!-- 结果列表 -->
      <div v-else class="results-list">
        <div class="results-header">
          <span>共找到 {{ flightResults.length }} 条相关航班动态</span>
        </div>
        <div class="fade-in-up">
          <flight-status-detail
              v-for="flight in flightResults"
              :key="flight.flightNumber"
              :flight="flight"
              class="status-detail-card"
          />
        </div>
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
      flightResults: [],
    };
  },
  methods: {
    async handleStatusSearch(flightNumber) {
      this.isLoading = true;
      this.searchPerformed = true;
      this.flightResults = [];

      try {
        const response = await api.getFlightStatus(flightNumber);
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
/* 颜色定义 */
:root {
  --primary-blue: #82B1B7;
  --accent-gold: #DCC87F;
  --danger-red: #9B1C31;
  --bg-sand: #E4DFDB;
  --text-gray: #ABAAA5;
}

.flight-status-page {
  background-color: #E4DFDB; /* 基色背景 */
  min-height: 100vh;
  padding-bottom: 60px;
}

/* Hero Banner 优化 */
.hero-banner {
  background: linear-gradient(135deg, rgba(26, 54, 93, 0.85) 0%, rgba(130, 177, 183, 0.9) 100%),
  url('https://images.unsplash.com/photo-1570715722755-58f86a5a55a5?auto=format&fit=crop&w=1920&q=80');
  background-size: cover;
  background-position: center;
  color: #fff;
  padding: 100px 20px 140px;
  text-align: center;
}

.hero-badge {
  display: inline-block;
  background: #DCC87F;
  color: #333;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 2px;
  margin-bottom: 20px;
}

.hero-content h1 {
  font-size: 42px;
  font-weight: 800;
  margin-bottom: 10px;
  text-shadow: 0 2px 10px rgba(0,0,0,0.2);
}

.hero-subtitle {
  font-size: 18px;
  opacity: 0.9;
  font-weight: 300;
}

/* 搜索卡片优化 */
.search-container {
  margin-top: -80px;
  max-width: 900px;
  margin-left: auto;
  margin-right: auto;
  padding: 0 20px;
}

.search-card-wrapper {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 20px 50px rgba(0,0,0,0.1);
}

.search-header-tabs {
  background: #f8fafc;
  padding: 15px 30px;
  border-bottom: 1px solid #eee;
}

.active-tab {
  color: #82B1B7;
  font-weight: 700;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-body {
  padding: 40px;
}

/* 结果区域 */
.results-container {
  margin-top: 40px;
  max-width: 900px;
  margin-left: auto;
  margin-right: auto;
  padding: 0 20px;
}

.results-header {
  margin-bottom: 20px;
  font-size: 14px;
  color: #ABAAA5;
  font-weight: 600;
  border-left: 4px solid #82B1B7;
  padding-left: 12px;
}

.status-detail-card {
  margin-bottom: 25px;
  transition: transform 0.3s cubic-bezier(0.15, 0.83, 0.66, 1);
}

.status-detail-card:hover {
  transform: translateY(-5px);
}

/* 状态展示 */
.state-box {
  background: #fff;
  border-radius: 12px;
  padding: 60px 40px;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0,0,0,0.05);
}

.loading-state p {
  color: #82B1B7;
  font-weight: 600;
  margin-top: 20px;
}

.loader-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #82B1B7;
  border-radius: 50%;
  margin: 0 auto;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.no-results-state h3 {
  color: #333;
  margin: 20px 0 10px;
}

.no-results-state p {
  color: #ABAAA5;
  font-size: 15px;
}

.empty-icon-wrapper {
  font-size: 60px;
  color: #9B1C31;
  opacity: 0.2;
}

/* 动画 */
.fade-in-up {
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .hero-banner { padding: 60px 20px 100px; }
  .hero-content h1 { font-size: 28px; }
  .search-body { padding: 25px; }
}
</style>