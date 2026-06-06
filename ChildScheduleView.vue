<template>
  <div class="child-view">
    <h2>🧸 My Tasks Today</h2>
    <div v-if="loading">Loading...</div>
    <div v-else-if="!instance">
      No active schedule. Please select a template from Templates view.
    </div>
    <div v-else>
      <div
        v-for="(step, idx) in steps"
        :key="step.stepId"
        class="step-card"
        :class="cardClass(idx)"
      >
        <div class="step-num">{{ idx + 1 }}</div>
        <div class="step-icon">{{ step.icon }}</div>
        <div class="step-label">{{ step.label }}</div>
        <div
          v-if="isCurrent(idx)"
          class="big-checkbox"
          @click="completeStep(step.stepId)"
        >
          ✔️
        </div>
        <div v-else-if="isCompleted(idx)" class="big-checkbox completed">
          ✅
        </div>
        <div v-else class="big-checkbox disabled">🔘</div>
      </div>
    </div>
    <CelebrationModal v-if="showCelebration" @close="closeCelebration" />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from "vue";
import CelebrationModal from "./CelebrationModal.vue";

const props = defineProps({ instanceId: String });
const emit = defineEmits(["complete"]);

const instance = ref(null);
const steps = ref([]);
const completedIds = ref([]);
const loading = ref(false);
const showCelebration = ref(false);

async function fetchInstance() {
  if (!props.instanceId) return;
  loading.value = true;
  try {
    const res = await fetch(`/api/schedule-instances/${props.instanceId}`);
    if (!res.ok) throw new Error();
    const data = await res.json();
    instance.value = data;
    steps.value = data.steps;
    completedIds.value = data.completed_step_ids;
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
}
function isCompleted(idx) {
  return completedIds.value.includes(steps.value[idx]?.stepId);
}
function isCurrent(idx) {
  if (isCompleted(idx)) return false;
  const firstIncomplete = steps.value.findIndex(
    (step) => !completedIds.value.includes(step.stepId),
  );
  return firstIncomplete === idx;
}
function cardClass(idx) {
  if (isCompleted(idx)) return "completed";
  if (isCurrent(idx)) return "current";
  return "upcoming";
}
async function completeStep(stepId) {
  if (!props.instanceId) return;
  try {
    await fetch(
      `/api/schedule-instances/${props.instanceId}/steps/${stepId}/complete`,
      {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
      },
    );
    await fetchInstance();
    const allCompleted = steps.value.every((step) =>
      completedIds.value.includes(step.stepId),
    );
    if (allCompleted) {
      showCelebration.value = true;
    }
    emit("complete");
  } catch (err) {
    alert("Failed to mark step as complete");
  }
}
function closeCelebration() {
  showCelebration.value = false;
}
onMounted(fetchInstance);
watch(() => props.instanceId, fetchInstance);
</script>

<style scoped>
.child-view {
  background: #fef9e3;
  border-radius: 48px;
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}
.step-card {
  display: flex;
  align-items: center;
  gap: 20px;
  background: white;
  margin: 16px 0;
  padding: 16px 24px;
  border-radius: 60px;
  transition: all 0.2s ease;
}
.step-card.current {
  background: #fff7ed;
  box-shadow:
    0 0 0 3px #f59e0b,
    0 8px 20px rgba(0, 0, 0, 0.1);
  transform: scale(1.02);
}
.step-card.upcoming {
  opacity: 0.55;
  filter: grayscale(0.2);
}
.step-card.completed {
  opacity: 0.7;
  background: #e2e8f0;
}
.step-num {
  background: #facc15;
  width: 48px;
  height: 48px;
  border-radius: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 1.2rem;
}
.step-icon {
  font-size: 2.2rem;
}
.step-label {
  flex: 1;
  font-weight: 600;
  font-size: 1.1rem;
}
.big-checkbox {
  width: 56px;
  height: 56px;
  background: #f59e0b;
  border-radius: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  cursor: pointer;
  color: white;
  transition: 0.1s;
}
.big-checkbox:hover {
  transform: scale(1.05);
  background: #ea580c;
}
.big-checkbox.completed {
  background: #22c55e;
  cursor: default;
}
.big-checkbox.disabled {
  background: #cbd5e1;
  cursor: not-allowed;
  opacity: 0.6;
}
</style>
