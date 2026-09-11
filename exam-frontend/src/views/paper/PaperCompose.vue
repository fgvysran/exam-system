<template>
  <div>
    <el-card class="paper-info">
      <div class="info-line">
        <div>
          <h2>{{ paper.name }}</h2>
          <p class="meta">
            学科：{{ paper.subjectName }} ｜ 总分：{{ paper.totalScore }} ｜ 时长：{{ paper.duration }} 分钟
          </p>
        </div>
        <el-button @click="$router.back()">返回列表</el-button>
      </div>
    </el-card>

    <div class="compose">
      <!-- 左侧：题库选题 -->
      <el-card class="left">
        <div class="filter">
          <el-select v-model="query.questionType" placeholder="题型" clearable style="width: 130px">
            <el-option v-for="t in questionTypes" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
          <el-select v-model="query.difficulty" placeholder="难度" clearable style="width: 110px">
            <el-option label="易" :value="1" />
            <el-option label="中" :value="2" />
            <el-option label="难" :value="3" />
          </el-select>
          <el-input v-model="query.keyword" placeholder="题干关键词" clearable style="width: 160px" @keyup.enter="search" />
          <el-button type="primary" @click="search">查询</el-button>
        </div>

        <el-table :data="questions" border max-height="600">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="questionTypeName" label="题型" width="80" />
          <el-table-column prop="content" label="题干" show-overflow-tooltip />
          <el-table-column label="难度" width="60">
            <template #default="{ row }">{{ difficultyText(row.difficulty) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ row }">
              <el-button link type="primary" :disabled="isAdded(row.id)" @click="addQuestion(row)">
                {{ isAdded(row.id) ? '已加入' : '加入' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, prev, pager, next"
          small
          style="margin-top: 12px"
          @current-change="loadQuestions"
        />
      </el-card>

      <!-- 右侧：已选题目 -->
      <el-card class="right">
        <div class="title">已选题目（{{ selected.length }}）</div>
        <el-table :data="selected" border max-height="600">
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column prop="questionTypeName" label="题型" width="80" />
          <el-table-column prop="content" label="题干" show-overflow-tooltip />
          <el-table-column label="分值" width="130">
            <template #default="{ row }">
              <el-input-number v-model="row.score" :min="0" :step="1" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="130">
            <template #default="{ $index }">
              <el-button link @click="move($index, -1)" :disabled="$index === 0">上移</el-button>
              <el-button link @click="move($index, 1)" :disabled="$index === selected.length - 1">下移</el-button>
              <el-button link type="danger" @click="selected.splice($index, 1)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="save-bar">
          <span class="total">总分：{{ totalScore }}</span>
          <el-button type="primary" @click="handleSave">保存试卷</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPaper, savePaperQuestions } from '@/api/paper'
import { pageQuestions } from '@/api/question'

const route = useRoute()
const paperId = Number(route.params.id)

const questionTypes = [
  { value: 1, label: '单选题' },
  { value: 2, label: '多选题' },
  { value: 3, label: '判断题' },
  { value: 4, label: '填空题' },
  { value: 5, label: '简答题' },
  { value: 6, label: '论述题' },
  { value: 7, label: '编程题' }
]

const paper = ref({})
const questions = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, questionType: null, difficulty: null, keyword: '' })
const selected = ref([])

const totalScore = computed(() =>
  selected.value.reduce((sum, q) => sum + (Number(q.score) || 0), 0)
)

function difficultyText(d) {
  return { 1: '易', 2: '中', 3: '难' }[d] || '-'
}

async function loadPaper() {
  const res = await getPaper(paperId)
  paper.value = res.data
  selected.value = (res.data.questions || []).map(q => ({
    questionId: q.questionId,
    questionType: q.questionType,
    questionTypeName: q.questionTypeName,
    content: q.content,
    score: Number(q.score) || 0,
    sort: q.sort
  }))
}

async function loadQuestions() {
  const res = await pageQuestions({ ...query, subjectId: paper.value.subjectId })
  questions.value = res.data.records
  total.value = res.data.total
}

function search() {
  query.pageNum = 1
  loadQuestions()
}

function isAdded(id) {
  return selected.value.some(q => q.questionId === id)
}

function addQuestion(row) {
  if (isAdded(row.id)) return
  selected.value.push({
    questionId: row.id,
    questionType: row.questionType,
    questionTypeName: row.questionTypeName,
    content: row.content,
    score: Number(row.defaultScore) || 5
  })
}

function move(index, dir) {
  const target = index + dir
  if (target < 0 || target >= selected.value.length) return
  const arr = selected.value
  const tmp = arr[index]
  arr[index] = arr[target]
  arr[target] = tmp
}

async function handleSave() {
  const data = selected.value.map((q, i) => ({
    questionId: q.questionId,
    score: q.score,
    sort: i + 1
  }))
  await savePaperQuestions(paperId, data)
  ElMessage.success('保存成功')
  await loadPaper()
}

onMounted(async () => {
  await loadPaper()
  await loadQuestions()
})
</script>

<style scoped>
.paper-info {
  margin-bottom: 16px;
}
.info-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.meta {
  color: #666;
  margin: 8px 0 0;
}
.compose {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}
.left {
  flex: 1;
  min-width: 0;
}
.right {
  width: 520px;
  flex-shrink: 0;
}
.filter {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.title {
  font-weight: 600;
  margin-bottom: 12px;
}
.save-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
}
.total {
  font-size: 15px;
  color: #e6a23c;
  font-weight: 600;
}
</style>
