<template>
  <header class="navbar">
    <div class="container nav-container">
      
      <router-link to="/" class="logo-link">
        <i class="fas fa-plane-departure logo-icon"></i>
        <span>清风航空</span>
      </router-link>

      
      <nav>
        <ul class="nav-links">
          <li><router-link to="/" exact-active-class="active">首页</router-link></li>
          <li><router-link to="/flights" active-class="active">航班动态</router-link></li>
          <li v-if="isLoggedIn"><router-link to="/orders" active-class="active">我的订单</router-link></li>
          <li v-if="isLoggedIn"><router-link to="/profile" active-class="active">用户中心</router-link></li>
          <li><router-link to="/service" active-class="active">客户服务</router-link></li>
        </ul>
      </nav>

      
      <div class="user-actions">
      
        <div v-if="isLoggedIn && user" class="user-info">
          <span>欢迎, {{ user.name }}</span>
          <button @click="$emit('logout')" class="btn-outline">退出</button>
        </div>
        
        
        <div v-else class="login-register-buttons">
          <button @click="$emit('show-register')" class="btn-styled btn-register">
            <i class="fas fa-user-plus"></i>
            <span>注册</span>
          </button>
          <button @click="$emit('show-login')" class="btn-styled btn-login">
            <i class="fas fa-sign-in-alt"></i>
            <span>登录</span>
          </button>
        </div>
      </div>
    </div>
  </header>
</template>

<script>
export default {
  name: 'AppHeader',
  props: {
    isLoggedIn: {
      type: Boolean,
      required: true,
    },
    user: {
      type: Object,
      default: null,
    }
  }
};
</script>

<style scoped>
.nav-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}
.logo-link {
  font-size: 24px;
  font-weight: bold;
  color: white;
  text-decoration: none;
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 6px;
  transition: all 0.3s ease;
  white-space: nowrap;
}
.logo-link:hover {
  background-color: rgba(255, 255, 255, 0.15);
  transform: scale(1.05);
}
.logo-icon {
  margin-right: 12px;
  font-size: 26px;
  transform: rotate(-15deg);
  transition: transform 0.3s ease;
}
.logo-link:hover .logo-icon {
  transform: rotate(0deg) scale(1.1);
}
.nav-links {
  display: flex;
  list-style: none;
  margin: 0;
  padding: 0;
}
.nav-links li {
  margin: 0 10px;
}
.nav-links a {
  color: white;
  text-decoration: none;
  font-weight: 500;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
  display: block;
  white-space: nowrap;
}
.nav-links a:hover,
.nav-links a.active {
  background-color: rgba(255, 255, 255, 0.2);
}
.user-actions {
  display: flex;
  align-items: center;
}
.user-info {
  display: flex;
  align-items: center;
  color: white;
  white-space: nowrap;
}
.user-info span {
  margin-right: 15px;
  font-weight: 500;
}
.btn-outline {
  background: transparent;
  border: 1px solid white;
  color: white;
  padding: 10px 22px;
  border-radius: 50px;
  cursor: pointer;
  font-weight: 600;
  font-size: 16px;
  transition: all 0.3s;
}
.btn-outline:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-2px);
}
.login-register-buttons {
  display: flex;
  gap: 12px;
}
.btn-styled {
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background-color: var(--secondary);
  color: white;
  border-radius: 50px;
  cursor: pointer;
  padding: 10px 20px;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.2s ease-in-out;
  white-space: nowrap;
}
.btn-styled:hover {
  background-color: #0056b3;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.2);
}
.btn-styled i {
  margin-right: 8px;
  font-size: 1.1em;
}
.btn-register {
  padding: 10px 22px;
}
</style>