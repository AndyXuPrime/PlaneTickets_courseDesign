<template>
  <div id="app">
    <app-header :is-logged-in="state.isLoggedIn" :user="state.user" @logout="handleLogout" @show-login="showLogin" @show-register="showRegister" />
    <main class="main-content">
      <div class="container">
        <router-view></router-view>
      </div>
    </main>
    <app-footer />

    <login-modal v-if="state.showLoginModal" @close="closeModals" @login-success="onLoginSuccess" @switch-to-register="showRegister(true)" />
    <register-modal v-if="state.showRegisterModal" @close="closeModals" @switch-to-login="showLogin(true)" />
  </div>
</template>

<script>
import AppHeader from './components/AppHeader.vue';
import AppFooter from './components/AppFooter.vue';
import LoginModal from './components/LoginModal.vue';
import RegisterModal from './components/RegisterModal.vue';
import { store, mutations } from './store';
import api from './api'; 

export default {
  name: 'App',
  components: { AppHeader, AppFooter, LoginModal, RegisterModal },
  computed: {
    state() {
      return store;
    }
  },
  created() {
    mutations.loadUserFromStorage();
  },
  methods: {
    showLogin(value = true) {
      mutations.setShowLoginModal(value);
    },
    showRegister(value = true) {
      mutations.setShowRegisterModal(value);
    },
    closeModals() {
      mutations.setShowLoginModal(false);
      mutations.setShowRegisterModal(false);
    },
     onLoginSuccess(loginData) {
      
      const userData = {
        username: loginData.username, // 手机号
        name: loginData.name,         
        level: loginData.membershipLevel,
      };
      
      mutations.setUser(userData, loginData.token);
      this.closeModals();
      this.$message.success('登录成功!');
      this.$router.go(0); 
    },
    handleLogout() {
      mutations.clearUser();
      this.$message.info('您已退出登录。');
      if (this.$route.meta.requiresAuth) {
        this.$router.push('/');
      }
    }
  }
};
</script>