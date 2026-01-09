<template>
  <div class="admin-register-wrapper">
    <!-- 背景：模拟Three.js的旋转粒子深空 -->
    <div class="space-container">
      <div class="stars-1"></div>
      <div class="stars-2"></div>
      <div class="stars-3"></div>
    </div>

    <!-- 装饰网格 -->
    <div class="grid-overlay"></div>

    <div class="register-container">
      <!-- 结构主义装饰条 -->
      <div class="constructivist-bar-top"></div>
      <div class="constructivist-bar-side"></div>

      <div class="register-box">
        <!-- HUD 角标装饰 -->
        <div class="hud-corner top-left"></div>
        <div class="hud-corner top-right"></div>
        <div class="hud-corner bottom-left"></div>
        <div class="hud-corner bottom-right"></div>

        <div class="header">
          <div class="logo-hex">
            <i class="el-icon-school"></i>
          </div>
          <div class="header-text">
            <h1>航司合作伙伴入驻</h1>
            <p>AIRLINE PARTNER APPLICATION // TERMINAL_02</p>
          </div>
        </div>

        <transition name="el-fade-in">
          <el-alert
              v-if="errorMessage"
              :title="errorMessage"
              type="error"
              show-icon
              class="custom-alert"
              @close="errorMessage = ''"
          ></el-alert>
        </transition>

        <el-form :model="form" ref="regForm" :rules="rules" label-position="top" class="constructivist-form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item prop="name">
                <label class="input-label" for="reg-name">ADMIN NAME <span class="status-dot"></span></label>
                <el-input id="reg-name" v-model="form.name" placeholder="真实姓名" class="custom-input"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item prop="phone">
                <label class="input-label" for="reg-phone">CONTACT NO. <span class="status-dot"></span></label>
                <el-input id="reg-phone" v-model="form.phone" placeholder="手机号" class="custom-input"></el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item prop="idCard">
            <label class="input-label" for="reg-idcard">IDENTITY CARD ID <span class="status-dot"></span></label>
            <el-input id="reg-idcard" v-model="form.idCard" placeholder="18位身份证号" class="custom-input"></el-input>
          </el-form-item>

          <!-- 修改部分开始：动态获取航司列表 -->
          <el-form-item prop="airlineCode">
            <label class="input-label" for="reg-airline">AFFILIATED AIRLINE <span class="status-dot"></span></label>
            <el-select
                id="reg-airline"
                v-model="form.airlineCode"
                placeholder="请选择您所属的航司"
                style="width: 100%"
                class="custom-select"
                popper-class="custom-dropdown"
                :loading="loadingAirlines"
            >
              <el-option
                  v-for="item in airlineOptions"
                  :key="item.airlineCode"
                  :label="`${item.airlineName} (${item.airlineCode})`"
                  :value="item.airlineCode">
              </el-option>
            </el-select>
          </el-form-item>
          <!-- 修改部分结束 -->

          <el-form-item prop="password">
            <label class="input-label" for="reg-pass">SECURE PASSWORD <span class="status-dot"></span></label>
            <el-input id="reg-pass" v-model="form.password" type="password" show-password placeholder="至少6位字符" class="custom-input"></el-input>
          </el-form-item>

          <div class="notice-bar">
            <div class="notice-icon"><i class="el-icon-warning-outline"></i></div>
            <div class="notice-content">
              <strong>SYSTEM NOTICE:</strong> 提交后需经平台管理员审核，结果将通过系统通知告知。
            </div>
          </div>

          <el-button type="primary" class="submit-btn" :loading="loading" @click="handleRegister">
            <span class="btn-content">提交入驻申请 [SUBMIT]</span>
            <div class="btn-glitch"></div>
          </el-button>
        </el-form>

        <div class="footer-links">
          <router-link to="/admin-login">
            <i class="el-icon-back"></i> ACCOUNT EXISTS? RETURN TO LOGIN
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/api';

