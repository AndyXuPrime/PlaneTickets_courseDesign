<template>
  <div class="user-center-page">
    <el-card class="main-layout-card" :body-style="{ padding: '0px' }">
      <el-container style="min-height: 650px;">

        <!-- 左侧：功能切换菜单 -->
        <el-aside width="240px" class="aside-menu">
          <div class="user-avatar-section">
            <el-avatar :size="80" :src="user.avatarUrl" icon="el-icon-user-solid"></el-avatar>
            <div class="user-name-tag">
              <span class="name">{{ user.name }}</span>
              <el-tag size="mini" type="warning" effect="dark">{{ user.membershipLevel }}会员</el-tag>
            </div>
          </div>

          <el-menu :default-active="activeTab" @select="activeTab = $event" class="side-menu">
            <el-menu-item index="info">
              <i class="el-icon-edit-outline"></i><span>修改个人资料</span>
            </el-menu-item>
            <el-menu-item index="family">
              <i class="el-icon-user"></i><span>常用乘机人</span>
            </el-menu-item>
            <el-menu-item index="messages"> <!-- 新增菜单项 -->
              <i class="el-icon-bell"></i><span>我的消息</span>
            </el-menu-item>
            <el-menu-item index="security">
              <i class="el-icon-lock"></i><span>安全中心 (改密)</span>
            </el-menu-item>
            <el-menu-item index="benefits">
              <i class="el-icon-medal"></i><span>会员权益 & 说明</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <!-- 右侧：功能面板 -->
        <el-main class="main-content-pane">

          <!-- 面板 A: 个人资料修改 -->
          <div v-if="activeTab === 'info'">
            <h2 class="section-title">个人资料修改</h2>
            <el-form label-position="top" class="form-container">
              <el-form-item label="用户头像 (点击图片更换)">
                <el-upload
                    class="avatar-uploader"
                    action="http://localhost:8080/api/files/upload?bizType=avatars"
                    :headers="uploadHeaders"
                    :show-file-list="false"
                    :on-success="handleAvatarSuccess">
                  <img v-if="user.avatarUrl" :src="user.avatarUrl" class="avatar-img">
                  <i v-else class="el-icon-plus avatar-icon"></i>
                </el-upload>
              </el-form-item>

              <el-form-item label="电子邮箱">
                <el-input v-model="user.email" placeholder="example@mail.com"></el-input>
              </el-form-item>

              <el-form-item label="手机号码 (不可修改)">
                <el-input :value="user.username" disabled></el-input>
              </el-form-item>

              <el-button type="primary" class="save-btn" @click="submitProfile" :loading="loading">保存资料修改</el-button>
            </el-form>
          </div>

          <!-- 面板 B: 常用乘机人管理 -->
          <div v-if="activeTab === 'family'">
            <div class="pane-header">
              <h2 class="section-title">常用乘机人管理</h2>
              <el-button type="primary" size="small" icon="el-icon-plus" @click="showFamilyDialog = true">新增乘机人</el-button>
            </div>

            <el-table :data="familyList" border stripe style="width: 100%; margin-top: 20px" v-loading="loading">
              <el-table-column prop="name" label="姓名" width="120"></el-table-column>
              <el-table-column prop="phone" label="手机号" width="150"></el-table-column>
              <el-table-column prop="idCard" label="身份证号 (加密存储)" min-width="200"></el-table-column>
              <el-table-column label="操作" width="100" align="center">
                <template slot-scope="scope">
                  <el-button type="text" style="color: #f56c6c" @click="handleDeleteFamily(scope.row.memberId)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 新增乘机人弹窗 -->
            <el-dialog title="添加乘机人" :visible.sync="showFamilyDialog" width="400px" append-to-body>
              <el-form :model="familyForm" label-position="top">
                <el-form-item label="真实姓名">
                  <el-input v-model="familyForm.name" placeholder="请输入姓名"></el-input>
                </el-form-item>
                <el-form-item label="手机号码">
                  <el-input v-model="familyForm.phone" placeholder="选填"></el-input>
                </el-form-item>
                <el-form-item label="身份证号">
                  <el-input v-model="familyForm.idCard" placeholder="请输入18位身份证号"></el-input>
                </el-form-item>
              </el-form>
              <div slot="footer">
                <el-button @click="showFamilyDialog = false">取消</el-button>
                <el-button type="primary" @click="submitAddFamily" :loading="loading">确认添加</el-button>
              </div>
            </el-dialog>
          </div>

          <!-- 面板 E: 我的消息 (新加入的模块) -->
          <div v-if="activeTab === 'messages'">
            <h2 class="section-title">系统通知</h2>

            <div v-if="loading" class="loading-box"><i class="el-icon-loading"></i> 正在获取最新消息...</div>

            <div v-else-if="messages.length === 0" class="empty-box">
              <i class="el-icon-chat-dot-round" style="font-size: 40px; color: #ddd;"></i>
              <p>暂无系统消息</p>
            </div>

            <el-collapse v-else v-model="activeMessageNames" accordion class="message-collapse">
              <el-collapse-item v-for="msg in messages" :key="msg.msgId" :name="msg.msgId">
                <template slot="title">
                  <div class="msg-header">
                    <span class="msg-title">
                      <i class="el-icon-message-solid" style="color: #E6A23C; margin-right: 5px;"></i>
                      {{ msg.title }}
                    </span>
                    <span class="msg-time">{{ formatTime(msg.createTime) }}</span>
                  </div>
                </template>
                <div class="msg-content">{{ msg.content }}</div>
                <div class="msg-footer">发布人: {{ msg.publisher }}</div>
              </el-collapse-item>
            </el-collapse>
          </div>

          <!-- 面板 C: 安全中心 -->
          <div v-if="activeTab === 'security'">
            <h2 class="section-title">修改登录密码</h2>
            <el-form :model="pwdForm" label-position="top" class="form-container">
              <el-form-item label="原密码">
                <el-input v-model="pwdForm.oldPassword" type="password" show-password></el-input>
              </el-form-item>
              <el-form-item label="新密码">
                <el-input v-model="pwdForm.newPassword" type="password" show-password></el-input>
              </el-form-item>
              <el-form-item label="确认新密码">
                <el-input v-model="pwdForm.confirmPassword" type="password" show-password></el-input>
              </el-form-item>
              <el-button type="danger" class="save-btn" @click="submitPassword" :loading="loading">确认重置密码</el-button>
            </el-form>
          </div>

          <!-- 面板 D: 会员权益 -->
          <div v-if="activeTab === 'benefits'">
            <h2 class="section-title">我的会员服务</h2>
            <div class="old-features-grid">
              <div class="mini-card">以家庭为单位累积里程</div>
              <div class="mini-card">清风航空高级会员服务</div>
              <div class="mini-card">各地区的会员优惠</div>
            </div>
            <el-collapse accordion style="margin-top: 30px;">
              <el-collapse-item title="清风航空会员卡 说明" name="1">
                <div>您的电子会员卡已在App中激活。实体卡片将在您完成首次飞行后的2-4周内寄出。</div>
              </el-collapse-item>
              <el-collapse-item title="关于会员号/主卡的说明" name="2">
                <div>您的会员号是您在我们这里的唯一身份标识。</div>
              </el-collapse-item>
            </el-collapse>
          </div>

        </el-main>
      </el-container>
    </el-card>
  </div>
