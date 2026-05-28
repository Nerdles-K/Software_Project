<script setup lang="ts">
import { useAuthStore } from './stores/auth'

const auth = useAuthStore()
</script>

<template>
  <div :class="auth.mode === 'child' ? 'child-mode' : 'parent-mode'">
    <router-view />
  </div>
</template>
