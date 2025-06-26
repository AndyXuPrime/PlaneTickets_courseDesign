<template>
  <div class="booking-page">
    <div class="booking-card">
      <div class="card-title">订单确认</div>

      <div class="card-content" v-if="flight">
        
        <div class="step">
          <div class="flight-route">
            <div class="airline">
              <i class="el-icon-s-promotion"></i>
              {{ flight.airlineName }} {{ flight.flightNumber }}
            </div>
            
            <div class="price">¥{{ flight.price.toFixed(2) }}</div>
          </div>
          <div class="flight-details">
            <div class="time-place">
              <span class="time">{{ flight.departureTime }}</span>
              <span class="place">{{ flight.departureAirport }}</span>
            </div>
            <div class="arrow-container"><div class="arrow"></div></div>
            <div class="time-place">
              <span class="time">{{ flight.arrivalTime }}</span>
              <span class="place">{{ flight.arrivalAirport }}</span>
            </div>
          </div>
        </div>

      
        <div class="step">
          <el-form label-position="top" class="booking-form">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="乘机人">
                  <el-input :value="currentUser ? currentUser.name : '请先登录'" disabled></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="选择航班日期">
                  <el-date-picker
                    v-model="selectedDate"
                    type="date"
                    placeholder="请选择日期"
                    format="yyyy-MM-dd"
                    value-format="yyyy-MM-dd"
                    :picker-options="pickerOptions"
                    style="width: 100%;"
                  >
                  </el-date-picker>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="舱位">
              <el-radio-group v-model="selectedCabinClass">
                <el-radio-button label="经济舱" :disabled="flight.economyRemainingSeats <= 0">
                  经济舱
                </el-radio-button>
                <el-radio-button label="商务舱" :disabled="flight.businessRemainingSeats <= 0">
                  商务舱
                </el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </div>


      
        <div class="step payments-details">
          <div class="detail-row">
            <span>机票原价</span>
            <span>¥{{ flight.price.toFixed(2) }}</span>
          </div>
          <div class="detail-row" v-if="currentUser && membershipDiscountRate < 1.0">
            <span>会员折扣 ({{ (1 - membershipDiscountRate).toFixed(2) * 100 }}%)</span>
            <span class="discount-price">- ¥{{ (flight.price * (1 - membershipDiscountRate)).toFixed(2) }}</span>
          </div>
        </div>
      </div>
      
      <div class="card-content" v-else>
        <p>正在加载航班信息...</p>
      </div>

      
      <div class="card-footer">
        <div class="total-price">
          <span>总计</span>
          
          <strong>¥{{ finalPrice.toFixed(2) }}</strong>
        </div>
        <el-button
          type="primary"
          class="checkout-btn"
          @click="handleSubmitBooking"
          :loading="isSubmitting"
        >
          {{ isSubmitting ? '提交中...' : '确认并预订' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/api'; 
import { store, mutations } from '@/store'; 

export default {
  name: 'BookingView',
  data() {
    return {
      flight: null, 
      selectedCabinClass: '经济舱',
      isSubmitting: false,
      selectedDate: '',
      pickerOptions: { 
        disabledDate(time) {
          const today = new Date();
          today.setHours(0, 0, 0, 0);
          const oneMonthLater = new Date();
          oneMonthLater.setDate(oneMonthLater.getDate() + 30);
          return time.getTime() < today.getTime() || time.getTime() > oneMonthLater.getTime();
        },
      },
    };
  },
  computed: {
    currentUser() {
      return store.user;
    },
    
    basePrice() {
      if (!this.flight) return 0;
      return this.flight.price || 0;
    },
    membershipDiscountRate() {
      if (!this.currentUser || !this.currentUser.membershipLevel) return 1.0;
      switch (this.currentUser.membershipLevel) {
        case '白金': return 0.85;
        case '金卡': return 0.90;
        case '银卡': return 0.95;
        default: return 1.0;
      }
    },
    
    finalPrice() {
      return this.basePrice * this.membershipDiscountRate;
    },
  },
  methods: {
    async handleSubmitBooking() {
      
      if (!this.selectedDate) {
        this.$message.error('请选择航班日期！');
        return;
      }

      this.isSubmitting = true;
      try {
        const bookingRequest = {
          flightNumber: this.flight.flightNumber,
          flightDate: this.selectedDate,
          passengerIds: [this.currentUser.id],
          cabinClass: this.selectedCabinClass,
        
        };

        console.log(bookingRequest);
        const response = await api.createBooking(bookingRequest);

        if (response.code === 200) {
          this.$message.success('预订成功！您的机票状态为“已支付”。即将跳转到我的订单页面。');
          setTimeout(() => {
            this.$router.push({ name: 'MyOrders' });
          }, 2000);
        }
      } catch (error) {
        const errorMessage = error.response?.data?.message || '预订失败，可能是余票不足，请稍后重试。';
        this.$message.error(errorMessage);
      } finally {
        this.isSubmitting = false;
      }
    },
  },
  created() {
    const flightData = this.$route.query.flight;
    if (flightData) {
      try {
        this.flight = JSON.parse(flightData);
        this.selectedDate = this.$route.query.date || '';
        
        
        const defaultCabin = this.flight.cabinClassForDisplay;
        if (defaultCabin === '经济舱' && this.flight.economyRemainingSeats > 0) {
          this.selectedCabinClass = '经济舱';
        } else if (defaultCabin === '商务舱' && this.flight.businessRemainingSeats > 0) {
          this.selectedCabinClass = '商务舱';
        } else if (this.flight.economyRemainingSeats > 0) {
          this.selectedCabinClass = '经济舱';
        } else if (this.flight.businessRemainingSeats > 0) {
          this.selectedCabinClass = '商务舱';
        }
      } catch (e) {
        this.$message.error('航班信息格式错误，请返回重试。');
        this.$router.push('/');
      }
    } else {
        this.$message.error('无法获取航班信息，请返回首页重新搜索。');
        this.$router.push('/');
    }
  },
};
</script>

<style scoped>
.booking-page {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  min-height: calc(100vh - 80px);
  padding: 40px 20px;
  background-color: #e9f7fa;
  box-sizing: border-box;
}
.booking-card {
  width: 100%;
  max-width: 600px;
  background: #e4fbfd;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 84, 166, 0.1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.card-title {
  padding: 15px 20px;
  font-weight: 600;
  font-size: 18px;
  color: #303133;
  border-bottom: 1px solid #eef5fc;
  background-color: #f9fcff;
}
.card-content {
  padding: 20px;
}
.step {
  margin-bottom: 25px;
  border-bottom: 1px solid #123050;
  padding-bottom: 25px;
}
.step:last-child {
  margin-bottom: 0;
  border-bottom: none;
  padding-bottom: 0;
}
.flight-route {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
.airline {
  font-weight: 500;
  color: #0d47a1;
  display: flex;
  align-items: center;
}
.airline i {
  margin-right: 8px;
  font-size: 18px;
}
.price {
  font-size: 20px;
  font-weight: bold;
  color: #195faa;
}
.flight-details {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.time-place {
  display: flex;
  flex-direction: column;
  text-align: center;
}
.time-place .time {
  font-size: 24px;
  font-weight: 500;
  color: #303133;
}
.time-place .place {
  font-size: 14px;
  color: #606266;
  margin-top: 5px;
}
.arrow-container {
  flex-grow: 1;
  padding: 0 20px;
}
.arrow {
  width: 100%;
  height: 2px;
  background-color: #072746;
  position: relative;
}
.arrow::after {
  content: '';
  position: absolute;
  right: -2px;
  top: -4px;
  border: solid #072746;
  border-width: 0 2px 2px 0;
  display: inline-block;
  padding: 4px;
  transform: rotate(-45deg);
}
.booking-form .el-form-item {
  margin-bottom: 15px;
}
::v-deep .el-form-item__label {
  font-weight: 500 !important;
  color: #303133;
  padding-bottom: 5px !important;
}
::v-deep .el-form-item .el-input.is-disabled .el-input__inner {
  font-size: 25px;      
  font-weight: 750;     
  color: #042c67;       
  background-color: #daeef0; 
  border-color: #d3e7fa;
}
.payments-details .detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #606266;
  padding: 8px 0;
}
.detail-row .discount-price {
  color: #67c23a; 
  font-weight: 500;
}
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background-color: #cfe5fa;
  border-top: 1px solid #133c65;
}
.total-price {
  color: #606266;
}
.total-price strong {
  font-size: 24px;
  color: #042241;
  margin-left: 10px;
}
.checkout-btn {
  font-weight: 500;
  padding: 12px 25px;
  font-size: 16px;
  border-radius: 6px;
}
</style>