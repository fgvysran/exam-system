<template>
  <el-card>
    <h2>欢迎使用在线考试系统</h2>
    <el-descriptions :column="2" border style="margin-top: 20px;">
      <el-descriptions-item label="当前用户">{{ userStore.userInfo?.realName }}</el-descriptions-item>
      <el-descriptions-item label="角色">{{ roleText }}</el-descriptions-item>
      <el-descriptions-item label="用户名">{{ userStore.userInfo?.username }}</el-descriptions-item>
      <el-descriptions-item label="邮箱">{{ userStore.userInfo?.email || '-' }}</el-descriptions-item>
    </el-descriptions>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const roleText = computed(() => {
  const roles = userStore.userInfo?.roles || []
  if (roles.includes('ADMIN')) return '管理员'
  if (roles.includes('TEACHER')) return '教师'
  if (roles.includes('STUDENT')) return '学生'
  return ''
})
</script>
