const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8080, // 设置前端服务端口为8080
    host: '0.0.0.0', // 允许所有IP访问
    allowedHosts: 'all', // 允许所有域名访问
    proxy: {
      '/api': {
        target: 'http://localhost:9090', // 后端API地址
        changeOrigin: true, // 允许跨域
        pathRewrite: {
          '^/api': '/api' // 路径重写，保持API路径不变
        }
      }
    },
    client: {
      webSocketURL: {
        hostname: 'localhost', // 使用localhost代替实际IP
        pathname: '/ws',
        port: 8080
      }
    }
  }
})
