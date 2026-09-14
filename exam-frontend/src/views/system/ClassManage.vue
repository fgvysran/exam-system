<template>
  <el-card>
    <div class="toolbar">
      <el-button type="primary" @click="openAdd">新增班级</el-button>
    </div>
    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="className" label="班级名称" />
      <el-table-column prop="grade" label="年级" width="140" />
      <el-table-column prop="major" label="专业" width="180" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑班级' : '新增班级'" width="420px" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" @submit.prevent="handleSave">
      <el-form-item label="名称" prop="className">
        <el-input v-model="form.className" />
      </el-form-item>
      <el-form-item label="年级">
        <el-input v-model="form.grade" placeholder="如 2023级" />
      </el-form-item>
      <el-form-item label="专业">
        <el-input v-model="form.major" />
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
import { listClasses, addClass, updateClass, deleteClass } from '@/api/class'

const list = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, className: '', grade: '', major: '' })
const rules = { className: [{ required: true, message: '请输入班级名称', trigger: 'blur' }] }

async function load() {
  const res = await listClasses()
  list.value = res.data
}

function openAdd() {
  Object.assign(form, { id: null, className: '', grade: '', major: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, { id: row.id, className: row.className, grade: row.grade, major: row.major })
  dialogVisible.value = true
}

async function handleSave() {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  if (form.id) {
    await updateClass(form.id, form)
  } else {
    await addClass(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除班级「${row.className}」吗？`, '提示', { type: 'warning' })
  await deleteClass(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
}
</style>
