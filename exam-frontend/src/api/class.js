import request from './request'

export function listClasses() {
  return request.get('/classes')
}

export function addClass(data) {
  return request.post('/classes', data)
}

export function updateClass(id, data) {
  return request.put(`/classes/${id}`, data)
}

export function deleteClass(id) {
  return request.delete(`/classes/${id}`)
}
