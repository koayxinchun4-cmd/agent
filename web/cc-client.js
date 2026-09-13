/**
 * CC Web client boundary.
 *
 * The UI talks to this client instead of owning the Agent execution timeline.
 * `previewTransport` is the safe Pages demo transport. A real CC Agent API can
 * be injected later without changing the UI contract.
 */

export const CC_STATES = Object.freeze([
  'CREATED',
  'UNDERSTANDING',
  'PLANNING',
  'EXECUTING',
  'VERIFYING',
  'RETRYING',
  'WAITING_FOR_USER',
  'WAITING_FOR_PERMISSION',
  'WAITING_FOR_TOOL',
  'COMPLETED',
  'FAILED',
]);

export function createTask(input, client = 'web') {
  const now = new Date().toISOString();
  const id = `task_${crypto.randomUUID()}`;
  return {
    id,
    sessionId: `session_${crypto.randomUUID()}`,
    input,
    createdAt: now,
    metadata: { locale: document.documentElement.lang || 'zh-TW', client },
  };
}

export function previewTransport(task, onProgress) {
  const stages = [
    ['UNDERSTANDING', '理解你的目標'],
    ['PLANNING', '拆解任務並選擇策略'],
    ['EXECUTING', '執行可用的 Web 試用流程'],
    ['VERIFYING', '檢查結果並整理回覆'],
  ];

  let sequence = 0;
  const timers = [];

  stages.forEach(([state, label], index) => {
    timers.push(setTimeout(() => {
      sequence += 1;
      onProgress({
        type: 'agent.progress',
        sessionId: task.sessionId,
        sequence,
        state,
        step: {
          id: `step_${index + 1}`,
          index,
          kind: 'agent',
          status: state === 'VERIFYING' ? 'COMPLETED' : 'RUNNING',
          attempt: 1,
          output: label,
          success: state === 'VERIFYING',
        },
      });
    }, 650 + index * 650));
  });

  return {
    cancel() {
      timers.forEach(clearTimeout);
    },
  };
}

/**
 * Real transport adapter shape. The backend implementation can satisfy this
 * contract later; secrets must never be placed in browser code.
 */
export function createApiTransport(baseUrl, fetchImpl = fetch) {
  return {
    async createTask(task) {
      const response = await fetchImpl(`${baseUrl}/v1/tasks`, {
        method: 'POST',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify(task),
      });
      if (!response.ok) throw new Error(`CC API create task failed: ${response.status}`);
      return response.json();
    },
  };
}
