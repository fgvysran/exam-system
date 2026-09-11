import request from './request'

export function getKnowledgePointTree(subjectId) {
  return request.get('/knowledge-points/tree', { params: { subjectId } })
}

export function addKnowledgePoint(data) {
  return request.post('/knowledge-points', data)
}

export function updateKnowledgePoint(id, data) {
  return request.put(`/knowledge-points/${id}`, data)
}

export function deleteKnowledgePoint(id) {
  return request.delete(`/knowledge-points/${id}`)
}
