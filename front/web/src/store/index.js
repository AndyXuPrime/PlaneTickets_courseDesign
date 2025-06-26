import Vue from 'vue';

export const store = Vue.observable({
    isLoggedIn: false,
    user: null, // 用于存储 { customerId, username, name, membershipLevel 等 }
    showLoginModal: false,
    showRegisterModal: false,
});

export const mutations = {
    // 【核心修复】确保 setUser 接收的是完整的 user 对象
    setUser(user, token) {
        store.isLoggedIn = true;
        // user 对象现在应该直接包含 { token, username, customerId, ... }
        // 我们直接把它存起来
        store.user = user;

        // 分别存储 token 和 user 对象
        localStorage.setItem('authToken', token);
        localStorage.setItem('user', JSON.stringify(user));

        console.log('User set in store:', store.user); // 增加调试日志
    },
    clearUser() {
        store.isLoggedIn = false;
        store.user = null;
        localStorage.removeItem('authToken');
        localStorage.removeItem('user');
        console.log('User cleared from store.'); // 增加调试日志
    },
    setShowLoginModal(value) {
        if (value) store.showRegisterModal = false;
        store.showLoginModal = value;
    },
    setShowRegisterModal(value) {
        if (value) store.showLoginModal = false;
        store.showRegisterModal = value;
    },
    // 【核心修复】确保从 localStorage 加载时，user 是一个完整的对象
    loadUserFromStorage() {
        const token = localStorage.getItem('authToken');
        const userJson = localStorage.getItem('user');
        if (token && userJson) {
            try {
                const user = JSON.parse(userJson);
                store.isLoggedIn = true;
                store.user = user;
                console.log('User loaded from storage:', store.user); // 增加调试日志
            } catch (e) {
                console.error("Failed to parse user from localStorage", e);
                this.clearUser();
            }
        }
    }
};