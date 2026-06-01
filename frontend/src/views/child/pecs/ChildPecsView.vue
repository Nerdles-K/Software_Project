<script setup lang="ts">
</script>

<template>
  <div class="min-h-screen bg-amber-50 p-6">
    <h2 class="text-3xl font-bold text-gray-900 mb-4">PECS Builder</h2>
    <div class="grid grid-cols-2 gap-4">
      <div v-for="cat in ['Eat', 'Drink', 'Play', 'Feel']" :key="cat"
        class="bg-white rounded-2xl shadow flex items-center justify-center font-semibold text-gray-800"
        style="min-height: 120px; min-width: 120px;">
        {{ cat }}
      </div>
    </div>
  </div>
</template>
