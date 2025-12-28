<template>
  <div class="user-center-page">
    <el-container class="main-card">
      <!-- 左侧菜单 -->
      <el-aside width="200px">
        <div class="user-info-side">
          <el-avatar :size="80" :src="user.avatarUrl" icon="el-icon-user-solid"></el-avatar>
          <div class="nickname">{{ user.name }}</div>
          <el-tag size="mini" type="warning">{{ user.membershipLevel }}会员</el-tag>
        </div>
        <el-menu :default-active="activeTab" @select="activeTab = $event">
          <el-menu-item index="info"><i class="el-icon-user"></i>个人资料</el-menu-item>
          <el-menu-item index="security"><i class="el-icon-lock"></i>安全设置</el-menu-item>
          <el-menu-item index="member"><i class="el-icon-medal"></i>会员权益</el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 右侧内容 -->
      <el-main class="content-area">
        <!-- 个人资料面板 -->
        <div v-if="activeTab === 'info'">
          <h3 class="pane-title">个人资料修改</h3>
          <el-form label-width="100px" class="profile-form">
            <el-form-item label="用户头像">
              <el-upload
                  class="avatar-uploader"
                  action="http://localhost:8080/api/files/upload"
                  :headers="uploadHeaders"
                  :show-file-list="false"
                  :on-success="handleAvatarSuccess">
                <img v-if="user.avatarUrl" :src="user.avatarUrl" class="avatar-preview">
                <i v-else class="el-icon-plus avatar-uploader-icon"></i>
              </el-upload>
              <span class="upload-tip">支持 JPG/PNG，尺寸建议 200x200</span>
            </el-form-item>
            <el-form-item label="真实姓名">
              <el-input :value="user.name" disabled></el-input>
            </el-form-item>
            <el-form-item label="电子邮箱">
              <el-input v-model="user.email" placeholder="请输入邮箱"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 安全设置面板 -->
        <div v-if="activeTab === 'security'">
          <h3 class="pane-title">修改登录密码</h3>
          <el-form label-width="100px" style="max-width: 400px">
            <el-form-item label="原密码">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password></el-input>
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="pwdForm.newPassword" type="password" show-password></el-input>
            </el-form-item>
            <el-form-item label="确认新密码">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="danger" @click="submitPassword">立即重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 会员信息面板 -->
        <div v-if="activeTab === 'member'">
          <h3 class="pane-title">我的会员权益</h3>
          <el-alert title="您的会员等级已激活" type="success" :closable="false" show-icon></el-alert>
          <div class="member-cards">
            <!-- 这里可以放你之前的那些会员卡片图片 -->
            <p style="margin-top: 20px; color: #666">当前等级：{{ user.membershipLevel }}</p>
            <p>享受购票折扣：{{ getDiscount }}折</p>
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script>
import api from '@/api';
import { store, mutations } from '@/store';

export default {
  data() {
    return {
      activeTab: 'info',
      user: { ...store.user },
      pwdForm: { oldPassword: '', newPassword: '', confirmPassword: '' },
      uploadHeaders: { Authorization: 'Bearer ' + localStorage.getItem('authToken') }
    };
  },
  computed: {
    getDiscount() {
      const map = { '白金': '8.5', '金卡': '9.0', '银卡': '9.5', '普通': '10' };
      return map[this.user.membershipLevel] || '10';
    }
  },
  methods: {
    handleAvatarSuccess(res) {
      if (res.code === 200) {
        this.user.avatarUrl = res.data;
        this.$message.success('头像上传成功');
      }
    },
    async submitProfile() {
      const res = await api.updateProfile({ email: this.user.email, avatarUrl: this.user.avatarUrl });
      if (res.code === 200) {
        mutations.setUser({ ...store.user, ...this.user }, localStorage.getItem('authToken'));
        this.$message.success('个人资料已更新');
      }
    },
    async submitPassword() {
      if (this.pwdForm.newPassword !== this.pwdForm.confirmPassword) {
        return this.$message.error('两次输入的新密码不一致');
      }
      const res = await api.updatePassword(this.pwdForm);
      if (res.code === 200) {
        this.$message.success('密码修改成功，请重新登录');
        mutations.clearUser();
        this.$router.push('/');
      }
    }
  }
};
</script>

<style scoped>
.user-center-page { padding: 40px 0; background: #f4f7f9; min-height: 80vh; }
.main-card { background: #fff; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); overflow: hidden; max-width: 1100px; margin: 0 auto; }
.el-aside { border-right: 1px solid #f0f0f0; padding: 30px 0; }
.user-info-side { text-align: center; padding-bottom: 30px; border-bottom: 1px solid #f9f9f9; margin-bottom: 20px; }
.nickname { font-size: 18px; font-weight: bold; margin: 10px 0; color: #2c3e50; }
.content-area { padding: 30px 50px; }
.pane-title { margin-bottom: 30px; padding-bottom: 10px; border-bottom: 2px solid #409EFF; display: inline-block; }

/* 头像上传样式 */
.avatar-uploader { border: 1px dashed #d9d9d9; border-radius: 50%; cursor: pointer; width: 100px; height: 100px; overflow: hidden; position: relative; transition: 0.3s; }
.avatar-uploader:hover { border-color: #409EFF; }
.avatar-uploader-icon { font-size: 28px; color: #8c939d; width: 100px; height: 100px; line-height: 100px; text-align: center; }
.avatar-preview { width: 100px; height: 100px; display: block; object-fit: cover; }
.upload-tip { font-size: 12px; color: #999; margin-left: 15px; }
.profile-form { max-width: 500px; }
</style>