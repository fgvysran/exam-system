<template>
  <el-card>
    <!-- 筛选栏 -->
    <div class="filter">
      <el-select v-model="query.subjectId" placeholder="学科" clearable style="width: 160px">
        <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
      </el-select>
      <el-select v-model="query.questionType" placeholder="题型" clearable style="width: 140px">
        <el-option v-for="t in questionTypes" :key="t.value" :label="t.label" :value="t.value" />
      </el-select>
      <el-select v-model="query.difficulty" placeholder="难度" clearable style="width: 120px">
        <el-option label="易" :value="1" />
        <el-option label="中" :value="2" />
        <el-option label="难" :value="3" />
      </el-select>
      <el-input v-model="query.keyword" placeholder="题干关键词" clearable style="width: 200px" @keyup.enter="search" />
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
      <div class="right">
        <el-button type="primary" @click="openAdd">新增题目</el-button>
        <el-button @click="openImport">批量导入</el-button>
        <el-button @click="handleExport">导出</el-button>
        <el-button type="danger" :disabled="!selection.length" @click="handleBatchDelete">批量删除</el-button>
      </div>
    </div>

    <!-- 表格 -->
    <el-table :data="list" border @selection-change="sel => (selection = sel)">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="subjectName" label="学科" width="120" />
      <el-table-column prop="questionTypeName" label="题型" width="90" />
      <el-table-column prop="content" label="题干" show-overflow-tooltip />
      <el-table-column label="难度" width="70">
        <template #default="{ row }">
          <el-tag size="small">{{ difficultyText(row.difficulty) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="defaultScore" label="分值" width="70" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-switch :model-value="row.status === 1" @change="v => handleStatus(row, v)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
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

  <!-- 新增/编辑 -->
  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑题目' : '新增题目'" width="760px" top="5vh" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="90px" @submit.prevent="handleSave">
      <el-form-item label="学科" prop="subjectId">
        <el-select v-model="form.subjectId" placeholder="选择学科" style="width: 240px">
          <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="题型" prop="questionType">
        <el-select v-model="form.questionType" style="width: 240px" @change="onTypeChange">
          <el-option v-for="t in questionTypes" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="题干" prop="content">
        <el-input v-model="form.content" type="textarea" :rows="3" />
      </el-form-item>

      <!-- 单选/多选: 选项 -->
      <template v-if="form.questionType === 1 || form.questionType === 2">
        <el-form-item label="选项">
          <div class="options">
            <div v-for="(opt, i) in form.options" :key="i" class="option-row">
              <el-tag>{{ opt.key }}</el-tag>
              <el-input v-model="opt.text" placeholder="选项内容" style="flex:1" />
              <el-button link type="danger" @click="removeOption(i)">删除</el-button>
            </div>
            <el-button link type="primary" @click="addOption">+ 添加选项</el-button>
          </div>
        </el-form-item>
        <!-- 单选答案 -->
        <el-form-item v-if="form.questionType === 1" label="答案">
          <el-radio-group v-model="form.answer">
            <el-radio v-for="opt in form.options" :key="opt.key" :value="opt.key">{{ opt.key }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <!-- 多选答案 -->
        <el-form-item v-else label="答案">
          <el-checkbox-group v-model="multiAnswer">
            <el-checkbox v-for="opt in form.options" :key="opt.key" :value="opt.key">{{ opt.key }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </template>

      <!-- 判断答案 -->
      <el-form-item v-if="form.questionType === 3" label="答案">
        <el-radio-group v-model="form.answer">
          <el-radio value="T">正确 (T)</el-radio>
          <el-radio value="F">错误 (F)</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 填空答案 -->
      <template v-if="form.questionType === 4">
        <el-form-item label="答案(每空)">
          <div class="options">
            <div v-for="(blank, i) in blankAnswers" :key="i" class="option-row">
              <el-input v-model="blankAnswers[i]" placeholder="第 {{ i + 1 }} 空答案" style="flex:1" />
              <el-button link type="danger" @click="blankAnswers.splice(i, 1)">删除</el-button>
            </div>
            <el-button link type="primary" @click="blankAnswers.push('')">+ 添加空</el-button>
          </div>
        </el-form-item>
      </template>

      <!-- 简答/论述答案 -->
      <el-form-item v-if="form.questionType === 5 || form.questionType === 6" label="参考答案">
        <el-input v-model="form.answer" type="textarea" :rows="3" />
        <div v-if="form.questionType === 5 && form.judgeMode === 1" class="answer-tip">
          自动判分按得分点加权给分：用 ; 分隔得分点，得分点内用 | 分隔等价答案（任一命中即可），可用冒号指定分值。
          例：封装:6;继承:4 = 封装 6 分、继承 4 分；不写分值则各得分点均分
        </div>
      </el-form-item>

      <!-- 判分方式(仅填空/简答) -->
      <el-form-item v-if="form.questionType === 4 || form.questionType === 5" label="判分方式">
        <el-radio-group v-model="form.judgeMode">
          <el-radio :value="1">自动判分</el-radio>
          <el-radio :value="2">人工阅卷</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 编程题扩展 -->
      <template v-if="form.questionType === 7">
        <el-form-item label="参考答案">
          <el-input v-model="form.answer" type="textarea" :rows="3" placeholder="参考答案代码" />
        </el-form-item>
        <el-form-item label="支持语言">
          <el-input v-model="form.programming.languages" placeholder="如 java,cpp,python" />
        </el-form-item>
        <el-form-item label="时间限制">
          <el-input-number v-model="form.programming.timeLimit" :min="1" /> 毫秒
        </el-form-item>
        <el-form-item label="内存限制">
          <el-input-number v-model="form.programming.memoryLimit" :min="1" /> MB
        </el-form-item>
        <el-form-item label="测试用例">
          <div class="options">
            <div v-for="(tc, i) in form.programming.testCases" :key="i" class="case-row">
              <el-input v-model="tc.input" placeholder="输入" />
              <el-input v-model="tc.output" placeholder="期望输出" />
              <el-button link type="danger" @click="form.programming.testCases.splice(i, 1)">删除</el-button>
            </div>
            <el-button link type="primary" @click="form.programming.testCases.push({ input: '', output: '' })">+ 添加用例</el-button>
          </div>
        </el-form-item>
      </template>

      <el-form-item label="难度">
        <el-radio-group v-model="form.difficulty">
          <el-radio :value="1">易</el-radio>
          <el-radio :value="2">中</el-radio>
          <el-radio :value="3">难</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="默认分值">
        <el-input-number v-model="form.defaultScore" :min="0" :step="1" />
      </el-form-item>
      <el-form-item label="解析">
        <el-input v-model="form.analysis" type="textarea" :rows="2" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>

  <!-- 导入 -->
  <el-dialog v-model="importVisible" title="批量导入题目" width="460px" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form label-width="80px">
      <el-form-item label="学科" required>
        <el-select v-model="importSubjectId" placeholder="选择学科" style="width: 100%">
          <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="文件">
        <el-upload :auto-upload="false" :limit="1" accept=".xlsx,.xls" :on-change="onFileChange" :on-remove="() => (importFile = null)">
          <el-button>选择 Excel 文件</el-button>
          <template #tip><div class="el-upload__tip">模板列：题型/题干/选项A-D/答案/难度/分值/解析</div></template>
        </el-upload>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="importVisible = false">取消</el-button>
      <el-button type="primary" :loading="importing" @click="handleImport">导入</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listSubjects } from '@/api/subject'
import {
  pageQuestions, getQuestion, addQuestion, updateQuestion, deleteQuestion,
  batchDeleteQuestions, updateQuestionStatus, importQuestions, exportQuestions
} from '@/api/question'
import { downloadBlob } from '@/utils/download'

const questionTypes = [
  { value: 1, label: '单选题' },
  { value: 2, label: '多选题' },
  { value: 3, label: '判断题' },
  { value: 4, label: '填空题' },
  { value: 5, label: '简答题' },
  { value: 6, label: '论述题' },
  { value: 7, label: '编程题' }
]

const subjects = ref([])
const list = ref([])
const total = ref(0)
const selection = ref([])
const query = reactive({ pageNum: 1, pageSize: 10, subjectId: null, questionType: null, difficulty: null, keyword: '' })

const dialogVisible = ref(false)
const formRef = ref()
const multiAnswer = ref([])
const blankAnswers = ref([])
const form = reactive({
  id: null, subjectId: null, questionType: 1, content: '', options: [],
  answer: '', analysis: '', difficulty: 2, defaultScore: 5, judgeMode: 1,
  programming: { languages: 'java', timeLimit: 1000, memoryLimit: 128, testCases: [] }
})
const rules = {
  subjectId: [{ required: true, message: '请选择学科', trigger: 'change' }],
  content: [{ required: true, message: '请输入题干', trigger: 'blur' }]
}

const importVisible = ref(false)
const importSubjectId = ref(null)
const importFile = ref(null)
const importing = ref(false)

function difficultyText(d) {
  return { 1: '易', 2: '中', 3: '难' }[d] || '-'
}

async function load() {
  const res = await pageQuestions(query)
  list.value = res.data.records
  total.value = res.data.total
}

function search() {
  query.pageNum = 1
  load()
}

function resetQuery() {
  Object.assign(query, { pageNum: 1, pageSize: 10, subjectId: null, questionType: null, difficulty: null, keyword: '' })
  load()
}

function addOption() {
  const key = String.fromCharCode(65 + form.options.length)
  form.options.push({ key, text: '' })
}

function removeOption(i) {
  form.options.splice(i, 1)
  form.options.forEach((opt, idx) => (opt.key = String.fromCharCode(65 + idx)))
}

function onTypeChange() {
  form.options = []
  form.answer = ''
  multiAnswer.value = []
  blankAnswers.value = ['']
  form.judgeMode = 1
  form.programming = { languages: 'java', timeLimit: 1000, memoryLimit: 128, testCases: [] }
}

function openAdd() {
  Object.assign(form, {
    id: null, subjectId: null, questionType: 1, content: '', options: [],
    answer: '', analysis: '', difficulty: 2, defaultScore: 5, judgeMode: 1,
    programming: { languages: 'java', timeLimit: 1000, memoryLimit: 128, testCases: [] }
  })
  multiAnswer.value = []
  blankAnswers.value = ['']
  dialogVisible.value = true
}

async function openEdit(row) {
  const res = await getQuestion(row.id)
  const q = res.data
  Object.assign(form, {
    id: q.id, subjectId: q.subjectId, questionType: q.questionType, content: q.content,
    options: q.options || [], answer: q.answer || '', analysis: q.analysis || '',
    difficulty: q.difficulty || 2, defaultScore: q.defaultScore || 5, judgeMode: q.judgeMode || 1,
    programming: q.programming || { languages: 'java', timeLimit: 1000, memoryLimit: 128, testCases: [] }
  })
  if (q.questionType === 2) {
    multiAnswer.value = q.answer ? q.answer.split(',') : []
  } else {
    multiAnswer.value = []
  }
  if (q.questionType === 4) {
    try {
      blankAnswers.value = q.answer ? JSON.parse(q.answer) : ['']
    } catch (e) {
      blankAnswers.value = ['']
    }
  } else {
    blankAnswers.value = ['']
  }
  dialogVisible.value = true
}

function buildAnswer() {
  switch (form.questionType) {
    case 1:
    case 3:
    case 5:
    case 6:
    case 7:
      return form.answer
    case 2:
      return multiAnswer.value.slice().sort().join(',')
    case 4:
      return JSON.stringify(blankAnswers.value)
    default:
      return form.answer
  }
}

async function handleSave() {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  const data = {
    subjectId: form.subjectId,
    questionType: form.questionType,
    content: form.content,
    answer: buildAnswer(),
    analysis: form.analysis,
    difficulty: form.difficulty,
    defaultScore: form.defaultScore,
    judgeMode: form.judgeMode,
    options: (form.questionType === 1 || form.questionType === 2) ? form.options : null,
    programming: form.questionType === 7 ? form.programming : null
  }
  if (form.id) {
    await updateQuestion(form.id, data)
  } else {
    await addQuestion(data)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该题目吗？', '提示', { type: 'warning' })
  await deleteQuestion(row.id)
  ElMessage.success('删除成功')
  load()
}

async function handleBatchDelete() {
  const ids = selection.value.map(s => s.id)
  await ElMessageBox.confirm(`确定删除选中的 ${ids.length} 道题目吗？`, '提示', { type: 'warning' })
  await batchDeleteQuestions(ids)
  ElMessage.success('删除成功')
  load()
}

async function handleStatus(row, val) {
  await updateQuestionStatus(row.id, val ? 1 : 0)
  row.status = val ? 1 : 0
  ElMessage.success('操作成功')
}

function openImport() {
  importSubjectId.value = null
  importFile.value = null
  importVisible.value = true
}

function onFileChange(uploadFile) {
  importFile.value = uploadFile.raw
}

async function handleImport() {
  if (!importSubjectId.value) return ElMessage.warning('请选择学科')
  if (!importFile.value) return ElMessage.warning('请选择文件')
  importing.value = true
  try {
    const res = await importQuestions(importFile.value, importSubjectId.value)
    const r = res.data
    ElMessage.success(`导入完成: 成功 ${r.successCount} 条, 失败 ${r.failCount} 条`)
    if (r.errors && r.errors.length) {
      console.log(r.errors)
      ElMessageBox.alert(r.errors.slice(0, 10).join('\n'), '失败明细', { type: 'warning' })
    }
    importVisible.value = false
    load()
  } finally {
    importing.value = false
  }
}

async function handleExport() {
  const res = await exportQuestions({ ...query, pageNum: undefined, pageSize: undefined })
  downloadBlob(res, '题目导出.xlsx')
}

onMounted(async () => {
  const s = await listSubjects()
  subjects.value = s.data
  load()
})
</script>

<style scoped>
.filter {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}
.right {
  margin-left: auto;
}
.options {
  width: 100%;
}
.option-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.case-row {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}
.answer-tip {
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
  margin-top: 4px;
}
</style>
