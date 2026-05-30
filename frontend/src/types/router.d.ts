import 'vue-router'

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    role?: 'user' | 'admin'
  }
}

export {}
