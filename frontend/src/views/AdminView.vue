<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar-container">
      <div class="admin-logo">
        <i class="el-icon-s-promotion"></i>
        <span v-show="!isCollapse" class="logo-text">清风航空后台</span>
      </div>

      <!-- 修改点：背景色设为 transparent，文字颜色调整为浅色以适应蓝色背景 -->
      <el-menu
          :default-active="$route.path"
          :collapse="isCollapse"
          background-color="transparent"
          text-color="#d1dbe5"
          active-text-color="#ffffff"
          class="el-menu-vertical"
          router
      >
        <el-menu-item index="/admin/dashboard">
          <i class="el-icon-odometer"></i>
          <span slot="title">数据概览</span>
        </el-menu-item>

        <el-submenu index="resources">
          <template slot="title">
            <i class="el-icon-office-building"></i>
            <span slot="title">资源管理</span>
          </template>
          <el-menu-item index="/admin/flights">航班调度</el-menu-item>
          <el-menu-item index="/admin/airlines" v-if="isPlatformAdmin">航司列表</el-menu-item>
        </el-submenu>

        <el-submenu index="ticketing">
          <template slot="title">
            <i class="el-icon-tickets"></i>
            <span slot="title">票务中心</span>
          </template>
          <el-menu-item index="/admin/orders">所有订单</el-menu-item>
        </el-submenu>

        <el-menu-item index="/admin/users" v-if="isPlatformAdmin">
          <i class="el-icon-user"></i>
          <span slot="title">用户管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/messages" v-if="isPlatformAdmin">
          <i class="el-icon-chat-dot-square"></i>
          <span slot="title">消息发布</span>
        </el-menu-item>

        <el-menu-item index="/admin/audit" v-if="isPlatformAdmin">
          <i class="el-icon-medal"></i>
          <span slot="title">入驻审核</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="main-container">
      <!-- 顶栏 -->
      <el-header class="admin-header">
        <div class="header-left">
          <div class="collapse-btn-wrapper" @click="isCollapse = !isCollapse">
            <i :class="isCollapse ? 'el-icon-s-unfold' : 'el-icon-s-fold'" class="collapse-btn"></i>
          </div>
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item>管理系统</el-breadcrumb-item>
            <el-breadcrumb-item>{{ $route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-dropdown trigger="click" placement="bottom-end">
            <div class="user-profile">
              <el-avatar size="small" icon="el-icon-user-solid" class="custom-avatar"></el-avatar>
              <div class="user-info">
                <span class="user-name">{{ user?.name || '管理员' }}</span>
                <span class="user-role-badge">{{ userRoleName }}</span>
              </div>
              <i class="el-icon-caret-bottom arrow-icon"></i>
            </div>
            <el-dropdown-menu slot="dropdown" class="custom-dropdown">
              <el-dropdown-item icon="el-icon-user">个人中心</el-dropdown-item>
              <el-dropdown-item divided @click.native="logout" icon="el-icon-switch-button" class="danger-item">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="admin-main">
        <transition name="fade-transform" mode="out-in">
          <router-view class="content-wrapper" />
        </transition>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { store, mutations } from '@/store';

export default {
  name: 'AdminView',
  data() {
    return {
      isCollapse: false
    };
  },
  computed: {
    user() { return store.user; },
    isPlatformAdmin() { return this.user?.role === 'ROLE_PLATFORM_ADMIN'; },
    userRoleName() {
      if (this.user?.role === 'ROLE_PLATFORM_ADMIN') return '平台总管';
      if (this.user?.role === 'ROLE_AIRLINE_ADMIN') return '航司管理员';
      return '未知角色';
    }
  },
  methods: {
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
/*
  配色参考:
  背景白: #feffff
  品牌蓝: #0050b2 (用于渐变起点)
  深蓝黑: #001529 (用于渐变终点)
  辅助灰: #777b7c
*/

.admin-layout { height: 100vh; overflow: hidden; background-color: #f5f7fa; }

/* ========== 侧边栏样式 (修改部分) ========== */
.sidebar-container {
  /* 核心修改：使用线性渐变，从品牌蓝到深蓝黑 */
  background: linear-gradient(180deg, #0050b2 0%, #001529 100%);
  box-shadow: 4px 0 10px rgba(0, 0, 0, 0.1);
  z-index: 1001;
  display: flex;
  flex-direction: column;
  transition: width 0.3s cubic-bezier(0.25, 0.8, 0.5, 1);
  border-right: none;
}

.admin-logo {
  height: 64px;
  line-height: 64px;
  display: flex;
  align-items: center;
  padding-left: 20px;
  color: #feffff;
  /* 修改：背景改为透明，让渐变透出来 */
  background: transparent;
  overflow: hidden;
  white-space: nowrap;
  /* 修改：分割线改为半透明白 */
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}
.admin-logo i {
  margin-right: 12px;
  font-size: 24px;
  color: #fff; /* Logo图标改为纯白 */
}
.logo-text {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 1px;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
  text-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

/* 菜单重写 */
.el-menu-vertical { border: none; flex: 1; }

/* 菜单项悬停和激活样式 */
::v-deep .el-menu-item, ::v-deep .el-submenu__title {
  transition: all 0.3s;
}

/* 悬停状态：微微发亮的半透明白 */
::v-deep .el-menu-item:hover, ::v-deep .el-submenu__title:hover {
  background-color: rgba(255, 255, 255, 0.1) !important;
  color: #feffff !important;
}
::v-deep .el-menu-item:hover i, ::v-deep .el-submenu__title:hover i {
  color: #feffff !important;
}

/* 激活状态：修改为磨砂玻璃效果（半透明白），不再使用深色块 */
::v-deep .el-menu-item.is-active {
  background-color: rgba(255, 255, 255, 0.2) !important;
  color: #feffff !important;
  font-weight: 600;
  border-right: 3px solid #ffffff; /* 右侧指示条改为白色 */
}
::v-deep .el-menu-item.is-active i {
  color: #feffff !important;
}

/* 图标颜色：默认浅灰蓝 */
::v-deep .el-menu-item i, ::v-deep .el-submenu__title i {
  color: #b0c4de;
  margin-right: 10px;
}

/* ========== 顶栏样式 ========== */
.admin-header {
  background: #feffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px !important;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  z-index: 1000;
}

.header-left { display: flex; align-items: center; gap: 20px; }

.collapse-btn-wrapper {
  cursor: pointer;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: background 0.3s;
}
.collapse-btn-wrapper:hover { background: #f0f2f5; }
.collapse-btn { font-size: 20px; color: #777b7c; transition: color 0.3s; }
.collapse-btn-wrapper:hover .collapse-btn { color: #0050b2; }

/* 面包屑 */
::v-deep .el-breadcrumb__inner {
  color: #777b7c !important;
  font-weight: 400;
}
::v-deep .el-breadcrumb__item:last-child .el-breadcrumb__inner {
  color: #24282a !important;
  font-weight: 600;
}

.header-right .user-profile {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.3s;
}
.header-right .user-profile:hover { background: #f5f7fa; }

.custom-avatar { background: #0050b2; color: #fff; margin-right: 10px; }
.user-info { display: flex; flex-direction: column; align-items: flex-start; line-height: 1.2; margin-right: 8px; }
.user-name { font-size: 14px; font-weight: 600; color: #24282a; }
.user-role-badge { font-size: 10px; color: #777b7c; background: #f0f2f5; padding: 1px 4px; border-radius: 2px; margin-top: 2px; }
.arrow-icon { color: #c0c4cc; font-size: 12px; }

/* ========== 主内容区 ========== */
.admin-main {
  background-color: #f5f7fa;
  padding: 24px;
  overflow-y: auto;
}
.content-wrapper {
  background: #feffff;
  padding: 30px;
  border-radius: 8px;
  min-height: calc(100vh - 112px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);
  border: 1px solid #ebeef5;
}

/* 动画 */
.fade-transform-enter-active, .fade-transform-leave-active { transition: all .4s cubic-bezier(0.25, 0.8, 0.5, 1); }
.fade-transform-enter { opacity: 0; transform: translateX(-20px); }
.fade-transform-leave-to { opacity: 0; transform: translateX(20px); }

/* 下拉菜单微调 */
.danger-item { color: #F56C6C !important; }
.danger-item:hover { background-color: #fef0f0 !important; }
</style>