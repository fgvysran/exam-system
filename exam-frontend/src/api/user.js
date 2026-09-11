import request from './request'

export function pageStudents(params) {
  return request.get('/users', { params })
}

export function addStudent(data) {
  return request.post('/users', data)
}

export function updateStudent(id, data) {
  return request.put(`/users/${id}`, data)
}

export function deleteStudent(id) {
  return request.delete(`/users/${id}`)
}
