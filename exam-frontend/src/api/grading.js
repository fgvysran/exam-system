import request from './request'

export function pendingGrading(params) {
  return request.get('/grading/pending', { params })
}

export function gradeAnswer(detailId, data) {
  return request.post(`/grading/${detailId}`, data)
}

export function regradeAnswer(examId) {
  return request.post('/grading/regrade', null, { params: { examId } })
}
