<script setup lang="ts">
import { onMounted, onUnmounted, nextTick, useTemplateRef } from 'vue'
import { useRouter } from 'vue-router'
import { useSentenceStore } from '../../../stores/sentences'
import { useCardStore } from '../../../stores/cards'
import ChatComposer from '../../../components/ChatComposer.vue'
import MessageBubble from '../../../components/MessageBubble.vue'

const sentences = useSentenceStore()
const cards = useCardStore()
const router = useRouter()
const scrollRef = useTemplateRef<HTMLElement>('scrollRef')
let pollTimer: ReturnType<typeof setInterval> | null = null

async function refresh() {
  const fresh = await sentences.pollNew()
  if (fresh.length > 0) scrollToBottom()
}

function scrollToBottom() {
  nextTick(() => {
    if (scrollRef.value) scrollRef.value.scrollTop = scrollRef.value.scrollHeight
  })
}

onMounted(async () => {
  await Promise.all([
    cards.cards.length === 0 ? cards.fetchCards() : Promise.resolve(),
    sentences.fetchAll(),
  ])
  scrollToBottom()
  // Poll for parent replies every 3 s while this view is mounted.
  pollTimer = setInterval(refresh, 3000)
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})

async function onSend(cardIds: number[]) {
  await sentences.send(cardIds)
  scrollToBottom()
}
</script>

<template>
  <div class="min-h-screen bg-amber-50 flex flex-col">
    <header class="flex items-center justify-between px-4 py-3 bg-white shadow-sm sticky top-0 z-10">
      <button @click="router.back()" aria-label="Back"
        class="w-12 h-12 rounded-2xl bg-amber-50 flex items-center justify-center text-2xl">
        ←
      </button>
      <h1 class="text-xl font-bold text-gray-800">Talk to family</h1>
      <div class="w-12"></div>
    </header>

    <!-- Conversation feed -->
    <main ref="scrollRef" class="flex-1 overflow-y-auto px-3 py-3 space-y-2">
      <div v-if="sentences.loading && sentences.messages.length === 0"
        class="text-gray-400 text-center py-10">Loading…</div>
      <div v-else-if="sentences.messages.length === 0"
        class="text-gray-400 text-center py-10">
        <p class="text-5xl mb-2">💬</p>
        <p>Make a sentence below and send it!</p>
      </div>
      <MessageBubble v-for="m in sentences.messages" :key="m.id" :message="m" self-role="child" />
    </main>

    <!-- Composer (child = light variant) -->
    <div class="p-3 bg-white border-t border-amber-200">
      <ChatComposer placeholder="Pick cards and tap Send to talk to mom or dad" @send="onSend" />
    </div>
  </div>
</template>
