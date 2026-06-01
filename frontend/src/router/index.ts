import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue'),
    },
    {
      path: '/child/pecs',
      name: 'child-pecs',
      component: () => import('../views/child/pecs/ChildPecsView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/child/schedule',
      name: 'child-schedule',
      component: () => import('../views/child/schedule/ChildScheduleView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/child/diary',
      name: 'child-diary',
      component: () => import('../views/child/diary/ChildDiaryView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/parent/pecs',
      name: 'parent-pecs',
      component: () => import('../views/parent/pecs/ParentPecsView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/parent/schedule',
      name: 'parent-schedule',
      component: () => import('../views/parent/schedule/ParentScheduleView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/parent/behavior',
      name: 'parent-behavior',
      component: () => import('../views/parent/behavior/ParentBehaviorView.vue'),
      meta: { requiresAuth: true },
    },
  ],
})

router.beforeEach((to, _from, next) => {
  if (to.meta.requiresAuth && !localStorage.getItem('visitalk_token')) {
    return next('/')
  }
  next()
})

export default router