</template>

<script>
import api from '@/api';
import { store, mutations } from '@/store';

export default {
  data() {
    return {
      activeTab: 'info',
      loading: false,
      user: { ...store.user },
      pwdForm: { oldPassword: '', newPassword: '', confirmPassword: '' },
      // 家人管理数据
      familyList: [],
      showFamilyDialog: false,
      familyForm: { name: '', phone: '', idCard: '' },
      // 消息管理数据
      messages: [],
      activeMessageNames: [],
      uploadHeaders: { Authorization: 'Bearer ' + localStorage.getItem('authToken') }
    };
  },
  watch: {
    activeTab(newTab) {
      if (newTab === 'family') {
        this.fetchFamily();
      } else if (newTab === 'messages') {
        this.fetchMessages();
      }
    }
  },
  methods: {
    // --- 消息逻辑 ---
    async fetchMessages() {
      this.loading = true;
      try {
        const res = await api.getSystemMessages();
        if (res.code === 200) {
          this.messages = res.data;
        }
      } catch (e) {
        // 错误已由拦截器处理
      } finally {
        this.loading = false;
      }
    },
    formatTime(time) {
      if (!time) return '';
      return new Date(time).toLocaleString();
    },

    // --- 个人资料逻辑 ---
    handleAvatarSuccess(res) {
      if (res.code === 200) {
        this.user.avatarUrl = res.data;
        this.$message.success('头像上传成功，请保存资料以生效');
      }
    },
    async submitProfile() {
      this.loading = true;
      try {
        const res = await api.updateProfile({ email: this.user.email, avatarUrl: this.user.avatarUrl });
        if (res.code === 200) {
          mutations.setUser({ ...store.user, ...this.user }, localStorage.getItem('authToken'));
          this.$message.success('资料已同步');
        }
      } finally { this.loading = false; }
    },

    // --- 家人管理逻辑 ---
    async fetchFamily() {
      this.loading = true;
      try {
        const res = await api.getFamily();
        if (res.code === 200) this.familyList = res.data;
      } finally { this.loading = false; }
    },
    async submitAddFamily() {
      if (!this.familyForm.name || !this.familyForm.idCard) {
        return this.$message.warning('请填写姓名和身份证号');
      }
      this.loading = true;
      try {
        const res = await api.addFamily(this.familyForm);
        if (res.code === 200) {
          this.$message.success('添加成功');
          this.showFamilyDialog = false;
          this.familyForm = { name: '', phone: '', idCard: '' };
          this.fetchFamily();
        }
      } finally { this.loading = false; }
    },
    async handleDeleteFamily(id) {
      try {
        await this.$confirm('确定要删除该乘机人吗？', '提示', { type: 'warning' });
        const res = await api.deleteFamily(id);
        if (res.code === 200) {
          this.$message.success('已删除');
          this.fetchFamily();
        }
      } catch (e) { /* 取消删除 */ }
    },

    // --- 密码逻辑 ---
    async submitPassword() {
      if (this.pwdForm.newPassword !== this.pwdForm.confirmPassword) {
        return this.$message.error('两次输入的新密码不匹配');
      }
      this.loading = true;
      try {
        const res = await api.updatePassword(this.pwdForm);
        if (res.code === 200) {
          this.$message.success('密码修改成功，请重新登录');
          mutations.clearUser();
          this.$router.push('/admin-login');
        }
      } finally { this.loading = false; }
    }
  }
};
</script>

