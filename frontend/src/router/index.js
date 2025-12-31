import Vue from 'vue';
import VueRouter from 'vue-router';
import HomeView from '../views/HomeView.vue';
// 1. 引入你的布局文件 AdminView
import AdminLayout from '../views/AdminView.vue';

Vue.use(VueRouter);

const routes = [
  /* --- 用户前台页面 --- */
  { path: '/', name: 'Home', component: HomeView, meta: { title: '航班查询' } },
  { path: '/flights', name: 'FlightStatus', component: () => import('../views/FlightStatusView.vue'), meta: { title: '航班动态' } },
  { path: '/service', name: 'CustomerService', component: () => import('../views/CustomerServiceView.vue'), meta: { title: '客户服务' } },
  { path: '/about', name: 'About', component: () => import('../views/AboutView.vue'), meta: { title: '关于我们' } },

  /* --- 用户受限页面 --- */
  { path: '/booking', name: 'Booking', component: () => import('../views/BookingView.vue'), meta: { title: '订单确认', requiresAuth: true } },
  { path: '/orders', name: 'MyOrders', component: () => import('../views/MyTicketsView.vue'), meta: { title: '我的行程', requiresAuth: true } },
  { path: '/profile', name: 'Profile', component: () => import('../views/ProfileView.vue'), meta: { title: '用户中心', requiresAuth: true } },

  /* --- 管理员登录/注册 --- */
  { path: '/admin-login', name: 'AdminLogin', component: () => import('../views/AdminLoginView.vue'), meta: { title: '系统管理登录' } },
  { path: '/admin-register', name: 'AdminRegister', component: () => import('../views/AdminRegisterView.vue'), meta: { title: '航司入驻申请' } },

  // === 核心后台路由配置 ===
  {
    path: '/admin',
    component: AdminLayout, // 使用 AdminView.vue 作为布局
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        redirect: 'dashboard'
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        // 注意路径：src/views/admin/AdminDashboard.vue
        component: () => import('../views/admin/AdminDashboard.vue'),
        meta: { title: '数据概览' }
      },
      {
        path: 'flights',
        name: 'FlightManager',
        component: () => import('../views/admin/FlightManager.vue'),
        meta: { title: '航班调度' }
      },
      {
        path: 'users',
        name: 'UserManager',
        component: () => import('../views/admin/UserManager.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'orders',
        name: 'OrderManager',
        component: () => import('../views/admin/OrderManager.vue'),
        meta: { title: '票务中心' }
      },
      {
        path: 'audit',
        name: 'AuditManager',
        component: () => import('../views/admin/AuditManager.vue'),
        meta: { title: '入驻审核' }
      },
      {
        path: 'airlines',
        name: 'AirlineManager',
        component: () => import('../views/admin/AirlineManager.vue'),
        meta: { title: '航司管理', role: 'ROLE_PLATFORM_ADMIN' }
      },
      {
        path: 'messages',
        name: 'MessageManager',
        component: () => import('../views/admin/MessagePublish.vue'),
        meta: { title: '消息发布', role: 'ROLE_PLATFORM_ADMIN' }
      }
    ]
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
    // 如果是去登录页或注册页，直接放行
    if (to.name === 'AdminLogin' || to.name === 'AdminRegister') {
      return next();
    }

    // 访问正式后台页面，校验身份
    if (!token || !user) {
      return next({ name: 'AdminLogin' });
    }
    if (!isAdmin) {
      alert('权限不足：请使用管理员账号登录');
      return next({ name: 'Home' });
    }
    return next();
  }

  // 2. 处理前台受限路径 (requiresAuth)
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!token) return next({ name: 'Home' });
  }

  // 3. 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 清风航空`;
  }

  next();
});

export default router;