export default {
  name: 'AdminRegisterView',
  data() {
    return {
      loading: false,
      loadingAirlines: false, // 加载航司数据的状态
      errorMessage: '',
      airlineOptions: [], // 存储从数据库获取的航司列表
      form: {
        name: '',
        phone: '',
        idCard: '',
        password: '',
        airlineCode: '',
        role: 'ROLE_AIRLINE_ADMIN',
        membershipLevel: '普通'
      },
      rules: {
        name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
        ],
        idCard: [
          { required: true, message: '请输入身份证号', trigger: 'blur' },
          { len: 18, message: '身份证号必须为18位', trigger: 'blur' }
        ],
        airlineCode: [{ required: true, message: '请选择所属航司', trigger: 'change' }],
        password: [{ required: true, min: 6, message: '密码至少6位', trigger: 'blur' }]
      }
    };
  },
  // 页面创建时自动获取航司列表
  created() {
    this.fetchAirlines();
  },
  methods: {
    async fetchAirlines() {
      this.loadingAirlines = true;
      try {
        // 调用后端接口获取真实数据
        const res = await api.getAllAirlines();
        if (res.code === 200) {
          this.airlineOptions = res.data;
        }
      } catch (error) {
        this.errorMessage = '无法加载航司列表，请检查网络或联系管理员';
      } finally {
        this.loadingAirlines = false;
      }
    },
    handleRegister() {
      this.$refs.regForm.validate(async (valid) => {
        if (!valid) return;
        this.loading = true;
        this.errorMessage = '';
        try {
          const res = await api.register(this.form);
          if (res.code === 200) {
            this.$alert('申请提交成功！请等待平台管理员审核。', '提交成功', {
              confirmButtonText: '确定',
              callback: () => { this.$router.push('/admin-login'); }
            });
          }
        } catch (error) {
          if (error.response && error.response.data) {
            this.errorMessage = error.response.data.message || '申请失败，请检查填写内容';
          } else {
            this.errorMessage = '网络繁忙，请稍后再试';
          }
        } finally {
          this.loading = false;
        }
      });
    }
  }
};
</script>

<style scoped>
/* 你的原有样式代码保持不变，完全兼容 */
@import url('https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;700&family=Rajdhani:wght@500;700&display=swap');

:root {
  --aviation-blue: #007AFF;
  --aviation-dark: #001a33;
}

.admin-register-wrapper {
  min-height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #000000;
  position: relative;
  overflow: hidden;
  font-family: 'Rajdhani', sans-serif;
  padding: 40px 0;
}

