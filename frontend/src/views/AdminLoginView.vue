<template>
  <div class="admin-login-wrapper">
    <div class="login-box">
      <!-- 头部标识 -->
      <div class="header">
        <div class="logo-icon">
          <i class="el-icon-s-platform"></i>
        </div>
        <h1>清风航空管理系统</h1>
        <p>INTERNAL ADMINISTRATION SYSTEM</p>
      </div>

      <!-- 登录表单 -->
      <el-form
          :model="loginForm"
          ref="loginForm"
          :rules="rules"
          @submit.native.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
              v-model="loginForm.username"
              placeholder="管理员账号 (手机号)"
              prefix-icon="el-icon-user"
              class="custom-input">
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="安全密码"
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
          登 录 系 统
        </el-button>
      </el-form>

      <!-- 管理员特有动作链接 -->
      <div class="admin-action-links">
        <span>还没有管理权限？</span>
        <router-link to="/admin-register" class="register-link">申请航司入驻</router-link>
      </div>

      <!-- 返回链接 -->
      <div class="footer-links">
        <router-link to="/">
          <i class="el-icon-back"></i> 返回乘客首页
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
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
/* 容器背景：深色渐变 */
.admin-login-wrapper {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: radial-gradient(circle at center, #2c3e50 0%, #000000 100%);
  overflow: hidden;
}

/* 登录框：玻璃拟态效果 */
.login-box {
  width: 420px;
  padding: 45px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  backdrop-filter: blur(15px);
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.5);
}

/* 头部样式 */
.header {
  text-align: center;
  margin-bottom: 35px;
}
.logo-icon {
  width: 64px;
  height: 64px;
  background: rgba(64, 158, 255, 0.1);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 15px;
}
.header i {
  font-size: 40px;
  color: #409EFF;
}
.header h1 {
  color: #fff;
  font-size: 22px;
  margin: 0;
  letter-spacing: 1px;
  font-weight: 600;
}
.header p {
  color: #5d6d7e;
  font-size: 11px;
  margin-top: 8px;
  letter-spacing: 1px;
}

/* 按钮样式 */
.login-btn {
  width: 100%;
  padding: 14px;
  font-size: 16px;
  letter-spacing: 4px;
  margin-top: 5px;
  border-radius: 8px;
  background: linear-gradient(90deg, #409EFF, #2980b9);
  border: none;
  transition: all 0.3s;
}
.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(64, 158, 255, 0.4);
}

/* 动作链接样式 */
.admin-action-links {
  margin-top: 25px;
  text-align: center;
  font-size: 14px;
  color: #95a5a6;
}
.register-link {
  color: #409EFF;
  text-decoration: none;
  font-weight: 500;
  margin-left: 8px;
  transition: color 0.3s;
}
.register-link:hover {
  color: #66b1ff;
  text-decoration: underline;
}

/* 底部返回链接 */
.footer-links {
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  text-align: center;
}
.footer-links a {
  color: #5d6d7e;
  text-decoration: none;
  font-size: 13px;
  transition: all 0.3s;
}
.footer-links a:hover {
  color: #fff;
}

/* 输入框深色适配 */
.custom-input ::v-deep .el-input__inner {
  background-color: rgba(255, 255, 255, 0.05) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  color: #fff !important;
  height: 45px;
  line-height: 45px;
}
.custom-input ::v-deep .el-input__inner:focus {
  border-color: #409EFF !important;
  background-color: rgba(255, 255, 255, 0.08) !important;
}
.custom-input ::v-deep .el-input__prefix i {
  line-height: 45px;
}
</style>