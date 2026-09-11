<template>
  <div>
    <el-card class="score-card">
      <div class="score-line">
        <span class="label">总分</span>
        <span class="score">{{ result.totalScore }}</span>
        <span class="sub">客观分 {{ result.objectiveScore }} / 主观分 {{ result.subjectiveScore }}</span>
      </div>
      <p v-if="result.status === 2" class="pending-tip">试卷已交卷，主观题正在等待老师阅卷</p>
    </el-card>

    <el-card v-for="(q, index) in result.questions" :key="q.questionId" class="question">
      <div class="q-title">
        <span class="q-no">{{ index + 1 }}.</span>
        <el-tag size="small">{{ q.questionTypeName }}</el-tag>
        <span class="q-score">得分：{{ q.score }}</span>
        <el-tag v-if="q.judgeStatus === 0" type="warning" size="small">待批改</el-tag>
        <el-tag v-else-if="q.isCorrect === 1" type="success" size="small">正确</el-tag>
        <el-tag v-else type="danger" size="small">错误</el-tag>
      </div>
      <div class="q-content">{{ q.content }}</div>
      <div v-if="q.options && q.options.length" class="options">
        <span v-for="opt in q.options" :key="opt.key" class="opt">{{ opt.key }}. {{ opt.text }}</span>
      </div>
      <div class="answer-line">你的答案：{{ displayAnswer(q) || '（未作答）' }}</div>
      <div v-if="q.correctAnswer" class="answer-line correct">参考答案：{{ q.correctAnswer }}</div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getResult } from '@/api/answer'

const route = useRoute()
const examId = Number(route.params.id)
const result = ref({ questions: [] })

function displayAnswer(q) {
  if (q.questionType === 4) {
    try {
      return JSON.parse(q.userAnswer || '[]').join('、')
    } catch (e) {
      return q.userAnswer
    }
  }
  return q.userAnswer
}

onMounted(async () => {
  const res = await getResult(examId)
  result.value = res.data
})
</script>

<style scoped>
.score-card {
  margin-bottom: 16px;
}
.score-line {
  display: flex;
  align-items: baseline;
  gap: 16px;
}
.label {
  font-size: 15px;
}
.score {
  font-size: 36px;
  font-weight: 700;
  color: #409eff;
}
.sub {
  color: #999;
}
.pending-tip {
  color: #e6a23c;
  margin-top: 8px;
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
  color: #666;
}
.q-content {
  margin-bottom: 8px;
  line-height: 1.6;
}
.options {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 8px;
  color: #666;
}
.answer-line {
  margin-top: 4px;
  color: #333;
}
.answer-line.correct {
  color: #67c23a;
}
</style>
