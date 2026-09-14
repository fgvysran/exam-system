<template>
  <el-card>
    <div class="toolbar">
      <el-select v-model="subjectId" placeholder="选择学科" style="width: 220px" @change="load">
        <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
      </el-select>
      <el-button type="primary" :disabled="!subjectId" @click="openAdd(null)">新增根知识点</el-button>
    </div>

    <el-tree :data="tree" node-key="id" :props="{ label: 'name', children: 'children' }" default-expand-all>
      <template #default="{ data }">
        <span>{{ data.name }}</span>
        <span class="actions">
          <el-button link type="primary" @click.stop="openAdd(data)">添加子节点</el-button>
          <el-button link type="primary" @click.stop="openEdit(data)">编辑</el-button>
          <el-button link type="danger" @click.stop="handleDelete(data)">删除</el-button>
        </span>
      </template>
    </el-tree>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑知识点' : '新增知识点'" width="420px" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" @submit.prevent="handleSave">
      <el-form-item label="名称" prop="name">
        <el-input v-model="form.name" />
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
import { listSubjects } from '@/api/subject'
import {
  getKnowledgePointTree,
  addKnowledgePoint,
  updateKnowledgePoint,
  deleteKnowledgePoint
} from '@/api/knowledgePoint'

const subjects = ref([])
const subjectId = ref(null)
const tree = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, name: '', subjectId: null, parentId: 0 })
const rules = { name: [{ required: true, message: '请输入知识点名称', trigger: 'blur' }] }

async function loadSubjects() {
  const res = await listSubjects()
  subjects.value = res.data
}

async function load() {
  if (!subjectId.value) {
    tree.value = []
    return
  }
  const res = await getKnowledgePointTree(subjectId.value)
  tree.value = res.data
}

function openAdd(parent) {
  Object.assign(form, {
    id: null,
    name: '',
    subjectId: subjectId.value,
    parentId: parent ? parent.id : 0
  })
  dialogVisible.value = true
}

function openEdit(node) {
  Object.assign(form, {
    id: node.id,
    name: node.name,
    subjectId: node.subjectId,
    parentId: node.parentId
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
    await updateKnowledgePoint(form.id, form)
  } else {
    await addKnowledgePoint(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

async function handleDelete(node) {
  await ElMessageBox.confirm(`确定删除知识点「${node.name}」吗？`, '提示', { type: 'warning' })
  await deleteKnowledgePoint(node.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(loadSubjects)
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
}
.actions {
  margin-left: 12px;
}
</style>
