<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { isRemoteImage, type PictogramCardDto } from '../../api/pecs'

defineProps<{
  card: PictogramCardDto
}>()

const { t } = useI18n()
</script>

<template>
  <div
    class="flex h-full w-full items-center justify-center"
    :aria-label="card.labelI18n ? t(card.labelI18n) : undefined"
  >
    <img
      v-if="isRemoteImage(card.imageUrl)"
      :src="card.imageUrl!"
      alt=""
      class="h-20 w-20 object-contain"
    />
    <span v-else class="text-5xl" aria-hidden="true">{{ card.imageUrl }}</span>
  </div>
</template>
