<template>
  <el-card>
    <el-table :data="list" border>
      <el-table-column prop="examName" label="考试名称" />
      <el-table-column prop="paperName" label="试卷" width="180" />
      <el-table-column prop="startTime" label="开始时间" width="170" />
      <el-table-column prop="endTime" label="结束时间" width="170" />
      <el-table-column label="考试状态" width="90">
        <template #default="{ row }">
          <el-tag :type="examStatusType(row.examStatus)">{{ examStatusText(row.examStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="我的状态" width="90">
        <template #default="{ row }">{{ recordStatusText(row.recordStatus) }}</template>
      </el-table-column>
      <el-table-column label="成绩" width="80">
        <template #default="{ row }">{{ row.recordStatus >= 2 ? row.totalScore : '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.examStatus === 2 && row.recordStatus < 2" link type="primary" @click="goAnswer(row)">
            {{ row.recordStatus === 1 ? '继续作答' : '开始考试' }}
          </el-button>
          <el-button v-if="row.recordStatus >= 2" link type="primary" @click="goResult(row)">查看成绩</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { myExams } from '@/api/answer'

const router = useRouter()
const list = ref([])

function examStatusText(s) {
  return { 1: '未开始', 2: '进行中', 3: '已结束' }[s] || '-'
}
function examStatusType(s) {
  return { 1: 'info', 2: 'success', 3: 'danger' }[s] || 'info'
}
function recordStatusText(s) {
  return { 0: '未参加', 1: '进行中', 2: '已交卷', 3: '已判分' }[s] || '-'
}

function goAnswer(row) {
  router.push(`/exam/${row.examId}/answer`)
}
function goResult(row) {
  router.push(`/exam/${row.examId}/result`)
}

onMounted(async () => {
  const res = await myExams()
  list.value = res.data
})
</script>
