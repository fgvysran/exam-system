<template>
  <el-card>
    <div class="toolbar">
      <el-select v-model="examId" placeholder="按考试筛选" clearable style="width: 220px" @change="search">
        <el-option v-for="e in exams" :key="e.id" :label="e.name" :value="e.id" />
      </el-select>
      <el-button type="primary" @click="load">刷新</el-button>
      <el-button type="warning" :disabled="!examId" @click="handleRegrade">重新自动判分</el-button>
    </div>

    <el-table :data="list" border>
      <el-table-column prop="examName" label="考试" width="180" />
      <el-table-column prop="studentName" label="学生" width="100" />
      <el-table-column prop="questionTypeName" label="题型" width="90" />
      <el-table-column prop="content" label="题干" show-overflow-tooltip />
      <el-table-column prop="userAnswer" label="学生答案" show-overflow-tooltip />
      <el-table-column prop="fullScore" label="满分" width="70" />
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openGrade(row)">阅卷</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      style="margin-top: 16px; justify-content: flex-end"
      @current-change="load"
      @size-change="load"
    />
  </el-card>

  <el-dialog v-model="dialogVisible" title="阅卷" width="640px" :close-on-click-modal="false" :close-on-press-escape="false">
    <div v-if="current">
      <el-descriptions :column="2" border style="margin-bottom: 16px">
        <el-descriptions-item label="考试">{{ current.examName }}</el-descriptions-item>
        <el-descriptions-item label="学生">{{ current.studentName }}</el-descriptions-item>
        <el-descriptions-item label="题型">{{ current.questionTypeName }}</el-descriptions-item>
        <el-descriptions-item label="满分">{{ current.fullScore }}</el-descriptions-item>
      </el-descriptions>
      <div class="block">
        <div class="block-title">题干</div>
        <div class="block-body">{{ current.content }}</div>
      </div>
      <div class="block">
        <div class="block-title">学生答案</div>
        <div class="block-body">{{ current.userAnswer || '（未作答）' }}</div>
      </div>
      <div class="block">
        <div class="block-title">参考答案</div>
        <div class="block-body">{{ current.referenceAnswer }}</div>
      </div>
      <el-form label-width="80px" @submit.prevent="handleSubmit">
        <el-form-item label="得分">
          <el-input-number v-model="form.score" :min="0" :max="Number(current.fullScore) || 100" :step="0.5" />
        </el-form-item>
        <el-form-item label="评语">
          <el-input v-model="form.comment" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit">提交评分</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pendingGrading, gradeAnswer, regradeAnswer } from '@/api/grading'
import { pageExams } from '@/api/exam'

const exams = ref([])
const examId = ref(null)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const dialogVisible = ref(false)
const current = ref(null)
const form = reactive({ score: 0, comment: '' })

async function load() {
  const res = await pendingGrading({ examId: examId.value, pageNum: pageNum.value, pageSize: pageSize.value })
  list.value = res.data.records
  total.value = res.data.total
}

function search() {
  pageNum.value = 1
  load()
}

function openGrade(row) {
  current.value = row
  form.score = Number(row.fullScore) || 0
  form.comment = ''
  dialogVisible.value = true
}

async function handleSubmit() {
  await gradeAnswer(current.value.detailId, { score: form.score, comment: form.comment })
  ElMessage.success('评分成功')
  dialogVisible.value = false
  load()
}

async function handleRegrade() {
  try {
    await ElMessageBox.confirm(
      '将对该场考试所有已交卷记录重新自动判分（保留人工已判分数），是否继续？',
      '提示',
      { type: 'warning' }
    )
  } catch {
    return
  }
  const res = await regradeAnswer(examId.value)
  ElMessage.success(`已重新判分 ${res.data} 条记录`)
  load()
}

onMounted(async () => {
  const e = await pageExams({ pageNum: 1, pageSize: 100 })
  exams.value = e.data.records
  load()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.block {
  margin-bottom: 14px;
}
.block-title {
  font-weight: 600;
  color: #909399;
  margin-bottom: 4px;
}
.block-body {
  line-height: 1.6;
}
</style>
