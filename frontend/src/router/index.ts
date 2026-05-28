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
    },
    {
      path: '/child/schedule',
      name: 'child-schedule',
      component: () => import('../views/child/schedule/ChildScheduleView.vue'),
    },
    {
      path: '/child/diary',
      name: 'child-diary',
      component: () => import('../views/child/diary/ChildDiaryView.vue'),
    },
    {
      path: '/parent/pecs',
      name: 'parent-pecs',
      component: () => import('../views/parent/pecs/ParentPecsView.vue'),
    },
    {
      path: '/parent/schedule',
      name: 'parent-schedule',
      component: () => import('../views/parent/schedule/ParentScheduleView.vue'),
    },
    {
      path: '/parent/behavior',
      name: 'parent-behavior',
      component: () => import('../views/parent/behavior/ParentBehaviorView.vue'),
    },
  ],
})

export default router
