<template>
  <div class="loginPage">
    <div class="loginStage">
      <section class="loginBrand">
        <div class="loginMark">云</div>
        <p class="loginKicker">人事工作台</p>
        <h1>云E办</h1>
        <p class="loginLead">员工档案、薪酬账套和组织权限，放在同一处处理。</p>
        <ul>
          <li>员工档案与调动</li>
          <li>工资账套与发放</li>
          <li>部门、职位与权限</li>
        </ul>
      </section>
      <el-form
          v-loading="loading"
          element-loading-text="正在登录......"
          element-loading-spinner="el-icon-loading"
          element-loading-background="rgba(20, 28, 26, 0.72)"
          ref="loginForm" :model="loginForm" :rules="rules" class="loginContainer">
        <p class="loginKicker">欢迎回来</p>
        <h3 class="loginTitle">登录系统</h3>
        <el-form-item prop="username">
          <el-input type="text" auto-complete="false" v-model="loginForm.username" prefix-icon="el-icon-user"
                    placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input type="password" auto-complete="false" v-model="loginForm.password" prefix-icon="el-icon-lock"
                    placeholder="请输入密码" show-password></el-input>
        </el-form-item>
        <el-form-item prop="code" class="captchaItem">
          <el-input type="text" auto-complete="false" v-model="loginForm.code" placeholder="点击图片更换验证码"
                    class="captchaInput"></el-input>
          <img :src="captchaUrl" alt="验证码" @click="updateCaptcha">
        </el-form-item>
        <el-checkbox v-model="checked" class="loginRemember">记住我</el-checkbox>
        <el-button type="primary" class="loginSubmit" @click="submitLogin">登录</el-button>
      </el-form>
    </div>
  </div>
</template>
<script>
export default {
  name: 'Login',
  components: {},
  props: [],
  data() {
    return {
      // 验证码
      captchaUrl: '/captcha?time=' + new Date(),
      loginForm: {
        username: 'admin',
        password: '123',
        code: '',
      },
      loading: false, // 加载中
      checked: true,
      rules: {
        username: [{required: true, message: '请输入用户名', trigger: 'blur'}],
        password: [{required: true, message: '请输入密码', trigger: 'blur'}],
        code: [{required: true, message: '请输入验证码', trigger: 'blur'}]
      }
    }
  },
  methods: {
    // 点击刷新验证码
    updateCaptcha() {
      this.captchaUrl = '/captcha?time=' + new Date()
    },
    submitLogin() {
      // 登录
      this.$refs.loginForm.validate((valid) => {
        if (valid) {
          this.loading = true
          // alert('submit!')
          this.postRequest('/login', {
            username: this.loginForm.username,
            password: this.loginForm.password,
            getCode: this.loginForm.code
          }).then(resp => {
            // alert(JSON.stringify(resp));
            this.loading = false
            if (resp) {
              // 存储用户 token 到 sessionStorage
              const tokenStr = resp.obj.tokenHead.trim() + ' ' + resp.obj.token
              window.sessionStorage.setItem('tokenStr', tokenStr)
              // 跳转到首页
              // this.$router.push('/home') // 路由跳转，可以回退到上一页
              // this.$router.replace('/home') // 路径替换，无法回退到上一页

              // 页面跳转
              // 拿到用户要跳转的路径
              let path = this.$route.query.redirect;
              // 用户可能输入首页地址或错误地址，让他跳到首页，否则跳转到他输入的地址
              this.$router.replace((path === '/' || path === undefined) ? '/home' : path)
            }

          })
        } else {
          this.$message.error('请输入所有字段！')
          return false;
        }
      })
    }
  }
}
</script>
<style>
.loginPage {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 16px;
  box-sizing: border-box;
  background:
      radial-gradient(circle at 12% 18%, rgba(243, 215, 176, 0.18), transparent 28%),
      radial-gradient(circle at 86% 80%, rgba(29, 107, 98, 0.28), transparent 32%),
      #1c2430;
}

.loginStage {
  width: min(980px, calc(100vw - 32px));
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  background: rgba(255, 253, 249, 0.96);
  border-radius: 28px;
  overflow: hidden;
  box-shadow: 0 30px 80px rgba(0, 0, 0, 0.28);
}

.loginBrand {
  padding: 56px 48px;
  color: #f7f3ea;
  background:
      linear-gradient(160deg, rgba(255, 255, 255, 0.04), transparent 42%),
      #24312a;
}

.loginMark {
  width: 54px;
  height: 54px;
  border-radius: 16px;
  display: grid;
  place-items: center;
  background: #f3d7b0;
  color: #1c2430;
  font-size: 26px;
  font-weight: 700;
}

.loginKicker {
  margin: 28px 0 8px;
  letter-spacing: 0.18em;
  font-size: 12px;
  color: #c9b89a;
}

.loginBrand h1 {
  margin: 0;
  font-size: 48px;
  font-weight: 680;
  letter-spacing: 0.04em;
}

.loginLead {
  margin: 16px 0 0;
  max-width: 280px;
  line-height: 1.7;
  color: #e4ddd0;
}

.loginBrand ul {
  margin: 36px 0 0;
  padding: 0;
  list-style: none;
}

.loginBrand li {
  margin-top: 12px;
  padding-left: 16px;
  position: relative;
  color: #f3d7b0;
}

.loginBrand li::before {
  content: "";
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #f3d7b0;
  position: absolute;
  left: 0;
  top: 8px;
}

.loginContainer {
  box-sizing: border-box;
  margin: 0;
  padding: 52px 42px 42px;
  background: #fffdf9;
}

.loginTitle {
  margin: 0 0 28px;
  text-align: left;
  color: #1c2430;
  font-size: 28px;
  font-weight: 680;
}

.loginContainer .loginKicker {
  margin-top: 0;
  color: #8a8174;
}

.loginRemember {
  display: block;
  text-align: left;
  margin: 0 0 18px;
}

.loginSubmit {
  width: 100%;
  height: 42px;
  letter-spacing: 0.24em;
  font-size: 15px;
}

.loginContainer .captchaItem .el-form-item__content {
  display: flex;
  align-items: center;
}

.loginContainer .captchaInput {
  flex: 1 1 auto;
  width: auto;
  min-width: 0;
  margin-right: 8px;
}

.loginContainer .el-form-item__content img {
  flex: none;
  width: 108px;
  height: 40px;
  cursor: pointer;
  border-radius: 8px;
  border: 1px solid #e7e1d6;
  background: #fff;
}

@media (max-width: 800px) {
  .loginPage {
    padding: 20px 12px;
  }

  .loginStage {
    width: min(440px, calc(100vw - 24px));
    grid-template-columns: 1fr;
    border-radius: 22px;
  }

  .loginBrand {
    display: none;
  }

  .loginContainer {
    padding: 32px 18px 24px;
  }

  .loginContainer .el-form-item__content img {
    width: 92px;
    height: 36px;
  }
}
</style>
