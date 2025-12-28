<template>
  <div class="admin-register-wrapper">
    <div class="register-box">
      <div class="header">
        <div class="logo-circle">
          <i class="el-icon-school"></i>
        </div>
        <h1>航司合作伙伴入驻</h1>
        <p>AIRLINE PARTNER APPLICATION</p>
      </div>

      <transition name="el-fade-in">
        <el-alert
            v-if="errorMessage"
            :title="errorMessage"
            type="error"
            show-icon
            style="margin-bottom: 20px"
            @close="errorMessage = ''"
        ></el-alert>
      </transition>

      <el-form :model="form" ref="regForm" :rules="rules" label-position="top">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="管理员姓名" prop="name">
              <el-input v-model="form.name" placeholder="真实姓名"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="手机号"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="18位身份证号"></el-input>
        </el-form-item>

        <el-form-item label="拟入驻航空公司" prop="airlineCode">
          <el-select v-model="form.airlineCode" placeholder="请选择您所属的航司" style="width: 100%">
            <el-option label="中国国际航空 (CA)" value="CA"></el-option>
            <el-option label="东方航空 (MU)" value="MU"></el-option>
            <el-option label="南方航空 (CZ)" value="CZ"></el-option>
            <el-option label="海南航空 (HU)" value="HU"></el-option>
            <el-option label="厦门航空 (MF)" value="MF"></el-option>
            <el-option label="深圳航空 (ZH)" value="ZH"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="设置安全密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="至少6位字符"></el-input>
        </el-form-item>

        <div class="notice-bar">
          <i class="el-icon-info"></i> 提交后需经平台管理员审核，审核结果将通过系统通知告知。
        </div>

        <el-button type="primary" class="submit-btn" :loading="loading" @click="handleRegister">
          提交入驻申请
        </el-button>
      </el-form>

      <div class="footer-links">
        <router-link to="/admin-login">已有账号？返回登录</router-link>
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
      errorMessage: '',
      form: {
        name: '',
        phone: '',
        idCard: '',
        password: '',
        airlineCode: '',
        role: 'ROLE_AIRLINE_ADMIN', // 匹配后端 UserRole 枚举
        membershipLevel: '普通'      // 【关键修复】匹配后端 MembershipLevel 枚举，通过 @NotNull 校验
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
  methods: {
    handleRegister() {
      this.$refs.regForm.validate(async (valid) => {
        if (!valid) return;
        this.loading = true;
        this.errorMessage = '';
        try {
          // 确保发送的是完整的 form 数据
          const res = await api.register(this.form);
          if (res.code === 200) {
            this.$alert('申请提交成功！请等待平台管理员审核。', '提交成功', {
              confirmButtonText: '确定',
              callback: () => { this.$router.push('/admin-login'); }
            });
          }
        } catch (error) {
          if (error.response && error.response.data) {
            // 显示后端返回的校验错误信息
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
/* 样式保持不变，已包含在之前的回复中 */
.admin-register-wrapper {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #1a252f;
  padding: 40px 0;
}
.register-box {
  width: 480px;
  padding: 40px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.4);
}
.header { text-align: center; margin-bottom: 30px; }
.logo-circle {
  width: 60px; height: 60px;
  background: #f0f7ff;
  color: #409EFF;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 15px;
  font-size: 30px;
}
.header h1 { font-size: 24px; color: #2c3e50; margin: 0; font-weight: 600; }
.header p { font-size: 11px; color: #95a5a6; margin-top: 5px; letter-spacing: 1px; }
.notice-bar {
  background: #fdf6ec;
  color: #e6a23c;
  padding: 12px;
  border-radius: 8px;
  font-size: 13px;
  margin-bottom: 25px;
  border: 1px solid #faecd8;
}
.submit-btn { width: 100%; padding: 14px; font-size: 16px; font-weight: bold; letter-spacing: 2px; border-radius: 8px; }
.footer-links { margin-top: 25px; text-align: center; }
.footer-links a { color: #409EFF; text-decoration: none; font-size: 14px; font-weight: 500; }
::v-deep .el-form-item { margin-bottom: 18px; }
</style>