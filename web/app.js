/**
 * CC Web Trial — app entry point.
 *
 * Imports the CC client boundary and wires up the trial UI.
 * Currently uses `previewTransport` for the demo; swap to a real
 * transport when the CC Agent Runtime backend is available.
 */

import { createTask, previewTransport } from './cc-client.js';

const runBtn = document.querySelector('#run');
const taskInput = document.querySelector('#task');
const timeline = document.querySelector('#timeline');
const result = document.querySelector('#result');
const sessionBar = document.querySelector('#session-bar');
const sessionIdEl = document.querySelector('#session-id');

let currentTransport = null;

function renderStep(event) {
  const { state, step } = event;
  const existing = timeline.querySelector(`[data-step-id="${step.id}"]`);
  if (existing) {
    existing.classList.toggle('done', step.status === 'COMPLETED');
    return;
  }

  const el = document.createElement('div');
  el.className = 'step';
  el.dataset.stepId = step.id;
  if (step.status === 'COMPLETED') el.classList.add('done');

  el.innerHTML = `
    <span class="dot"></span>
    <div>
      <strong>${state}</strong>
      <small>${step.output}</small>
    </div>
  `;
  timeline.appendChild(el);
}

function showResult(task) {
  result.hidden = false;
  result.innerHTML = `
    <strong>任務完成</strong><br>
    「${task.input}」已完成試用流程。<br>
    目前這個 Web Trial 是安全的產品體驗原型；真實模型與工具執行層會在後續接入 CC Agent Runtime。
  `;
}

function resetUI() {
  timeline.hidden = true;
  timeline.innerHTML = '';
  result.hidden = true;
  sessionBar.hidden = true;
}

function runTask() {
  const input = taskInput.value.trim();
  if (!input) {
    taskInput.focus();
    return;
  }

  resetUI();
  runBtn.disabled = true;

  const task = createTask(input);
  timeline.hidden = false;
  sessionBar.hidden = false;
  sessionIdEl.textContent = task.sessionId.slice(0, 16) + '…';

  currentTransport = previewTransport(task, (event) => {
    renderStep(event);

    if (event.state === 'VERIFYING' && event.step.status === 'COMPLETED') {
      showResult(task);
      runBtn.disabled = false;
    }
  });
}

runBtn.addEventListener('click', runTask);

taskInput.addEventListener('keydown', (e) => {
  if (e.key === 'Enter' && (e.metaKey || e.ctrlKey)) {
    runTask();
  }
});

/* ── Service Worker Registration ── */
if ('serviceWorker' in navigator) {
  navigator.serviceWorker.register('./sw.js').catch(() => {
    /* SW registration is best-effort; trial works without it */
  });
}
