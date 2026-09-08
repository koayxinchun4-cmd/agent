# Nexus Project Report

> 基於 GitHub Compare：`e143a5e297f3ee97d518fafb50527355048b9438...main` 的階段性分析。
>
> 本文件用來記錄 Nexus 在這一輪大改前後的重要功能、歷史功能、架構方向，以及後續整理建議，避免在重構時誤刪已經做過的能力。

## 1. Compare 摘要

比較基準為：

- Base：`e143a5e297f3ee97d518fafb50527355048b9438`
- Main：目前比較結果指向 `6a127080a22326939b5903261b40264921058928`
- Main 相對 Base：**ahead 43 commits，behind 0**
- Base commit：`ci: update Android SDK setup and keystore handling`

這代表目前 `main` 在該基準之後已經累積了大量功能、資料層、UI、CI/CD 與 Nexus 架構相關修改。

## 2. Nexus 最終定位

Nexus 不應只是「手機上的聊天 App」，而應定位為：

> **原生 Android、Jetpack Compose、Mobile-first、免 Root 的全功能 AI Agent。**

核心原則：

1. 手機本身就是 Agent 的主要執行環境。
2. 不依賴電腦才能完成核心工作。
3. 不要求 Root。
4. Gemini 是主要 AI 能力之一，但架構不要綁死單一模型。
5. Skills、Memory、Tools 與 Agent 能力應該可持續擴充。
6. 舊功能應先標記與保留，再決定重構或恢復，不因大改而直接遺失。

## 3. 已經出現過的重要 AI 能力

### 3.1 Gemini

Gemini 是原專案的重要 AI API 基礎，也是目前 Nexus 對話能力的主要方向。

### 3.2 OpenRouter

歷史架構曾加入 OpenRouter，並形成 AI engine 抽象層與 Hybrid AI repository 的方向。

建議未來演進成：

```text
Nexus AI Router
├── Gemini
├── OpenRouter
└── Future providers
```

不要讓 UI 直接依賴某一家模型 API。

### 3.3 Local Autonomous Engine

歷史程式碼曾包含本地 Autonomous Engine / Preset 類型的能力。

這種設計值得保留，因為未來可以作為：

- 無 API Key 時的 fallback
- 本地工具決策
- 預設 Agent persona
- 離線能力

## 4. Agent / Specialist 能力

歷史功能中已經出現專業 Agent 的方向，包括：

### CTO Agent

定位可以包括：

- 系統架構分析
- 技術選型
- 模組設計
- Code Review
- 開發規劃

### Codex / Coding Agent

定位可以包括：

- Coding
- Kotlin / Android / Compose
- Bug 分析
- Code 改進
- GitHub 開發流程

這些能力不應被當成普通聊天 persona，而應逐步升級為真正可執行任務的 Agent。

## 5. Office Agent Studio

歷史版本曾有 Office Agent Studio，並逐步增加 Agent 協同、CI/CD、GitHub 等能力。

建議未來重構為：

> **Nexus Agent Studio / Agent Workspace**

可以承擔：

- 多 Agent 協作
- 任務分派
- Workflow 設計
- GitHub 操作
- Office 任務
- 自訂 Agent

## 6. Skills 系統

歷史程式碼曾朝 GitHub `SKILL.md` 學習 / 提取技能的方向發展。

這是 Nexus 很重要的長期架構。

目標：

```text
GitHub / local Skill
        ↓
     SKILL.md
        ↓
   Skill Parser
        ↓
 Skill Registry
        ↓
     Nexus Agent
```

未來可以讓使用者從 GitHub 發現 Skills，再由 Nexus 管理、啟用或停用。

## 7. Memory

原專案已有 Room、ChatSession、AgentMemory 等資料層方向。

後續建議升級成分層 Memory：

```text
Nexus Memory
├── Conversation Memory
├── User Preferences
├── Important Facts
├── Project Memory
├── Skill Memory
└── Agent Task Memory
```

Memory 應以本地優先為原則，並清楚區分短期上下文與長期記憶。

## 8. GitHub 能力

歷史版本已經出現 GitHub API 與相關操作能力，包括：

- Repository
- Branch
- File content
- Pull Request
- Issues
- Issue comments
- Issue updates
- GitHub user / account
- OAuth / Device Flow 方向

其中歷史 commit `5d80b1a28b9c9898c5126e0c227cef7f2e6028cc` 明確增加了 Issue comment 與 Issue update API，同時擴充了 ChatSession / AgentMemory 資料層。

未來 Nexus GitHub Agent 可以逐步形成：

```text
使用者
  ↓
Nexus Planner
  ↓
GitHub Tool
  ├── Read repository
  ├── Read / edit files
  ├── Issues
  ├── Pull Requests
  ├── Actions
  └── Commit / workflow
```

