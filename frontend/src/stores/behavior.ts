import { ref } from 'vue'
import { defineStore } from 'pinia'
import { api } from '../api/client'

export interface BehaviorEvent {
  id: number
  parentId: number
  childId: number
  intensity: number
  triggerTags: string[]
  occurredAt: string
}

export interface WeeklyReport {
  status: 'success' | 'insufficient'
  message?: string
  weekStartDate: string
  weekEndDate: string
  chartData: Array<{ date: string; avg_emotion: number }>
  top3Triggers: string[]
}

export const TRIGGER_TAGS = [
  'loud_noise',
  'transitions',
  'hunger',
  'fatigue',
  'crowd',
  'new_place',
  'denied_request',
  'sensory_overload',
] as const

export const useBehaviorStore = defineStore('behavior', () => {
  const events = ref<BehaviorEvent[]>([])
  const report = ref<WeeklyReport | null>(null)
  const alerts = ref<string[]>([])

  async function logEvent(intensity: number, triggerTags: string[], occurredAt?: string) {
    const body: Record<string, unknown> = { intensity, triggerTags }
    if (occurredAt) body.occurredAt = occurredAt
    const created = await api<BehaviorEvent>('/api/behavior-events', {
      method: 'POST',
      body: JSON.stringify(body),
    })
    events.value.unshift(created)
    return created
  }

  async function fetchEvents(limit = 50) {
    events.value = await api<BehaviorEvent[]>(`/api/behavior-events?limit=${limit}`)
  }

  async function fetchWeeklyReport(date?: string) {
    const q = date ? `?date=${date}` : ''
    report.value = await api<WeeklyReport>(`/api/reports/weekly${q}`)
    return report.value
  }

  async function createShareLink(date?: string) {
    return await api<{ token: string; expiresAt: string }>('/api/reports/share', {
      method: 'POST',
      body: JSON.stringify(date ? { date } : {}),
    })
  }

  async function fetchAlerts() {
    const res = await api<{ alerts: string[] }>('/api/alerts')
    alerts.value = res.alerts
    return res.alerts
  }

  async function dismissAlert(triggerTag: string) {
    await api('/api/alerts/dismiss', {
      method: 'POST',
      body: JSON.stringify({ triggerTag }),
    })
    alerts.value = alerts.value.filter(a => a !== triggerTag)
  }

  return {
    events, report, alerts,
    logEvent, fetchEvents, fetchWeeklyReport, createShareLink,
    fetchAlerts, dismissAlert,
  }
})
