<template>
  <div class="template-list">
    <h2>📋 Saved Templates</h2>
    <div v-if="loading">Loading...</div>
    <div v-else-if="templates.length === 0" class="empty">
      No templates yet. Go to Parent Edit to create one.
    </div>
    <div class="template-grid" v-else>
      <div v-for="tpl in templates" :key="tpl.id" class="template-card">
        <h3>{{ tpl.name }}</h3>
        <div class="steps-preview">
          <span
            v-for="step in tpl.steps.slice(0, 4)"
            :key="step.stepId"
            class="preview-icon"
            >{{ step.icon }}</span
          >
          <span v-if="tpl.steps.length > 4">+{{ tpl.steps.length - 4 }}</span>
        </div>
        <div class="actions">
          <button class="use-btn" @click="useTemplate(tpl)">Use Today</button>
          <button class="edit-btn" @click="editTemplate(tpl)">Edit</button>
          <button class="delete-btn" @click="deleteTemplate(tpl.id)">
            Delete
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";

const emit = defineEmits(["load-template", "refresh"]);

const templates = ref([]);
const loading = ref(false);

async function fetchTemplates() {
  loading.value = true;
  try {
    const res = await fetch("/api/schedule-templates?family_id=family_demo");
    templates.value = await res.json();
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
}
async function deleteTemplate(id) {
  if (!confirm("Delete this template permanently?")) return;
  await fetch(`/api/schedule-templates/${id}`, { method: "DELETE" });
  fetchTemplates();
  emit("refresh");
}
function useTemplate(tpl) {
  emit("load-template", tpl);
}
function editTemplate(tpl) {
  alert(
    "Edit not yet implemented. You can load template into Parent Builder manually.",
  );
  // In a full app, you would switch to parent mode and load steps
}
onMounted(fetchTemplates);
</script>

<style scoped>
.template-list {
  max-width: 1000px;
  margin: 0 auto;
}
.template-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-top: 20px;
}
.template-card {
  background: white;
  border-radius: 32px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
.template-card h3 {
  margin: 0 0 12px 0;
  font-size: 1.2rem;
}
.steps-preview {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}
.preview-icon {
  font-size: 1.5rem;
  background: #f1f5f9;
  padding: 4px 8px;
  border-radius: 40px;
}
.actions {
  display: flex;
  gap: 8px;
}
.actions button {
  flex: 1;
  padding: 8px 12px;
  border: none;
  border-radius: 40px;
  font-weight: 600;
  cursor: pointer;
}
.use-btn {
  background: #3b82f6;
  color: white;
}
.edit-btn {
  background: #f1f5f9;
}
.delete-btn {
  background: #fee2e2;
  color: #b91c1c;
}
.empty {
  text-align: center;
  padding: 48px;
  background: white;
  border-radius: 48px;
}
</style>
