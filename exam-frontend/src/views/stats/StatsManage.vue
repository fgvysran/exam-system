<template>
  <div>
    <el-card class="selector">
      <el-select v-model="examId" placeholder="选择考试" style="width: 260px" @change="load">
        <el-option v-for="e in exams" :key="e.id" :label="e.name" :value="e.id" />
      </el-select>
      <el-button type="primary" :disabled="!examId" @click="handleExport">导出成绩</el-button>
    </el-card>

    <template v-if="summary">
      <el-card class="stats">
        <div class="stat-cards">
          <div class="stat"><div class="num">{{ summary.totalStudents }}</div><div class="label">参加人数</div></div>
          <div class="stat"><div class="num">{{ summary.submittedCount }}</div><div class="label">已交卷</div></div>
          <div class="stat"><div class="num">{{ summary.gradedCount }}</div><div class="label">已判分</div></div>
          <div class="stat"><div class="num">{{ summary.avgScore ?? '-' }}</div><div class="label">平均分</div></div>
          <div class="stat"><div class="num">{{ summary.maxScore ?? '-' }}</div><div class="label">最高分</div></div>
          <div class="stat"><div class="num">{{ summary.minScore ?? '-' }}</div><div class="label">最低分</div></div>
          <div class="stat"><div class="num">{{ summary.passRate != null ? summary.passRate + '%' : '-' }}</div><div class="label">及格率</div></div>
        </div>
      </el-card>

      <div class="columns">
        <el-card class="column">
          <div class="col-title">成绩排名</div>
          <el-table :data="ranking" border max-height="480">
            <el-table-column prop="rank" label="排名" width="70" />
            <el-table-column prop="studentName" label="姓名" width="100" />
            <el-table-column prop="className" label="班级" width="140" />
            <el-table-column prop="objectiveScore" label="客观" width="80" />
            <el-table-column prop="subjectiveScore" label="主观" width="80" />
            <el-table-column prop="totalScore" label="总分" width="80" />
          </el-table>
        </el-card>

        <el-card class="column">
          <div class="col-title">班级对比</div>
          <el-table :data="classCompare" border max-height="480">
            <el-table-column prop="className" label="班级" />
            <el-table-column prop="studentCount" label="人数" width="90" />
            <el-table-column prop="avgScore" label="平均分" width="100" />
          </el-table>
        </el-card>
      </div>
    </template>

    <el-empty v-else description="请选择一场考试查看统计" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { pageExams } from '@/api/exam'
import { examSummary, examRanking, examClassCompare, exportScore } from '@/api/stats'
import { downloadBlob } from '@/utils/download'

const exams = ref([])
const examId = ref(null)
const summary = ref(null)
const ranking = ref([])
const classCompare = ref([])

async function load() {
  if (!examId.value) return
  const [s, r, c] = await Promise.all([
    examSummary(examId.value),
    examRanking(examId.value),
    examClassCompare(examId.value)
  ])
  summary.value = s.data
  ranking.value = r.data
  classCompare.value = c.data
}

async function handleExport() {
  const res = await exportScore(examId.value)
  downloadBlob(res, '成绩表.xlsx')
}

onMounted(async () => {
  const e = await pageExams({ pageNum: 1, pageSize: 100 })
  exams.value = e.data.records
})
</script>

<style scoped>
.selector {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
  align-items: center;
}
.stats {
  margin-bottom: 16px;
}
.stat-cards {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}
.stat {
  flex: 1;
  min-width: 100px;
  text-align: center;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}
.stat .num {
  font-size: 26px;
  font-weight: 700;
  color: #409eff;
}
.stat .label {
  color: #909399;
  font-size: 13px;
  margin-top: 4px;
}
.columns {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}
.column {
  flex: 1;
  min-width: 0;
}
.col-title {
  font-weight: 600;
  margin-bottom: 12px;
}
</style>
