<template>
  <el-dialog
    title="新用户注册"
    :visible.sync="showModal"
    width="400px"
    @close="close"
    :close-on-click-modal="false"
  >
    <el-form :model="form" :rules="rules" ref="registerForm" label-position="top">
      
      <!-- 【核心修改 1】在这里添加错误提示区域 -->
      <el-alert
        v-if="errorMessage"
        :title="errorMessage"
        type="error"
        show-icon
        class="error-alert"
        @close="errorMessage = ''"
      ></el-alert>

      <el-form-item label="姓名" prop="name">
        <el-input v-model="form.name" placeholder="请输入您的真实姓名"></el-input>
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" placeholder="请输入您的手机号"></el-input>
      </el-form-item>
      <el-form-item label="密码 (至少6位)" prop="password">
        <el-input v-model="form.password" type="password" placeholder="请输入密码"></el-input>
      </el-form-item>
      <el-form-item label="身份证号" prop="idCard">
        <el-input v-model="form.idCard" placeholder="请输入18位身份证号"></el-input>
      </el-form-item>
      <el-form-item label="邮箱 (可选)" prop="email">
        <el-input v-model="form.email" placeholder="请输入您的电子邮箱"></el-input>
      </el-form-item>
      <el-form-item label="选择会员等级" prop="membershipLevel">
        <el-select v-model="form.membershipLevel" placeholder="请选择" style="width: 100%;">
          <el-option label="普通会员" value="普通"></el-option>
          <el-option label="银卡会员" value="银卡"></el-option>
          <el-option label="金卡会员" value="金卡"></el-option>
          <el-option label="白金会员" value="白金"></el-option>
        </el-select>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button 
        type="primary" 
        @click="handleRegister" 
        :loading="isLoading" 
        style="width: 100%;"
      >
        {{ isLoading ? '注册中...' : '同意协议并注册' }}
      </el-button>
      <div class="switch-login">
        已有账号？ <el-link type="primary" @click="switchToLogin">立即登录</el-link>
      </div>
    </span>
  </el-dialog>
</template>

<script>
import api from '@/api';
import { store, mutations } from '@/store';

export default {
  name: 'RegisterModal',
  data() {
    return {
      form: {
        name: '',
        phone: '',
        password: '',
        idCard: '',
        email: '',
        membershipLevel: '普通', // 默认值
      },
      rules: {
        // 表单验证规则
        name: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
        phone: [
          { required: true, message: '手机号不能为空', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '密码不能为空', trigger: 'blur' },
          { min: 6, message: '密码长度至少为6位', trigger: 'blur' }
        ],
        idCard: [
          { required: true, message: '身份证号不能为空', trigger: 'blur' },
          { len: 18, message: '身份证号必须为18位', trigger: 'blur' }
        ],
        membershipLevel: [{ required: true, message: '请选择会员等级', trigger: 'change' }],
      },
      isLoading: false,
      // 【核心修改 2】增加一个用于存储错误信息的变量
      errorMessage: '', 
    };
  },
  computed: {
    showModal: {
      get() {
        return store.showRegisterModal;
      },
      set(value) {
        mutations.setShowRegisterModal(value);
      }
    }
  },
  methods: {
    handleRegister() {
      this.$refs.registerForm.validate(async (valid) => {
        if (valid) {
          this.isLoading = true;
          this.errorMessage = ''; // 开始注册前，清空旧的错误信息
          try {
            await api.register(this.form);
            this.$message.success('注册成功！现在您可以登录了。');
            this.switchToLogin(); // 注册成功后，自动切换到登录弹窗
          } catch (error) {
            // 【核心修改 3】捕获错误，并从后端返回的响应中提取 message
            if (error.response && error.response.data && error.response.data.message) {
              this.errorMessage = error.response.data.message;
            } else {
              this.errorMessage = '发生未知错误，请稍后重试。';
            }
            console.error('注册时出错:', error);
          } finally {
            this.isLoading = false;
          }
        }
      });
    },
    switchToLogin() {
      mutations.setShowLoginModal(true);
    },
    close() {
      this.$refs.registerForm.resetFields();
      this.errorMessage = ''; // 关闭弹窗时也清空错误信息
      mutations.setShowRegisterModal(false);
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
.switch-login {
  text-align: center;
  margin-top: 15px;
}
.error-alert {
  margin-bottom: 20px;
}
</style>