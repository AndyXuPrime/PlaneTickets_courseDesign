<template>
  <div class="modal" v-if="showModal">
    <div class="modal-content">
      <div class="modal-header">
        <h3 class="modal-title">用户登录</h3>
        <button class="close-btn" @click="closeModal">×</button>
      </div>
      <div class="modal-body">
        <el-form :model="loginForm" :rules="rules" ref="loginForm" @submit.native.prevent="handleLogin">
          <el-form-item prop="username">
            <el-input v-model="loginForm.username" placeholder="手机号"></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input type="password" v-model="loginForm.password" placeholder="密码"></el-input>
          </el-form-item>
          <el-alert
            v-if="errorMessage"
            :title="errorMessage"
            type="error"
            show-icon
            :closable="false"
            style="margin-bottom: 20px;">
          </el-alert>
          <el-form-item>
            <el-button type="primary" @click="handleLogin" :loading="isLoading" style="width: 100%;">
              {{ isLoading ? '登录中...' : '登录' }}
            </el-button>
          </el-form-item>
        </el-form>
        <div class="switch-text text-center">
          还没有账号？ <a @click="switchToRegister">立即注册</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// 【核心修改】直接引入 axios 和 Message
import axios from 'axios';
import { Message } from 'element-ui';
import { store, mutations } from '@/store';

export default {
  name: 'LoginModal',
  data() {
    return {
      loginForm: {
        username: '',
        password: '',
      },
      rules: {
        username: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
      },
      isLoading: false,
      errorMessage: '',
    };
  },
  computed: {
    showModal() {
      return store.showLoginModal;
    },
  },
  methods: {
    closeModal() {
      mutations.setShowLoginModal(false);
    },
    switchToRegister() {
      mutations.setShowRegisterModal(true);
    },
    async handleLogin() {
      this.$refs.loginForm.validate(async (valid) => {
        if (valid) {
          this.isLoading = true;
          this.errorMessage = '';
          try {
            // 【核心修复】我们在这里创建一个全新的、独立的 Axios 请求
            // 绕过所有外部可能被污染的实例
            const response = await axios.post(
              // 1. 请求 URL
              'http://localhost:8080/api/auth/login', 
              // 2. 请求体 (Body)
              {
                username: this.loginForm.username,
                password: this.loginForm.password,
              },
              // 3. 配置对象，强制指定 headers
              {
                headers: {
                  'Content-Type': 'application/json'
                }
              }
            );

            // 后续逻辑与之前相同
            const apiResponse = response.data; // axios 返回的 data 是我们 ApiResponse
            if (apiResponse.code === 200) {
              const loginData = apiResponse.data;
              mutations.setUser(loginData, loginData.token);
              Message.success('登录成功');
              this.closeModal();
            } else {
              // 处理后端返回的业务错误
              this.errorMessage = apiResponse.message || '登录失败';
            }

          } catch (error) {
            console.error('Login failed in component:', error);
            if (error.response) {
              // 处理 HTTP 错误
              this.errorMessage = error.response.data?.message || `登录失败，错误码: ${error.response.status}`;
            } else {
              this.errorMessage = '网络错误，请检查连接';
            }
          } finally {
            this.isLoading = false;
          }
        }
      });
    },
  },
};
</script>

<style scoped>
/* 样式保持不变 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-content {
  background: white;
  border-radius: 8px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}
.modal-header {
  padding: 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.modal-title {
  font-size: 20px;
  font-weight: 600;
}
.modal-body {
  padding: 20px;
}
.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #909399;
}
.switch-text {
  margin-top: 15px;
  font-size: 14px;
  color: #606266;
}
.switch-text a {
  color: #409eff;
  cursor: pointer;
  text-decoration: none;
}
.switch-text a:hover {
  text-decoration: underline;
}
.text-center {
  text-align: center;
}
</style>