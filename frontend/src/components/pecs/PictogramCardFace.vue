<script setup lang="ts">
import { isRemoteImage, cardDisplaySymbol, type PictogramCardDto } from '../../api/pecs'

defineProps<{
  card: PictogramCardDto
  /** Screen-reader label; child UI stays picture-only */
  ariaLabel?: string
}>()
</script>

<template>
  <div class="flex h-full w-full items-center justify-center" :aria-label="ariaLabel">
    <img
      v-if="isRemoteImage(card.imageUrl)"
      :src="card.imageUrl!"
      alt=""
      class="h-20 w-20 object-contain"
    />
    <span v-else class="text-5xl" aria-hidden="true">{{ cardDisplaySymbol(card.imageUrl) }}</span>
  </div>
</template>
