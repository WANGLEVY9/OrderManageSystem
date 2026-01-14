import http from './http'

export async function createOrder(payload) {
  const resp = await http.post('/orders', payload)
  return resp.data.data
}

export async function myOrders(page = 0, size = 10) {
  const resp = await http.get('/orders/mine', { params: { page, size } })
  return resp.data.data
}

export async function allOrders(page = 0, size = 10) {
  const resp = await http.get('/orders', { params: { page, size } })
  return resp.data.data
}

export async function ordersByUser(userId, page = 0, size = 10) {
  const resp = await http.get(`/orders/by-user/${userId}`, { params: { page, size } })
  return resp.data.data
}

export async function updateStatus(id, status) {
  const resp = await http.patch(`/orders/${id}/status`, null, { params: { status } })
  return resp.data.data
}

export async function updateItems(id, payload) {
  const resp = await http.put(`/orders/${id}/items`, payload)
  return resp.data.data
}

export async function deleteOrder(id) {
  const resp = await http.delete(`/orders/${id}`)
  return resp.data.data
}

export async function fetchLogs(id) {
  const resp = await http.get(`/orders/${id}/logs`)
  return resp.data.data
}
