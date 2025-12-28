<template>
  <div class="flight-list-container">
    <!-- 当无数据时显示 -->
    <div v-if="!flights || flights.length === 0" class="empty-wrapper">
      <el-empty description="暂无符合条件的航班信息" :image-size="100"></el-empty>
    </div>

    <!-- 航班列表 -->
    <div v-else class="list-wrapper">
      <div v-for="flight in flights" :key="flight.flightNumber + flight.cabinClassForDisplay" class="flight-card">

        <!-- 1. 航空公司信息 -->
        <div class="section airline-section">
          <div class="airline-logo">
            {{ getAirlineShortName(flight.airlineName) }}
          </div>
          <div class="airline-detail">
            <div class="airline-name">{{ flight.airlineName }}</div>
            <div class="flight-no">{{ flight.flightNumber }}</div>
          </div>
        </div>

        <!-- 2. 航线与时间轴 -->
        <div class="section route-section">
          <div class="node departure">
            <div class="time">{{ flight.departureTime }}</div>
            <div class="airport">{{ flight.departureAirport }}</div>
          </div>

          <div class="route-line">
            <div class="line-text">直飞</div>
            <div class="line-main">
              <i class="el-icon-location-outline start-dot"></i>
              <div class="dash-line"></div>
              <i class="fas fa-plane plane-icon"></i>
            </div>
          </div>

          <div class="node arrival">
            <div class="time">{{ flight.arrivalTime }}</div>
            <div class="airport">{{ flight.arrivalAirport }}</div>
          </div>
        </div>

        <!-- 3. 价格与舱位 -->
        <div class="section price-section">
          <div class="price-wrapper">
            <span class="currency">¥</span>
            <span class="amount">{{ flight.price }}</span>
            <span class="suffix">起</span>
          </div>
          <div class="cabin-info">
            <el-tag :type="getCabinTagType(flight.cabinClassForDisplay)" size="mini" effect="light">
              {{ flight.cabinClassForDisplay }}
            </el-tag>
            <span class="seats" :class="{ 'low-stock': flight.remainingSeats < 5 }">
              剩 {{ flight.remainingSeats }} 票
            </span>
          </div>
        </div>

        <!-- 4. 操作区 -->
        <div class="section action-section">
          <el-button
              type="primary"
              class="book-button"
              @click="bookFlight(flight)"
              :disabled="flight.remainingSeats === 0"
          >
            {{ flight.remainingSeats > 0 ? '立即预订' : '已售罄' }}
          </el-button>
        </div>

      </div>
    </div>
  </div>
</template>

<script>
import { store, mutations } from '../store';

export default {
  name: 'FlightList',
  props: {
    flights: {
      type: Array,
      required: true,
      default: () => []
    },
    searchDate: {
      type: String,
      required: true,
    }
  },
  methods: {
    bookFlight(flight) {
      if (!store.isLoggedIn) {
        this.$message.warning('请先登录再进行预订！');
        mutations.setShowLoginModal(true);
        return;
      }

      if (!this.searchDate) {
        this.$message.error('缺少航班日期，无法预订。请返回首页重新搜索。');
        return;
      }

      this.$router.push({
        name: 'Booking',
        query: {
          flight: JSON.stringify(flight),
          date: this.searchDate // 统一使用 date 键名
        }
      });
    },

    getAirlineShortName(name) {
      if (!name) return '航司';
      const map = { '国际': '国航', '东方': '东航', '南方': '南航', '海南': '海航', '四川': '川航', '厦门': '厦航' };
      for (let key in map) {
        if (name.includes(key)) return map[key];
      }
      return name.substring(0, 2);
    },

    getCabinTagType(cabin) {
      return cabin === '商务舱' ? 'warning' : 'info';
    }
  }
};
</script>

<style scoped>
.flight-list-container {
  padding: 10px 0;
}

.empty-wrapper {
  padding: 60px 0;
  background: #fff;
  border-radius: 12px;
}

.flight-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px 30px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1px solid #f0f0f0;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  position: relative;
  overflow: hidden;
}

.flight-card:hover {
  box-shadow: 0 10px 20px rgba(0,0,0,0.08);
  transform: translateY(-3px);
  border-color: #409EFF;
}

/* 装饰条 */
.flight-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: transparent;
  transition: background 0.3s;
}
.flight-card:hover::before {
  background: #409EFF;
}

/* 航司部分 */
.airline-section {
  display: flex;
  align-items: center;
  width: 180px;
}
.airline-logo {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  color: #1976d2;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 15px;
  margin-right: 15px;
}
.airline-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}
.flight-no {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

/* 航线轴部分 */
.route-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 30px;
}
.node .time {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
}
.node .airport {
  font-size: 14px;
  color: #606266;
  margin-top: 8px;
}
.departure { text-align: right; }
.arrival { text-align: left; }

.route-line {
  flex: 0.5;
  margin: 0 25px;
  text-align: center;
}
.line-text {
  font-size: 12px;
  color: #c0c4cc;
  margin-bottom: 5px;
}
.line-main {
  display: flex;
  align-items: center;
  position: relative;
}
.dash-line {
  flex: 1;
  height: 2px;
  background-image: linear-gradient(to right, #dcdfe6 50%, rgba(255, 255, 255, 0) 0%);
  background-position: bottom;
  background-size: 8px 2px;
  background-repeat: repeat-x;
}
.plane-icon {
  color: #409EFF;
  font-size: 16px;
  margin-left: 5px;
}

/* 价格部分 */
.price-section {
  width: 160px;
  text-align: right;
  padding-right: 30px;
}
.price-wrapper {
  color: #f56c6c;
  margin-bottom: 8px;
}
.currency { font-size: 16px; font-weight: 600; }
.amount { font-size: 32px; font-weight: 700; margin: 0 2px; }
.suffix { font-size: 12px; color: #909399; }

.cabin-info {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
}
.seats { font-size: 12px; color: #67c23a; }
.seats.low-stock { color: #e6a23c; font-weight: 600; }

/* 按钮部分 */
.book-button {
  padding: 12px 25px;
  font-weight: bold;
  border-radius: 8px;
  letter-spacing: 1px;
}
</style>