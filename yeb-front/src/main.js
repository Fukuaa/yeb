import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import '@/assets/theme.css';
import 'font-awesome/css/font-awesome.min.css'

import {postRequest} from "@/utils/api";
import {putRequest} from "@/utils/api";
import {getRequest} from "@/utils/api";
import {deleteRequest} from "@/utils/api";
import {initMenu} from "@/utils/menus";
import {downloadRequest} from "@/utils/download";

Vue.config.productionTip = false
Vue.use(ElementUI,{size:'small'});
// 插件形式使用请求
Vue.prototype.postRequest = postRequest
Vue.prototype.putRequest = putRequest
Vue.prototype.getRequest = getRequest
Vue.prototype.deleteRequest = deleteRequest
Vue.prototype.downloadRequest = downloadRequest // 以插件的形式使用下载相关请求

// 使用 router.beforeEach 注册一个全局前置守卫
router.beforeEach((to, from, next) => {
    // to 要去的路由; from 来自哪里的路由 ; next() 放行
    // 用户登录成功时，把 token 存入 sessionStorage，如果携带 token，初始化菜单，放行
    if (window.sessionStorage.getItem('tokenStr')) {
        const needsMenu = store.state.routes.length === 0
        const userRequest = window.sessionStorage.getItem('user')
            ? Promise.resolve(JSON.parse(window.sessionStorage.getItem('user')))
            : getRequest('/admin/info')
        Promise.all([initMenu(router, store), userRequest]).then(([menuReady, user]) => {
            if (!menuReady || !user) {
                window.sessionStorage.removeItem('tokenStr')
                window.sessionStorage.removeItem('user')
                next('/')
                return
            }
            window.sessionStorage.setItem('user', JSON.stringify(user))
            store.commit('INIT_ADMIN', user)
            if (needsMenu) store.dispatch('connect').catch(() => {})
            // 新增动态路由后重新匹配当前地址，保证刷新子页面也能打开。
            next(needsMenu ? {...to, replace: true} : undefined)
        }).catch(() => next('/'))
    } else {
        if (to.path === '/') {
            next()
        } else {
            next('/?redirect=' + to.path)
        }
    }
})

new Vue({
    router,
    store,
    render: h => h(App)
}).$mount('#app')
