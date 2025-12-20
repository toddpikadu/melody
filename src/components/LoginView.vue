<template>
  <div class="login-container">
    <div class="login-form">
      <!-- 公司Logo -->
      <div class="logo-container">
        <img src="../assets/WL.png" alt="公司Logo" class="logo">
      </div>
      
      <!-- 登录表单 -->
      <form @submit.prevent="handleLogin">
        <h2 class="login-title">用户登录</h2>
        <div class="form-group">
          <label for="username">用户名</label>
          <input 
            type="text" 
            id="username" 
            v-model="loginForm.username" 
            placeholder="请输入用户名" 
            required
          >
        </div>
        
        <div class="form-group">
          <label for="password">密码</label>
          <input 
            type="password" 
            id="password" 
            v-model="loginForm.password" 
            placeholder="请输入密码" 
            required
          >
        </div>
        
        <!-- 错误信息显示 -->
        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>
        
        <div class="form-actions">
          <button type="submit" class="login-button">登录</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LoginView',
  data() {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      errorMessage: ''
    }
  },
  methods: {
    handleLogin() {
      // 这里可以添加登录逻辑
      console.log('登录信息:', this.loginForm)
      
      // 示例登录逻辑
      if (this.loginForm.username && this.loginForm.password) {
        // 发送登录请求
        fetch('/api/auth/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(this.loginForm)
        })
        .then(response => response.json())
        .then(data => {
          if (data.success) {
            // 登录成功，保存token等信息
            localStorage.setItem('token', data.token)
            localStorage.setItem('username', this.loginForm.username)
            // 跳转到主页面
            this.$router.push('/home')
          } else {
            this.errorMessage = data.message || '登录失败'
          }
        })
        .catch(error => {
          console.error('登录请求错误:', error)
          this.errorMessage = '网络错误，请稍后重试'
        })
      } else {
        this.errorMessage = '请填写完整的登录信息'
      }
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.login-form {
  width: 100%;
  max-width: 400px;
  padding: 30px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
}

.logo-container {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.logo {
  width: 120px;
  height: auto;
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
  color: #555;
}

.form-group input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
  transition: border-color 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #409eff;
}

.form-actions {
  margin-top: 30px;
}

.login-button {
  width: 100%;
  padding: 12px;
  background-color: #409eff;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.login-button:hover {
  background-color: #66b1ff;
}

.error-message {
  margin-top: 15px;
  padding: 10px;
  background-color: #fef0f0;
  color: #f56c6c;
  border-radius: 4px;
  text-align: center;
}
</style>
