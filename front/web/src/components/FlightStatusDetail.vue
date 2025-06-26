<template>
  <div class="flight-status-detail-card">
    <div class="header">
      <div class="flight-info">
        <span class="airline-name">{{ flight.airlineName }}</span>
        <strong class="flight-number">{{ flight.flightNumber }}</strong>
      </div>
      <div class="status" :class="statusClass">
        {{ flightStatus.text }}
      </div>
    </div>
    <div class="route-timeline">
      <div class="airport departure">
        <div class="time">{{ flight.departureTime }}</div>
        <div class="city">{{ flight.departureAirport }}</div>
      </div>
      <div class="arrow-line">
        <i class="fas fa-plane"></i>
      </div>
      <div class="airport arrival">
        <div class="time">{{ flight.arrivalTime }}</div>
        <div class="city">{{ flight.arrivalAirport }}</div>
      </div>
    </div>
    <div class="footer-details">
      <span>计划起飞: {{ flight.departureTime }}</span>
      <span>计划到达: {{ flight.arrivalTime }}</span>
      <span>状态更新于: {{ lastUpdated }}</span>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FlightStatusDetail',
  props: {
    flight: {
      type: Object,
      required: true,
    }
  },
  computed: {
    // 模拟航班状态，实际应用中这个状态应该由后端API提供
    flightStatus() {
      const statuses = [
        { text: '计划中', class: 'on-time' },
        { text: '已起飞', class: 'in-flight' },
        { text: '延误', class: 'delayed' },
        { text: '已到达', class: 'arrived' },
        { text: '已取消', class: 'cancelled' },
      ];
      // 随机返回一个状态用于演示
      return statuses[Math.floor(Math.random() * statuses.length)];
    },
    statusClass() {
      return `status-${this.flightStatus.class}`;
    },
    lastUpdated() {
      return new Date().toLocaleTimeString('zh-CN');
    }
  }
}
</script>

<style scoped>
.flight-status-detail-card {
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #e0e0e0;
  border-radius: 12px 12px 0 0;
}
.flight-info .airline-name {
  color: #555;
  margin-right: 10px;
}
.flight-info .flight-number {
  font-size: 20px;
  color: #333;
}
.status {
  padding: 6px 15px;
  border-radius: 20px;
  font-weight: bold;
  color: white;
}
.status-on-time { background-color: #28a745; }
.status-in-flight { background-color: #007bff; }
.status-delayed { background-color: #ffc107; color: #333; }
.status-arrived { background-color: #6c757d; }
.status-cancelled { background-color: #dc3545; }

.route-timeline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 40px 20px;
}
.airport { text-align: center; }
.airport .time { font-size: 28px; font-weight: bold; }
.airport .city { font-size: 16px; color: #555; }

.arrow-line {
  flex-grow: 1;
  height: 2px;
  background-color: #0a25bd;
  margin: 0 20px;
  position: relative;
}
.arrow-line i {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 24px;
  color: var(--primary);
  background-color: white;
  padding: 0 10px;
}

.footer-details {
  background-color: #f8f9fa;
  padding: 15px 20px;
  border-top: 1px solid #e0e0e0;
  border-radius: 0 0 12px 12px;
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #6c757d;
}
</style>