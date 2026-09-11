import request from './request'

export function pagePapers(params) {
  return request.get('/papers', { params })
}

export function getPaper(id) {
  return request.get(`/papers/${id}`)
}

export function addPaper(data) {
  return request.post('/papers', data)
}

export function updatePaper(id, data) {
  return request.put(`/papers/${id}`, data)
}

export function deletePaper(id) {
  return request.delete(`/papers/${id}`)
}

export function updatePaperStatus(id, status) {
  return request.patch(`/papers/${id}/status`, { status })
}

export function savePaperQuestions(id, questions) {
  return request.put(`/papers/${id}/questions`, questions)
}
