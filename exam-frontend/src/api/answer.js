import request from './request'

export function myExams() {
  return request.get('/exams/my')
}

export function startExam(id) {
  return request.post(`/exams/${id}/start`)
}

export function saveAnswers(id, answers) {
  return request.post(`/exams/${id}/answers`, answers)
}

export function submitExam(id, answers) {
  return request.post(`/exams/${id}/submit`, answers)
}

export function getResult(id) {
  return request.get(`/exams/${id}/result`)
}
