import { ref } from 'vue'
import { defineStore } from 'pinia'
import { api } from '../api/client'

export interface DiaryEntry {
  id: number
  childId: number
  emotionCardId: number
  doodleUrl: string | null
  createdAt: string
}

export interface FamilySettings {
  familyId: string
  diaryFeatureEnabled: boolean
}

export const EMOTION_CARDS = [
  { id: 1, key: 'happy', emoji: '😊', color: 'bg-yellow-200' },
  { id: 2, key: 'sad', emoji: '😢', color: 'bg-blue-200' },
  { id: 3, key: 'angry', emoji: '😠', color: 'bg-red-200' },
  { id: 4, key: 'scared', emoji: '😨', color: 'bg-purple-200' },
  { id: 5, key: 'calm', emoji: '😌', color: 'bg-green-200' },
] as const

export const useDiaryStore = defineStore('diary', () => {
  const entries = ref<DiaryEntry[]>([])
  const settings = ref<FamilySettings | null>(null)
  const todayWritten = ref<{ enabled: boolean; writtenToday: boolean } | null>(null)

  async function fetchSettings() {
    settings.value = await api<FamilySettings>('/api/family-settings')
    return settings.value
  }

  async function setDiaryEnabled(enabled: boolean) {
    settings.value = await api<FamilySettings>('/api/family-settings/diary-enabled', {
      method: 'PUT',
      body: JSON.stringify({ enabled }),
    })
    return settings.value
  }

  async function createEntry(emotionCardId: number, doodleUrl: string | null) {
    const e = await api<DiaryEntry>('/api/diary-entries', {
      method: 'POST',
      body: JSON.stringify({ emotionCardId, doodleUrl }),
    })
    entries.value.unshift(e)
    return e
  }

  async function fetchOwnEntries() {
    entries.value = await api<DiaryEntry[]>('/api/diary-entries')
    return entries.value
  }

  async function fetchTodayStatus() {
    todayWritten.value = await api<{ enabled: boolean; writtenToday: boolean }>(
      '/api/diary-entries/check-today'
    )
    return todayWritten.value
  }

  return {
    entries, settings, todayWritten,
    fetchSettings, setDiaryEnabled,
    createEntry, fetchOwnEntries, fetchTodayStatus,
  }
})
