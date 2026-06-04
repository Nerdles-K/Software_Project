<script setup lang="ts">
import { VueDraggable } from 'vue-draggable-plus'
import { useI18n } from 'vue-i18n'
import type { SentenceCard } from '../../api/pecs'
import PictogramCardFace from './PictogramCardFace.vue'

const sentenceCards = defineModel<SentenceCard[]>({ required: true })

const props = defineProps<{
  pulseInstanceId: string | null
}>()

const emit = defineEmits<{
  cardAdded: []
}>()

const { t } = useI18n()

const dragGroup = { name: 'pecs', pull: false, put: true }

function onAdd() {
  emit('cardAdded')
}
</script>

<template>
  <section
    class="mb-6 rounded-3xl bg-white/90 p-4 shadow-sm ring-2 ring-amber-200"
    :aria-label="t('child.pecs.sentence_bar')"
  >
    <VueDraggable
      v-model="sentenceCards"
      :group="dragGroup"
      item-key="instanceId"
      class="flex min-h-[120px] flex-wrap items-center gap-3"
      :animation="200"
      :delay="150"
      :delay-on-touch-only="true"
      @add="onAdd"
    >
      <div
        v-for="item in sentenceCards"
        :key="item.instanceId"
        class="flex min-h-[100px] min-w-[100px] cursor-grab items-center justify-center rounded-2xl bg-amber-50 shadow-sm ring-2 ring-black/10 transition-transform duration-200 active:cursor-grabbing"
        :class="{ 'scale-105 ring-amber-400': props.pulseInstanceId === item.instanceId }"
      >
        <PictogramCardFace :card="item.card" />
      </div>
    </VueDraggable>
  </section>
</template>
