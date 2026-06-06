<template>
  <div class="modal-overlay" @click.self="close">
    <div class="modal-card">
      <h2>🎉 Amazing! 🎉</h2>
      <p>You completed every step!</p>
      <p class="congrats">🌟 Great job, superstar! 🌟</p>
      <button @click="close">Close</button>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from "vue";
const emit = defineEmits(["close"]);

onMounted(() => {
  // Simple confetti effect (canvas-confetti is optional, but we simulate with basic)
  if (typeof window.canvasConfetti === "function") {
    window.canvasConfetti({
      particleCount: 150,
      spread: 70,
      origin: { y: 0.6 },
    });
  } else {
    // Fallback: just show modal
    console.log("Add canvas-confetti for extra fun");
  }
});
function close() {
  emit("close");
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}
.modal-card {
  background: white;
  border-radius: 64px;
  padding: 32px 48px;
  text-align: center;
  animation: bounce 0.3s ease;
}
.modal-card h2 {
  font-size: 2.5rem;
  color: #f59e0b;
  margin-bottom: 16px;
}
.congrats {
  font-size: 1.2rem;
  margin: 16px 0;
  color: #16a34a;
}
button {
  background: #3b82f6;
  border: none;
  padding: 12px 32px;
  border-radius: 60px;
  color: white;
  font-weight: bold;
  cursor: pointer;
  margin-top: 16px;
}
@keyframes bounce {
  0% {
    transform: scale(0.8);
    opacity: 0;
  }
  80% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
