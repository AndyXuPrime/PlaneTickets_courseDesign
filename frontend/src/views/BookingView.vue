<template>
  <div class="booking-page">
    <div class="booking-card" v-loading="loading">
      <div class="card-title">订单确认</div>

      <div class="card-content" v-if="flight">
        <!-- 1. 航班信息 -->
        <div class="step flight-brief">
          <div class="flight-header">
            <span class="airline">
              <i class="el-icon-s-promotion"></i>
              {{ flight.airlineName }} {{ flight.flightNumber }}
            </span>
            <el-tag size="small" effect="dark" type="primary">{{ selectedDate }}</el-tag>
          </div>
          <div class="route-display">
            <div class="node">
              <div class="time">{{ flight.departureTime }}</div>
              <div class="airport">{{ flight.departureAirport }}</div>
            </div>
            <div class="arrow-box">
              <div class="line-text">直飞</div>
              <div class="line"></div>
              <i class="fas fa-plane"></i>
            </div>
            <div class="node">
              <div class="time">{{ flight.arrivalTime }}</div>
              <div class="airport">{{ flight.arrivalAirport }}</div>
            </div>
          </div>
        </div>

        <!-- 2. 乘机人选择 -->
        <div class="step">
          <div class="section-header">
            <span class="label"><i class="el-icon-user"></i> 选择乘机人</span>
            <el-button type="text" size="mini" @click="$router.push('/profile')">管理常用乘机人</el-button>
          </div>

          <el-checkbox-group v-model="selectedPassengerIds" class="passenger-group">
            <!-- 本人 -->
            <el-checkbox-button :label="String(currentUser.id)" class="p-item">
              {{ currentUser.name }} (本人)
            </el-checkbox-button>

            <!-- 常用乘机人 (ID前加 f_ 区分) -->
            <el-checkbox-button
                v-for="p in familyList"
                :key="p.memberId"
                :label="'f_' + p.memberId"
                class="p-item"
            >
              {{ p.name }}
            </el-checkbox-button>
          </el-checkbox-group>

          <el-alert
              v-if="selectedPassengerIds.length === 0"
              title="请至少选择一位乘机人"
              type="warning"
              :closable="false"
              show-icon
              style="margin-top: 10px"
          ></el-alert>
        </div>

        <!-- 3. 舱位选择 -->
        <div class="step">
          <span class="label"><i class="el-icon-s-marketing"></i> 选择舱位</span>
          <el-radio-group v-model="selectedCabinClass" size="medium" style="margin-top: 10px;">
            <el-radio-button label="经济舱" :disabled="flight.economyRemainingSeats < selectedPassengerIds.length">
              经济舱 (¥{{ flight.price }})
            </el-radio-button>
            <el-radio-button label="商务舱" :disabled="flight.businessRemainingSeats < selectedPassengerIds.length">
              商务舱 (¥{{ (flight.price * 2.5).toFixed(0) }})
            </el-radio-button>
          </el-radio-group>
          <div class="stock-tip">
            当前舱位剩余 {{ selectedCabinClass === '经济舱' ? flight.economyRemainingSeats : flight.businessRemainingSeats }} 席
          </div>
        </div>

        <!-- 4. 价格明细 -->
        <div class="step price-detail">
          <div class="detail-row">
            <span>成人机票 ({{ selectedCabinClass }})</span>
            <span>¥{{ currentUnitPrice.toFixed(2) }} × {{ selectedPassengerIds.length }}</span>
          </div>
          <div class="detail-row" v-if="membershipDiscountRate < 1">
            <span>会员折扣 ({{ currentUser.membershipLevel }})</span>
            <span class="discount">- ¥{{ discountAmount.toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <!-- 底部操作栏 -->
      <div class="card-footer">
        <div class="total-box">
          <span class="text">订单总额</span>
          <span class="price">¥{{ finalTotalPrice.toFixed(2) }}</span>
        </div>
        <el-button
            type="primary"
            class="submit-btn"
            :disabled="selectedPassengerIds.length === 0"
            @click="handleSubmit"
            :loading="isSubmitting"
        >
          确认并预订
        </el-button>
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
      selectedPassengerIds: [], // 这里存的是 ID (如 "4", "f_1")
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

    // 默认选中本人 (转String防止类型不匹配)
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

      // === 核心修改：将选中的 ID 转换为 真实姓名 ===
      // 这样后端就不用再去查数据库匹配 ID 了，直接存名字，绝对不会错
      const passengerNames = this.selectedPassengerIds.map(id => {
        // 1. 如果 ID 等于当前用户 ID，则是本人
        if (id === String(this.currentUser.id)) {
          return this.currentUser.name;
        }
        // 2. 否则是家人，去掉前缀后去 familyList 查找
        const cleanId = id.replace('f_', '');
        // 这里的查找使用 String 强转比较，防止 int/string 不匹配
        const member = this.familyList.find(m => String(m.memberId) === cleanId);
        return member ? member.name : '未知乘客';
      });

      console.log('提交给后端的姓名列表:', passengerNames);

      try {
        // 构造请求参数
        const bookingRequest = {
          flightNumber: this.flight.flightNumber,
          flightDate: this.selectedDate,
          cabinClass: this.selectedCabinClass,
          // 注意：这里传的是 names，需要确保后端 DTO 里的字段名改为 passengerNames
          passengerNames: passengerNames
        };

        const res = await api.createBooking(bookingRequest);
        if (res.code === 200) {
          this.$message.success('预订成功！');
          this.$router.push('/orders');
        }
      } catch (error) {
        // 错误由拦截器统一处理
        console.error(error);
      } finally {
        this.isSubmitting = false;
      }
    }
  }
};
</script>

