<template>
  <div class="modal" @click.self="$emit('close')">
    <div class="modal-content-styled">
      <div class="heading">用户登录</div>
      
      <form @submit.prevent="handleLogin" class="form">
        <div class="input-field">
          <input type="text" id="login-username" v-model.trim="credentials.username" required />
          <label for="login-username">手机号</label>
        </div>

        <div class="input-field">
          <input :type="passwordFieldType" id="login-password" v-model="credentials.password" required />
          <label for="login-password">密码</label>
          <i :class="['passicon', isPasswordVisible ? 'fas fa-eye-slash' : 'fas fa-eye']" @click="togglePasswordVisibility"></i>
        </div>

        <!-- 【新增】用于显示错误信息的元素 -->
        <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>

        <div class="btn-container">
          <button type="submit" class="btn" :disabled="isLoading">
            <span>{{ isLoading ? '登录中...' : '登录' }}</span>
          </button>
        </div>
      </form>

      <div class="switch-text text-center">
        <p>还没有账号？ <a @click="$emit('switch-to-register')">立即注册</a></p>
      </div>

      <button class="close-button" @click="$emit('close')">×</button>
    </div>
  </div>
</template>

<script>
import api from '../api';

export default {
  name: 'LoginModal',
  data() {
    return {
      isLoading: false,
      isPasswordVisible: false,
      errorMessage: '', // 新增：用于存储错误信息
      credentials: {
        username: '',
        password: '',
      },
    };
  },
  computed: {
    passwordFieldType() {
      return this.isPasswordVisible ? 'text' : 'password';
    }
  },
  methods: {
    async handleLogin() {
      this.isLoading = true;
      this.errorMessage = ''; // 每次登录前清空错误信息

      try {
        const response = await api.login(this.credentials);
        if (response.code === 200) {
          this.$emit('login-success', response.data);
        }
      } catch (error) {
        console.error("Login failed in component:", error);
        
        // 【核心修改】检查 error.response 对象是否存在以及其状态码
        if (error.response && error.response.status === 401) {
          // 如果是 401 错误，说明是认证失败，显示后端返回的特定消息
          this.errorMessage = error.response.data.message || '手机号或密码错误';
        } else {
          // 对于其他错误（如500服务器错误、网络问题等），显示通用错误信息
          // 注意：API拦截器可能已经弹出了全局消息，这里的消息是显示在模态框内部的
          this.errorMessage = '登录时发生错误，请稍后重试。';
        }
      } finally {
        this.isLoading = false;
      }
    },
    togglePasswordVisibility() {
      this.isPasswordVisible = !this.isPasswordVisible;
    }
  },
};
</script>

<style scoped>
/* 模态框背景 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(5px);
}

/* 模态框内容区样式 */
.modal-content-styled {
  position: relative;
  border: solid 1px #dcdcdc;
  padding: 30px 40px;
  border-radius: 20px;
  background-color: #fff;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
}

.modal-content-styled .heading {
  font-size: 1.5rem;
  margin-bottom: 25px;
  font-weight: bolder;
  text-align: center;
  color: #333;
}

.form {
  width: 300px;
  display: flex;
  flex-direction: column;
  gap: 25px;
}

/* 按钮样式 */
.form .btn-container {
  width: 100%;
  display: flex;
  justify-content: center;
  margin-top: 10px;
}

.form .btn {
  padding: 10px 20px;
  font-size: 1rem;
  text-transform: uppercase;
  letter-spacing: 3px;
  border-radius: 10px;
  border: solid 1px #1034aa;
  border-bottom: solid 1px #90c2ff;
  background: linear-gradient(135deg, #0034de, #006eff);
  color: #fff;
  font-weight: bolder;
  transition: all 0.2s ease;
  box-shadow: 0px 2px 3px #000d3848, inset 0px 4px 5px #0070f0,
    inset 0px -4px 5px #002cbb;
  cursor: pointer;
}

.form .btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.form .btn:active {
  box-shadow: inset 0px 4px 5px #0070f0, inset 0px -4px 5px #002cbb;
  transform: scale(0.995);
}

/* 输入框浮动标签样式 */
.input-field {
  position: relative;
}

.input-field label {
  position: absolute;
  color: #8d8d8d;
  pointer-events: none;
  background-color: transparent;
  left: 15px;
  top: 0;
  transform: translateY(0.7rem);
  transition: all 0.2s ease-in-out;
}

.input-field input {
  padding: 12px 15px;
  font-size: 1rem;
  border-radius: 8px;
  border: solid 1px #8d8d8d;
  letter-spacing: 1px;
  width: 100%;
  box-sizing: border-box;
}

.input-field input:focus,
.input-field input:valid {
  outline: none;
  border: solid 1px #0034de;
}

.input-field input:focus ~ label,
.input-field input:valid ~ label {
  transform: translateY(-50%) translateX(-10px) scale(0.8);
  background-color: #fff;
  padding: 0px 5px;
  color: #0034de;
  font-weight: bold;
}

.form .passicon {
  cursor: pointer;
  font-size: 1.2rem;
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  right: 12px;
  color: #8d8d8d;
  transition: color 0.2s;
}
.form .passicon:hover {
  color: #333;
}

/* 附加样式 */
.switch-text {
  margin-top: 25px;
  color: var(--gray);
  font-size: 14px;
}
.switch-text a {
  color: var(--primary);
  cursor: pointer;
  text-decoration: none;
  font-weight: 500;
}
.switch-text a:hover {
  text-decoration: underline;
}
.text-center {
  text-align: center;
}

.close-button {
  position: absolute;
  top: 15px;
  right: 20px;
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #aaa;
  transition: color 0.2s;
}
.close-button:hover {
  color: #333;
}
/* 【新增】错误信息的样式 */
.error-message {
  color: #e53935; /* 使用 main.css 中的 var(--danger) 颜色 */
  background-color: #ffebee;
  border: 1px solid #e53935;
  border-radius: 8px;
  padding: 10px;
  text-align: center;
  font-size: 14px;
  margin-top: -10px; /* 调整位置，使其更靠近输入框 */
}
</style>