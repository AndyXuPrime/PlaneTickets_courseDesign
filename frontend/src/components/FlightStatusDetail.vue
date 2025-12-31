<template>
  <div class="flight-status-detail-card">
    <!-- 顶部：航班号与状态标签 -->
    <div class="card-header">
      <div class="flight-brand">
        <div class="airline-icon"><i class="el-icon-position"></i></div>
        <div class="flight-meta">
          <span class="airline-name">{{ flight.airlineName }}</span>
          <strong class="flight-number">{{ flight.flightNumber }}</strong>
        </div>
      </div>
      <div class="status-badge" :class="statusClass">
        <span class="status-dot"></span>
        {{ flightStatus.text }}
      </div>
    </div>

    <!-- 中部：航程时间轴 -->
    <div class="route-timeline">
      <div class="airport-node departure">
        <div class="label">DEPARTURE</div>
        <div class="time">{{ flight.departureTime }}</div>
        <div class="city">{{ flight.departureAirport }}</div>
      </div>

      <div class="path-container">
        <div class="path-line">
          <div class="plane-icon-wrapper">
            <i class="fas fa-plane"></i>
          </div>
        </div>
      </div>

      <div class="airport-node arrival">
        <div class="label">ARRIVAL</div>
        <div class="time">{{ flight.arrivalTime }}</div>
        <div class="city">{{ flight.arrivalAirport }}</div>
      </div>
    </div>

    <!-- 底部：详细计划与更新时间 -->
    <div class="card-footer">
      <div class="detail-item">
        <span class="d-label">计划起飞</span>
        <span class="d-val">{{ flight.departureTime }}</span>
      </div>
      <div class="detail-item">
        <span class="d-label">计划到达</span>
        <span class="d-val">{{ flight.arrivalTime }}</span>
      </div>
      <div class="update-info">
        <i class="el-icon-refresh"></i> 数据更新于: {{ lastUpdated }}
      </div>
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
    // 保持原功能逻辑不变
    flightStatus() {
      const statuses = [
        { text: '计划中', class: 'on-time' },
        { text: '已起飞', class: 'in-flight' },
        { text: '延误', class: 'delayed' },
        { text: '已到达', class: 'arrived' },
        { text: '已取消', class: 'cancelled' },
      ];
      // 注意：这里保留了您的随机逻辑，实际生产中应由后端状态决定
      return statuses[Math.floor(Math.random() * statuses.length)];
    },
    statusClass() {
      return `status-${this.flightStatus.class}`;
    },
    lastUpdated() {
      return new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
    }
  }
}
</script>

<style scoped>
/* 引入您指定的颜色变量 */
.flight-status-detail-card {
  --color-grey: #ABAAA5;
  --color-blue: #82B1B7;
  --color-red: #9B1C31;
  --color-gold: #DCC87F;
  --color-sand: #E4DFDB;

  background: #ffffff;
  border-radius: 16px;
  border: 1px solid var(--color-sand);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  transition: transform 0.3s ease;
  margin-bottom: 20px;
}

.flight-status-detail-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.08);
}

/* 头部样式 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background-color: #fafafa;
  border-bottom: 1px solid var(--color-sand);
}

.flight-brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.airline-icon {
  width: 36px;
  height: 36px;
  background: var(--color-blue);
  color: white;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.flight-meta {
  display: flex;
  flex-direction: column;
}

.airline-name {
  font-size: 12px;
  color: var(--color-grey);
  font-weight: 600;
  letter-spacing: 0.5px;
}

.flight-number {
  font-size: 20px;
  color: #333;
  letter-spacing: -0.5px;
}

/* 状态标签 */
.status-badge {
  padding: 6px 14px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: currentColor;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.4; }
  100% { opacity: 1; }
}

/* 状态颜色映射 */
.status-on-time, .status-arrived { background-color: #f0f7f8; color: var(--color-blue); }
.status-in-flight { background-color: #ebf4ff; color: #3182ce; }
.status-delayed { background-color: #fffaf0; color: var(--color-gold); }
.status-cancelled { background-color: #fff5f5; color: var(--color-red); }

/* 时间轴样式 */
.route-timeline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 40px 30px;
}

.airport-node {
  flex: 0 0 120px;
}

.airport-node .label {
  font-size: 10px;
  color: var(--color-grey);
  letter-spacing: 1.5px;
  margin-bottom: 8px;
}

.airport-node .time {
  font-size: 32px;
  font-weight: 800;
  color: #1a1a1a;
  line-height: 1;
}

.airport-node .city {
  font-size: 14px;
  color: #4a4a4a;
  margin-top: 6px;
  font-weight: 500;
}

.arrival { text-align: right; }

/* 航线路径装饰 */
.path-container {
  flex: 1;
  padding: 0 30px;
}

.path-line {
  height: 2px;
  background: repeating-linear-gradient(to right, var(--color-sand) 0, var(--color-sand) 5px, transparent 5px, transparent 10px);
  position: relative;
}

.plane-icon-wrapper {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: white;
  padding: 0 15px;
  color: var(--color-blue);
  font-size: 22px;
}

/* 页脚详情 */
.card-footer {
  padding: 16px 24px;
  background-color: #fcfcfc;
  border-top: 1px solid var(--color-sand);
  display: flex;
  align-items: center;
  gap: 24px;
}

.detail-item {
  display: flex;
  flex-direction: column;
}

.d-label {
  font-size: 11px;
  color: var(--color-grey);
  margin-bottom: 2px;
}

.d-val {
  font-size: 13px;
  color: #333;
  font-weight: 600;
}

.update-info {
  margin-left: auto;
  font-size: 12px;
  color: var(--color-grey);
  display: flex;
  align-items: center;
  gap: 4px;
}

@media (max-width: 600px) {
  .route-timeline { padding: 30px 15px; }
  .airport-node .time { font-size: 24px; }
  .path-container { padding: 0 10px; }
  .card-footer { flex-wrap: wrap; gap: 15px; }
}
</style>