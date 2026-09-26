<template>
  <div>
    <el-container class="homeShell">
      <el-aside class="homeAside" :width="collapsed ? '72px' : '232px'">
        <div class="brand">
          <span class="brandMark">云</span>
          <div class="brandText" v-show="!collapsed">
            <strong>云E办</strong>
            <span>人事与薪酬</span>
          </div>
        </div>
        <el-menu
            class="homeMenu"
            router
            unique-opened
            :collapse="collapsed"
            :collapse-transition="false"
            background-color="#1c2430"
            text-color="#d5ddd8"
            active-text-color="#f3d7b0"
            popper-class="homeMenuPopup">
          <el-submenu :index="index +''" v-for="(item,index) in routes"
                      :key="index" v-if="!item.hidden">
            <template slot="title">
              <i :class="item.iconCls" class="menuIcon"></i>
              <span>{{ item.name }}</span>
            </template>
            <el-menu-item :index="children.path"
                          v-for="(children,index) in item.children" :key="index">{{ children.name }}
            </el-menu-item>
          </el-submenu>
        </el-menu>
      </el-aside>
      <el-container class="homeBody">
        <el-header class="homeHeader" height="64px">
          <div class="headerLeft">
            <button class="collapseBtn" type="button" @click="collapsed = !collapsed">
              <i :class="collapsed ? 'el-icon-s-unfold' : 'el-icon-s-fold'"></i>
            </button>
            <div class="headerTitle">{{ pageTitle }}</div>
          </div>
          <div class="headerRight">
            <el-button class="bellBtn" icon="el-icon-bell" circle @click="goChar"></el-button>
            <el-dropdown class="userInfo" @command="commandHandler">
              <span class="el-dropdown-link">
                <img :src="user.userFace" alt="">
                <span class="userName">{{ user.name }}</span>
                <i class="el-icon-arrow-down"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="userinfo">个人中心</el-dropdown-item>
                <el-dropdown-item command="setting">设置</el-dropdown-item>
                <el-dropdown-item command="logout">注销登录</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </el-header>
        <el-main class="homeMain">
          <el-breadcrumb separator-class="el-icon-arrow-right"
                         v-if="this.$router.currentRoute.path!=='/home'">
            <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ this.$router.currentRoute.name }}</el-breadcrumb-item>
          </el-breadcrumb>
          <div class="homeWelcome" v-if="this.$router.currentRoute.path==='/home'">
            <div class="welcomeHero">
              <p>工作台</p>
              <h1>{{ greeting }}，{{ user.name || '同事' }}</h1>
              <span>从下面的模块进入员工、薪酬和系统管理。</span>
            </div>
            <div class="welcomeGrid">
              <button class="welcomeCard" type="button" v-for="item in menuCards" :key="item.path || item.name"
                      @click="openMenu(item)">
                <i :class="item.iconCls"></i>
                <strong>{{ item.name }}</strong>
                <span>{{ (item.children || []).length }} 个功能</span>
              </button>
            </div>
          </div>
          <router-view class="homeRouterView"/>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>
<script>
export default {
  name: 'Home',
  data() {
    return {
      collapsed: false
    }
  },
  computed: {
    routes() {
      return this.$store.state.routes
    },
    user() {
      return this.$store.state.currentAdmin || {}
    },
    pageTitle() {
      if (this.$route.path === '/home') return '工作台'
      return this.$route.name || '云E办'
    },
    greeting() {
      const hour = new Date().getHours()
      if (hour < 11) return '早上好'
      if (hour < 14) return '中午好'
      if (hour < 18) return '下午好'
      return '晚上好'
    },
    menuCards() {
      return (this.routes || []).filter(item => !item.hidden && item.children && item.children.length)
    }
  },
  mounted() {
    if (window.innerWidth < 900) this.collapsed = true
    window.addEventListener('resize', this.onResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.onResize)
  },
  methods: {
    onResize() {
      if (window.innerWidth < 900) this.collapsed = true
    },
    openMenu(item) {
      const child = (item.children || []).find(entry => entry.path)
      if (child) this.$router.push(child.path)
    },
    goChar() {
      this.$router.push('/chat')
    },
    commandHandler(command) {
      if (command === 'logout') {
        this.$confirm('此操作将注销登录, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.postRequest('/logout')
          window.sessionStorage.removeItem('tokenStr')
          window.sessionStorage.removeItem('user')
          this.$store.commit('initRoutes', [])
          this.$router.replace('/')
        }).catch(() => {
          this.$message({
            type: 'info',
            message: '已取消注销登录'
          });
        });
      }
      if (command === 'userinfo') {
        this.$router.push('/userinfo')
      }
    }
  }
}
</script>
<style scoped>
.homeShell {
  height: 100vh;
  background: #f3efe7;
}

