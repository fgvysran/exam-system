<template>
  <el-card>
    <div class="toolbar">
      <el-button type="primary" @click="openAdd">创建考试</el-button>
    </div>

    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="考试名称" />
      <el-table-column prop="paperName" label="试卷" width="180" />
      <el-table-column prop="startTime" label="开始时间" width="170" />
      <el-table-column prop="endTime" label="结束时间" width="170" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
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

  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑考试' : '创建考试'" width="560px">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="90px" @submit.prevent="handleSave">
      <el-form-item label="名称" prop="name">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="试卷" prop="paperId">
        <el-select v-model="form.paperId" placeholder="选择试卷" style="width: 100%">
          <el-option v-for="p in papers" :key="p.id" :label="`${p.name}（${p.totalScore}分）`" :value="p.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="开始时间" prop="startTime">
        <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择开始时间" />
      </el-form-item>
      <el-form-item label="结束时间" prop="endTime">
        <el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择结束时间" />
      </el-form-item>
      <el-form-item label="作答时长(分)">
        <el-input-number v-model="form.duration" :min="1" />
        <span class="tip">留空则按试卷时长</span>
      </el-form-item>
      <el-form-item label="参加班级">
        <el-select v-model="form.classIds" multiple placeholder="选择班级" style="width: 100%">
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
import { pageExams, getExam, addExam, updateExam, deleteExam } from '@/api/exam'
import { pagePapers } from '@/api/paper'
import { listClasses } from '@/api/class'

const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })
const papers = ref([])
const classes = ref([])

const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({
  id: null, name: '', paperId: null, startTime: '', endTime: '', duration: null, classIds: []
})
const rules = {
  name: [{ required: true, message: '请输入考试名称', trigger: 'blur' }],
  paperId: [{ required: true, message: '请选择试卷', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

function statusText(s) {
  return { 1: '未开始', 2: '进行中', 3: '已结束' }[s] || '-'
}
function statusType(s) {
  return { 1: 'info', 2: 'success', 3: 'danger' }[s] || 'info'
}

async function load() {
  const res = await pageExams(query)
  list.value = res.data.records
  total.value = res.data.total
}

async function loadOptions() {
  const [p, c] = await Promise.all([
    pagePapers({ pageNum: 1, pageSize: 100, status: 2 }),
    listClasses()
  ])
  papers.value = p.data.records
  classes.value = c.data
}

function openAdd() {
  Object.assign(form, {
    id: null, name: '', paperId: null, startTime: '', endTime: '', duration: null, classIds: []
  })
  dialogVisible.value = true
}

async function openEdit(row) {
  const res = await getExam(row.id)
  const e = res.data
  Object.assign(form, {
    id: e.id, name: e.name, paperId: e.paperId,
    startTime: e.startTime, endTime: e.endTime, duration: e.duration,
    classIds: (e.classes || []).map(c => c.id)
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
    await updateExam(form.id, form)
  } else {
    await addExam(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除考试「${row.name}」吗？`, '提示', { type: 'warning' })
  await deleteExam(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(() => {
  load()
  loadOptions()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
}
.tip {
  margin-left: 8px;
  color: #999;
  font-size: 12px;
}
</style>
