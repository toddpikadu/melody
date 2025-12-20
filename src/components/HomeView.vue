<template>
  <el-container class="home-container">
    <!-- 顶部导航栏 -->
    <el-header class="home-header">
      <div class="header-left">
        <img src="../assets/WL.png" alt="公司Logo" class="header-logo">
        <h1 class="header-title">生产管理系统</h1>
      </div>
      <div class="header-right">
        <el-dropdown trigger="hover">
          <span class="el-dropdown-link username">
            <el-avatar :size="32" icon="User" class="header-avatar" />
            欢迎，{{ username }}<el-icon class="el-icon--right"><arrow-down /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item divided @click="handleLogout" class="logout-item">
                <el-icon><SwitchButton /></el-icon>
                <span>退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container>
      <!-- 左侧导航菜单 -->
      <el-aside class="home-aside" width="200px">
        <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical-demo"
          router
          @open="handleOpen"
          @close="handleClose"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          :collapse-transition="true"
        >
          <el-menu-item index="/home/workpoint-approval" class="menu-item">
            <el-icon><document /></el-icon>
            <span>工序工分核准</span>
          </el-menu-item>
          <el-menu-item index="/home/process-management" class="menu-item">
            <el-icon><collection /></el-icon>
            <span>产品管理</span>
          </el-menu-item>
          <el-menu-item index="/home/production-plan" class="menu-item">
            <el-icon><calendar /></el-icon>
            <span>生产计划</span>
          </el-menu-item>
          <el-menu-item index="/home/report-statistics" class="menu-item">                                                                            
            <el-icon><data-analysis /></el-icon>
            <span>报表统计</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 右侧内容区域 -->
      <el-main class="home-main">
        <transition name="fade" mode="out-in">
          <router-view />
        </transition>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { ArrowDown, Document, Collection, Calendar, DataAnalysis, SwitchButton } from '@element-plus/icons-vue'

export default {
  name: 'HomeView',
  components: {
    ArrowDown,
    Document,
    Collection,
    Calendar,
    DataAnalysis,
    SwitchButton
  },
  data() {
    return {
      username: localStorage.getItem('username') || '',
      activeMenu: '/home/workpoint-approval' // 默认选中工分核准菜单
    }
  },
  mounted() {
    // 设置当前路由对应的菜单项为激活状态
    this.activeMenu = this.$route.path
  },
  watch: {
    // 监听路由变化，更新激活的菜单项
    '$route.path'(newPath) {
      this.activeMenu = newPath
    }
  },
  methods: {
    handleMenuSelect() {
      // 路由变化会自动更新菜单激活状态
    },
    handleCommand(key) {
      if (key === 'logout') {
        this.$router.push('/')
      }
    },
    handleLogout() {
      // 清除本地存储的登录信息
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      
      // 跳转到登录页面
      this.$router.push('/login')
    },
    handleOpen() {
      // 可以在这里添加菜单展开时的动画或日志
    },
    handleClose() {
      // 可以在这里添加菜单关闭时的动画或日志
    }
  }
}
</script>

<style scoped>
.home-container {
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

.home-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  background-color: #409eff;
  color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 100;
  transition: all 0.3s ease;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-logo {
  width: 40px;
  height: 40px;
  margin-right: 10px;
  border-radius: 50%;
  transition: transform 0.3s ease;
}

.header-logo:hover {
  transform: scale(1.1);
}

.header-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  letter-spacing: 1px;
}

.header-right {
  display: flex;
  align-items: center;
}

.username {
  color: #fff;
  cursor: pointer;
  font-weight: 500;
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.username:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.header-avatar {
  margin-right: 8px;
  background-color: rgba(255, 255, 255, 0.2);
}

.logout-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.home-aside {
  background-color: #304156;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
  z-index: 50;
}

.el-menu-vertical-demo {
  height: 100%;
  border-right: none;
  background-color: #304156;
  overflow-y: auto;
}

.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 200px;
}

.menu-item {
  transition: all 0.3s ease;
  margin: 2px 0;
  border-radius: 4px;
  margin-right: 8px;
}

.menu-item:hover {
  background-color: rgba(255, 255, 255, 0.1) !important;
}

.menu-item.is-active {
  background-color: rgba(64, 158, 255, 0.2) !important;
  border-right: 3px solid #409EFF;
}

.home-main {
  background-color: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
  transition: all 0.3s ease;
  width: 100%; /* 明确设置宽度为100% */
  box-sizing: border-box;
}

/* 页面切换动画 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

/* 滚动条样式 */
.el-menu-vertical-demo::-webkit-scrollbar,
.home-main::-webkit-scrollbar {
  width: 6px;
}

.el-menu-vertical-demo::-webkit-scrollbar-track,
.home-main::-webkit-scrollbar-track {
  background-color: rgba(0, 0, 0, 0.1);
}

.el-menu-vertical-demo::-webkit-scrollbar-thumb,
.home-main::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.el-menu-vertical-demo::-webkit-scrollbar-thumb:hover,
.home-main::-webkit-scrollbar-thumb:hover {
  background-color: rgba(0, 0, 0, 0.3);
}
</style>