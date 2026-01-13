import http from './http'

export async function fetchCounters() {
  const resp = await http.get('/stats/counters')
  return resp.data.data
}

export async function fetchRank(limit = 10) {
  const resp = await http.get('/stats/rank', { params: { limit } })
  return resp.data.data
}