.space-container {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background: radial-gradient(ellipse at bottom, #0b1a2a 0%, #000000 100%);
  z-index: 0;
}
.stars-1, .stars-2, .stars-3 {
  position: absolute; top: 50%; left: 50%; width: 2px; height: 2px;
  background: transparent;
  box-shadow: -40vw 20vh 1px 1px #fff, 30vw -40vh 0px 1px #fff, -20vw -30vh 1px 0px #409EFF;
  animation: rotateParticles 100s linear infinite;
}
.stars-1 { animation-duration: 200s; opacity: 0.8; }
.stars-2 { animation-duration: 150s; transform: scale(1.5); opacity: 0.6; }
.stars-3 { animation-duration: 100s; transform: scale(2); opacity: 0.4; }
@keyframes rotateParticles {
  from { transform: translate(-50%, -50%) rotate(0deg); }
  to { transform: translate(-50%, -50%) rotate(360deg); }
}
.grid-overlay {
  position: absolute; width: 200%; height: 200%;
  background-image: linear-gradient(rgba(0, 122, 255, 0.05) 1px, transparent 1px),
  linear-gradient(90deg, rgba(0, 122, 255, 0.05) 1px, transparent 1px);
  background-size: 50px 50px;
  transform: perspective(500px) rotateX(60deg) translateY(-100px) translateZ(-200px);
  pointer-events: none; z-index: 1;
}

.register-container { position: relative; z-index: 10; }

.constructivist-bar-top {
  position: absolute; top: -10px; right: 20px;
  width: 150px; height: 8px;
  background: #007AFF;
  transform: skewX(-45deg);
  z-index: 2;
}
.constructivist-bar-side {
  position: absolute; bottom: 40px; left: -10px;
  width: 8px; height: 100px;
  background: #007AFF;
  z-index: 2;
}

.register-box {
  width: 520px;
  padding: 40px 50px;
  background: rgba(10, 20, 30, 0.85);
  border: 1px solid rgba(0, 122, 255, 0.3);
  backdrop-filter: blur(12px);
  box-shadow: 0 20px 60px rgba(0,0,0,0.6);
  position: relative;
}

.hud-corner {
  position: absolute; width: 15px; height: 15px; border: 2px solid #007AFF;
}
.top-left { top: -1px; left: -1px; border-right: none; border-bottom: none; }
.top-right { top: -1px; right: -1px; border-left: none; border-bottom: none; }
.bottom-left { bottom: -1px; left: -1px; border-right: none; border-top: none; }
.bottom-right { bottom: -1px; right: -1px; border-left: none; border-top: none; }

.header { display: flex; align-items: center; margin-bottom: 30px; border-bottom: 1px solid rgba(0,122,255,0.2); padding-bottom: 20px; }
.logo-hex {
  width: 50px; height: 50px;
  background: #007AFF;
  clip-path: polygon(25% 0%, 75% 0%, 100% 50%, 75% 100%, 25% 100%, 0% 50%);
  display: flex; align-items: center; justify-content: center;
  margin-right: 20px;
  font-size: 24px; color: #fff;
}
.header-text h1 { font-size: 22px; color: #fff; margin: 0; letter-spacing: 1px; text-transform: uppercase; }
.header-text p { font-size: 10px; color: #007AFF; margin-top: 5px; letter-spacing: 2px; font-family: 'JetBrains Mono', monospace; }

.custom-alert {
  background: rgba(245, 108, 108, 0.1) !important;
  border: 1px solid #f56c6c !important;
  color: #f56c6c !important;
  border-radius: 0 !important;
  margin-bottom: 25px;
}
.custom-alert ::v-deep .el-alert__title { color: #f56c6c; font-weight: bold; }
.custom-alert ::v-deep .el-alert__icon { color: #f56c6c; }

.input-label {
  color: #8aaacc; font-family: 'JetBrains Mono', monospace; font-size: 11px; font-weight: bold;
  display: flex; align-items: center;
}
.status-dot { width: 4px; height: 4px; background: #007AFF; margin-left: 6px; box-shadow: 0 0 5px #007AFF; }

.custom-input ::v-deep .el-input__inner,
.custom-select ::v-deep .el-input__inner {
  background-color: rgba(0, 122, 255, 0.05) !important;
  border: 1px solid rgba(0, 122, 255, 0.2) !important;
  border-radius: 0 !important;
  color: #fff !important;
  height: 42px;
  font-family: 'JetBrains Mono', monospace;
  transition: all 0.3s;
}
.custom-input ::v-deep .el-input__inner:focus,
.custom-select ::v-deep .el-input__inner:focus {
  border-color: #007AFF !important;
  background-color: rgba(0, 122, 255, 0.1) !important;
  box-shadow: inset 2px 0 0 #007AFF;
}

.notice-bar {
  background: rgba(230, 162, 60, 0.1);
  border: 1px dashed rgba(230, 162, 60, 0.4);
  color: #e6a23c;
  padding: 12px;
  display: flex;
  align-items: flex-start;
  font-size: 12px;
  margin-bottom: 25px;
  font-family: 'JetBrains Mono', monospace;
}
.notice-icon { margin-right: 10px; font-size: 16px; }

.submit-btn {
  width: 100%; height: 50px;
  background: linear-gradient(90deg, #007AFF, #0055b3);
  border: none; border-radius: 0;
  clip-path: polygon(0 0, 100% 0, 100% 80%, 97% 100%, 0 100%);
  position: relative; overflow: hidden;
  transition: all 0.3s;
}
.btn-content { font-family: 'Rajdhani', sans-serif; font-weight: 700; font-size: 18px; letter-spacing: 2px; }
.submit-btn:hover { background: #0066cc; transform: translateY(-2px); box-shadow: 0 5px 15px rgba(0,122,255,0.3); }

.footer-links { margin-top: 25px; text-align: center; border-top: 1px solid rgba(255,255,255,0.05); padding-top: 15px; }
.footer-links a { color: #607d8b; text-decoration: none; font-size: 12px; font-family: 'JetBrains Mono', monospace; transition: color 0.3s; }
.footer-links a:hover { color: #007AFF; }

::v-deep .el-form-item { margin-bottom: 22px; }
::v-deep .el-form-item__error { color: #ff4d4f; padding-top: 2px; font-family: 'JetBrains Mono', monospace; font-size: 11px; }
</style>

<style>
.custom-dropdown {
  background-color: #0b1a2a !important;
  border: 1px solid #007AFF !important;
  border-radius: 0 !important;
}
.custom-dropdown .el-select-dropdown__item {
  color: #8aaacc !important;
  font-family: 'JetBrains Mono', monospace !important;
}
.custom-dropdown .el-select-dropdown__item.hover,
.custom-dropdown .el-select-dropdown__item:hover {
  background-color: rgba(0, 122, 255, 0.2) !important;
  color: #fff !important;
}
.custom-dropdown .el-select-dropdown__item.selected {
  color: #007AFF !important;
  font-weight: bold;
}
.custom-dropdown .popper__arrow { border-bottom-color: #007AFF !important; }
.custom-dropdown .popper__arrow::after { border-bottom-color: #0b1a2a !important; }
</style>