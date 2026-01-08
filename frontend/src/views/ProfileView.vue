<template>
  <div class="user-center-page">
    <el-card class="main-layout-card" :body-style="{ padding: '0px' }">
      <el-container style="min-height: 720px;">
        <!-- 左侧导航 -->
        <el-aside width="280px" class="aside-menu">
          <div class="user-profile-header">
            <div class="avatar-wrapper">
              <el-avatar :size="96" :src="user.avatarUrl" icon="el-icon-user-solid" class="main-avatar"></el-avatar>
              <div class="vip-badge"><i class="el-icon-medal-1"></i></div>
            </div>
            <div class="user-name-box">
              <span class="name">{{ user.name }}</span>
              <div class="membership-pill">
                <i class="el-icon-trophy"></i> {{ user.membershipLevel }}会员
              </div>
            </div>
          </div>

          <div class="menu-wrapper">
            <el-menu :default-active="activeTab" @select="activeTab = $event" class="side-menu-custom">
              <el-menu-item index="info"><i class="el-icon-edit-outline"></i><span>修改个人资料</span></el-menu-item>
              <el-menu-item index="family"><i class="el-icon-user"></i><span>常用乘机人</span></el-menu-item>
              <el-menu-item index="messages"><i class="el-icon-bell"></i><span>系统通知</span></el-menu-item>
              <el-menu-item index="security"><i class="el-icon-lock"></i><span>安全中心</span></el-menu-item>
              <el-menu-item index="benefits"><i class="el-icon-present"></i><span>会员权益</span></el-menu-item>
            </el-menu>
          </div>
        </el-aside>

        <!-- 右侧内容 -->
        <el-main class="main-content-pane">
          <!-- 面板 A: 个人资料修改 -->
          <div v-if="activeTab === 'info'" class="pane-fade-in">
            <div class="pane-header-simple">
              <h2 class="pane-title">个人资料</h2>
              <span class="sub-text">管理您的基本账号信息</span>
            </div>
            <el-form label-position="top" class="custom-form">
              <el-form-item label="用户头像">
                <el-upload class="custom-avatar-uploader" action="http://localhost:8080/api/files/upload?bizType=avatars"
                           :headers="uploadHeaders" :show-file-list="false" :on-success="handleAvatarSuccess">
                  <img v-if="user.avatarUrl" :src="user.avatarUrl" class="uploaded-img">
                  <div v-else class="upload-placeholder">
                    <i class="el-icon-camera-solid"></i>
                    <span>点击上传</span>
                  </div>
                </el-upload>
              </el-form-item>
              <el-form-item label="电子邮箱">
                <el-input v-model="user.email" placeholder="请输入邮箱"></el-input>
              </el-form-item>
              <el-form-item label="手机号码">
                <el-input :value="user.username" disabled>
                  <template slot="append"><i class="el-icon-lock"></i> 已认证</template>
                </el-input>
              </el-form-item>
              <div class="form-actions">
                <el-button type="primary" class="theme-btn" @click="submitProfile" :loading="loading">保存更改</el-button>
              </div>
            </el-form>
          </div>

          <!-- 面板 B: 常用乘机人管理 -->
          <div v-if="activeTab === 'family'" class="pane-fade-in">
            <div class="pane-header-flex">
              <div>
                <h2 class="pane-title">常用乘机人</h2>
                <p class="sub-text">管理您的亲友信息，购票更便捷</p>
              </div>
              <el-button type="primary" size="medium" icon="el-icon-plus" class="theme-btn" @click="showFamilyDialog = true">新增乘机人</el-button>
            </div>

            <el-table :data="familyList" class="custom-table" v-loading="loading" stripe border header-row-class-name="table-header">
              <el-table-column prop="name" label="姓名" width="140">
                <template slot-scope="scope">
                  <span class="strong-text">{{ scope.row.name }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="phone" label="联系电话" width="160"></el-table-column>
              <el-table-column prop="idCard" label="证件号码 (加密)" min-width="200"></el-table-column>
              <el-table-column label="操作" width="100" align="center">
                <template slot-scope="scope">
                  <el-button type="text" class="danger-link" icon="el-icon-delete" @click="handleDeleteFamily(scope.row.memberId)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 面板 C: 系统消息 -->
          <div v-if="activeTab === 'messages'" class="pane-fade-in">
            <div class="pane-header-simple">
              <h2 class="pane-title">消息中心</h2>
            </div>
            <div v-if="messages.length === 0" class="empty-state">
              <i class="el-icon-chat-dot-square"></i>
              <p>暂无新通知</p>
            </div>
            <el-collapse v-else v-model="activeMessageNames" accordion class="custom-collapse">
              <el-collapse-item v-for="msg in messages" :key="msg.msgId" :name="msg.msgId">
                <template slot="title">
                  <div class="msg-head-wrapper">
                    <div class="msg-left">
                      <i class="el-icon-message-solid msg-icon"></i>
                      <span class="msg-t">{{ msg.title }}</span>
                    </div>
                    <span class="msg-d">{{ formatTime(msg.createTime) }}</span>
                  </div>
                </template>
                <div class="msg-body">
                  <div class="msg-content-text">{{ msg.content }}</div>
                  <div class="msg-footer">发布人: {{ msg.publisher }}</div>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>

          <!-- 面板 D: 安全中心 -->
          <div v-if="activeTab === 'security'" class="pane-fade-in">
            <div class="pane-header-simple">
              <h2 class="pane-title">安全中心</h2>
              <span class="sub-text">定期修改密码可以保护您的账号安全</span>
            </div>
            <el-form :model="pwdForm" label-position="top" class="custom-form security-form">
              <el-form-item label="当前密码">
                <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入当前使用的密码"></el-input>
              </el-form-item>
              <el-form-item label="新密码">
                <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="6-20位字符"></el-input>
              </el-form-item>
              <el-form-item label="确认新密码">
                <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码"></el-input>
              </el-form-item>
              <div class="form-actions">
                <el-button type="danger" class="danger-btn" @click="submitPassword" :loading="loading">确认重置密码</el-button>
              </div>
            </el-form>
          </div>

          <!-- 面板 E: 会员权益 -->
          <div v-if="activeTab === 'benefits'" class="pane-fade-in">
            <h2 class="pane-title">会员尊享权益</h2>
            <div class="membership-container">
              <!-- 核心特权卡片 -->
              <div class="premium-card">
                <div class="card-bg-pattern"></div>
                <div class="card-content">
                  <div class="card-top">
                    <span class="brand">清风航空 | QingFeng Air</span>
                    <span class="level-tag">{{ user.membershipLevel }} MEMBER</span>
                  </div>
                  <div class="card-mid">
                    <div class="chip"></div>
                    <div class="card-number">NO. {{ String(user.id).padStart(8, '0') }}</div>
                  </div>
                  <div class="card-bottom">
                    <div class="holder-name">{{ user.name }}</div>
                    <div class="valid-date">VALID THRU 12/28</div>
                  </div>
                </div>
              </div>

              <div class="rights-list">
                <div class="right-item">
                  <div class="icon-box blue"><i class="el-icon-suitcase"></i></div>
                  <div class="text-box">
                    <h4>额外行李额</h4>
                    <p>尊享额外 20KG 免费托运额度</p>
                  </div>
                </div>
                <div class="right-item">
                  <div class="icon-box purple"><i class="el-icon-coffee"></i></div>
                  <div class="text-box">
                    <h4>贵宾休息室</h4>
                    <p>全球自营休息室无限次使用</p>
                  </div>
                </div>
                <div class="right-item">
                  <div class="icon-box gold"><i class="el-icon-star-on"></i></div>
                  <div class="text-box">
                    <h4>优先服务</h4>
                    <p>优先值机、登机与行李提取</p>
                  </div>
                </div>
              </div>
            </div>

            <div class="stats-row">
              <div class="stat-box">
                <div class="label">当前里程积分</div>
                <div class="value">12,840</div>
              </div>
              <div class="stat-box">
                <div class="label">本年度航段</div>
                <div class="value">12 <span class="unit">次</span></div>
              </div>
              <div class="stat-box">
                <div class="label">距离下一等级</div>
                <div class="value">3 <span class="unit">航段</span></div>
              </div>
            </div>
          </div>
        </el-main>
      </el-container>
    </el-card>

    <!-- 弹窗 -->
    <el-dialog title="新增常用乘机人" :visible.sync="showFamilyDialog" width="420px" append-to-body custom-class="custom-dialog">
      <el-form :model="familyForm" label-position="top">
        <el-form-item label="真实姓名"><el-input v-model="familyForm.name" placeholder="需与证件一致"></el-input></el-form-item>
        <el-form-item label="身份证号"><el-input v-model="familyForm.idCard" placeholder="18位身份证号码"></el-input></el-form-item>
        <el-form-item label="手机号码"><el-input v-model="familyForm.phone" placeholder="选填"></el-input></el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showFamilyDialog = false">取消</el-button>
        <el-button type="primary" class="theme-btn" @click="submitAddFamily" :loading="loading">保存</el-button>
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
        this.$message.success('头像上传成功，请点击保存资料');
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
      if (!this.familyForm.name || !this.familyForm.idCard) return this.$message.warning('请填写姓名和身份证号');
      this.loading = true;
      try {
        const res = await api.addFamily(this.familyForm);
        if (res.code === 200) {
          this.$message.success('添加成功');
          this.showFamilyDialog = false;
          this.familyForm = { name: '', phone: '', idCard: '' };
          this.fetchFamily();
        }
      } catch (e) { console.error(e); } finally { this.loading = false; }
    },
    async handleDeleteFamily(id) {
      try {
        await this.$confirm('确定要删除该乘机人吗？', '提示', { type: 'warning' });
        const res = await api.deleteFamily(id);
        if (res.code === 200) { this.$message.success('已删除'); this.fetchFamily(); }
      } catch (e) { /* 取消 */ }
    },
    async submitPassword() {
      if (this.pwdForm.newPassword !== this.pwdForm.confirmPassword) return this.$message.error('两次密码不一致');
      this.loading = true;
      try {
        const res = await api.updatePassword(this.pwdForm);
        if (res.code === 200) {
          this.$message.success('密码修改成功，请重新登录');
          mutations.clearUser();
          this.$router.push('/admin-login');
        }
      } catch (e) { console.error(e); } finally { this.loading = false; }
    }
  }
};
</script>

