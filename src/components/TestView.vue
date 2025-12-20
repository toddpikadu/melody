<template>
  <div class="test-container">
    <h1>测试组件</h1>
    <p>这是一个简单的测试组件，用于验证Vue应用是否正常工作。</p>
  </div>
</template>

<script>
export default {
  name: 'TestView'
}
</script>

<style scoped>
.test-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background-color: #f5f5f5;
}

h1 {
  color: #409eff;
  margin-bottom: 20px;
}
</style>