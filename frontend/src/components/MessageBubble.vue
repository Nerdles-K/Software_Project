<script setup lang="ts">
import { computed } from 'vue'
import { useCardStore, cardIcon, isPhotoCard, isTextCard, cardText } from '../stores/cards'
import { useSentenceStore, type Sentence } from '../stores/sentences'
import { assetUrl } from '../api/client'

const props = defineProps<{
  message: Sentence
  selfRole: 'child' | 'parent'
}>()

const cards = useCardStore()
const sentences = useSentenceStore()
void cards

const isSelf = computed(() => props.message.senderRole === props.selfRole)
const expanded = computed(() => sentences.expand(props.message))

const timeStr = computed(() => {
  const t = new Date(props.message.createdAt)
  return isNaN(t.getTime()) ? '' : t.toLocaleString(undefined, {
    hour: '2-digit', minute: '2-digit', month: 'short', day: 'numeric',
  })
})

const senderLabel = computed(() => {
  const role = props.message.senderRole === 'child' ? 'Child' : 'Parent'
  const name = props.message.senderName?.trim()
  // Show the person's name when we have it (tells mum/dad or child A/B apart).
  return name ? `${name} (${role})` : role
})
</script>

<template>
  <div :class="isSelf ? 'justify-end' : 'justify-start'" class="flex w-full">
    <div :class="isSelf
      ? 'bg-emerald-100 border-emerald-200 items-end'
      : (message.senderRole === 'child' ? 'bg-orange-100 border-orange-200' : 'bg-blue-100 border-blue-200')"
      class="max-w-[80%] rounded-2xl border px-3 py-2 shadow-sm">
      <div class="text-[10px] uppercase tracking-wide text-gray-500 mb-1 font-semibold">
        {{ senderLabel }} · {{ timeStr }}
      </div>
      <div class="flex flex-wrap items-center gap-2">
        <div v-for="(card, i) in expanded" :key="`${message.id}-${i}`"
          class="flex items-center gap-1.5 bg-white/60 rounded-lg px-2 py-1">
          <img v-if="isPhotoCard(card)" :src="assetUrl(card.imageUrl)" :alt="card.labelI18n"
            class="w-7 h-7 object-cover rounded" />
          <span v-else-if="isTextCard(card)" class="text-sm font-semibold text-gray-800">{{ cardText(card) }}</span>
          <template v-else>
            <span class="text-2xl">{{ cardIcon(card) }}</span>
            <span class="text-sm font-semibold text-gray-800">{{ card.labelI18n }}</span>
          </template>
        </div>
        <span v-if="expanded.length === 0" class="italic text-gray-400 text-xs">
          (cards removed)
        </span>
      </div>
    </div>
  </div>
</template>