## 9. GitHub Actions / CI/CD

目前專案已經建立 Android CI/CD 流程，並持續用 GitHub Actions 驗證 Android Build。

CI/CD 應視為 Nexus 的基礎設施，而不是 App 功能本身。

未來可以把：

- Build
- Test
- APK Artifact
- GitHub Release
- GitHub Pages

維持在自動化 pipeline 中。

## 10. WhatsApp 歷史功能

歷史 commit `5d80b1a28b9c9898c5126e0c227cef7f2e6028cc` 曾加入 WhatsApp 相關資料層與 API client，包括發送訊息與 WhatsApp 設定方向。

這項能力後來部分被清理 / 移除，因此目前應標記為：

> **RECOVER / FUTURE**

而不是假設它目前已經完整可用。

如果未來恢復，建議把它做成 Tool / Connector：

```text
Nexus
 ↓
WhatsApp Tool
 ↓
WhatsApp Cloud API
```

而不是把 WhatsApp 邏輯硬塞進聊天 UI。

## 11. Mobile-first 能力規劃

Nexus 與傳統桌面 Agent 最大差異是手機本身就是工具環境。

規劃中的能力包括：

- 📁 File Agent
- 📲 App Agent
- 🌐 Web Research
- 🔔 Notification / system integration
- 🎤 Voice
- 🧠 Memory
- 🧩 Skills

核心要求：

> **免 Root。**

Android 能力應優先使用正式 SDK、Intent、Storage Access Framework、Accessibility（在合法且使用者明確授權的情況下）等機制，而不是依賴 Root 或私人系統 API。

## 12. AI Moments / AI 朋友圈

這兩項屬於 Nexus 的產品體驗層：

### AI Moments

記錄 Agent 執行任務、推理、創作或自動化過程中的重要事件。

### AI 朋友圈

可以作為 Agent activity / social-style feed，用於展示：

- Agent 完成的任務
- Skills 成長
- 有價值的工作紀錄
- 使用者允許公開的內容

這些功能不應阻塞核心 Agent 架構，可以在核心穩定後加入。

## 13. 儲存空間策略

Nexus 未來功能可能很多，因此「App 最終佔用空間可能達到 GB 級」是需要管理的工程問題，但不應因此刪除核心功能。

建議分離：

```text
APK
├── Core application
├── UI
├── Agent runtime
└── 必要 libraries

App data
├── Chat history
├── Memory
├── Skills
├── Downloaded assets
├── AI Moments
└── User files
```

如果未來加入大型本地模型或大量媒體，應讓它們成為可選資料，而不是直接塞入 APK。

## 14. Repository Cleanup 建議

Compare 分析時需要特別檢查是否有 Build / Cache 類檔案被 Git tracking，例如：

```text
.gradle/
**/build/
app/build/
KSP generated/cache outputs
IDE temporary files
```

這類檔案通常可以由 Gradle / Android build 重新產生，不應因為 repository 變大而誤刪真正的 Nexus source code。

### 清理原則

**可以移除：**

- Build output
- Gradle cache
- KSP cache / generated build artifacts（如果它們屬於可再生輸出）
- IDE temporary files

**不要因為空間而直接移除：**

- `app/src/main`
- Agent logic
- Skills logic
- Memory logic
- GitHub integration
- CI/CD configuration
- Documentation
- 重要測試

清理前應先確認 `.gitignore` 與 Git tracking 狀態，再逐步處理，避免一次誤刪真正的 source。

## 15. ChatGPT @Connectors / Apps：Nexus 架構參考

> 本節是根據 ChatGPT 對話介面中輸入 `@` 後出現的 Apps / Connectors 概念整理，**不是說這些服務目前已經存在於 Nexus**。

在 ChatGPT 中，`@` 可以用來選擇特定的 App / Connector，讓對話在需要時使用外部資料來源或工具。截圖中曾看到的例子包括：

- ClinicalTrials.gov
- CMS Coverage
- CMS Open Data
- DailyMed
- Medicare Care Compare
- NPI Registry
- OpenAI Platform
- openFDA
- PubMed
- RxNorm

這些名稱代表不同的外部資料來源 / 服務連接能力。例如 PubMed 可作為醫學文獻資料來源，openFDA 可作為 FDA 公開資料來源；具體可用能力取決於對應 Connector 的實際實作與授權。

### Nexus 不應直接複製 UI，而應吸收架構概念

建議 Nexus 將 `@` 設計成 **Tools / Skills / Connectors 的入口**：

