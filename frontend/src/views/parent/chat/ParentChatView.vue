<script setup lang="ts">
import { onMounted, onUnmounted, nextTick, useTemplateRef } from 'vue'
import { useSentenceStore } from '../../../stores/sentences'
import { useCardStore } from '../../../stores/cards'
import ChatComposer from '../../../components/ChatComposer.vue'
import MessageBubble from '../../../components/MessageBubble.vue'
import ParentNav from '../../../components/ParentNav.vue'

const sentences = useSentenceStore()
const cards = useCardStore()
const scrollRef = useTemplateRef<HTMLElement>('scrollRef')
let pollTimer: ReturnType<typeof setInterval> | null = null

function scrollToBottom() {
  nextTick(() => {
    if (scrollRef.value) scrollRef.value.scrollTop = scrollRef.value.scrollHeight
  })
}

async function refresh() {
  const fresh = await sentences.pollNew()
  if (fresh.length > 0) scrollToBottom()
}

onMounted(async () => {
  // Always fetch the family-wide card set so MessageBubble can resolve cards
  // from any category, including ones added by the parent moments ago.
  await Promise.all([
    cards.fetchAllCards(),
    sentences.fetchAll(),
  ])
  scrollToBottom()
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
  <ParentNav />
  <div class="min-h-screen bg-slate-900 text-slate-100 flex flex-col">
    <header class="px-4 sm:px-8 py-4 border-b border-slate-800">
      <h1 class="text-2xl font-bold">Family conversation</h1>
      <p class="text-xs text-slate-400 mt-1">
        Reply to your child using the same card library. Polled every 3 s.
      </p>
    </header>

    <main ref="scrollRef" class="flex-1 overflow-y-auto px-3 sm:px-8 py-4 space-y-2">
      <div v-if="sentences.loading && sentences.messages.length === 0"
        class="text-slate-500 text-center py-10">Loading…</div>
      <div v-else-if="sentences.messages.length === 0"
        class="text-slate-500 text-center py-10">
        <p class="text-5xl mb-2">💬</p>
        <p>No messages yet. Wait for your child or send the first card.</p>
      </div>
      <MessageBubble v-for="m in sentences.messages" :key="m.id" :message="m" self-role="parent" />
    </main>

    <div class="p-3 sm:px-8 bg-slate-900 border-t border-slate-800">
      <ChatComposer dark placeholder="Pick cards to reply your child" @send="onSend" />
    </div>
  </div>
</template>
