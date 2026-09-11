import request from './request'

export function pageQuestions(params) {
  return request.get('/questions', { params })
}

export function getQuestion(id) {
  return request.get(`/questions/${id}`)
}

export function addQuestion(data) {
  return request.post('/questions', data)
}

export function updateQuestion(id, data) {
  return request.put(`/questions/${id}`, data)
}

export function deleteQuestion(id) {
  return request.delete(`/questions/${id}`)
}

export function batchDeleteQuestions(ids) {
  return request.delete('/questions/batch', { data: ids })
}

export function updateQuestionStatus(id, status) {
  return request.patch(`/questions/${id}/status`, { status })
}

export function importQuestions(file, subjectId) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('subjectId', subjectId)
  return request.post('/questions/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function exportQuestions(params) {
  return request.get('/questions/export', { params, responseType: 'blob' })
}
