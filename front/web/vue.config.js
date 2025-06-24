module.exports = {
  devServer: {
    host: '127.0.0.1', // 使用 IP 地址而不是 'localhost'
    port: 8888,        // 使用一个不常用的端口
    open: true,        // 自动打开浏览器
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 代理到你的后端
        changeOrigin: true,
      }
    }
  }
};