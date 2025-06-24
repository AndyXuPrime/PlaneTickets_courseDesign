import Vue from 'vue';
import App from './App.vue';
import router from './router';
import 'element-ui/lib/theme-chalk/index.css';
import './assets/main.css';
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import './assets/main.css';
// 如果安装了 FontAwesome
import { library } from '@fortawesome/fontawesome-svg-core';
import { faPlane, faSearch, faUser, faSignOutAlt, faCrown, faStar } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
export const EventBus = new Vue();
library.add(faPlane, faSearch, faUser, faSignOutAlt, faCrown, faStar);
Vue.component('font-awesome-icon', FontAwesomeIcon);
Vue.use(ElementUI);
Vue.config.productionTip = false;

new Vue({
  router,
  render: h => h(App),
}).$mount('#app');