import axios from 'axios';
import { Message } from 'element-ui';
import { mutations } from '@/store';

// 1. 创建 Axios 实例 (指向 Gateway 端口 8080)
const apiClient = axios.create({
    baseURL: 'http://localhost:8080', // 网关地址
    timeout: 10000,
});

// 2. 请求拦截器 (自动携带 Token)
apiClient.interceptors.request.use(
    config => {
        const token = localStorage.getItem('authToken');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    error => Promise.reject(error)
);

// 3. 响应拦截器 (统一处理错误)
apiClient.interceptors.response.use(
    response => {
        const res = response.data;
        // 后端统一返回 ApiResponse: { code: 200, message: "...", data: ... }
        if (res.code !== 200) {
            Message({
                message: res.message || '操作失败',
                type: 'error',
                duration: 3000
            });
            // 如果 Token 失效 (后端通常返回 401)
            if (res.code === 401) {
                mutations.clearUser();
            }
            return Promise.reject(new Error(res.message || 'Error'));
        } else {
            return res;
        }
    },
    error => {
        console.error('API Error:', error);
        let msg = '服务器连接失败';
        if (error.response) {
            const status = error.response.status;
            if (status === 401) {
                msg = '登录已过期，请重新登录';
                mutations.clearUser();
            } else if (status === 403) {
                msg = '您没有权限执行此操作';
            } else if (error.response.data && error.response.data.message) {
                msg = error.response.data.message;
            } else {
                msg = `请求错误 ${status}`;
            }
        }
        Message.error(msg);
        return Promise.reject(error);
    }
);

export default {
    // --- Auth Service ---
    login(credentials) {
        return apiClient.post('/api/auth/login', credentials);
    },
    register(userData) {
        return apiClient.post('/api/auth/register', userData);
    },

    // --- Flight Service ---
    // 搜索航班
    searchFlights(params) {
        return apiClient.get('/api/flights/search', { params });
    },
    // 获取所有航班 (带日期)
    getAllFlights(date) {
        return apiClient.get('/api/flights/all', { params: { flightDate: date } });
    },
    // 获取航班动态
    getFlightStatus(flightNumber) {
        return apiClient.get('/api/flights/status', { params: { flightNumber } });
    },
    // 管理员获取航班列表
    getAdminFlights() {
        return apiClient.get('/api/flights/admin/list');
    },
    // 获取所有航空公司列表
    getAllAirlines() {
        return apiClient.get('/api/airlines');
    },
    // 更新航司 Logo
    updateAirlineLogo(code, logoUrl) {
        return apiClient.put(`/api/airlines/${code}/logo?logoUrl=${logoUrl}`);
    },

    // --- Order Service ---
    createBooking(bookingData) {
        return apiClient.post('/api/bookings', bookingData);
    },
    getMyOrders() {
        return apiClient.get('/api/bookings/my-tickets');
    },
    refundTicket(ticketId) {
        return apiClient.post(`/api/bookings/tickets/${ticketId}/refund`);
    },

    // --- Admin Service & Auth Admin ---
    // 获取仪表盘统计数据
    getDashboardStats() {
        return apiClient.get('/api/admin/dashboard/stats');
    },
    // 获取待审核管理员列表 (Auth服务)
    getPendingAdmins() {
        return apiClient.get('/api/auth/admin/pending-list');
    },
    // 审核用户 (Auth服务)
    auditUser(userId, status) {
        return apiClient.put(`/api/auth/admin/audit?userId=${userId}&status=${status}`);
    }
};