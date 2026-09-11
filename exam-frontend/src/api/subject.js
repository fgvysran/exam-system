import request from './request'

export function listSubjects() {
  return request.get('/subjects')
}

export function addSubject(data) {
  return request.post('/subjects', data)
}

export function updateSubject(id, data) {
  return request.put(`/subjects/${id}`, data)
}

export function deleteSubject(id) {
  return request.delete(`/subjects/${id}`)
}
