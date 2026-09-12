# Nexus AI — Memory System

## 這個文件是什麼？

這份文件定義 Nexus AI 的 Memory System：

- Memory 有哪些種類。
- 每一種 Memory 解決什麼問題。
- Agent Core 應該怎樣使用 Memory。
- Memory 如何保持 local-first、可測試、可擴充。
- 每個主要責任對應哪一個 Kotlin file。

Memory 的目標不是把所有資料混在一起，而是讓不同 context 有清楚的 scope。

---

## 1. Memory System 的基本概念

Nexus AI 把 context 分成四個主要 scope：

```text
Memory System
│
├── Conversation Memory
│   └── 一段對話發生了什麼
│
├── User Preference Memory
│   └── 使用者長期偏好
│
├── Project Memory
│   └── 某個 project 的長期 context
│
└── Task Memory
    └── 某個 task 執行期間或之後的重要 context
```

這四種 Memory 不應該因為目前資料結構相似，就在 Agent Core 裡混成一個沒有 scope 的 storage API。

---

## 2. 為什麼要分開？

不同 Memory 有不同生命週期：

| Memory | Scope | 主要用途 |
|---|---|---|
| Conversation | conversation | 保留對話上下文 |
| User Preference | user | 保留使用者偏好 |
| Project | project | 保留專案 context |
| Task | task | 保留單一任務 context |

例如：

```text
User Preference
→ 使用者偏好 Traditional Chinese

Project Memory
→ Nexus project 使用 Kotlin + Compose

Task Memory
→ task-123 已經完成 repository validation

Conversation Memory
→ 最近幾輪對話內容
```

這樣 Agent 在需要 context 時，可以知道「這個資訊屬於哪個 scope」。

---

## 3. Architecture

Memory System 保持在 Agent Core 可以理解的抽象層，而不是把 Room、Android API 或 UI 細節直接暴露給 Agent。

```text
NexusAgent
    │
    ├── Conversation Memory
    ├── User Preference Memory
    ├── Project Memory
    └── Task Memory
             │
             ▼
        Memory Store
             │
             ▼
        AgentMemoryDao
             │
             ▼
        Room / Local DB
```

目前不同 Memory Store 可以共用 generic `AgentMemory` table。

這是一個 implementation choice，不是永遠固定的 database contract。

如果未來 Memory 的資料量、查詢需求或生命週期變得不同，可以再演進 storage layer，而不需要先改變 Memory 的概念 contract。

---

## 4. Local-first 原則

目前 Memory 採 local-first：

```text
Agent
  ↓
Memory Store
  ↓
Local Database
```

優點：

- 不需要依賴 cloud service 才能使用 Memory。
- Unit test 可以驗證主要行為。
- 使用者 context 可以先留在裝置本地。
- 未來若需要同步，可以在 Store 外增加 synchronization layer。

不要讓目前的 local storage implementation 限制未來的 Memory API。

---

# 5. File Responsibility Map

## Conversation Memory

### ChatDao.kt

**這個 file 是什麼？**

→ Conversation message 的 database access contract。

→ 負責新增、讀取、查詢與清除 conversation messages。

→ 不負責 UI、不負責 model routing，也不負責決定 Agent 要記住什麼。

---

## User Preference Memory

### UserPreference.kt

**這個 file 是什麼？**

→ 定義一筆 User Preference 的資料結構。

→ 只描述「一個使用者偏好長什麼樣」。

主要資料：

