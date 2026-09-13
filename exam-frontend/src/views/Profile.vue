<template>
  <el-card>
    <h2>个人设置</h2>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" style="max-width: 480px; margin-top: 24px;">
      <el-form-item label="用户名">
        <el-input :model-value="userStore.userInfo?.username" disabled />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input :model-value="userStore.userInfo?.realName" disabled />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="请输入邮箱" clearable />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" placeholder="请输入手机号" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { updateProfile } from '@/api/auth'

const userStore = useUserStore()
const formRef = ref()
const saving = ref(false)
const form = reactive({ email: '', phone: '' })

const rules = {
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }]
}

onMounted(() => {
  form.email = userStore.userInfo?.email || ''
  form.phone = userStore.userInfo?.phone || ''
})

async function handleSave() {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  saving.value = true
  try {
    await updateProfile({ email: form.email.trim(), phone: form.phone.trim() })
    await userStore.fetchUserInfo()
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}
</script>
