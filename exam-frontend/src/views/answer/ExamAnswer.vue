<template>
  <div>
    <el-card class="exam-header">
      <div class="header-line">
        <div>
          <h2>{{ paper.examName }}</h2>
          <p class="end-time">截止时间：{{ paper.endTime }}</p>
        </div>
        <div class="actions">
          <el-button @click="handleSave">保存答案</el-button>
          <el-button type="danger" @click="handleSubmit">交卷</el-button>
        </div>
      </div>
    </el-card>

    <el-card v-for="(q, index) in paper.questions" :key="q.questionId" class="question">
      <div class="q-title">
        <span class="q-no">{{ index + 1 }}.</span>
        <el-tag size="small">{{ q.questionTypeName }}</el-tag>
        <span class="q-score">（{{ q.score }} 分）</span>
      </div>
      <div class="q-content">{{ q.content }}</div>

      <!-- 单选 -->
      <el-radio-group v-if="q.questionType === 1" v-model="answers[q.questionId]" class="q-options">
        <el-radio v-for="opt in q.options" :key="opt.key" :value="opt.key" class="q-option">
          {{ opt.key }}. {{ opt.text }}
        </el-radio>
      </el-radio-group>

      <!-- 多选 -->
      <el-checkbox-group v-else-if="q.questionType === 2" v-model="answers[q.questionId]" class="q-options">
        <el-checkbox v-for="opt in q.options" :key="opt.key" :value="opt.key" class="q-option">
          {{ opt.key }}. {{ opt.text }}
        </el-checkbox>
      </el-checkbox-group>

      <!-- 判断 -->
      <el-radio-group v-else-if="q.questionType === 3" v-model="answers[q.questionId]" class="q-options">
        <el-radio value="T">正确</el-radio>
        <el-radio value="F">错误</el-radio>
      </el-radio-group>

      <!-- 填空 -->
      <div v-else-if="q.questionType === 4">
        <el-input v-model="answers[q.questionId]" type="textarea" :rows="2" placeholder="每行填写一个空" />
      </div>

      <!-- 简答/论述/编程 -->
      <el-input v-else v-model="answers[q.questionId]" type="textarea" :rows="5" placeholder="请输入答案" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { startExam, saveAnswers, submitExam } from '@/api/answer'

const route = useRoute()
const router = useRouter()
const examId = Number(route.params.id)

const paper = ref({ questions: [] })
const answers = reactive({})

function buildAnswer(q) {
  const a = answers[q.questionId]
  if (q.questionType === 2) {
    return (a || []).slice().sort().join(',')
  }
  if (q.questionType === 4) {
    const blanks = (a || '').split('\n').map(s => s.trim()).filter(s => s !== '')
    return JSON.stringify(blanks)
  }
  return a || ''
}

function buildAnswers() {
  return paper.value.questions.map(q => ({ questionId: q.questionId, answer: buildAnswer(q) }))
}

async function loadPaper() {
  const res = await startExam(examId)
  paper.value = res.data
  for (const q of paper.value.questions) {
    answers[q.questionId] = q.questionType === 2 ? [] : ''
  }
}

async function handleSave() {
  await saveAnswers(examId, buildAnswers())
  ElMessage.success('已保存')
}

async function handleSubmit() {
  await ElMessageBox.confirm('确定交卷吗？交卷后不能再修改。', '提示', { type: 'warning' })
  await submitExam(examId, buildAnswers())
  ElMessage.success('交卷成功')
  router.push(`/exam/${examId}/result`)
}

onMounted(loadPaper)
</script>

<style scoped>
.exam-header {
  margin-bottom: 16px;
}
.header-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.end-time {
  color: #e6a23c;
  margin-top: 6px;
}
.question {
  margin-bottom: 16px;
}
.q-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.q-no {
  font-weight: 600;
}
.q-score {
  color: #999;
  font-size: 13px;
}
.q-content {
  margin-bottom: 12px;
  line-height: 1.6;
}
.q-options {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}
.q-option {
  margin-right: 0;
}
</style>
