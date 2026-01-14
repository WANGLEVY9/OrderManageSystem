import http from './http'

export async function listProducts() {
  const resp = await http.get('/products')
  return resp.data.data
}

export async function createProduct(payload) {
  const resp = await http.post('/products', payload)
  return resp.data.data
}
