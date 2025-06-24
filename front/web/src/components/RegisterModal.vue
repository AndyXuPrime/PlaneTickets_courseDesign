<template>
  <div class="modal" @click.self="$emit('close')">
    <div class="modal-content-styled">
      <!-- 1. 标题和关闭按钮 -->
      <div class="heading">新用户注册</div>
      <button class="close-button" @click="$emit('close')" title="关闭">×</button>
      
      <!-- 2. 注册表单 -->
      <form @submit.prevent="handleRegister" class="form">
        <!-- 输入框网格布局 -->
        <div class="form-grid">
          <!-- 姓名 -->
          <div class="input-field">
            <input type="text" id="reg-name" v-model.trim="form.name" required />
            <label for="reg-name">姓名</label>
          </div>
          <!-- 手机号 -->
          <div class="input-field">
            <input type="tel" id="reg-phone" v-model.trim="form.phone" required pattern="^1[3-9]\d{9}$" />
            <label for="reg-phone">手机号</label>
          </div>
          <!-- 密码 -->
          <div class="input-field">
            <input :type="passwordFieldType" id="reg-password" v-model="form.password" required minlength="6" />
            <label for="reg-password">密码 (至少6位)</label>
            <i :class="['passicon', isPasswordVisible ? 'fas fa-eye-slash' : 'fas fa-eye']" @click="togglePasswordVisibility"></i>
          </div>
          <!-- 身份证号 -->
          <div class="input-field">
            <input type="text" id="reg-idcard" v-model.trim="form.idCard" required pattern="\d{17}[\dX]" />
            <label for="reg-idcard">身份证号</label>
          </div>
        </div>

        <!-- 邮箱 (可选，单独一行) -->
        <div class="input-field">
          <!-- 将 type 设置为 "email" 可以利用浏览器内置的格式校验 -->
          <input type="email" id="reg-email" v-model.trim="form.email" />
          <label for="reg-email">邮箱 (可选)</label>
        </div>

        <!-- 会员等级选择 -->
        <div class="membership-selection">
          <label class="group-label">选择会员等级</label>
          <div class="options-container">
            <div 
              v-for="level in membershipLevels" 
              :key="level.value"
              class="option-card"
              :class="{ selected: form.membershipLevel === level.value }"
              @click="form.membershipLevel = level.value"
            >
              <div class="level-name">{{ level.name }}</div>
              <div class="level-desc">{{ level.desc }}</div>
              <div v-if="form.membershipLevel === level.value" class="selected-badge">
                <i class="fas fa-check"></i>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 主操作按钮 -->
        <div class="btn-container">
          <button type="submit" class="btn-submit" :disabled="isLoading">
            <span>{{ isLoading ? '注册中...' : '同意协议并注册' }}</span>
          </button>
        </div>
      </form>

      <!-- 切换到登录 -->
      <div class="switch-text text-center">
        <p>已有账号？ <a @click="$emit('switch-to-login')">立即登录</a></p>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../api';

export default {
  name: 'RegisterModal',
  data() {
    return {
      isLoading: false,
      isPasswordVisible: false,
      form: {
        name: '',
        password: '',
        phone: '',
        email: '', // email 初始值为空字符串
        idCard: '',
        membershipLevel: '普通', 
      },
      membershipLevels: [
        { name: '普通会员', value: '普通', desc: '基础预订服务' },
        { name: '银卡会员', value: '银卡', desc: '享95折票价' },
        { name: '金卡会员', value: '金卡', desc: '享9折优惠' },
        { name: '白金会员', value: '白金', desc: '享85折优惠' }
      ]
    };
  },
  computed: {
    passwordFieldType() {
      return this.isPasswordVisible ? 'text' : 'password';
    }
  },
  methods: {
    async handleRegister() {
      this.isLoading = true;
      try {
        // 【核心修复】在发送前，对可选的 email 字段进行处理
        // 创建一个载荷副本，避免直接修改 this.form
        const payload = { ...this.form };

        // 如果 email 字段为空字符串，就从载荷中删除它或设为 null
        // 这样可以确保不会将空的 email 字符串发送给后端，从而避免违反数据库约束
        if (!payload.email) {
          delete payload.email; // 或者 payload.email = null;
        }
        
        const response = await api.register(payload); // 发送处理过的 payload
        
        if (response && response.code === 200) { 
          this.$message.success('注册成功！请登录。');
          this.$emit('switch-to-login');
        } else {
          console.warn('注册API的响应格式不符合预期:', response);
          if (!response) {
             this.$message.error('注册请求失败，请稍后重试。');
          }
        }
      } catch (error) {
        console.error('注册时出错:', error);
        // 错误消息已由API拦截器处理
      } finally {
        this.isLoading = false;
      }
    },
    togglePasswordVisibility() {
      this.isPasswordVisible = !this.isPasswordVisible;
    }
  }
};
</script>

