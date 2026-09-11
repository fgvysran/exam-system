import request from './request'

export function pageExams(params) {
  return request.get('/exams', { params })
}

export function getExam(id) {
  return request.get(`/exams/${id}`)
}

export function addExam(data) {
  return request.post('/exams', data)
}

export function updateExam(id, data) {
  return request.put(`/exams/${id}`, data)
}

export function deleteExam(id) {
  return request.delete(`/exams/${id}`)
}
