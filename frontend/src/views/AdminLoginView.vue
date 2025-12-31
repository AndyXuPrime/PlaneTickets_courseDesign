<template>
  <div class="admin-login-wrapper">
    <!-- 背景：模拟Three.js的旋转粒子深空 -->
    <div class="space-container">
      <div class="stars-1"></div>
      <div class="stars-2"></div>
      <div class="stars-3"></div>
    </div>

    <!-- 装饰网格 -->
    <div class="grid-overlay"></div>

    <div class="login-container">
      <!-- 结构主义装饰条 (航空蓝) -->
      <div class="constructivist-bar"></div>
      <div class="constructivist-stripe"></div>

      <div class="login-box">
        <!-- HUD 角标装饰 -->
        <div class="hud-corner top-left"></div>
        <div class="hud-corner top-right"></div>
        <div class="hud-corner bottom-left"></div>
        <div class="hud-corner bottom-right"></div>

        <!-- 头部标识 -->
        <div class="header">
          <div class="logo-area">
            <div class="logo-box">
              <i class="el-icon-s-platform"></i>
            </div>
            <div class="logo-text-group">
              <h1>清风航空管理系统</h1>
              <span class="logo-decoration">/// FLIGHT.CONTROL.SYSTEM ///</span>
            </div>
          </div>
          <p class="sub-title">INTERNAL ADMINISTRATION TERMINAL</p>
        </div>

        <!-- 登录表单 -->
        <el-form
            :model="loginForm"
            ref="loginForm"
            :rules="rules"
            @submit.native.prevent="handleLogin"
            class="constructivist-form"
        >
          <el-form-item prop="username">
            <!-- 修改点：div 改为 label，并添加 for 属性 -->
            <label class="input-label" for="login-username">ADMIN ID <span class="status-dot"></span></label>
            <!-- 修改点：添加 id 属性 -->
            <el-input
                id="login-username"
                v-model="loginForm.username"
                placeholder="请输入管理员账号"
                prefix-icon="el-icon-user-solid"
                class="custom-input">
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <!-- 修改点：div 改为 label，并添加 for 属性 -->
            <label class="input-label" for="login-password">PASSCODE <span class="status-dot"></span></label>
            <!-- 修改点：添加 id 属性 -->
            <el-input
                id="login-password"
                v-model="loginForm.password"
                type="password"
                placeholder="请输入安全密码"
                prefix-icon="el-icon-lock"
                show-password
                class="custom-input">
            </el-input>
          </el-form-item>

          <el-button
              type="primary"
              class="login-btn"
              :loading="loading"
              native-type="submit">
            <span class="btn-content">
              <span>INITIATE LOGIN</span>
              <i class="el-icon-s-promotion"></i>
            </span>
            <div class="btn-glitch"></div>
          </el-button>
        </el-form>

        <!-- 管理员特有动作链接 -->
        <div class="admin-action-links">
          <span>ACCESS RESTRICTED?</span>
          <router-link to="/admin-register" class="register-link">
            APPLY FOR AIRLINE ENTRY <i class="el-icon-d-arrow-right"></i>
          </router-link>
        </div>

        <!-- 返回链接 -->
        <div class="footer-links">
          <router-link to="/">
            <i class="el-icon-caret-left"></i> RETURN TO PASSENGER VIEW
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<!-- Script 和 Style 部分保持完全不变，无需修改 -->
<script>
// ... 保持原有代码 ...
import api from '@/api';
import { mutations } from '@/store';

export default {
  name: 'AdminLoginView',
  data() {
    return {
      loading: false,
      loginForm: {
        username: '',
        password: ''
      },
      rules: {
        username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      }
    };
  },
  methods: {
    async handleLogin() {
      this.$refs.loginForm.validate(async (valid) => {
        if (!valid) return;

        this.loading = true;
        try {
          const res = await api.login(this.loginForm);

          if (res.code === 200) {
            const user = res.data;
            // 权限校验逻辑
            if (user.role === 'ROLE_PLATFORM_ADMIN' || user.role === 'ROLE_AIRLINE_ADMIN') {
              mutations.setUser(user, user.token);
              this.$message.success('身份验证通过，欢迎回来');
              this.$router.push('/admin');
            } else {
              this.$message.error('权限不足：该账号不是系统管理员');
            }
          }
        } catch (error) {
          console.error("登录失败", error);
        } finally {
          this.loading = false;
        }
      });
    }
  }
};
</script>
<style scoped>
/* ... 保持原有样式代码不变 ... */
@import url('https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;700&family=Rajdhani:wght@500;700&display=swap');

