import axios from 'axios';
import { Message } from 'element-ui';
import { mutations } from '@/store';

const apiClient = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 10000,
});

// 请求拦截：携带 Token
apiClient.interceptors.request.use(config => {
    const token = localStorage.getItem('authToken');
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
}, error => Promise.reject(error));

// 响应拦截：统一错误处理
apiClient.interceptors.response.use(response => {
    const res = response.data;
    if (res.code !== 200) {
        Message.error(res.message || '操作失败');
        if (res.code === 401) mutations.clearUser();
        return Promise.reject(new Error(res.message));
    }
    return res;
}, error => {
    Message.error(error.response?.data?.message || '服务器连接失败');
    return Promise.reject(error);
});

export default {
    // --- 认证与用户中心 (Auth Service) ---
    login: (credentials) => apiClient.post('/api/auth/login', credentials),
    register: (userData) => apiClient.post('/api/auth/register', userData),
    updateProfile: (data) => apiClient.put('/api/auth/user/profile', data),
    updatePassword: (data) => apiClient.put('/api/auth/user/password', data),
    getPendingAdmins: () => apiClient.get('/api/auth/admin/pending-list'),
    auditUser: (userId, status) => apiClient.put(`/api/auth/admin/audit?userId=${userId}&status=${status}`),

    // --- 航班与文件 (Flight Service) ---
    searchFlights: (params) => apiClient.get('/api/flights/search', { params }),
    getAllFlights: (date) => apiClient.get('/api/flights/all', { params: { flightDate: date } }),
    getFlightStatus: (num) => apiClient.get('/api/flights/status', { params: { flightNumber: num } }),
    getAdminFlights: () => apiClient.get('/api/flights/admin/list'),
    updateFlightPrice: (num, price) => apiClient.put(`/api/flights/${num}/price?newPrice=${price}`),
    getAllAirlines: () => apiClient.get('/api/airlines'),
    updateAirlineLogo: (code, url) => apiClient.put(`/api/airlines/${code}/logo?logoUrl=${url}`),
    // 添加发布航班接口
    createFlight: (data) => apiClient.post('/api/flights', data),
    uploadFile: (formData) => apiClient.post('/api/files/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    }),

    // --- 订单管理 (Order Service) ---
    createBooking: (data) => apiClient.post('/api/bookings', data),
    getMyOrders: () => apiClient.get('/api/bookings/my-tickets'),
    refundTicket: (id) => apiClient.post(`/api/bookings/tickets/${id}/refund`),
    // --- 统计 (Admin Service) ---
    getDashboardStats: () => apiClient.get('/api/admin/dashboard/stats'),

    // 在 export default 中增加
    getFamily: () => apiClient.get('/api/auth/family'),
    addFamily: (data) => apiClient.post('/api/auth/family', data),
    deleteFamily: (id) => apiClient.delete(`/api/auth/family/${id}`),


    // === 管理员接口 ===
    // 用户管理 (注意：这里全部改成了 apiClient)
    getAllUsers: (phone) => apiClient.get('/api/auth/admin/users', { params: { phone } }),
    updateUserMembership: (userId, level) => apiClient.put(`/api/auth/admin/users/${userId}/membership`, null, { params: { level } }),
    resetUserPassword: (userId) => apiClient.put(`/api/auth/admin/users/${userId}/reset-password`),

    // 订单管理 (注意：这里全部改成了 apiClient)
    getAllTickets: (params) => apiClient.get('/api/bookings/admin/tickets', { params }),
    checkInTicket: (ticketId) => apiClient.put(`/api/bookings/admin/tickets/${ticketId}/check-in`),
    getTicketLogs: (ticketId) => apiClient.get(`/api/bookings/admin/tickets/${ticketId}/logs`)
};