<style scoped>
.user-center-page { padding: 50px 20px; background-color: #f0f2f5; min-height: 90vh; }
.main-layout-card { max-width: 1100px; margin: 0 auto; border-radius: 15px; }
.aside-menu { background-color: #fff; border-right: 1px solid #f0f0f0; }
.user-avatar-section { padding: 40px 20px; text-align: center; border-bottom: 1px solid #f5f7fa; }
.user-name-tag { margin-top: 15px; }
.user-name-tag .name { display: block; font-weight: bold; font-size: 18px; margin-bottom: 5px; }
.side-menu { border-right: none; margin-top: 10px; }

.main-content-pane { padding: 40px 60px; background-color: #fff; }
.pane-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.section-title { font-size: 22px; color: #303133; border-left: 4px solid #409EFF; padding-left: 15px; margin: 0 0 20px 0; }
.form-container { max-width: 500px; margin-top: 20px; }

/* 消息样式 */
.message-collapse { border-top: 1px solid #ebeef5; }
.msg-header { display: flex; justify-content: space-between; width: 100%; padding-right: 20px; }
.msg-title { font-weight: bold; color: #303133; }
.msg-time { color: #909399; font-size: 13px; }
.msg-content { padding: 10px 0; color: #606266; line-height: 1.6; white-space: pre-wrap; }
.msg-footer { text-align: right; color: #C0C4CC; font-size: 12px; margin-top: 10px; }
.empty-box { text-align: center; color: #909399; margin-top: 50px; }
.loading-box { text-align: center; padding: 20px; color: #909399; }

/* 头像上传美化 */
.avatar-uploader { border: 1px dashed #d9d9d9; border-radius: 50%; cursor: pointer; width: 100px; height: 100px; overflow: hidden; transition: 0.3s; }
.avatar-uploader:hover { border-color: #409EFF; }
.avatar-icon { font-size: 28px; color: #8c939d; line-height: 100px; text-align: center; width: 100px; }
.avatar-img { width: 100px; height: 100px; object-fit: cover; }
.save-btn { margin-top: 20px; width: 200px; }

.old-features-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin-top: 20px; }
.mini-card { padding: 20px; background: #f8f9fb; border-radius: 8px; text-align: center; font-size: 14px; color: #666; }
</style>