:root {
  --aviation-blue: #007AFF;
  --aviation-dark: #001a33;
  --aviation-light: #409EFF;
  --bg-deep: #020408;
  --text-primary: #ffffff;
  --text-secondary: #8aaacc;
}

.admin-login-wrapper {
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #000000;
  position: relative;
  overflow: hidden;
  font-family: 'Rajdhani', sans-serif;
}

.space-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(ellipse at bottom, #0b1a2a 0%, #000000 100%);
  overflow: hidden;
  z-index: 0;
}

.stars-1, .stars-2, .stars-3 {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 2px;
  height: 2px;
  background: transparent;
  box-shadow:
      -40vw 20vh 1px 1px #fff,
      30vw -40vh 0px 1px #fff,
      -20vw -30vh 1px 0px #409EFF,
      20vw 20vh 0px 1px #fff,
      -10vw -10vh 1px 1px #007AFF,
      40vw 10vh 0px 1px #fff,
      -30vw 30vh 1px 0px #fff;
  animation: rotateParticles 100s linear infinite;
}

.stars-1 { animation-duration: 200s; transform: scale(1); opacity: 0.8; }
.stars-2 { animation-duration: 150s; transform: scale(1.5); opacity: 0.6; box-shadow: -30vw 10vh 1px 1px #fff, 20vw -20vh 0px 1px #409EFF; }
.stars-3 { animation-duration: 100s; transform: scale(2); opacity: 0.4; }

@keyframes rotateParticles {
  from { transform: translate(-50%, -50%) rotate(0deg); }
  to { transform: translate(-50%, -50%) rotate(360deg); }
}

.grid-overlay {
  position: absolute;
  width: 200%;
  height: 200%;
  background-image:
      linear-gradient(rgba(0, 122, 255, 0.05) 1px, transparent 1px),
      linear-gradient(90deg, rgba(0, 122, 255, 0.05) 1px, transparent 1px);
  background-size: 50px 50px;
  transform: perspective(500px) rotateX(60deg) translateY(-100px) translateZ(-200px);
  pointer-events: none;
  z-index: 1;
}

.login-container {
  position: relative;
  z-index: 10;
}

.constructivist-bar {
  position: absolute;
  top: -15px;
  left: -15px;
  width: 80px;
  height: 80px;
  background-color: #007AFF;
  z-index: -1;
  clip-path: polygon(0 0, 100% 0, 0 100%);
}

.constructivist-stripe {
  position: absolute;
  bottom: -20px;
  right: -40px;
  width: 120px;
  height: 10px;
  background: repeating-linear-gradient(
      45deg,
      #007AFF,
      #007AFF 10px,
      transparent 10px,
      transparent 20px
  );
  opacity: 0.5;
}

.login-box {
  width: 420px;
  padding: 50px 40px;
  background: rgba(10, 20, 30, 0.85);
  border: 1px solid rgba(0, 122, 255, 0.3);
  backdrop-filter: blur(10px);
  position: relative;
  box-shadow: 0 0 40px rgba(0, 122, 255, 0.1);
}

.hud-corner {
  position: absolute;
  width: 20px;
  height: 20px;
  border: 2px solid #007AFF;
  transition: all 0.3s ease;
}
.top-left { top: -1px; left: -1px; border-right: none; border-bottom: none; }
.top-right { top: -1px; right: -1px; border-left: none; border-bottom: none; }
.bottom-left { bottom: -1px; left: -1px; border-right: none; border-top: none; }
.bottom-right { bottom: -1px; right: -1px; border-left: none; border-top: none; }

.login-box:hover .hud-corner {
  width: 30px;
  height: 30px;
  box-shadow: 0 0 10px #007AFF;
}

.header {
  margin-bottom: 45px;
}

.logo-area {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.logo-box {
  width: 50px;
  height: 50px;
  background: #007AFF;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  clip-path: polygon(10% 0, 100% 0, 100% 90%, 90% 100%, 0 100%, 0 10%);
}

.logo-box i {
  font-size: 30px;
  color: #fff;
}

.logo-text-group h1 {
  color: #fff;
  font-size: 24px;
  margin: 0;
  font-weight: 700;
  letter-spacing: 2px;
  text-transform: uppercase;
}

.logo-decoration {
  font-family: 'JetBrains Mono', monospace;
  font-size: 10px;
  color: #007AFF;
  letter-spacing: 1px;
  display: block;
  margin-top: 4px;
}

.sub-title {
  color: #8aaacc;
  font-family: 'JetBrains Mono', monospace;
  font-size: 11px;
  letter-spacing: 3px;
  border-bottom: 1px solid rgba(0, 122, 255, 0.2);
  padding-bottom: 10px;
  margin-top: 15px;
}

.constructivist-form {
  position: relative;
}

.input-label {
  color: #007AFF;
  font-family: 'JetBrains Mono', monospace;
  font-size: 12px;
  font-weight: bold;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}

.status-dot {
  width: 6px;
  height: 6px;
  background: #007AFF;
  border-radius: 50%;
  margin-left: 8px;
  box-shadow: 0 0 5px #007AFF;
}

.custom-input ::v-deep .el-input__inner {
  background-color: rgba(0, 122, 255, 0.05) !important;
  border: 1px solid rgba(0, 122, 255, 0.2) !important;
  border-left: 3px solid rgba(0, 122, 255, 0.2) !important;
  border-radius: 0 !important;
  color: #fff !important;
  height: 48px;
  font-family: 'JetBrains Mono', monospace;
  transition: all 0.3s;
  padding-left: 40px !important;
}

.custom-input ::v-deep .el-input__inner:focus {
  border-color: #007AFF !important;
  border-left-color: #007AFF !important;
  box-shadow: 0 0 15px rgba(0, 122, 255, 0.1);
  background-color: rgba(0, 122, 255, 0.1) !important;
}

.custom-input ::v-deep .el-input__prefix {
  left: 10px;
}
.custom-input ::v-deep .el-input__prefix i {
  color: #007AFF;
  line-height: 48px;
  font-size: 16px;
}

.login-btn {
  width: 100%;
  height: 54px;
  margin-top: 25px;
  background: linear-gradient(90deg, #007AFF, #0055b3);
  border: none;
  border-radius: 0;
  position: relative;
  overflow: hidden;
  clip-path: polygon(0 0, 100% 0, 100% 85%, 95% 100%, 0 100%);
  transition: all 0.3s;
}

.btn-content {
  position: relative;
  z-index: 2;
  font-family: 'Rajdhani', sans-serif;
  font-weight: 700;
  font-size: 18px;
  letter-spacing: 3px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 10px;
}

.login-btn:hover {
  background: #0066cc;
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(0, 122, 255, 0.3);
}

.login-btn::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transform: skewX(-20deg);
  animation: btnScan 3s infinite;
}

@keyframes btnScan {
  0% { left: -100%; }
  20% { left: 200%; }
  100% { left: 200%; }
}

.admin-action-links {
  margin-top: 35px;
  font-family: 'JetBrains Mono', monospace;
  font-size: 12px;
  color: #607d8b;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px dashed rgba(0, 122, 255, 0.2);
  padding-top: 20px;
}

.register-link {
  color: #007AFF;
  text-decoration: none;
  font-weight: bold;
  transition: all 0.3s;
  text-transform: uppercase;
}

.register-link:hover {
  text-shadow: 0 0 8px rgba(0, 122, 255, 0.6);
  padding-left: 5px;
}

.footer-links {
  margin-top: 15px;
  text-align: center;
}

.footer-links a {
  color: #4a5a6a;
  text-decoration: none;
  font-family: 'JetBrains Mono', monospace;
  font-size: 11px;
  letter-spacing: 1px;
  transition: color 0.3s;
}

.footer-links a:hover {
  color: #fff;
}

::v-deep .el-form-item__error {
  color: #ff4d4f;
  font-family: 'JetBrains Mono', monospace;
  padding-top: 4px;
  font-size: 11px;
}
</style>