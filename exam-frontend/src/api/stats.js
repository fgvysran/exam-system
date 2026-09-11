import request from './request'

export function examSummary(examId) {
  return request.get(`/stats/exam/${examId}`)
}

export function examRanking(examId) {
  return request.get(`/stats/exam/${examId}/ranking`)
}

export function examClassCompare(examId) {
  return request.get(`/stats/exam/${examId}/class-compare`)
}

export function exportScore(examId) {
  return request.get(`/stats/exam/${examId}/export`, { responseType: 'blob' })
}
