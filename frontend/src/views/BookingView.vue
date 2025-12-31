<template>
  <div class="booking-page">
    <div class="booking-card" v-loading="loading">
      <!-- 1. 标题栏 -->
      <div class="card-header-title">确认订单信息 / ORDER CONFIRMATION</div>

      <div class="card-content" v-if="flight">
        <div class="steps-container">

          <!-- 1. 航班简报 -->
          <div class="step">
            <span class="section-label">航班行程信息</span>
            <div class="flight-info-box">
              <div class="flight-top">
                <span class="airline-brand">
                  <i class="el-icon-s-promotion"></i>
                  {{ flight.airlineName }} <small>{{ flight.flightNumber }}</small>
                </span>
                <div class="date-badge">{{ selectedDate }}</div>
              </div>
              <div class="route-grid">
                <div class="airport-node">
                  <div class="time">{{ flight.departureTime }}</div>
                  <div class="name">{{ flight.departureAirport }}</div>
                </div>
                <div class="flight-path">
                  <div class="path-text">直飞航线</div>
                  <div class="path-line"></div>
                  <i class="el-icon-location-information"></i>
                </div>
                <div class="airport-node">
                  <div class="time">{{ flight.arrivalTime }}</div>
                  <div class="name">{{ flight.arrivalAirport }}</div>
                </div>
              </div>
            </div>
          </div>

          <hr class="divider-line" />

          <!-- 2. 乘机人选择 -->
          <div class="step">
            <div class="flex-header">
              <span class="section-label">选择乘机人</span>
              <el-button type="text" class="manage-btn" @click="$router.push('/profile')">管理常用乘机人</el-button>
            </div>
            <el-checkbox-group v-model="selectedPassengerIds" class="passenger-selector">
              <el-checkbox-button :label="String(currentUser.id)" class="custom-p-item">
                {{ currentUser.name }} <small>(本人)</small>
              </el-checkbox-button>
              <el-checkbox-button
                  v-for="p in familyList"
                  :key="p.memberId"
                  :label="'f_' + p.memberId"
                  class="custom-p-item"
              >
                {{ p.name }}
              </el-checkbox-button>
            </el-checkbox-group>

            <el-alert
                v-if="selectedPassengerIds.length === 0"
                title="提示：请至少选择一位乘机人以继续预订"
                type="info"
                :closable="false"
                show-icon
                class="mini-alert"
            ></el-alert>
          </div>

          <!-- 3. 舱位选择 -->
          <div class="step">
            <span class="section-label">选择旅行舱位</span>
            <el-radio-group v-model="selectedCabinClass" size="small" class="cabin-selector">
              <el-radio-button label="经济舱" :disabled="flight.economyRemainingSeats < selectedPassengerIds.length">
                经济舱 <small>¥{{ flight.price }}</small>
              </el-radio-button>
              <el-radio-button label="商务舱" :disabled="flight.businessRemainingSeats < selectedPassengerIds.length">
                商务舱 <small>¥{{ (flight.price * 2.5).toFixed(0) }}</small>
              </el-radio-button>
            </el-radio-group>
            <p class="stock-info">
              实时余位：{{ selectedCabinClass === '经济舱' ? flight.economyRemainingSeats : flight.businessRemainingSeats }} 席
            </p>
          </div>

          <!-- 4. 价格明细 -->
          <div class="step payments">
            <span class="section-label">费用明细</span>
            <div class="price-details-grid">
              <div class="detail-item">
                <span>成人机票 ({{ selectedCabinClass }})</span>
                <span>¥{{ currentUnitPrice.toFixed(2) }} × {{ selectedPassengerIds.length }}</span>
              </div>
              <div class="detail-item" v-if="membershipDiscountRate < 1">
                <span>会员折扣 ({{ currentUser.membershipLevel }})</span>
                <span class="discount-text">- ¥{{ discountAmount.toFixed(2) }}</span>
              </div>
            </div>
          </div>

        </div>
      </div>

      <!-- 底部操作栏 -->
      <div class="card-footer-action">
        <div class="total-price-display">
          <span class="total-label">应付总额</span>
          <span class="currency">¥</span>
          <span class="amount">{{ finalTotalPrice.toFixed(2) }}</span>
        </div>
        <button
            class="checkout-btn"
            :disabled="selectedPassengerIds.length === 0"
            @click="handleSubmit"
        >
          {{ isSubmitting ? '处理中...' : '提交订单' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/api';
import { store } from '@/store';

export default {
  name: 'BookingView',
  data() {
    return {
      flight: null,
      selectedDate: '',
      selectedCabinClass: '经济舱',
      selectedPassengerIds: [],
      familyList: [],
      loading: false,
      isSubmitting: false
    };
  },
  computed: {
    currentUser() { return store.user || {}; },
    currentUnitPrice() {
      if (!this.flight) return 0;
      return this.selectedCabinClass === '经济舱' ? this.flight.price : this.flight.price * 2.5;
    },
    membershipDiscountRate() {
      const map = { '白金': 0.85, '金卡': 0.90, '银卡': 0.95 };
      return map[this.currentUser.membershipLevel] || 1.0;
    },
    discountAmount() {
      return (this.currentUnitPrice * (1 - this.membershipDiscountRate)) * this.selectedPassengerIds.length;
    },
    finalTotalPrice() {
      return (this.currentUnitPrice * this.selectedPassengerIds.length) - this.discountAmount;
    }
  },
  async created() {
    const flightData = this.$route.query.flight;
    if (!flightData) {
      this.$message.error('参数缺失，请重新搜索航班');
      return this.$router.push('/');
    }
    this.flight = JSON.parse(flightData);
    this.selectedDate = this.$route.query.date;

    if (this.currentUser.id) {
      this.selectedPassengerIds.push(String(this.currentUser.id));
    }

    this.loading = true;
    try {
      const res = await api.getFamily();
      if (res.code === 200) {
        this.familyList = res.data;
      }
    } catch (e) {
      console.warn('获取常用乘机人失败', e);
    } finally {
      this.loading = false;
    }
  },
  methods: {
    async handleSubmit() {
      if (this.selectedPassengerIds.length === 0) return;
      this.isSubmitting = true;

      const passengerNames = this.selectedPassengerIds.map(id => {
        if (id === String(this.currentUser.id)) {
          return this.currentUser.name;
        }
        const cleanId = id.replace('f_', '');
        const member = this.familyList.find(m => String(m.memberId) === cleanId);
        return member ? member.name : '未知乘客';
      });

      try {
        const bookingRequest = {
          flightNumber: this.flight.flightNumber,
          flightDate: this.selectedDate,
          cabinClass: this.selectedCabinClass,
          passengerNames: passengerNames
        };

        const res = await api.createBooking(bookingRequest);
        if (res.code === 200) {
          this.$message.success('预订成功！');
          this.$router.push('/orders');
        }
      } catch (error) {
        console.error(error);
      } finally {
        this.isSubmitting = false;
      }
    }
  }
};
</script>

<style scoped>
/* 页面背景 */
.booking-page {
  padding: 60px 20px;
  background: #f4f7f9;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  font-family: 'Inter', -apple-system, sans-serif;
}

/* 卡片主体：参考提供的 box-shadow 样式 */
.booking-card {
  width: 550px;
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow:
      0px 187px 75px rgba(0, 0, 0, 0.01),
      0px 105px 63px rgba(0, 0, 0, 0.05),
      0px 47px 47px rgba(0, 0, 0, 0.09),
      0px 12px 26px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(26, 54, 93, 0.1);
}

/* 标题栏 */
.card-header-title {
  width: 100%;
  height: 50px;
  display: flex;
  align-items: center;
  padding-left: 25px;
  border-bottom: 1px solid rgba(26, 54, 93, 0.2);
  background: #fcfdfe;
  font-weight: 700;
  font-size: 13px;
  color: #1a365d;
  letter-spacing: 1px;
}

.card-content {
  padding: 25px;
  background: #fdfefe;
}

.section-label {
  font-size: 13px;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 12px;
  display: block;
}

/* 航班信息卡片 */
.flight-info-box {
  background: #f8fafc;
  border: 1px solid rgba(26, 54, 93, 0.1);
  border-radius: 8px;
  padding: 20px;
}

.flight-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.airline-brand {
  font-weight: 700;
  color: #2a4365;
  font-size: 15px;
}

.airline-brand small {
  color: #718096;
  margin-left: 5px;
}

.date-badge {
  background: #ebf4ff;
  color: #3182ce;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.route-grid {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.airport-node .time {
  font-size: 24px;
  font-weight: 900;
  color: #1a202c;
}

.airport-node .name {
  font-size: 12px;
  color: #718096;
  margin-top: 4px;
}

.flight-path {
  flex: 1;
  text-align: center;
  margin: 0 20px;
  position: relative;
}

.path-text {
  font-size: 11px;
  color: #a0aec0;
  margin-bottom: 4px;
}

.path-line {
  height: 1px;
  background: #cbd5e0;
  width: 100%;
}

.flight-path i {
  position: absolute;
  top: 14px;
  left: 50%;
  transform: translateX(-50%);
  background: #f8fafc;
  padding: 0 8px;
  color: #3182ce;
}

.divider-line {
  height: 1px;
  background-color: rgba(26, 54, 93, 0.1);
  border: none;
  margin: 25px 0;
}

/* 乘机人选择器 */
.passenger-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.custom-p-item ::v-deep .el-checkbox-button__inner {
  border-radius: 6px !important;
  border: 1px solid #e2e8f0 !important;
  background: #fff !important;
  color: #4a5568 !important;
  font-size: 13px;
  padding: 8px 15px;
  box-shadow: none !important;
}

.custom-p-item.is-checked ::v-deep .el-checkbox-button__inner {
  background: #ebf4ff !important;
  border-color: #3182ce !important;
  color: #2b6cb0 !important;
}

.mini-alert {
  margin-top: 12px;
  padding: 6px 12px;
}

/* 舱位选择 */
.cabin-selector ::v-deep .el-radio-button__inner {
  border: 1px solid #e2e8f0 !important;
  margin-right: 10px;
  border-radius: 6px !important;
  font-weight: 600;
}

.cabin-selector ::v-deep .is-active .el-radio-button__inner {
  background-color: #3182ce !important;
  border-color: #3182ce !important;
}

.stock-info {
  font-size: 11px;
  color: #a0aec0;
  margin-top: 8px;
}

/* 价格明细：参考提供的 grid 布局 */
.price-details-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-item {
  display: grid;
  grid-template-columns: 10fr 2fr;
  align-items: center;
}

.detail-item span:first-child {
  font-size: 12px;
  font-weight: 600;
  color: #4a5568;
}

.detail-item span:last-child {
  font-size: 13px;
  font-weight: 700;
  color: #2d3748;
  text-align: right;
}

.discount-text {
  color: #38a169 !important;
}

/* 底部操作区：参考提供的 Checkout Footer */
.card-footer-action {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 20px;
  background-color: rgba(49, 130, 206, 0.08);
  border-top: 1px solid rgba(26, 54, 93, 0.1);
}

.total-price-display {
  display: flex;
  align-items: baseline;
}

.total-label {
  font-size: 12px;
  font-weight: 600;
  color: #4a5568;
  margin-right: 8px;
}

.currency {
  font-size: 14px;
  color: #e53e3e;
  font-weight: 700;
}

.amount {
  font-size: 26px;
  color: #e53e3e;
  font-weight: 900;
  margin-left: 2px;
}

.checkout-btn {
  width: 140px;
  height: 42px;
  background: #2b6cb0;
  border-radius: 8px;
  border: 1px solid #2c5282;
  color: #ffffff;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 6px rgba(43, 108, 176, 0.2);
}

.checkout-btn:hover {
  background: #2c5282;
  transform: translateY(-1px);
}

.checkout-btn:disabled {
  background: #cbd5e0;
  border-color: #a0aec0;
  cursor: not-allowed;
  box-shadow: none;
}
</style>