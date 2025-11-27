import Vue from 'vue';
import VueRouter from 'vue-router';

// 导入核心页面组件
import HomeView from '../views/HomeView.vue';

Vue.use(VueRouter);

const routes = [
  // 1. 首页 (航班查询)
  {
    path: '/',
    name: 'Home', // 命名为 Home 更清晰
    component: HomeView,
    meta: { title: '航班查询' } // meta 信息，用于设置页面标题等
  },



  // 3. 订单确认/预订页面
  {
    path: '/booking',
    name: 'Booking',
    component: () => import(/* webpackChunkName: "booking" */ '../views/BookingView.vue'),
    meta: {
      title: '订单确认',
      requiresAuth: true // 添加一个标志，表示这个页面需要登录才能访问
    }
  },

  // 4. 我的订单列表页
  {
    path: '/orders',
    name: 'MyOrders',
    component: () => import(/* webpackChunkName: "my-orders" */ '../views/MyTicketsView.vue'),
    meta: {
      title: '我的订单',
      requiresAuth: true
    }
  },

  // 5. 订单详情页 (使用动态路由参数 :orderId)
  {
    path: '/orders/:orderId', // :orderId 是一个动态参数
    name: 'OrderDetail',
    component: () => import(/* webpackChunkName: "order-detail" */ '../views/OrderDetailView.vue'),
    props: true, // 将路由参数作为 props 传递给组件，方便组件获取 orderId
    meta: {
      title: '订单详情',
      requiresAuth: true
    }
  },

  // 6. 会员中心/个人资料页
  {
    path: '/profile',
    name: 'Profile',
    component: () => import(/* webpackChunkName: "profile" */ '../views/ProfileView.vue'),
    meta: {
      title: '会员中心',
      requiresAuth: true
    }
  },

  // 7. 客户服务/帮助中心
  {
    path: '/service',
    name: 'CustomerService',
    component: () => import(/* webpackChunkName: "service" */ '../views/CustomerServiceView.vue'),
    meta: { title: '客户服务' }
  },

  // 8. 关于我们页面 (保留)
  {
    path: '/about',
    name: 'About',
    component: () => import(/* webpackChunkName: "about" */ '../views/AboutView.vue'),
    meta: { title: '关于我们' }
  },
  {
    path: '/flights',
    name: 'FlightStatus', // 将原有的 FlightResults 改为 FlightStatus 更贴切
    component: () => import(/* webpackChunkName: "flight-status" */ '../views/FlightStatusView.vue'),
    meta: { title: '航班动态' }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import(/* webpackChunkName: "admin" */ '../views/AdminView.vue'),
    meta: {
      title: '后台管理',
      requiresAuth: true, // 需要登录
      requiresAdmin: true // 自定义元信息，用于路由守卫判断角色
    }
  },
  // 9. (可选) 404 页面
  {
    path: '*',
    name: 'NotFound',
    component: () => import(/* webpackChunkName: "not-found" */ '../views/NotFoundView.vue'),
    meta: { title: '页面未找到' }
  }
];

const router = new VueRouter({
  mode: 'history', // 使用 History 模式，URL 更美观 (需要后端支持)
  base: process.env.BASE_URL,
  routes,
  // 每次切换路由时，都滚动到页面顶部
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    } else {
      // 如果没有查询参数，则滚动到顶部
      if (to.query.section) {
        return {}; // 返回空对象，让我们的自定义逻辑处理滚动
      }
      return { x: 0, y: 0 };
    }
  }
});

// --- 全局路由守卫 (Navigation Guard) ---
// 这是实现“需要登录才能访问”功能的关键
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('authToken');
  const user = JSON.parse(localStorage.getItem('user')); // 假设用户信息（含角色）已存入storage

  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!token) {
      // 未登录，处理逻辑
    } else {
      // 已登录，检查是否需要管理员权限
      if (to.matched.some(record => record.meta.requiresAdmin)) {
        if (user && user.roles.includes('ADMIN')) { // 假设用户对象中有 roles 数组
          next();
        } else {
          // 无权限，可以重定向到无权限页面或首页
          next({ name: 'Home' });
        }
      } else {
        next();
      }
    }
  } else {
    next();
  }

  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 蓝天航空`;
  } else {
    document.title = '蓝天航空';
  }
});


export default router;
