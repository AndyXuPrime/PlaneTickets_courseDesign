<template>
  <div class="user-center-page">
    <el-card class="main-layout-card" :body-style="{ padding: '0px' }">
      <el-container style="min-height: 720px;">
        <!-- 左侧导航 -->
        <el-aside width="260px" class="aside-menu">
          <div class="user-profile-header">
            <div class="avatar-wrapper">
              <el-avatar :size="90" :src="user.avatarUrl" icon="el-icon-user-solid"></el-avatar>
              <div class="vip-badge" :style="{ backgroundColor: '#DCC87F' }"><i class="el-icon-medal"></i></div>
            </div>
            <div class="user-name-tag">
              <span class="name">{{ user.name }}</span>
              <div class="membership-label">VIP {{ user.membershipLevel }}</div>
            </div>
          </div>
          <el-menu :default-active="activeTab" @select="activeTab = $event" class="side-menu-custom">
            <el-menu-item index="info"><i class="el-icon-edit"></i><span>资料修改</span></el-menu-item>
            <el-menu-item index="family"><i class="el-icon-user"></i><span>乘机人管理</span></el-menu-item>
            <el-menu-item index="messages"><i class="el-icon-bell"></i><span>系统通知</span></el-menu-item>
            <el-menu-item index="security"><i class="el-icon-lock"></i><span>安全中心</span></el-menu-item>
            <el-menu-item index="benefits"><i class="el-icon-trophy"></i><span>会员权益</span></el-menu-item>
          </el-menu>
        </el-aside>

        <!-- 右侧内容 -->
        <el-main class="main-content-pane">
          <!-- 个人资料 -->
          <div v-if="activeTab === 'info'" class="pane-fade-in">
            <h2 class="pane-title">个人资料修改</h2>
            <el-form label-position="top" class="custom-form">
              <el-form-item label="形象头像">
                <el-upload class="custom-avatar-uploader" action="http://localhost:8080/api/files/upload?bizType=avatars"
                           :headers="uploadHeaders" :show-file-list="false" :on-success="handleAvatarSuccess">
                  <img v-if="user.avatarUrl" :src="user.avatarUrl" class="uploaded-img">
                  <i v-else class="el-icon-camera uploader-icon"></i>
                </el-upload>
              </el-form-item>
              <el-form-item label="电子邮箱"><el-input v-model="user.email"></el-input></el-form-item>
              <el-form-item label="手机号码 (只读)"><el-input :value="user.username" disabled></el-input></el-form-item>
              <el-button type="primary" class="theme-btn" @click="submitProfile" :loading="loading">保存修改</el-button>
            </el-form>
          </div>

          <!-- 常用乘机人 -->
          <div v-if="activeTab === 'family'" class="pane-fade-in">
            <div class="pane-header">
              <h2 class="pane-title">常用乘机人</h2>
              <el-button type="primary" size="small" class="theme-btn" @click="showFamilyDialog = true">新增人员</el-button>
            </div>
            <el-table :data="familyList" class="custom-table" v-loading="loading">
              <el-table-column prop="name" label="姓名"></el-table-column>
              <el-table-column prop="phone" label="联系电话"></el-table-column>
              <el-table-column prop="idCard" label="证件号" min-width="180"></el-table-column>
              <el-table-column label="操作" width="80" align="center">
                <template slot-scope="scope">
                  <el-button type="text" class="danger-text" @click="handleDeleteFamily(scope.row.memberId)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 系统消息 -->
          <div v-if="activeTab === 'messages'" class="pane-fade-in">
            <h2 class="pane-title">系统通知</h2>
            <div v-if="messages.length === 0" class="center-box">目前没有新通知</div>
            <el-collapse v-else v-model="activeMessageNames" accordion class="custom-collapse">
              <el-collapse-item v-for="msg in messages" :key="msg.msgId" :name="msg.msgId">
                <template slot="title">
                  <div class="msg-head-wrapper">
                    <span class="msg-t">{{ msg.title }}</span>
                    <span class="msg-d">{{ formatTime(msg.createTime) }}</span>
                  </div>
                </template>
                <div class="msg-body">{{ msg.content }}</div>
              </el-collapse-item>
            </el-collapse>
          </div>

          <!-- 安全中心 -->
          <div v-if="activeTab === 'security'" class="pane-fade-in">
            <h2 class="pane-title">安全中心</h2>
            <el-form :model="pwdForm" label-position="top" class="custom-form">
              <el-form-item label="当前密码"><el-input v-model="pwdForm.oldPassword" type="password" show-password></el-input></el-form-item>
              <el-form-item label="设置新密码"><el-input v-model="pwdForm.newPassword" type="password" show-password></el-input></el-form-item>
              <el-form-item label="确认新密码"><el-input v-model="pwdForm.confirmPassword" type="password" show-password></el-input></el-form-item>
              <el-button type="danger" class="danger-btn" @click="submitPassword" :loading="loading">重置登录密码</el-button>
            </el-form>
          </div>

          <!-- 会员权益模块 (参考设计优化) -->
          <div v-if="activeTab === 'benefits'" class="pane-fade-in">
            <h2 class="pane-title">蓝天云悦会员特权</h2>
            <div class="membership-container">
              <!-- 核心特权卡片 -->
              <div class="dark-membership-card">
                <div class="card-title-box">
                  <span class="main-title">至臻飞行礼遇</span>
                  <span class="sub-title">LEVEL: {{ user.membershipLevel }}</span>
                </div>
                <div class="separator"></div>
                <ul class="benefit-list">
                  <li class="element">
                    <i class="el-icon-circle-check"></i>
                    <span class="label">优先办理值机与登机</span>
                  </li>
                  <li class="element">
                    <i class="el-icon-suitcase"></i>
                    <span class="label">额外 20KG 免费托运行李额</span>
                  </li>
                  <li class="element">
                    <i class="el-icon-coffee"></i>
                    <span class="label">全球自营贵宾休息室准入</span>
                  </li>
                </ul>
                <div class="separator"></div>
                <ul class="benefit-list special">
                  <li class="element highlight">
                    <i class="el-icon-refresh"></i>
                    <span class="label">积分商城 8.5 折兑换权益</span>
                  </li>
                </ul>
              </div>

              <div class="membership-stats">
                <div class="stat-item" :style="{ borderLeft: '4px solid #DCC87F' }">
                  <span class="s-label">当前里程积分</span>
                  <span class="s-val">12,840 <small>pts</small></span>
                </div>
                <div class="stat-item" :style="{ borderLeft: '4px solid #82B1B7' }">
                  <span class="s-label">升级还需航段</span>
                  <span class="s-val">3 <small>segments</small></span>
                </div>
              </div>
            </div>
          </div>
        </el-main>
      </el-container>
    </el-card>

    <!-- 弹窗 -->
    <el-dialog title="新增常用乘机人" :visible.sync="showFamilyDialog" width="400px" append-to-body>
      <el-form :model="familyForm" label-position="top">
        <el-form-item label="姓名"><el-input v-model="familyForm.name" placeholder="与证件一致"></el-input></el-form-item>
        <el-form-item label="证件号码"><el-input v-model="familyForm.idCard" placeholder="18位身份证号"></el-input></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="familyForm.phone" placeholder="选填"></el-input></el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showFamilyDialog = false" size="small">取消</el-button>
        <el-button type="primary" class="theme-btn" @click="submitAddFamily" :loading="loading" size="small">提交保存</el-button>
      </div>
    </el-dialog>
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
      familyList: [],
      showFamilyDialog: false,
      familyForm: { name: '', phone: '', idCard: '' },
      messages: [],
      activeMessageNames: [],
      uploadHeaders: { Authorization: 'Bearer ' + localStorage.getItem('authToken') }
    };
  },
  watch: {
    activeTab(val) {
      if (val === 'family') this.fetchFamily();
      else if (val === 'messages') this.fetchMessages();
    }
  },
  methods: {
    async fetchMessages() {
      this.loading = true;
      try {
        const res = await api.getSystemMessages();
        if (res.code === 200) this.messages = res.data;
      } catch (e) { console.error('获取消息失败'); } finally { this.loading = false; }
    },
    formatTime(t) { return t ? new Date(t).toLocaleString() : ''; },
    handleAvatarSuccess(res) {
      if (res.code === 200) {
        this.user.avatarUrl = res.data;
        this.$message.success('预览头像已就绪，请保存资料');
      }
    },
    async submitProfile() {
      this.loading = true;
      try {
        const res = await api.updateProfile({ email: this.user.email, avatarUrl: this.user.avatarUrl });
        if (res.code === 200) {
          mutations.setUser({ ...store.user, ...this.user }, localStorage.getItem('authToken'));
          this.$message.success('个人档案同步成功');
        }
      } catch (e) { console.error(e); } finally { this.loading = false; }
    },
    async fetchFamily() {
      this.loading = true;
      try {
        const res = await api.getFamily();
        if (res.code === 200) this.familyList = res.data;
      } catch (e) { console.error(e); } finally { this.loading = false; }
    },
    async submitAddFamily() {
      if (!this.familyForm.name || !this.familyForm.idCard) return this.$message.warning('必填项缺失');
      this.loading = true;
      try {
        const res = await api.addFamily(this.familyForm);
        if (res.code === 200) {
          this.$message.success('乘机人已添加');
          this.showFamilyDialog = false;
          this.familyForm = { name: '', phone: '', idCard: '' };
          this.fetchFamily();
        }
      } catch (e) { console.error(e); } finally { this.loading = false; }
    },
    async handleDeleteFamily(id) {
      try {
        await this.$confirm('确定移除该乘机人信息吗？', '操作确认', { type: 'warning' });
        const res = await api.deleteFamily(id);
        if (res.code === 200) { this.$message.success('已移除'); this.fetchFamily(); }
      } catch (e) { console.log('用户取消操作'); }
    },
    async submitPassword() {
      if (this.pwdForm.newPassword !== this.pwdForm.confirmPassword) return this.$message.error('两次输入的密码不一致');
      this.loading = true;
      try {
        const res = await api.updatePassword(this.pwdForm);
        if (res.code === 200) {
          this.$message.success('密码重置成功，请重新验证身份');
          mutations.clearUser();
          this.$router.push('/admin-login');
        }
      } catch (e) { console.error(e); } finally { this.loading = false; }
    }
  }
};
</script>

