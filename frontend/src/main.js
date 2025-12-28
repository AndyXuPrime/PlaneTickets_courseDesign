/**
 * 1. 全局性能补丁：修复现代浏览器滚动性能警告 [Violation]
 * 必须放在所有 import 之前，以确保捕获所有组件的事件绑定
 */
(function() {
  const originalAddEventListener = EventTarget.prototype.addEventListener;
  EventTarget.prototype.addEventListener = function(type, listener, options) {
    let wrappedOptions = options;
    if (['mousewheel', 'wheel', 'touchmove', 'touchstart'].includes(type)) {
      if (typeof options === 'object') {
        wrappedOptions = { ...options, passive: true };
      } else {
        wrappedOptions = { passive: true };
      }
    }
    originalAddEventListener.call(this, type, listener, wrappedOptions);
  };
})();

import Vue from 'vue';
import App from './App.vue';
import router from './router';

// --- UI 框架与样式 ---
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import './assets/main.css';

// --- 图标库 (FontAwesome) ---
import { library } from '@fortawesome/fontawesome-svg-core';
import {
  faPlane,
  faSearch,
  faUser,
  faSignOutAlt,
  faCrown,
  faStar
} from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';

/**
 * 2. 全局事件总线 (EventBus)
 * 用于非父子组件间的通信
 */
export const EventBus = new Vue();

// --- 插件配置 ---
library.add(faPlane, faSearch, faUser, faSignOutAlt, faCrown, faStar);
Vue.component('font-awesome-icon', FontAwesomeIcon);

Vue.use(ElementUI);

Vue.config.productionTip = false;

/**
 * 3. 初始化 Vue 实例
 */
new Vue({
  router,
  render: h => h(App),
}).$mount('#app');