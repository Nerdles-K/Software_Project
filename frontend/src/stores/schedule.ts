import { ref } from 'vue'
import { defineStore } from 'pinia'
import { api } from '../api/client'
import type { Card } from './cards'

export interface ScheduleTemplate {
  id: number
  familyId: string
  name: string
  steps: number[]
  createdAt: string | null
}

export interface ScheduleInstance {
  id: number
  templateId: number
  date: string
  completedStepIndices: number[]
}

export interface ScheduleTodayView {
  template: ScheduleTemplate
  instance: ScheduleInstance
  cards: Card[]
}

export const MAX_STEPS = 10

export const useScheduleStore = defineStore('schedule', () => {
  const templates = ref<ScheduleTemplate[]>([])
  const todayView = ref<ScheduleTodayView | null>(null)

  async function fetchTemplates() {
    templates.value = await api<ScheduleTemplate[]>('/api/schedules/templates')
  }

  async function createTemplate(name: string, steps: number[]) {
    const t = await api<ScheduleTemplate>('/api/schedules/templates', {
      method: 'POST',
      body: JSON.stringify({ name, steps }),
    })
    templates.value.unshift(t)
    return t
  }

  async function updateTemplate(id: number, name: string, steps: number[]) {
    const t = await api<ScheduleTemplate>(`/api/schedules/templates/${id}`, {
      method: 'PUT',
      body: JSON.stringify({ name, steps }),
    })
    const i = templates.value.findIndex(x => x.id === id)
    if (i >= 0) templates.value[i] = t
    return t
  }

  async function deleteTemplate(id: number) {
    await api(`/api/schedules/templates/${id}`, { method: 'DELETE' })
    templates.value = templates.value.filter(t => t.id !== id)
  }

  async function fetchToday(templateId: number) {
    todayView.value = await api<ScheduleTodayView>(`/api/schedules/today?templateId=${templateId}`)
    return todayView.value
  }

  async function toggleStep(instanceId: number, stepIndex: number, completed: boolean) {
    const inst = await api<ScheduleInstance>(`/api/schedules/instances/${instanceId}/step`, {
      method: 'PUT',
      body: JSON.stringify({ stepIndex, completed }),
    })
    if (todayView.value) todayView.value.instance = inst
    return inst
  }

  return {
    templates, todayView, MAX_STEPS,
    fetchTemplates, createTemplate, updateTemplate, deleteTemplate,
    fetchToday, toggleStep,
  }
})
