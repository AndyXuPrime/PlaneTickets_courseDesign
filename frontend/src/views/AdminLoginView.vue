<template>
  <div class="admin-login-wrapper">
    <div class="login-box">
      <div class="header">
        <i class="el-icon-s-platform"></i>
        <h1>清风航空管理系统</h1>
        <p>INTERNAL ADMINISTRATION SYSTEM</p>
      </div>

      <el-form :model="loginForm" ref="loginForm" :rules="rules" @submit.native.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input
              v-model="loginForm.username"
              placeholder="管理员账号 (手机号)"
              prefix-icon="el-icon-user">
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="安全密码"
              prefix-icon="el-icon-lock"
              show-password>
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

      <div class="footer-links">
        <router-link to="/"><i class="el-icon-back"></i> 返回乘客首页</router-link>
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
          // 调用通用的 login 接口
          const res = await api.login(this.loginForm);

          if (res.code === 200) {
            const user = res.data;
            // 【关键判断】只有角色为管理员才允许进入后台
            if (user.role === 'ROLE_PLATFORM_ADMIN' || user.role === 'ROLE_AIRLINE_ADMIN') {
              mutations.setUser(user, user.token);
              this.$message.success('身份验证通过，正在进入管理面板...');
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
.admin-login-wrapper {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: radial-gradient(circle at center, #2c3e50 0%, #000000 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 15px;
  backdrop-filter: blur(10px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.5);
}

.header {
  text-align: center;
  margin-bottom: 30px;
}

.header i {
  font-size: 50px;
  color: #409EFF;
  margin-bottom: 10px;
}

.header h1 {
  color: #fff;
  font-size: 24px;
  margin: 0;
  letter-spacing: 2px;
}

.header p {
  color: #5d6d7e;
  font-size: 12px;
  margin-top: 5px;
}

.login-btn {
  width: 100%;
  padding: 12px;
  font-size: 16px;
  letter-spacing: 4px;
  margin-top: 10px;
}

.footer-links {
  margin-top: 20px;
  text-align: center;
}

.footer-links a {
  color: #85929e;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.footer-links a:hover {
  color: #409EFF;
}

/* 覆盖 Element UI 样式使输入框更符合深色背景 */
::v-deep .el-input__inner {
  background-color: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #fff;
}
</style>