<style scoped>
/* 全局布局 */
.user-center-page { padding: 40px 20px; background-color: #f5f7fa; min-height: 100vh; }
.main-layout-card { max-width: 1100px; margin: 0 auto; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.05); border: 1px solid #ebeef5; }

/* 左侧侧边栏 */
.aside-menu { background-color: #fff; border-right: 1px solid #e6e6e6; display: flex; flex-direction: column; }
.user-profile-header { padding: 40px 20px; text-align: center; background: linear-gradient(180deg, #fdfdfd 0%, #5ec7ff 100%); border-bottom: 1px solid #eee; }

.avatar-wrapper { position: relative; display: inline-block; margin-bottom: 15px; }
.main-avatar { border: 4px solid #fff; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.vip-badge { position: absolute; bottom: 2px; right: 2px; background: #E6A23C; color: #fff; width: 26px; height: 26px; border-radius: 50%; display: flex; align-items: center; justify-content: center; border: 2px solid #fff; font-size: 14px; }

.user-name-box .name { font-size: 18px; font-weight: 700; color: #303133; margin-bottom: 8px; display: block; }
.membership-pill { display: inline-block; background-color: #ecf5ff; color: #409EFF; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; border: 1px solid #d9ecff; }

.menu-wrapper { padding: 15px 0; flex: 1; }
.side-menu-custom { border-right: none; }
.side-menu-custom .el-menu-item { height: 50px; line-height: 50px; margin: 4px 12px; border-radius: 8px; color: #606266; }
.side-menu-custom .el-menu-item i { color: #909399; margin-right: 10px; }
.side-menu-custom .el-menu-item.is-active { background-color: #e6f1fc !important; color: #409EFF !important; font-weight: 600; }
.side-menu-custom .el-menu-item.is-active i { color: #409EFF; }
.side-menu-custom .el-menu-item:hover { background-color: #f5f7fa; }

/* 右侧内容区 */
.main-content-pane { padding: 40px 50px; background-color: #fff; }
.pane-header-simple { margin-bottom: 30px; border-bottom: 1px solid #f0f0f0; padding-bottom: 15px; }
.pane-header-flex { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 30px; border-bottom: 1px solid #f0f0f0; padding-bottom: 15px; }
.pane-title { font-size: 22px; color: #303133; font-weight: 700; margin: 0 0 8px 0; }
.sub-text { font-size: 13px; color: #909399; }

/* 表单样式 */
.custom-form { max-width: 500px; }
.custom-form .el-form-item__label { color: #606266; font-weight: 500; padding-bottom: 4px; }
.form-actions { margin-top: 30px; }

/* 头像上传 */
.custom-avatar-uploader { width: 100px; height: 100px; border: 1px dashed #dcdfe6; border-radius: 50%; overflow: hidden; cursor: pointer; transition: 0.3s; display: flex; align-items: center; justify-content: center; background-color: #fafafa; }
.custom-avatar-uploader:hover { border-color: #409EFF; background-color: #f0f7ff; }
.upload-placeholder { text-align: center; color: #909399; font-size: 12px; }
.upload-placeholder i { font-size: 24px; display: block; margin-bottom: 4px; }
.uploaded-img { width: 100%; height: 100%; object-fit: cover; }

/* 按钮 */
.theme-btn { background-color: #409EFF; border-color: #409EFF; font-weight: 500; padding: 10px 25px; transition: 0.3s; }
.theme-btn:hover { background-color: #66b1ff; border-color: #66b1ff; transform: translateY(-1px); }
.danger-btn { padding: 10px 25px; }
.danger-link { color: #F56C6C; font-size: 13px; }
.danger-link:hover { text-decoration: underline; }

/* 表格 */
.custom-table { border-radius: 8px; overflow: hidden; border: 1px solid #ebeef5; }
.strong-text { font-weight: 600; color: #303133; }

/* 消息中心 */
.empty-state { text-align: center; padding: 60px 0; color: #909399; }
.empty-state i { font-size: 48px; margin-bottom: 15px; color: #e0e0e0; }
.custom-collapse { border-top: none; }
.msg-head-wrapper { display: flex; justify-content: space-between; width: 100%; align-items: center; padding-right: 15px; }
.msg-left { display: flex; align-items: center; gap: 8px; }
.msg-icon { color: #E6A23C; }
.msg-t { font-weight: 600; color: #303133; font-size: 15px; }
.msg-d { font-size: 13px; color: #909399; }
.msg-body { background: #f8f9fa; padding: 15px 20px; border-radius: 8px; margin-top: 10px; }
.msg-content-text { color: #555; line-height: 1.6; font-size: 14px; }
.msg-footer { text-align: right; color: #909399; font-size: 12px; margin-top: 10px; border-top: 1px dashed #e4e7ed; padding-top: 8px; }

/* 会员权益卡片 */
.membership-container { display: flex; gap: 40px; margin-bottom: 40px; align-items: center; }
.premium-card { width: 340px; height: 200px; background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%); border-radius: 16px; position: relative; color: #fff; box-shadow: 0 10px 30px rgba(30, 60, 114, 0.3); overflow: hidden; flex-shrink: 0; }
.card-bg-pattern { position: absolute; top: -50%; right: -50%; width: 200%; height: 200%; background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 60%); transform: rotate(30deg); }
.card-content { position: relative; z-index: 2; padding: 25px; height: 100%; display: flex; flex-direction: column; justify-content: space-between; box-sizing: border-box; }
.card-top { display: flex; justify-content: space-between; align-items: center; }
.brand { font-size: 14px; font-weight: 500; opacity: 0.9; letter-spacing: 1px; }
.level-tag { background: rgba(255,255,255,0.2); padding: 2px 8px; border-radius: 4px; font-size: 10px; font-weight: bold; }
.card-mid { display: flex; align-items: center; gap: 15px; margin-top: 20px; }
.chip { width: 35px; height: 25px; background: linear-gradient(135deg, #e0e0e0 0%, #b0b0b0 100%); border-radius: 4px; }
.card-number { font-family: 'Courier New', monospace; font-size: 18px; letter-spacing: 2px; text-shadow: 0 1px 2px rgba(0,0,0,0.3); }
.card-bottom { display: flex; justify-content: space-between; align-items: flex-end; }
.holder-name { font-size: 16px; font-weight: 600; text-transform: uppercase; }
.valid-date { font-size: 10px; opacity: 0.7; }

.rights-list { flex: 1; display: grid; grid-template-columns: 1fr; gap: 20px; }
.right-item { display: flex; align-items: flex-start; gap: 15px; }
.icon-box { width: 40px; height: 40px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 20px; flex-shrink: 0; }
.icon-box.blue { background: #e6f1fc; color: #409EFF; }
.icon-box.purple { background: #f2f0fe; color: #722ed1; }
.icon-box.gold { background: #fff7e6; color: #faad14; }
.text-box h4 { margin: 0 0 4px 0; color: #303133; font-size: 15px; }
.text-box p { margin: 0; color: #909399; font-size: 13px; }

.stats-row { display: flex; gap: 20px; border-top: 1px solid #f0f0f0; padding-top: 30px; }
.stat-box { flex: 1; background: #f8f9fa; padding: 20px; border-radius: 8px; text-align: center; }
.stat-box .label { color: #909399; font-size: 13px; margin-bottom: 8px; }
.stat-box .value { color: #303133; font-size: 24px; font-weight: 800; }
.stat-box .unit { font-size: 12px; font-weight: normal; color: #999; }

/* 动画 */
.pane-fade-in { animation: slideUp 0.4s ease-out; }
@keyframes slideUp { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
</style>