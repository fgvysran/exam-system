<template>
  <el-card>
    <div class="toolbar">
      <el-button type="primary" @click="openAdd">新增学科</el-button>
    </div>
    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="学科名称" />
      <el-table-column prop="code" label="编码" />
      <el-table-column prop="questionCount" label="题目数" width="100" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑学科' : '新增学科'" width="420px" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" @submit.prevent="handleSave">
      <el-form-item label="名称" prop="name">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="编码">
        <el-input v-model="form.code" />
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
import { listSubjects, addSubject, updateSubject, deleteSubject } from '@/api/subject'

const list = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, name: '', code: '' })
const rules = { name: [{ required: true, message: '请输入学科名称', trigger: 'blur' }] }

async function load() {
  const res = await listSubjects()
  list.value = res.data
}

function openAdd() {
  Object.assign(form, { id: null, name: '', code: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, { id: row.id, name: row.name, code: row.code })
  dialogVisible.value = true
}

async function handleSave() {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  if (form.id) {
    await updateSubject(form.id, form)
  } else {
    await addSubject(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除学科「${row.name}」吗？`, '提示', { type: 'warning' })
  await deleteSubject(row.id)
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
