<template>
  <div class="flight-list">
    <div v-if="!flights || flights.length === 0" class="no-flights-placeholder">
      <p>暂无航班信息</p>
    </div>
    
    <div v-else>
      <div v-for="flight in flights" :key="flight.flightNumber + flight.cabinClassForDisplay" class="flight-item">
        <!-- 航空公司信息 -->
        <div class="airline-info">
          <div class="airline-logo">{{ getAirlineShortName(flight.airlineName) }}</div>
          <div class="airline-name">{{ flight.airlineName }}</div>
          <div class="flight-details">{{ flight.flightNumber }}</div>
        </div>

        <!-- 航线和时间信息 -->
        <div class="flight-route">
          <div class="departure">
            <div class="flight-time">{{ flight.departureTime }}</div>
            <div class="airport-name">{{ flight.departureAirport }}</div>
          </div>
          <div class="route-arrow">
            <i class="el-icon-right"></i>
          </div>
          <div class="arrival">
            <div class="flight-time">{{ flight.arrivalTime }}</div>
            <div class="airport-name">{{ flight.arrivalAirport }}</div>
          </div>
        </div>

        <!-- 价格和舱位信息 -->
        <div class="price-section">
          <div class="flight-price">
            ¥<span>{{ flight.price }}</span>
          </div>
          <div class="flight-details">
            <span class="badge" :class="getCabinClassBadge(flight.cabinClassForDisplay)">
              {{ flight.cabinClassForDisplay }}
            </span>
            | 剩余: {{ flight.remainingSeats }}
          </div>
        </div>

        <!-- 操作区 -->
        <div class="action-section">
          <button class="btn btn-book" @click="bookFlight(flight)" :disabled="flight.remainingSeats === 0">
            {{ flight.remainingSeats > 0 ? '预订' : '无票' }}
          </button>
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
    /**
     * 【核心】处理预订按钮点击事件
     * @param {object} flight - 被点击的航班对象
     */
    bookFlight(flight) {
      // 1. 检查用户是否登录
      if (!store.isLoggedIn) {
        this.$message.warning('请先登录再进行预订！');
        mutations.setShowLoginModal(true);
        return;
      }

      // 2. 检查必要的日期信息是否存在
      if (!this.searchDate) {
        this.$message.error('缺少航班日期，无法预订。请返回首页重新搜索。');
        return;
      }

      // 3. 用户已登录，执行跳转，并传递完整数据
      this.$router.push({
        name: 'Booking', // 使用路由名称跳转
        query: {
          // 将完整的 flight 对象转换为 JSON 字符串进行传递
          flight: JSON.stringify(flight),
          // 将搜索日期也一并传递过去
          flightDate: this.searchDate
        }
      });
    },
    
    getAirlineShortName(name) {
      if (!name) return '航司';
      if (name.includes('国际')) return '国航';
      if (name.includes('东方')) return '东航';
      if (name.includes('南方')) return '南航';
      if (name.includes('海南')) return '海航';
      if (name.includes('四川')) return '川航';
      if (name.includes('厦门')) return '厦航';
      return name.substring(0, 2);
    },
    
    getCabinClassBadge(cabinClass) {
      if (cabinClass === '商务舱') return 'badge-gold';
      return 'badge-info';
    },
  }
};
</script>

<style scoped>
/* 样式部分保持不变 */
.no-flights-placeholder {
  text-align: center;
  padding: 50px;
  color: #888;
}
.flight-item {
  display: grid; /* 确保你的CSS中有grid布局 */
  grid-template-columns: 150px 1fr 180px 120px;
  align-items: center;
  gap: 20px;
  padding: 15px;
  margin-bottom: 15px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  border-left: 4px solid transparent;
  transition: all 0.3s ease;
}
.flight-item:hover {
  border-left-color: #409EFF;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}
.airline-info { text-align: center; }
.airline-logo { 
  width: 50px; height: 50px;
  line-height: 50px;
  margin: 0 auto 8px; 
  border-radius: 50%;
  font-size: 16px;
  background-color: #e3f2fd;
  color: #409EFF;
  font-weight: bold;
}
.airline-name { font-weight: 500; font-size: 14px; }
.flight-details { font-size: 13px; color: #909399; }
.flight-route { display: flex; align-items: center; justify-content: center; }
.departure, .arrival { text-align: center; }
.flight-time { font-size: 22px; font-weight: 500; color: #303133; }
.airport-name { font-size: 14px; color: #606266; }
.route-arrow { font-size: 24px; color: #DCDFE6; margin: 0 20px; }
.price-section { text-align: right; }
.price-section .flight-price {
    color: #F56C6C; 
    font-size: 18px;
}
.price-section .flight-price span { font-size: 28px; font-weight: bold; }
.price-section .flight-details { margin-top: 5px; }
.badge {
  padding: 3px 8px;
  border-radius: 10px;
  color: #fff;
  font-size: 12px;
}
.badge-info { background-color: #909399; }
.badge-gold { background-color: #E6A23C; }
.action-section { display: flex; align-items: center; justify-content: center; }
.btn-book {
  padding: 8px 25px;
  border: none;
  background-color: #409EFF;
  color: white;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}
.btn-book:hover { background-color: #66b1ff; }
.btn-book:disabled {
  background-color: #c0c4cc;
  cursor: not-allowed;
}
</style>