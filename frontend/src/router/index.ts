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
    {
      path: '/child/chat',
      name: 'child-chat',
      component: () => import('../views/child/chat/ChildChatView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/parent/chat',
      name: 'parent-chat',
      component: () => import('../views/parent/chat/ParentChatView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/parent/report',
      name: 'parent-report',
      component: () => import('../views/parent/behavior/ParentReportView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/parent/settings',
      name: 'parent-settings',
      component: () => import('../views/parent/settings/ParentSettingsView.vue'),
      meta: { requiresAuth: true },
    },
    {
      // C-4: anonymous public share page (no requiresAuth).
      path: '/share/reports/:token',
      name: 'public-share',
      component: () => import('../views/PublicShareView.vue'),
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
