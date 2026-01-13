import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../store/auth'
import Login from '../views/Login.vue'
import Dashboard from '../views/Dashboard.vue'
import Orders from '../views/Orders.vue'
import OrderDetail from '../views/OrderDetail.vue'
import Users from '../views/Users.vue'
import Stats from '../views/Stats.vue'
import Layout from '../views/Layout.vue'

const routes = [
  { path: '/login', name: 'login', component: Login },
  {
    path: '/',
    component: Layout,
    children: [
      { path: '', name: 'dashboard', component: Dashboard },
      { path: 'orders', name: 'orders', component: Orders },
      { path: 'orders/:id', name: 'order-detail', component: OrderDetail, props: true },
      { path: 'users', name: 'users', component: Users },
      { path: 'stats', name: 'stats', component: Stats }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  if (to.name !== 'login' && !auth.isAuthenticated) {
    next({ name: 'login' })
    return
  }
  next()
})

export default router