```kotlin
data class UserPreference(
    val key: String,
    val value: String,
    val metadata: Map<String, String> = emptyMap(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

### UserPreferenceStore.kt

**這個 file 是什麼？**

→ User Preference 的 local-first storage。

→ 負責 `get / set / remove / list`。

→ 不負責 UI，也不負責決定 preference 的業務意義。

---

## Project Memory

### ProjectMemory.kt

**這個 file 是什麼？**

→ 定義 Project Memory 的資料結構。

→ 只負責描述「一筆 Project Memory 長什麼樣」。

```kotlin
data class ProjectMemory(
    val projectId: String,
    val key: String,
    val value: String,
    val metadata: Map<String, String> = emptyMap(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

### ProjectMemoryStore.kt

**這個 file 是什麼？**

→ Project Memory 的 local-first persistence。

→ 負責依 `projectId + key` 讀寫 Project Memory。

→ 不負責 project UI，也不負責判斷某個 project context 是否「值得記住」。

---

## Task Memory

### TaskMemory.kt

**這個 file 是什麼？**

→ 定義 Task Memory 的資料結構。

→ 只負責描述「一筆 Task Memory 長什麼樣」。

```kotlin
data class TaskMemory(
    val taskId: String,
    val key: String,
    val value: String,
    val metadata: Map<String, String> = emptyMap(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

### TaskMemoryStore.kt

**這個 file 是什麼？**

→ Task Memory 的 local-first persistence。

→ 負責依 `taskId + key` 讀寫 Task Memory。

→ 使用 platform-neutral serialization，避免 Memory logic 直接依賴 Android-only API。

→ 目前使用 generic `AgentMemory` table 作為 storage foundation。

---

## 6. Metadata 為什麼存在？

Memory contract 不應該把未來的 input/output 形式寫死。

因此 Memory 不只保存：

```text
key → value
```

也保留：

```text
metadata: Map<String, String>
```

例如未來可以表達：

```text
source = github
confidence = high
origin = agent
channel = voice
```

這些只是可能的 metadata，不代表現在就要固定所有欄位。

核心原則：

> Safety 與 architecture 定義邊界，但不要把未來的資料形狀寫死。

---

## 7. Memory Data Flow

```text
InputChannel
    ↓
AgentMessage
    ↓
NexusAgent
    ↓
Task / Project / User / Conversation context
    ↓
Memory Store
    ↓
Local Memory
```

讀取 context 時則反向：

```text
Memory Store
    ↓
Scoped Memory
    ↓
Agent Context
    ↓
Planner / Model / Tool / Verification
```

Memory 本身不是 Intelligence。

Memory 提供 context；Agent Core 決定如何使用 context。

---

## 8. Memory 與 Intelligence 的關係

```text
Memory
  ↓
Context
  ↓
Reasoning
  ↓
Planning
  ↓
Tool Calling
  ↓
Verification
  ↓
Recovery
```

因此：

- Memory 增加可用 context。
- Planner 決定下一步做什麼。
- Model Router 決定使用哪個 model。
- Tool Calling 負責實際操作。
- Verification 判斷結果是否符合要求。
- Recovery 在失敗時調整策略。

不要把「有 Memory」等同於「Agent 已經會自主推理」。

---

## 9. File Design Rule

Nexus Memory 遵守：

> **One main responsibility per file.**

這不是「一個 function 一個 file」。

判斷方式：

```text
File
 ↓
有沒有一個清楚的主要責任？
 ↓
這個責任是否有獨立的修改原因？
 ↓
是否容易單獨測試與閱讀？
```

如果答案是 yes，就值得獨立成 file。

如果只是非常小、只服務同一個責任的 private helper，不需要為了拆而拆。

---

## 10. Future Evolution

未來可以在不破壞目前 Memory contract 的情況下增加：

```text
MemoryReader
MemoryWriter
MemorySearch
MemoryRetentionPolicy
MemoryImportance
MemorySource / Provenance
MemorySync
MemoryConflictResolution
```

但只有當這些能力真的有獨立責任時才拆成新的 abstraction/file。

不要提前建立大量空 abstraction。

---

## 11. Current Status

```text
Conversation Memory       ✅
User Preference Memory    ✅
Project Memory            ✅
Task Memory               ✅

Memory Architecture Doc   ✅
Local-first               ✅
Platform-neutral Task Store ✅
Unit-test coverage        ✅
```

下一階段應該開始讓 Agent Core **真正消費 scoped Memory context**，而不只是完成 storage layer。