<style scoped>
.booking-page { padding: 40px 20px; background: #f0f4f8; min-height: 90vh; display: flex; justify-content: center; }
.booking-card { width: 100%; max-width: 700px; background: #fff; border-radius: 16px; box-shadow: 0 10px 30px rgba(0,0,0,0.08); overflow: hidden; }
.card-title { padding: 20px; background: #f8fbff; font-weight: bold; font-size: 18px; border-bottom: 1px solid #edf2f7; color: #303133; }
.card-content { padding: 30px; }
.step { margin-bottom: 30px; }
.label { font-weight: bold; color: #2d3748; font-size: 16px; display: block; margin-bottom: 10px; }
.label i { margin-right: 8px; color: #409EFF; }

.route-display { display: flex; align-items: center; justify-content: space-around; background: #f8fafc; padding: 25px; border-radius: 12px; border: 1px solid #edf2f7; }
.node .time { font-size: 28px; font-weight: 800; color: #1a202c; }
.node .airport { font-size: 14px; color: #718096; margin-top: 8px; }

.arrow-box { flex: 0.4; text-align: center; position: relative; }
.arrow-box .line { height: 2px; background: #cbd5e0; }
.arrow-box .line-text { font-size: 12px; color: #a0aec0; margin-bottom: 5px; }
.arrow-box i { color: #4a5568; position: absolute; top: 16px; left: 45%; background: #f8fafc; padding: 0 10px; }

.passenger-group { display: flex; flex-wrap: wrap; gap: 12px; }
.p-item { margin-bottom: 5px; }
.stock-tip { font-size: 12px; color: #909399; margin-top: 10px; }

.price-detail { background: #fffaf0; padding: 20px; border-radius: 12px; border: 1px dashed #fbd38d; }
.detail-row { display: flex; justify-content: space-between; font-size: 15px; margin-bottom: 10px; color: #4a5568; }
.discount { color: #48bb78; font-weight: bold; }

.card-footer { padding: 25px 40px; background: #f8fbff; border-top: 1px solid #edf2f7; display: flex; justify-content: space-between; align-items: center; }
.total-box .text { font-size: 14px; color: #606266; }
.total-box .price { font-size: 32px; font-weight: 800; color: #e53e3e; margin-left: 10px; font-family: DIN, sans-serif; }
.submit-btn { padding: 14px 40px; font-size: 16px; border-radius: 8px; letter-spacing: 1px; font-weight: 600; }
</style>