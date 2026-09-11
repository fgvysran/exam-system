import request from './request'

export function pendingGrading(params) {
  return request.get('/grading/pending', { params })
}

export function gradeAnswer(detailId, data) {
  return request.post(`/grading/${detailId}`, data)
}
