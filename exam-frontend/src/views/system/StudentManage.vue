<template>
  <el-card>
    <div class="toolbar">
      <el-select v-model="query.role" placeholder="角色" clearable style="width: 130px" @change="search">
        <el-option label="学生" value="STUDENT" />
        <el-option label="教师" value="TEACHER" />
      </el-select>
      <el-select v-model="query.classId" placeholder="按班级筛选" clearable style="width: 180px" @change="search">
        <el-option v-for="c in classes" :key="c.id" :label="c.className" :value="c.id" />
      </el-select>
      <el-input v-model="query.keyword" placeholder="用户名/姓名" clearable style="width: 180px" @keyup.enter="search" />
      <el-button type="primary" @click="search">查询</el-button>
      <el-button type="primary" class="add-btn" @click="openAdd">新增用户</el-button>
    </div>

    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="realName" label="姓名" width="120" />
      <el-table-column prop="email" label="邮箱" width="180" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column label="角色" width="90">
        <template #default="{ row }">
          <el-tag :type="row.roleCode === 'TEACHER' ? 'warning' : 'success'">
            {{ row.roleCode === 'TEACHER' ? '教师' : '学生' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="className" label="班级" width="160" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      style="margin-top: 16px; justify-content: flex-end"
      @current-change="load"
      @size-change="load"
    />
  </el-card>

  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="460px">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="90px" @submit.prevent="handleSave">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" />
      </el-form-item>
      <el-form-item label="姓名" prop="realName">
        <el-input v-model="form.realName" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="选填" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" placeholder="选填" />
      </el-form-item>
      <el-form-item label="角色">
        <el-radio-group v-model="form.role" :disabled="!!form.id">
          <el-radio value="STUDENT">学生</el-radio>
          <el-radio value="TEACHER">教师</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item :label="form.id ? '新密码' : '密码'" :prop="form.id ? '' : 'password'">
        <el-input v-model="form.password" type="password" show-password :placeholder="form.id ? '留空则不修改' : ''" />
      </el-form-item>
      <el-form-item v-if="form.role === 'STUDENT'" label="班级">
        <el-select v-model="form.classId" placeholder="选择班级" clearable style="width: 100%">
          <el-option v-for="c in classes" :key="c.id" :label="c.className" :value="c.id" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listClasses } from '@/api/class'
import { pageStudents, addStudent, updateStudent, deleteStudent } from '@/api/user'

const classes = ref([])
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, role: null, classId: null, keyword: '' })

const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, username: '', realName: '', role: 'STUDENT', password: '', classId: null, email: '', phone: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }]
}

async function load() {
  const res = await pageStudents(query)
  list.value = res.data.records
  total.value = res.data.total
}

function search() {
  query.pageNum = 1
  load()
}

function openAdd() {
  Object.assign(form, { id: null, username: '', realName: '', role: 'STUDENT', password: '', classId: null, email: '', phone: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id, username: row.username, realName: row.realName,
    role: row.roleCode || 'STUDENT', password: '', classId: row.classId,
    email: row.email || '', phone: row.phone || ''
  })
  dialogVisible.value = true
}

async function handleSave() {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  if (form.id) {
    await updateStudent(form.id, form)
  } else {
    await addStudent(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除用户「${row.realName}」吗？`, '提示', { type: 'warning' })
  await deleteStudent(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(async () => {
  const c = await listClasses()
  classes.value = c.data
  load()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}
.add-btn {
  margin-left: auto;
}
</style>
