<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar-container">
      <div class="admin-logo">
        <i class="el-icon-s-promotion"></i>
        <span v-show="!isCollapse">清风航空后台</span>
      </div>

      <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          class="el-menu-vertical"
          @select="handleMenuSelect"
      >
        <el-menu-item index="dashboard">
          <i class="el-icon-odometer"></i>
          <span slot="title">数据概览</span>
        </el-menu-item>

        <el-submenu index="resources">
          <template slot="title">
            <i class="el-icon-office-building"></i>
            <span slot="title">资源管理</span>
          </template>
          <el-menu-item index="airlines" v-if="isPlatformAdmin">航司列表</el-menu-item>
          <el-menu-item index="flights">航班调度</el-menu-item>
        </el-submenu>

        <el-submenu index="ticketing">
          <template slot="title">
            <i class="el-icon-tickets"></i>
            <span slot="title">票务中心</span>
          </template>
          <el-menu-item index="orders">所有订单</el-menu-item>
          <el-menu-item index="audit-logs">操作审计</el-menu-item>
        </el-submenu>

        <el-menu-item index="crm" v-if="isPlatformAdmin">
          <i class="el-icon-user"></i>
          <span slot="title">用户管理</span>
        </el-menu-item>

        <el-menu-item index="audit" v-if="isPlatformAdmin">
          <i class="el-icon-medal"></i>
          <span slot="title">入驻审核</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="main-container">
      <!-- 顶栏 -->
      <el-header class="admin-header">
        <div class="header-left">
          <i
              :class="isCollapse ? 'el-icon-s-unfold' : 'el-icon-s-fold'"
              class="collapse-btn"
              @click="isCollapse = !isCollapse"
          ></i>
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item>管理系统</el-breadcrumb-item>
            <el-breadcrumb-item>{{ menuTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-dropdown trigger="click">
            <div class="user-profile">
              <el-avatar size="small" icon="el-icon-user-solid"></el-avatar>
              <span class="user-name">{{ user?.name || '管理员' }}</span>
              <i class="el-icon-caret-bottom"></i>
            </div>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item icon="el-tag">{{ userRoleName }}</el-dropdown-item>
              <el-dropdown-item divided @click.native="logout" icon="el-icon-switch-button">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="admin-main">
        <transition name="fade-transform" mode="out-in">
          <div :key="activeMenu" class="content-wrapper">
            <component :is="currentComponent" :is-platform-admin="isPlatformAdmin" />
          </div>
        </transition>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { store, mutations } from '@/store';
import AdminDashboard from './admin/AdminDashboard.vue';
import AirlineManager from './admin/AirlineManager.vue';
import FlightManager from './admin/FlightManager.vue';
import OrderManager from './admin/OrderManager.vue';
import UserManager from './admin/UserManager.vue';
import AuditManager from './admin/AuditManager.vue';

export default {
  name: 'AdminView',
  components: { AdminDashboard, AirlineManager, FlightManager, OrderManager, UserManager, AuditManager },
  data() {
    return {
      activeMenu: 'dashboard',
      isCollapse: false,
      componentMap: {
        'dashboard': 'AdminDashboard',
        'airlines': 'AirlineManager',
        'flights': 'FlightManager',
        'orders': 'OrderManager',
        'crm': 'UserManager',
        'audit': 'AuditManager'
      },
      titleMap: {
        'dashboard': '数据概览',
        'airlines': '航司管理',
        'flights': '航班调度',
        'orders': '订单管理',
        'crm': '用户管理',
        'audit': '入驻审核'
      }
    };
  },
  computed: {
    user() { return store.user; },
    isPlatformAdmin() { return this.user?.role === 'ROLE_PLATFORM_ADMIN'; },
    userRoleName() {
      if (this.user?.role === 'ROLE_PLATFORM_ADMIN') return '平台总管';
      if (this.user?.role === 'ROLE_AIRLINE_ADMIN') return '航司管理员';
      return '未知角色';
    },
    currentComponent() { return this.componentMap[this.activeMenu] || 'AdminDashboard'; },
    menuTitle() { return this.titleMap[this.activeMenu] || '控制台'; }
  },
  methods: {
    handleMenuSelect(index) { this.activeMenu = index; },
    logout() {
      this.$confirm('确认退出管理系统吗？', '提示', { type: 'warning' }).then(() => {
        mutations.clearUser();
        this.$router.push('/admin-login');
      });
    }
  }
};
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  overflow: hidden;
}

/* 侧边栏 */
.sidebar-container {
  background-color: #304156;
  box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
  z-index: 1001;
  display: flex;
  flex-direction: column;
}

.admin-logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  background: #2b2f3a;
  overflow: hidden;
  white-space: nowrap;
}
.admin-logo i {
  margin-right: 8px;
  font-size: 20px;
  color: #409EFF;
}

.el-menu-vertical {
  border: none;
  flex: 1;
}

/* 顶栏 */
.admin-header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  z-index: 1000;
}

.header-left {
  display: flex;
  align-items: center;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  margin-right: 20px;
  transition: color 0.3s;
}
.collapse-btn:hover {
  color: #409EFF;
}

.header-right .user-profile {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 5px 8px;
  border-radius: 4px;
  transition: background 0.3s;
}
.header-right .user-profile:hover {
  background: #f6f6f6;
}
.user-name {
  margin: 0 8px;
  font-size: 14px;
  color: #606266;
}

/* 主内容区 */
.admin-main {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

.content-wrapper {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  min-height: 100%;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}

/* 动画效果 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all .3s;
}
.fade-transform-enter {
  opacity: 0;
  transform: translateX(-30px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>