<style scoped>
/* 所有样式均与您提供的版本保持一致 */
/* --- 通用模态框样式 --- */
.modal{position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,.5);display:flex;align-items:center;justify-content:center;z-index:1000}
.modal-content-styled{position:relative;background-color:#fff;padding:25px 35px;border-radius:16px;box-shadow:0 5px 20px rgba(0,0,0,.1);width:500px}
.heading{font-size:24px;font-weight:600;text-align:center;margin-bottom:25px;color:#333}
.close-button{position:absolute;top:15px;right:15px;background:none;border:none;font-size:24px;cursor:pointer;color:#aaa;transition:color .2s}
.close-button:hover{color:#333}
/* --- 表单样式 --- */
.form{display:flex;flex-direction:column;gap:20px}
.form-grid{display:grid;grid-template-columns:1fr 1fr;gap:20px}
.input-field{position:relative}
.input-field label{position:absolute;color:#007bff;pointer-events:none;background-color:#fff;padding:0 4px;left:12px;top:-10px;font-size:14px;transition:all .2s ease-in-out}
.input-field input{padding:12px 15px;font-size:16px;border-radius:8px;border:1px solid #ced4da;width:100%;box-sizing:border-box;transition:border-color .2s}
.input-field input:focus{outline:none;border-color:#007bff}
.form .passicon{cursor:pointer;font-size:1.2rem;position:absolute;top:50%;transform:translateY(-50%);right:12px;color:#8d8d8d}
/* --- 会员等级选择 --- */
.membership-selection{margin-top:10px}
.group-label{display:block;margin-bottom:10px;font-weight:500;color:#555;font-size:14px}
.options-container{display:grid;grid-template-columns:1fr 1fr;gap:15px}
.option-card{position:relative;border:1px solid #ced4da;border-radius:8px;padding:15px;text-align:center;cursor:pointer;transition:all .2s ease-in-out}
.option-card:hover{border-color:#89bdf7}
.option-card.selected{border-color:#007bff;background-color:#e7f3ff;box-shadow:0 0 0 2px #007bff}
.level-name{font-weight:600;color:#333;font-size:16px}
.level-desc{font-size:13px;color:#888;margin-top:5px}
.option-card.selected::after{content:'';position:absolute;bottom:-1px;right:20px;width:16px;height:16px;background-color:#007bff;border-radius:50%;border:3px solid #fff;transform:translateY(50%)}
/* --- 提交按钮 --- */
.btn-container{margin-top:10px}
.btn-submit{width:100%;padding:14px 20px;font-size:16px;border-radius:50px;border:none;background:linear-gradient(135deg,#006eff,#0034de);color:#fff;font-weight:700;cursor:pointer;transition:all .3s ease}
.btn-submit:hover{opacity:.9;box-shadow:0 4px 15px rgba(0,110,255,.3)}
.btn-submit:disabled{opacity:.6;cursor:not-allowed}
/* --- 切换链接 --- */
.switch-text{margin-top:20px;color:var(--gray);font-size:14px}
.switch-text a{color:var(--primary);cursor:pointer;text-decoration:none;font-weight:500}
.text-center{text-align:center}
</style>