const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
    transpileDependencies: true,
    // --- 核心修复开始 ---
    configureWebpack: {
        resolve: {
            alias: {
                // 将旧版的 babel-runtime 强行指向新版的 @babel/runtime
                'babel-runtime': '@babel/runtime'
            }
        }
    },


    devServer: {
        port: 8081,
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                pathRewrite: {

                }
            }
        }
    }
})