<style scoped>
.user-center-page { padding: 60px 20px; background-color: #E4DFDB; min-height: 100vh; }
.main-layout-card { max-width: 1100px; margin: 0 auto; border-radius: 20px; border: none; box-shadow: 0 25px 50px rgba(0,0,0,0.1); }

/* 侧边栏 */
.aside-menu { background-color: #fff; border-right: 1px solid #ABAAA5; }
.user-profile-header { padding: 50px 20px 30px; text-align: center; background: #fdfdfd; }
.avatar-wrapper { position: relative; display: inline-block; }
.vip-badge { position: absolute; bottom: 0; right: 0; color: #fff; width: 28px; height: 28px; border-radius: 50%; display: flex; align-items: center; justify-content: center; border: 2px solid #fff; }
.user-name-tag { margin-top: 15px; }
.user-name-tag .name { display: block; font-weight: 800; font-size: 20px; color: #333; }
.membership-label { display: inline-block; margin-top: 8px; background: #DCC87F; color: #fff; padding: 2px 14px; border-radius: 20px; font-size: 11px; font-weight: bold; }

.side-menu-custom { border-right: none; padding: 10px; }
.side-menu-custom .el-menu-item { border-radius: 12px; margin-bottom: 6px; transition: 0.3s; }
.side-menu-custom .el-menu-item.is-active { background-color: #82B1B7 !important; color: #fff !important; }

/* 内容区 */
.main-content-pane { padding: 40px 60px; background-color: #fff; }
.pane-title { font-size: 24px; color: #333; font-weight: 800; margin: 0 0 35px 0; display: flex; align-items: center; }
.pane-title::before { content: ''; width: 6px; height: 24px; background: #82B1B7; margin-right: 12px; border-radius: 3px; }

/* 会员特权卡片 (参考设计优化) */
.membership-container { display: flex; gap: 30px; align-items: flex-start; }
.dark-membership-card { width: 320px; background-color: #242832; background-image: linear-gradient(139deg, #242832 0%, #251c28 100%); border-radius: 16px; padding: 25px 0; display: flex; flex-direction: column; gap: 15px; box-shadow: 0 15px 35px rgba(0,0,0,0.2); }
.card-title-box { padding: 0 20px; display: flex; flex-direction: column; }
.main-title { color: #DCC87F; font-weight: 800; font-size: 18px; letter-spacing: 1px; }
.sub-title { color: #ABAAA5; font-size: 11px; margin-top: 4px; }
.dark-membership-card .separator { border-top: 1.5px solid #42434a; }
.benefit-list { list-style: none; padding: 0 15px; display: flex; flex-direction: column; gap: 10px; }
.benefit-list .element { display: flex; align-items: center; color: #ABAAA5; gap: 12px; padding: 10px 12px; border-radius: 8px; transition: all 0.3s ease-out; cursor: pointer; }
.benefit-list .element i { font-size: 18px; color: #82B1B7; }
.benefit-list .element .label { font-size: 13px; font-weight: 600; }
.benefit-list .element:hover { background-color: #82B1B7; color: #ffffff; transform: translate(3px, -3px); }
.benefit-list .element:hover i { color: #fff; }
.benefit-list.special .element { color: #DCC87F; }
.benefit-list.special .element:hover { background-color: rgba(220, 200, 127, 0.15); color: #DCC87F; }

.membership-stats { flex: 1; display: flex; flex-direction: column; gap: 20px; }
.stat-item { background: #f9f9f9; padding: 20px; border-radius: 8px; display: flex; flex-direction: column; gap: 8px; }
.s-label { color: #ABAAA5; font-size: 12px; }
.s-val { font-size: 24px; font-weight: 900; color: #333; }
.s-val small { font-size: 12px; font-weight: normal; color: #999; margin-left: 5px; }

/* 其他通用样式 */
.theme-btn { background-color: #82B1B7 !important; border-color: #82B1B7 !important; font-weight: bold; }
.danger-btn { background-color: #9B1C31 !important; border-color: #9B1C31 !important; font-weight: bold; }
.danger-text { color: #9B1C31 !important; font-weight: bold; }
.custom-avatar-uploader { width: 110px; height: 110px; border: 2px dashed #ABAAA5; border-radius: 50%; overflow: hidden; display: flex; align-items: center; justify-content: center; cursor: pointer; }
.uploaded-img { width: 100%; height: 100%; object-fit: cover; }
.pane-fade-in { animation: fadeIn 0.4s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

.msg-head-wrapper { display: flex; justify-content: space-between; width: 100%; padding-right: 25px; }
.msg-t { font-weight: 700; color: #333; }
.msg-d { font-size: 12px; color: #ABAAA5; }
.msg-body { background: #f5f7fa; padding: 15px; border-radius: 8px; color: #666; font-size: 14px; }
</style>