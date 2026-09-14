<template>
  <el-card>
    <div class="toolbar">
      <el-button type="primary" @click="openAdd">新建试卷</el-button>
    </div>

    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="试卷名称" />
      <el-table-column prop="subjectName" label="学科" width="140" />
      <el-table-column prop="totalScore" label="总分" width="80" />
      <el-table-column prop="duration" label="时长(分)" width="90" />
      <el-table-column label="难度" width="70">
        <template #default="{ row }">{{ difficultyText(row.difficulty) }}</template>
      </el-table-column>
      <el-table-column prop="questionCount" label="题目数" width="80" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 2 ? 'success' : 'info'">{{ row.status === 2 ? '已发布' : '草稿' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="goCompose(row)">组卷</el-button>
          <el-button link :type="row.status === 2 ? 'warning' : 'success'" @click="handlePublish(row)">
            {{ row.status === 2 ? '取消发布' : '发布' }}
          </el-button>
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

  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑试卷' : '新建试卷'" width="520px" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" @submit.prevent="handleSave">
      <el-form-item label="名称" prop="name">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="学科" prop="subjectId">
        <el-select v-model="form.subjectId" placeholder="选择学科" style="width: 100%">
          <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="时长(分)">
        <el-input-number v-model="form.duration" :min="1" />
      </el-form-item>
      <el-form-item label="难度">
        <el-radio-group v-model="form.difficulty">
          <el-radio :value="1">易</el-radio>
          <el-radio :value="2">中</el-radio>
          <el-radio :value="3">难</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="说明">
        <el-input v-model="form.description" type="textarea" :rows="2" />
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listSubjects } from '@/api/subject'
import { pagePapers, getPaper, addPaper, updatePaper, deletePaper, updatePaperStatus } from '@/api/paper'

const router = useRouter()
const subjects = ref([])
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })

const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, name: '', subjectId: null, duration: 60, difficulty: 2, description: '' })
const rules = {
  name: [{ required: true, message: '请输入试卷名称', trigger: 'blur' }],
  subjectId: [{ required: true, message: '请选择学科', trigger: 'change' }]
}

function difficultyText(d) {
  return { 1: '易', 2: '中', 3: '难' }[d] || '-'
}

async function load() {
  const res = await pagePapers(query)
  list.value = res.data.records
  total.value = res.data.total
}

function openAdd() {
  Object.assign(form, { id: null, name: '', subjectId: null, duration: 60, difficulty: 2, description: '' })
  dialogVisible.value = true
}

async function openEdit(row) {
  const res = await getPaper(row.id)
  const p = res.data
  Object.assign(form, {
    id: p.id, name: p.name, subjectId: p.subjectId,
    duration: p.duration, difficulty: p.difficulty, description: p.description || ''
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
    await updatePaper(form.id, form)
  } else {
    await addPaper(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

function goCompose(row) {
  router.push(`/paper/${row.id}/compose`)
}

async function handlePublish(row) {
  const newStatus = row.status === 2 ? 1 : 2
  await updatePaperStatus(row.id, newStatus)
  ElMessage.success(newStatus === 2 ? '已发布' : '已取消发布')
  load()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除试卷「${row.name}」吗？`, '提示', { type: 'warning' })
  await deletePaper(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(async () => {
  const s = await listSubjects()
  subjects.value = s.data
  load()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
}
</style>
