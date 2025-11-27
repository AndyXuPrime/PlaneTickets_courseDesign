const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
    transpileDependencies: true,

    // 关闭生产环境的 source map，减小打包体积
    productionSourceMap: false,

    devServer: {
        // 前端项目启动端口，设置为 8081 以避免与 Spring Boot (8080) 冲突
        port: 8081,

        // 自动打开浏览器
        open: true,

        // 配置反向代理，解决开发环境跨域问题
        proxy: {
            '/api': {
                // 后端接口地址
                target: 'http://localhost:8080',
                // 允许跨域
                changeOrigin: true,
                // 由于你的后端 Controller 定义包含 /api (如 @RequestMapping("/api/auth"))
                // 这里不需要 pathRewrite 去掉 /api
            }
        }
    },

    // 页面标题配置
    chainWebpack: config => {
        config.plugin('html').tap(args => {
            args[0].title = '蓝天航空 - 机票预订系统';
            return args;
        });
    }
})