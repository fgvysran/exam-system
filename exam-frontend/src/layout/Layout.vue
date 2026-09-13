<template>
  <el-container class="layout">
    <el-aside width="200px" class="aside">
      <div class="logo">在线考试系统</div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>

        <el-menu-item index="/profile">
          <el-icon><Setting /></el-icon>
          <span>个人设置</span>
        </el-menu-item>

        <el-sub-menu v-if="isTeacher" index="question-bank">
          <template #title>
            <el-icon><Collection /></el-icon>
            <span>题库管理</span>
          </template>
          <el-menu-item index="/subject">学科管理</el-menu-item>
          <el-menu-item index="/knowledge-point">知识点管理</el-menu-item>
          <el-menu-item index="/question">题目管理</el-menu-item>
        </el-sub-menu>

        <el-menu-item v-if="isTeacher" index="/paper">
          <el-icon><Document /></el-icon>
          <span>试卷管理</span>
        </el-menu-item>

        <el-menu-item v-if="isTeacher" index="/exam">
          <el-icon><Calendar /></el-icon>
          <span>考试管理</span>
        </el-menu-item>

        <el-menu-item v-if="isAdmin" index="/class">
          <el-icon><School /></el-icon>
          <span>班级管理</span>
        </el-menu-item>

        <el-menu-item v-if="isAdmin" index="/student">
          <el-icon><UserFilled /></el-icon>
          <span>用户管理</span>
        </el-menu-item>

        <el-menu-item v-if="isStudent" index="/my-exam">
          <el-icon><EditPen /></el-icon>
          <span>我的考试</span>
        </el-menu-item>

        <el-menu-item v-if="isTeacher" index="/grading">
          <el-icon><Finished /></el-icon>
          <span>阅卷</span>
        </el-menu-item>

        <el-menu-item v-if="isTeacher" index="/stats">
          <el-icon><TrendCharts /></el-icon>
          <span>成绩统计</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-title">{{ $route.meta.title || '' }}</div>
        <div class="user">
          <span class="username">{{ userStore.userInfo?.realName }}（{{ roleText }}）</span>
          <el-button link type="primary" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { HomeFilled, Collection, Document, Calendar, School, UserFilled, EditPen, Finished, TrendCharts, Setting } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const roleText = computed(() => {
  const roles = userStore.userInfo?.roles || []
  if (roles.includes('ADMIN')) return '管理员'
  if (roles.includes('TEACHER')) return '教师'
  if (roles.includes('STUDENT')) return '学生'
  return ''
})

const isTeacher = computed(() => {
  const roles = userStore.userInfo?.roles || []
  return roles.includes('ADMIN') || roles.includes('TEACHER')
})

const isStudent = computed(() => {
  const roles = userStore.userInfo?.roles || []
  return roles.includes('STUDENT')
})

const isAdmin = computed(() => {
  const roles = userStore.userInfo?.roles || []
  return roles.includes('ADMIN')
})

onMounted(async () => {
  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch (e) {
      // 忽略, 401 由拦截器处理跳转
    }
  }
})

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  height: 100%;
}
.aside {
  background-color: #304156;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #eee;
  background: #fff;
}
.header-title {
  font-size: 16px;
  font-weight: 600;
}
.user {
  display: flex;
  align-items: center;
  gap: 8px;
}
.username {
  color: #333;
}
.main {
  background: #f0f2f5;
}
</style>
