import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    component: () => import('@/layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'subject',
        name: 'Subject',
        component: () => import('@/views/question/SubjectManage.vue'),
        meta: { title: '学科管理' }
      },
      {
        path: 'knowledge-point',
        name: 'KnowledgePoint',
        component: () => import('@/views/question/KnowledgePointManage.vue'),
        meta: { title: '知识点管理' }
      },
      {
        path: 'question',
        name: 'Question',
        component: () => import('@/views/question/QuestionManage.vue'),
        meta: { title: '题目管理' }
      },
      {
        path: 'paper',
        name: 'Paper',
        component: () => import('@/views/paper/PaperManage.vue'),
        meta: { title: '试卷管理' }
      },
      {
        path: 'paper/:id/compose',
        name: 'PaperCompose',
        component: () => import('@/views/paper/PaperCompose.vue'),
        meta: { title: '组卷' }
      },
      {
        path: 'exam',
        name: 'Exam',
        component: () => import('@/views/exam/ExamManage.vue'),
        meta: { title: '考试管理' }
      },
      {
        path: 'class',
        name: 'Class',
        component: () => import('@/views/system/ClassManage.vue'),
        meta: { title: '班级管理' }
      },
      {
        path: 'student',
        name: 'Student',
        component: () => import('@/views/system/StudentManage.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'my-exam',
        name: 'MyExam',
        component: () => import('@/views/answer/MyExam.vue'),
        meta: { title: '我的考试' }
      },
      {
        path: 'exam/:id/answer',
        name: 'ExamAnswer',
        component: () => import('@/views/answer/ExamAnswer.vue'),
        meta: { title: '在线答题' }
      },
      {
        path: 'exam/:id/result',
        name: 'ExamResult',
        component: () => import('@/views/answer/ExamResult.vue'),
        meta: { title: '考试结果' }
      },
      {
        path: 'grading',
        name: 'Grading',
        component: () => import('@/views/grading/GradingManage.vue'),
        meta: { title: '阅卷' }
      },
      {
        path: 'stats',
        name: 'Stats',
        component: () => import('@/views/stats/StatsManage.vue'),
        meta: { title: '成绩统计' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫: 未登录跳登录页
router.beforeEach((to, from, next) => {
  const token = getToken()
  if (to.path === '/login') {
    next()
  } else if (!token) {
    next('/login')
  } else {
    next()
  }
})

export default router
