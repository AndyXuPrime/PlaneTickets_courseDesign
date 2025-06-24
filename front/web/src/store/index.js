// src/store/index.js
import Vue from 'vue';

export const store = Vue.observable({
    isLoggedIn: false,
    user: null, // 用于存储 { username, token, membershipLevel 等 }
    showLoginModal: false,
    showRegisterModal: false,
});

export const mutations = {
    setUser(user, token) {
        store.isLoggedIn = true;
        store.user = user;
        localStorage.setItem('authToken', token);
        localStorage.setItem('user', JSON.stringify(user));
    },
    clearUser() {
        store.isLoggedIn = false;
        store.user = null;
        localStorage.removeItem('authToken');
        localStorage.removeItem('user');
    },
    setShowLoginModal(value) {
        if (value) store.showRegisterModal = false;
        store.showLoginModal = value;
    },
    setShowRegisterModal(value) {
        if (value) store.showLoginModal = false;
        store.showRegisterModal = value;
    },
    loadUserFromStorage() {
        const token = localStorage.getItem('authToken');
        const user = localStorage.getItem('user');
        if (token && user) {
            store.isLoggedIn = true;
            store.user = JSON.parse(user);
        }
    }
};