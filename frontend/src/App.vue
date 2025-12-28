<template>
  <div id="app">
    <!-- 1. 只有非管理员页面才显示 Header -->
    <app-header
        v-if="!isAdminPage"
        :is-logged-in="state.isLoggedIn"
        :user="state.user"
        @logout="handleLogout"
        @show-login="showLogin"
        @show-register="showRegister"
    />

    <!-- 2. 主内容区：根据页面类型切换样式 -->
    <main :class="isAdminPage ? 'admin-main' : 'main-content'">
      <!-- 普通页面需要 container 居中，后台页面通常需要全屏 -->
      <div :class="{ 'container': !isAdminPage }">
        <router-view></router-view>
      </div>
    </main>

    <!-- 3. 只有非管理员页面才显示 Footer -->
    <app-footer v-if="!isAdminPage" />

    <!-- 4. 全局模态框 (由 store 状态控制) -->
    <login-modal
        v-if="state.showLoginModal"
        @close="closeModals"
        @switch-to-register="showRegister"
    />
    <register-modal
        v-if="state.showRegisterModal"
        @close="closeModals"
        @switch-to-login="showLogin"
    />
  </div>
</template>

<script>
import AppHeader from './components/AppHeader.vue';
import AppFooter from './components/AppFooter.vue';
import LoginModal from './components/LoginModal.vue';
import RegisterModal from './components/RegisterModal.vue';
import { store, mutations } from './store';

export default {
  name: 'App',
  components: { AppHeader, AppFooter, LoginModal, RegisterModal },
  computed: {
    state() {
      return store;
    },
    // 判断当前是否处于管理员相关页面
    isAdminPage() {
      // 包含 /admin 开头的路径以及管理员登录页
      return this.$route.path.startsWith('/admin') || this.$route.name === 'AdminLogin';
    }
  },
  created() {
    // 初始化时从本地存储恢复用户信息
    mutations.loadUserFromStorage();
  },
  methods: {
    showLogin() {
      mutations.setShowLoginModal(true);
    },
    showRegister() {
      mutations.setShowRegisterModal(true);
    },
    closeModals() {
      mutations.setShowLoginModal(false);
      mutations.setShowRegisterModal(false);
    },
    handleLogout() {
      mutations.clearUser();
      this.$message.success('您已安全退出');
      // 如果当前在受限页面，退出后跳转回首页
      if (this.$route.meta.requiresAuth) {
        this.$router.push('/');
      }
    }
  }
};
</script>

<style scoped>
/* 普通用户界面样式 */
.main-content {
  padding: 30px 0;
  min-height: calc(100vh - 160px); /* 减去 header 和 footer 大致高度 */
}

/* 管理员界面样式：全屏、浅灰色背景 */
.admin-main {
  min-height: 100vh;
  background-color: #f0f2f5;
  padding: 0; /* 后台界面通常自带 padding */
}

#app {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

main {
  flex: 1;
}
</style>