.homeAside {
  background: #1c2430;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: width 0.2s ease;
}

.brand {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 16px;
  box-sizing: border-box;
  color: #f7f3ea;
}

.brandMark {
  width: 36px;
  height: 36px;
  flex: none;
  border-radius: 12px;
  display: grid;
  place-items: center;
  background: #f3d7b0;
  color: #1c2430;
  font-weight: 700;
}

.brandText {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
  min-width: 0;
}

.brandText strong {
  font-size: 16px;
  letter-spacing: 0.08em;
}

.brandText span {
  margin-top: 3px;
  color: #b7c0ba;
  font-size: 12px;
}

.homeMenu {
  border-right: none;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

.menuIcon {
  color: #f3d7b0;
  width: 18px;
  text-align: center;
}

.homeMenu:not(.el-menu--collapse) .menuIcon {
  margin-right: 8px;
}

.homeBody {
  min-width: 0;
  min-height: 0;
}

.homeHeader {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 18px;
  box-sizing: border-box;
  background: rgba(255, 253, 249, 0.92);
  border-bottom: 1px solid #e7e1d6;
}

.headerLeft,
.headerRight,
.el-dropdown-link {
  display: flex;
  align-items: center;
}

.headerLeft {
  gap: 12px;
  min-width: 0;
}

.collapseBtn {
  width: 36px;
  height: 36px;
  border: 1px solid #e7e1d6;
  border-radius: 10px;
  background: #fff;
  color: #1c2430;
  cursor: pointer;
}

.headerTitle {
  font-size: 16px;
  font-weight: 650;
  color: #1c2430;
}

.bellBtn {
  margin-right: 14px;
  color: #1c2430;
  border-color: #e7e1d6;
}

.userInfo {
  cursor: pointer;
}

.el-dropdown-link img {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 8px;
  background: #efe8dc;
}

.userName {
  color: #1c2430;
  font-weight: 600;
}

.el-dropdown-link .el-icon-arrow-down {
  margin-left: 6px;
  color: #8a8174;
  font-size: 12px;
}

.homeMain {
  background: #f3efe7;
}

.homeWelcome {
  margin-bottom: 16px;
}

.welcomeHero {
  padding: 8px 4px 18px;
}

.welcomeHero p {
  margin: 0 0 8px;
  color: #8a8174;
  letter-spacing: 0.16em;
  font-size: 12px;
}

.welcomeHero h1 {
  margin: 0;
  font-size: 32px;
  color: #1c2430;
  font-weight: 680;
}

.welcomeHero span {
  display: block;
  margin-top: 8px;
  color: #6d655b;
}

.welcomeGrid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
}

.welcomeCard {
  text-align: left;
  border: 1px solid #e7e1d6;
  background: #fffdf9;
  border-radius: 16px;
  padding: 16px;
  cursor: pointer;
  color: #1c2430;
}

.welcomeCard i {
  color: #1d6b62;
  font-size: 18px;
}

.welcomeCard strong,
.welcomeCard span {
  display: block;
}

.welcomeCard strong {
  margin-top: 14px;
  font-size: 16px;
}

.welcomeCard span {
  margin-top: 6px;
  color: #8a8174;
  font-size: 12px;
}

.welcomeCard:hover {
  border-color: #d9c7ae;
  box-shadow: 0 10px 24px rgba(28, 36, 48, 0.06);
}

.homeRouterView {
  display: block;
  margin-top: 4px;
  background: #fffdf9;
  border: 1px solid #e7e1d6;
  border-radius: 18px;
  padding: 16px;
  box-sizing: border-box;
  min-height: 180px;
}

::v-deep .homeMenu .el-submenu__title:hover,
::v-deep .homeMenu .el-menu-item:hover {
  background: rgba(255, 255, 255, 0.05) !important;
}

::v-deep .homeMenu .el-menu-item.is-active {
  background: rgba(243, 215, 176, 0.14) !important;
}

@media (max-width: 720px) {
  .userName,
  .el-dropdown-link .el-icon-arrow-down {
    display: none;
  }

  .welcomeHero h1 {
    font-size: 26px;
  }

  .homeRouterView {
    padding: 12px;
  }
}
</style>
