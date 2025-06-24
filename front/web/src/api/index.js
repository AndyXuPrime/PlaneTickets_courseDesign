import axios from 'axios';

// 1. 只创建一个 Axios 实例，并从环境变量读取 baseURL
const apiClient = axios.create({

    timeout: 10000, // 设置一个合理的超时时间，例如 10 秒
    headers: {
        'Content-Type': 'application/json'
    },
    withCredentials: false
});

// 2. 为这个统一的实例添加请求拦截器
apiClient.interceptors.request.use(
    config => {
        // 在发送请求之前，从 localStorage 获取 token
        const token = localStorage.getItem('authToken');

        // 如果 token 存在，则将其添加到请求的 Authorization 头中
        if (token) {
            // 'Bearer ' 是 JWT 的标准前缀
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    error => {
        // 对请求错误做些什么
        console.error('Request Interceptor Error:', error); // 添加日志
        return Promise.reject(error);
    }
);

// 3. (可选但推荐) 添加响应拦截器，用于统一处理错误
apiClient.interceptors.response.use(
    response => {
        const res = response.data;
        if (res.code !== 200) {
            Message({
                message: res.message || 'Error',
                type: 'error',
                duration: 5 * 1000
            });
            return Promise.reject(new Error(res.message || 'Error'));
        } else {
            // FIX: 返回整个 res 对象 { code, message, data }
            return res;
        }
    },
    error => {
        // 超出 2xx 范围的状态码都会触发该函数。
        console.error('Response Interceptor Error:', error.response);
        // 在这里可以进行全局的错误处理，例如：
        // if (error.response.status === 401) {
        //   // 处理 token 过期或无效，例如跳转到登录页
        // }
        return Promise.reject(error);
    }
);


// 4. 导出一个包含了所有 API 函数的对象
//    所有函数都使用我们配置好的 apiClient 实例
export default {
    /**
     * 用户登录
     * @param {object} credentials - 包含 username 和 password 的对象
     * @returns {Promise}
     */
    login(credentials) {
        // 请求的最终 URL 将是: baseURL + '/api/auth/login'
        return apiClient.post('/api/auth/login', credentials);
    },

    /**
     * 用户注册
     * @param {object} userData - 用户注册信息
     * @returns {Promise}
     */
    register(userData) {
        return apiClient.post('/api/auth/register', userData);
    },

    /**
     * 根据条件搜索航班
     * @param {object} params - 包含 searchType, value, flightDate 的对象
     * @returns {Promise}
     */
    searchFlights(params) {
        // GET请求的参数应该放在 config.params 中
        return apiClient.get('/api/flights/search', { params });
    },
    getAllFlights(params) {
        return apiClient.get('/api/flights/all', { params });
    },
    getFlightStatus(params) {
        return apiClient.get('/api/flights/status', { params });
    },
    getMyOrders() { // 获取我的订单列表
        return apiClient.get('/api/orders/my-orders');
    },
    cancelOrder(orderId) {
        return apiClient.post(`/api/orders/${orderId}/cancel`);
    },
    getFlightByNumber(flightNumber) {
        // 直接将航班号拼接到URL中
        return apiClient.get(`/api/flights/${flightNumber}`);
    },
    /**
     * 获取指定日期的所有航班
     * @param {string} date - 日期字符串 'YYYY-MM-DD'
     * @returns {Promise}
     */
    getAllFlights(date) {
        return apiClient.get('/api/flights/all', { params: { flightDate: date } });
    },

    /**
    * 【核心修改】根据航班号获取航班状态
     * @param { string } flightNumber - 航班号字符串
    * @returns { Promise }
     */
    getFlightStatus(flightNumber) {
        // 直接将 flightNumber 字符串作为参数对象的值
        return apiClient.get('/api/flights/status', { params: { flightNumber } });
    },

    /**
     * 【新增】获取当前登录用户的所有订单
     * @returns {Promise}
     */
    getMyOrders() {
        return apiClient.get('/api/orders/my-orders');
    },

    /**
     * 【新增】取消一个指定的订单
     * @param {number | string} orderId - 要取消的订单ID
     * @returns {Promise}
     */
    cancelOrder(orderId) {
        // 取消订单通常使用 PUT 或 POST/DELETE 方法，根据后端 TicketOrderController 定义是 @PostMapping
        // 但为了符合RESTful风格，这里也可以用DELETE，如果后端支持的话。我们遵循后端的 @PostMapping。
        // 注意：后端 cancelOrder 需要一个 body，但我们的实现不需要，所以传一个空对象。
        return apiClient.post(`/api/orders/${orderId}/cancel`, {});
    },
    /**
     * 创建订单
     * @param {object} orderData - 订单信息
     * @returns {Promise}
     */
    createOrder(orderData) {
        return apiClient.post('/api/orders', orderData);
    }

};