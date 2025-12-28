import Vue from 'vue';
import VueRouter from 'vue-router';
import HomeView from '../views/HomeView.vue';

Vue.use(VueRouter);

const routes = [
  /* --- 用户前台页面 --- */
  { path: '/', name: 'Home', component: HomeView, meta: { title: '航班查询' } },
  { path: '/flights', name: 'FlightStatus', component: () => import('../views/FlightStatusView.vue'), meta: { title: '航班动态' } },
  { path: '/service', name: 'CustomerService', component: () => import('../views/CustomerServiceView.vue'), meta: { title: '客户服务' } },
  { path: '/about', name: 'About', component: () => import('../views/AboutView.vue'), meta: { title: '关于我们' } },

  /* --- 用户受限页面 (需登录) --- */
  { path: '/booking', name: 'Booking', component: () => import('../views/BookingView.vue'), meta: { title: '订单确认', requiresAuth: true } },
  { path: '/orders', name: 'MyOrders', component: () => import('../views/MyTicketsView.vue'), meta: { title: '我的行程', requiresAuth: true } },
  { path: '/profile', name: 'Profile', component: () => import('../views/ProfileView.vue'), meta: { title: '会员中心', requiresAuth: true } },

  /* --- 管理员后台页面 --- */
  {
    path: '/admin-login',
    name: 'AdminLogin',
    component: () => import('../views/AdminLoginView.vue'),
    meta: { title: '系统管理登录' }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/AdminView.vue'),
    meta: { title: '后台管理面板', requiresAuth: true, requiresAdmin: true }
  },

  /* --- 异常页面 --- */
  { path: '*', name: 'NotFound', component: () => import('../views/NotFoundView.vue'), meta: { title: '页面未找到' } }
];

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition;
    if (to.query.section) return {}; // 配合客户服务页面的锚点跳转
    return { x: 0, y: 0 };
  }
});

/**
 * 核心路由守卫
 */
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('authToken');
  let user = null;
  try {
    user = JSON.parse(localStorage.getItem('user'));
  } catch (e) { user = null; }

  const isAdmin = user && (user.role === 'ROLE_PLATFORM_ADMIN' || user.role === 'ROLE_AIRLINE_ADMIN');

  // 1. 处理后台管理路径 (/admin/**)
  if (to.path.startsWith('/admin')) {
    if (to.name === 'AdminLogin') return next(); // 已在登录页，放行

    if (!token || !user) {
      return next({ name: 'AdminLogin' }); // 未登录后台，跳转后台独立登录页
    }
    if (!isAdmin) {
      alert('权限不足：请使用管理员账号登录');
      return next({ name: 'Home' }); // 已登录但非管理员，踢回首页
    }
    return next();
  }

  // 2. 处理前台受限路径 (requiresAuth)
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!token) {
      // 未登录前台，跳转首页并由 App.vue 逻辑弹出登录框
      return next({ name: 'Home' });
    }
  }

  // 3. 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 清风航空`;
  }

  next();
});

export default router;