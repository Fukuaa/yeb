// webpack-dev-server 3 在新版 Node 上遇到浏览器断开时会抛出未处理的 ECONNRESET，直接退出。
process.on('uncaughtException', (err) => {
    if (err && (err.code === 'ECONNRESET' || err.code === 'EPIPE')) {
        return
    }
    throw err
})

let proxyObj = {} // 代理对象

proxyObj['/'] = {
    // websocket
    ws: false,
    // 代理目标地址
    target: 'http://localhost:8080',
    // 发送请求头 host 会被设置 target
    changeOrigin: true,
    // 不重写请求地址
    pathRewrite: {
        '^/': '/'
    }
}

// 在线聊天 代理
proxyObj['/ws'] = {
    ws: true,
    target: 'ws://localhost:8080'
}


// 访问的默认的路径和端口
module.exports = {
    devServer: {
        host: 'localhost',
        port: 8080,
        proxy: proxyObj // 代理
    },
    chainWebpack: config => {
        config.plugin('html').tap(args => {
            args[0].title = '云E办'
            return args
        })
    }
}