```text
Nexus Chat
     │
     ├── @GitHub
     ├── @Files
     ├── @Web
     ├── @PubMed
     ├── @openFDA
     ├── @Memory
     ├── @Office
     ├── @Codex
     └── @Apps
             │
             ↓
       Tool / Connector Registry
             │
             ↓
          Agent Core
             │
       ┌─────┴─────┐
       ↓           ↓
   Planner       Executor
       │           │
       └─────┬─────┘
             ↓
          Result
```

### 建議的 Nexus Tool Registry

每一個 Connector / Tool 可以具有：

```text
Tool
├── id
├── name
├── description
├── capabilities
├── input schema
├── output schema
├── permissions
├── authentication state
└── execution handler
```

這樣未來新增工具時，不需要修改整個聊天 UI，只要註冊新的 Tool 即可。

### `@` 不應只是 UI 標籤

例如使用者輸入：

> `@GitHub 幫我查看 agent 最近一次 Actions 是否成功`

理想流程應是：

```text
@GitHub
   ↓
Resolve Tool
   ↓
Check permission / authentication
   ↓
Planner
   ↓
GitHub API
   ↓
Normalize result
   ↓
Nexus response
```

而：

> `@PubMed 搜尋某個主題的研究`

則應交給 PubMed Connector，而不是讓模型假裝自己查過資料。

### 與 Skills 的關係

建議不要把 Skill、Tool、Connector 混成同一個概念：

```text
Skill      = Agent「知道怎麼做」
Tool       = Agent「可以做什麼」
Connector  = Tool「連接到哪個外部服務」
```

例如：

```text
Skill: GitHub Code Review
        ↓
Tools: repository.read / file.read / pull_request.read
        ↓
Connector: GitHub
```

這樣 Nexus 才能逐步從「聊天 App」變成真正可擴充的 Mobile Agent。

### Mobile-first 的特殊設計

Nexus 是手機 Agent，因此 Connector 不應只限制在網路服務：

```text
External Connectors
├── GitHub
├── Google services
├── Web / search
└── Future APIs

Device Tools
├── Files
├── Share
├── Camera
├── Notifications
├── Intents
├── App links
└── Voice
```

這能讓 Nexus 同時具備「雲端服務 Agent」與「手機本地 Agent」能力，而且維持免 Root 的設計原則。

## 16. 功能狀態分類

之後所有大型修改建議使用以下分類：

| Status | 意義 |
|---|---|
| `CURRENT` | 現在已存在並應保持可用 |
| `REFACTOR` | 已存在，但應重新整理架構 |
| `RECOVER` | 歷史上存在，值得重新實作 |
| `PLANNED` | 已確定方向，但尚未完成 |
| `REFERENCE` | 歷史設計 / 原型，保留作為參考 |
| `DEPRECATED` | 確認不再需要，只有在確認後才移除 |

## 17. 建議開發路線

### Phase 1 — Nexus Foundation

- Nexus UI
- Agent Core
- AI engine abstraction
- Tool registry
- Skill registry
- Memory foundation

### Phase 2 — Real Agent

- Planner
- Task model
- Tool execution
- Result verification
- Retry / fallback

### Phase 3 — Mobile Agent

- File Agent
- App Agent
- Android integrations
- Voice

### Phase 4 — Skills + Memory

- Skill import
- `SKILL.md`
- Skill management
- Long-term memory

### Phase 5 — Web / Office / GitHub

- Web Research
- Office Agent Studio
- GitHub Agent
- Actions / workflow automation

### Phase 6 — Nexus Experience

- AI Moments
- AI 朋友圈
- Agent activity feed

### Phase 7 — Advanced Nexus

- Multi-model routing
- More autonomous workflows
- Optional large local assets/models
- Advanced agent collaboration

## 18. 最重要的結論

這個 repository 不應被視為「從零開始的新 App」。

它已經累積了多輪 Android、AI、資料層、GitHub、CI/CD、Agent 與自動化嘗試。

這次大改的正確方式不是：

> **刪掉舊的 → 重寫全部。**

而是：

> **盤點 → 保留 → 分類 → 重構 → 恢復有價值的歷史能力 → 建立 Nexus Agent Core。**

最終目標：

> **Nexus = 一個以 Android 手機為主要執行環境、免 Root、可擴充 Skills、具備 Memory、Tools、Web、GitHub、Office 與多 Agent 能力的原生 Mobile AI Agent。**

---

## Appendix — GitHub 歷史證據

### Compare

`e143a5e297f3ee97d518fafb50527355048b9438...main`

- 43 commits ahead
- 0 commits behind

### 代表性歷史 commit

`5d80b1a28b9c9898c5126e0c227cef7f2e6028cc`

`feat: enhance data layer and add WhatsApp support`

該 commit 的變更包括 ChatSession / AgentMemory 擴充、ChatDao 功能增加，以及 GitHub Issue comment / update API 與 WhatsApp API 相關程式碼。
