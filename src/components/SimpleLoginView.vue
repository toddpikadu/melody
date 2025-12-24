<template>
  <div>
    <h2>登录</h2>
    <form @submit.prevent="handleLogin">
      <div>
        <label>用户名:</label>
        <input type="text" v-model="username" required>
      </div>
      <div>
        <label>密码:</label>
        <input type="password" v-model="password" required>
      </div>
      <button type="submit">登录</button>
    </form>
    <p v-if="message">{{ message }}</p>
  </div>
</template>

<script>
export default {
  name: 'SimpleLoginView',
  data() {
    return {
      username: '',
      password: '',
      message: ''
    }
  },
  methods: {
    handleLogin() {
      this.message = '登录按钮被点击！用户名: ' + this.username
    }
  }
}
</script>