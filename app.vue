<template>
  <div class="app">
    <div class="mode-bar">
      <button :class="{ active: mode === 'parent' }" @click="mode = 'parent'">
        👨‍👩 Parent Edit
      </button>
      <button :class="{ active: mode === 'child' }" @click="mode = 'child'">
        🧒 Child View
      </button>
      <button
        :class="{ active: mode === 'templates' }"
        @click="mode = 'templates'"
      >
        📋 Templates
      </button>
    </div>
    <ParentScheduleBuilder v-if="mode === 'parent'" @save="refreshTemplates" />
    <ChildScheduleView
      v-if="mode === 'child'"
      :instance-id="currentInstanceId"
      @complete="onStepComplete"
    />
    <TemplateList
      v-if="mode === 'templates'"
      @load-template="loadTemplate"
      @refresh="refreshTemplates"
    />
  </div>
</template>

<script setup>
import { ref } from "vue";
import ParentScheduleBuilder from "./components/ParentScheduleBuilder.vue";
import ChildScheduleView from "./components/ChildScheduleView.vue";
import TemplateList from "./components/TemplateList.vue";

const mode = ref("parent");
const currentInstanceId = ref(null);

function refreshTemplates() {
  // No need to do anything, child components will refetch when shown
}
function loadTemplate(template) {
  // Create a new instance for today
  fetch("/api/schedule-instances", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "X-Family-Id": "family_demo",
    },
    body: JSON.stringify({
      template_id: template.id,
      date: new Date().toISOString().slice(0, 10),
    }),
  })
    .then((res) => res.json())
    .then((data) => {
      currentInstanceId.value = data.id;
      mode.value = "child";
    });
}
function onStepComplete() {
  // Optionally refresh after completion
}
</script>

<style scoped>
.mode-bar {
  display: flex;
  gap: 12px;
  justify-content: center;
  background: white;
  padding: 8px;
  border-radius: 60px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
.mode-bar button {
  background: #f1f5f9;
  border: none;
  padding: 10px 24px;
  border-radius: 40px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
}
.mode-bar button.active {
  background: #3b82f6;
  color: white;
}
</style>
