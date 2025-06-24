<template>
  <div class="flight-list">
    <!-- 检查是否有航班数据，如果没有则显示提示信息 -->
    <div v-if="!flights || flights.length === 0" class="no-flights-placeholder">
      <p>暂无航班信息</p>
    </div>
    
    <!-- v-else 用于在有航班数据时渲染列表 -->
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
import { store, mutations } from '../store'; // 引入全局状态，用于检查登录

export default {
  name: 'FlightList',
  props: {
    flights: {
      type: Array,
      required: true,
      // 添加一个默认值，增加组件的健壮性
      default: () => []
    },
    // searchDate 这个 prop 保持不变，因为无论何种查询方式，预订时都需要日期
    searchDate: { 
      type: String,
      required: true,
    }
  },
  methods: {
    /**
     * 处理预订按钮点击事件
     * @param {object} flight - 被点击的航班对象
     */
    bookFlight(flight) {
      // 1. 检查用户是否登录
      if (!store.isLoggedIn) {
        this.$message.warning('请先登录再进行预订！');
        // 2. 调用 mutation 打开登录模态框，这是与全局状态交互的最佳实践
        mutations.setShowLoginModal(true);
        return;
      }

      // 3. 用户已登录，执行跳转到订单确认页面
      this.$router.push({
        name: 'Booking', // 确保路由配置中有名为 'Booking' 的路由
        query: {
          flightNumber: flight.flightNumber,
          flightDate: this.searchDate,
          cabinClass: flight.cabinClassForDisplay,
        }
      });
    },

    /**
     * 根据航空公司全称获取简称，用于logo显示
     * @param {string} name - 航空公司全称
     * @returns {string} - 航空公司简称
     */
    getAirlineShortName(name) {
      if (!name) return '航司';
      // 这里的逻辑可以根据实际需要进行扩展
      if (name.includes('国际')) return '国航';
      if (name.includes('东方')) return '东航';
      if (name.includes('南方')) return '南航';
      if (name.includes('海南')) return '海航';
      if (name.includes('四川')) return '川航';
      if (name.includes('厦门')) return '厦航';
      return name.substring(0, 2);
    },

    /**
     * 根据舱位类型返回不同的徽章CSS类
     * @param {string} cabinClass - 舱位名称
     * @returns {string} - CSS类名
     */
    getCabinClassBadge(cabinClass) {
      if (cabinClass === '商务舱') return 'badge-gold';
      return 'badge-info';
    }
  }
};
</script>

<style scoped>
.no-flights-placeholder {
  text-align: center;
  padding: 50px;
  color: var(--gray);
}

.flight-item {
  grid-template-columns: 150px 1fr 180px 120px;
  align-items: center;
  gap: 20px;
  border-left: 4px solid transparent;
  transition: all 0.3s ease;
}
.flight-item:hover {
  border-left-color: var(--primary);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
}
.airline-info { text-align: center; }
.airline-logo { 
  width: 60px; height: 60px;
  line-height: 60px;
  margin: 0 auto 10px; 
  border-radius: 50%;
  font-size: 18px;
  background-color: #e3f2fd;
  color: var(--primary);
  font-weight: bold;
}
.airline-name { font-weight: 500; }
.flight-details { font-size: 13px; color: var(--gray); }
.flight-route { display: flex; align-items: center; justify-content: space-between; }
.departure, .arrival { text-align: center; }
.flight-time { font-size: 22px; font-weight: 600; color: var(--dark); }
.airport-name { font-size: 14px; color: #555; }
.route-arrow { font-size: 24px; color: var(--gray); margin: 0 20px; }
.price-section { text-align: right; }
.price-section .flight-price {
    color: var(--danger); 
}
.price-section .flight-price span { font-size: 28px; font-weight: bold; }
.price-section .flight-details { margin-top: 5px; }
.action-section { justify-content: center; }
.btn-book:disabled {
  background-color: var(--gray);
  cursor: not-allowed;
}
</style>