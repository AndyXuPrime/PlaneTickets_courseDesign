import axios from 'axios';
import { Message } from 'element-ui';
import { store, mutations } from '@/store';


const apiClient = axios.create({
    baseURL: process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080',
    timeout: 10000,
});


apiClient.interceptors.request.use(
    config => {
        const token = localStorage.getItem('authToken');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    error => {
        console.error('Request Interceptor Error:', error);
        return Promise.reject(error);
    }
);


apiClient.interceptors.response.use(
    response => {
        const res = response.data;
        if (res.code !== 200) {
            Message({
                message: res.message || '操作失败',
                type: 'error',
                duration: 5 * 1000
            });
            return Promise.reject(new Error(res.message || 'Error'));
        } else {
            return res;
        }
    },
    error => {
        console.error('Response Interceptor Error:', error.response);
        if (error.response) {
            switch (error.response.status) {
                case 401:
                case 403:
                    if (store.showLoginModal === false) {
                        Message.error('登录已过期或无权限，请重新登录');
                        mutations.clearUser();
                        mutations.setShowLoginModal(true);
                    }
                    break;
                case 500: {
                    const errorMessage = error.response.data?.message || '服务器内部错误，请联系管理员';
                    Message.error(errorMessage);
                    break;
                }

                default:
                    Message.error(`请求错误，状态码: ${error.response.status}`);
            }
        } else if (error.request) {
            Message.error('网络连接失败，请检查您的网络设置');
        } else {
            Message.error('发生未知错误，请稍后重试');
        }
        return Promise.reject(error);
    }
);



export default {

    login(credentials) {
        return apiClient.post('/api/auth/login', credentials, {
            headers: {
                'Content-Type': 'application/json'
            }
        });
    },


    register(userData) {
        return apiClient.post('/api/auth/register', userData, {
            headers: {
                'Content-Type': 'application/json'
            }
        });
    },


    createBooking(bookingData) {
        return apiClient.post('/api/bookings', bookingData, {
            headers: {
                'Content-Type': 'application/json'
            }
        });
    },


    refundTicket(ticketId) {
        return apiClient.post(`/api/bookings/tickets/${ticketId}/refund`, null, {
            headers: {
                'Content-Type': 'application/json'
            }
        });
    },


    searchFlights(params) {
        return apiClient.get('/api/flights/search', { params });
    },

    getAllFlights(date) {
        return apiClient.get('/api/flights/all', { params: { flightDate: date } });
    },

    getFlightStatus(flightNumber) {
        return apiClient.get('/api/flights/status', { params: { flightNumber } });
    },

    getMyOrders() {
        return apiClient.get('/api/bookings/my-tickets');
    },
};