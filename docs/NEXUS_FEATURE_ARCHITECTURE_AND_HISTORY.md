# Nexus 智能助手：功能架構與歷史功能總表

> **定位**：Nexus 智能助手是一款以 Android 手機為完整執行環境的原生 AI Agent。目標是不依賴電腦、不需要 Root，透過 Android 正規能力、Gemini 與可擴充 Skills 幫助使用者完成實際任務。

## 1. 為什麼需要這份文件

`agent` 專案在早期開發過程中已經累積不少 AI、Agent、GitHub、Office、Memory 等能力；部分功能後來因重構、清理或安全原因被移除或改名。因此本文件用來保存：

- 現在已存在的能力
- 歷史上曾經存在、值得恢復的能力
- 正在重新設計的能力
- 未來規劃能力

**原則：功能先登記，不因 APK 大小而隨意刪除產品能力。** 大型資料、媒體、可選 Skills 與未來本地模型應盡量採用按需下載/獨立資料空間，而不是全部塞進核心 APK。

---

## 2. Nexus 產品定位

### 核心目標

Nexus 不是單純的 Gemini Chat，而是 Mobile-first Agent：

```text
使用者目標
    ↓
Nexus 理解意圖
    ↓
Planner / Agent Core
    ↓
選擇 Skill / Tool
    ↓
執行 Android / Web / GitHub 等操作
    ↓
檢查結果
    ↓
需要時調整策略
    ↓
向使用者回報
```

### 硬性產品方向

- 原生 Android
- Kotlin
- Jetpack Compose
- 手機優先
- 不依賴電腦
- 不要求 Root
- 優先使用 Android 正規 API 與明確的使用者授權
- Gemini 作為主要 AI 能力之一
- 支援多語言，特別重視中文、Bahasa Melayu、English 以及大馬使用者常見的混合語言

---

## 3. 功能狀態分類

| 狀態 | 意義 |
|---|---|
| `CURRENT` | 目前 main / 現代架構中已有或正在使用 |
| `RECOVER` | 歷史版本曾有，值得重新實作 |
| `REFACTOR` | 概念仍保留，但需要以 Nexus 架構重新設計 |
| `PLANNED` | 已確定方向，尚未完整實作 |
| `REFERENCE` | 來自外部 Agent / Skills 的產品參考，不代表目前已實作 |

---

## 4. AI Core

### 4.1 Gemini

**狀態：`CURRENT`**

Gemini 是 Nexus 的主要雲端 AI 能力。現有 Android 架構已經包含 Gemini API、Retrofit / Moshi / OkHttp 等相關依賴。

未來需要逐步加入：

- 多輪 Context
- Agent System Prompt
- Tool Calling / Function Calling
- 任務規劃
- 執行結果回傳
- 錯誤恢復

### 4.2 OpenRouter

**狀態：`RECOVER` / `REFACTOR`**

歷史版本曾經建立：

- `BaseAiEngine`
- Gemini Engine
- OpenRouter Engine
- `HybridAiRepository`
- fallback AI 路徑

未來不要只把 OpenRouter 當成另一個聊天 API，而應升級成：

> **Multi-Model Router**

由 Nexus 根據任務、模型可用性與使用者設定選擇適合的 AI Provider。

### 4.3 Local Autonomous Engine

**狀態：`RECOVER` / `REFACTOR`**

歷史版本存在 `AutonomousEngineLocalPresets` 等本地預設能力。這個方向可作為：

- API Key 不可用時的 fallback
- 離線 UI / 任務模板
- 本地工具路由
- Skill metadata / prompt templates

注意：它不等於完整的本地大模型。

---

## 5. Agent System

### 5.1 Agent Core

**狀態：`PLANNED`**

核心應負責：

- Intent detection
- Task decomposition
- Planner
- Tool / Skill selection
- Execution
- Observation
- Verification
- Retry / recovery
- User confirmation

### 5.2 CTO Agent

**狀態：`RECOVER`**

歷史版本曾存在 `CTO.new` 類型的本地 Agent preset，能力包括：

- 全棧架構設計
- 技術選型
- 模組設計
- Code review / refactor
- Sprint / milestone 規劃

未來應作為 Nexus 的一個可安裝/啟用 Skill 或 Agent Persona，而不是把它硬編碼成唯一 AI。

### 5.3 Codex 研發助理

**狀態：`RECOVER` / `REFACTOR`**

歷史版本已有 Codex 研發助理概念，主要服務：

- Coding
- Kotlin / Compose
- Bug analysis
- Code review
- Repository tasks

未來與 GitHub Agent 深度整合。

---

## 6. Skills 系統

### Skills Registry

**狀態：`REFACTOR`**

目標：讓 Nexus 可以增加技能，而不是每新增一個能力都修改核心 App。

```text
Skill Registry
├── Web Research
├── File Agent
├── App Agent
├── Office Agent
├── GitHub Agent
├── Coding Agent
├── Voice
└── User / Community Skills
```

### GitHub `SKILL.md` 匯入

**狀態：`RECOVER` / `REFACTOR`**

歷史版本曾有從 GitHub URL / Repository 讀取 `SKILL.md`，再解析技能內容的方向。

未來可演進成：

```text
GitHub Skill
    ↓
讀取 SKILL.md
    ↓
解析 metadata / instructions
    ↓
安全檢查
    ↓
加入 Skill Registry
    ↓
按需啟用
```

所有外部 Skill 必須經過安全與權限檢查，不應允許任意內容直接取得手機高權限。

---

## 7. Mobile Agent：純手機、免 Root

### 7.1 Phone File Agent

**狀態：`PLANNED`**

把 PC Agent 的「整理電腦文件」轉換成手機版本：

- Download
- Documents
- Images
- User-selected folders
- 分類 / 搜尋 / 整理
- 需要修改或移動資料時先確認

優先使用 Storage Access Framework 等 Android 正規機制。

### 7.2 App Agent

**狀態：`PLANNED`**

目標：讓 Nexus 在使用者授權下協助操作手機 App。

可能使用：

- AccessibilityService
- Android Intent
- Deep Links
- Share APIs
- Clipboard
- Notification access（需要授權）

高風險或不可逆操作應要求使用者確認。

### 7.3 Browser / Web Research Agent

**狀態：`PLANNED`**

目標：

```text
任務
 ↓
搜尋
 ↓
讀取來源
 ↓
比較
 ↓
Gemini 分析
 ↓
摘要 + 來源
```

重點是讓 Nexus **完成研究任務**，而不是只把搜尋結果丟給使用者。

### 7.4 Notification / System Integration

**狀態：`PLANNED`**

可按 Android 權限模型逐步加入。所有需要特殊權限的能力必須清楚告知使用者用途。

---

## 8. Memory

### Chat / Room

**狀態：`CURRENT` / `REFACTOR`**

目前已有 Chat、Session、Room 等本地資料基礎。

### Agent Memory

**狀態：`RECOVER` / `REFACTOR`**

歷史版本曾有 `AgentMemory` 概念。

最終 Nexus Memory 建議分層：

```text
Memory
├── Conversation Memory
├── User Preferences
├── Important Facts
├── Project Memory
├── Skill Memory
└── Agent Task Memory
```

本地資料優先保存於手機；敏感資料不應因 AI 功能而自動上傳。

---

## 9. Office Agent Studio

**狀態：`RECOVER` / `REFACTOR`**

歷史版本曾有 Office Agent Studio，並從較早的多 Tab 結構持續擴充。

未來名稱可以統一成：

> **Nexus Agent Studio**

它應成為進階使用者管理 Agent / Workflow / Skills 的工作區。

可能包含：

- Agent 協同
- Workflow Studio
- GitHub / CI/CD
- 自訂 Agent 任務
- Office automation
- Skill 管理

---

## 10. GitHub / Coding Agent

**狀態：`CURRENT` + `RECOVER` / `REFACTOR`**

歷史與現代架構都已經有 GitHub 整合方向。

能力方向：

- Repository
- Branch
- Files
- Issues
- Pull Requests
- GitHub account / Device Flow / OAuth（依目前安全架構重新實作）
- Commit / PR workflow
- GitHub Actions
- CI/CD

目標：

> 使用者可以直接在手機上讓 Nexus 協助管理自己的程式專案。

---

## 11. CI/CD Workflow Studio

**狀態：`RECOVER` / `REFACTOR`**

歷史版本有直接建立 / 注入 GitHub Actions workflow 的方向。

目前專案也已經擁有 Android CI、自動 APK artifact、Pages 與 Release 等 CI/CD 基礎。

未來可以讓 Nexus 在手機上：

- 分析 build failure
- 讀 Actions log
- 建議修復
- 產生 workflow
- 經使用者確認後提交修改

---

## 12. WhatsApp / 外部通訊

**狀態：`RECOVER` / `REFERENCE`**

歷史版本曾加入 WhatsApp Cloud API、send message、webhook 等資料模型與 service，並有 Gemini + GitHub + WhatsApp automation bridge 的方向。

後續部分舊程式碼因安全 / 重構而移除，因此不要直接恢復舊實作。

如果未來重新加入，應採：

- 明確授權
- 安全 token 管理
- 最小權限
- 使用者確認
- 不在 APK / Git repository 硬編碼 secrets

---

## 13. AI Moments

**狀態：`PLANNED` / `REFERENCE`**

用來記錄 Nexus 的重要 Agent 活動，例如：

- 任務完成
- 研究成果
- 自動化流程
- 創作內容
- 技能成長

可以形成類似 Agent activity feed 的體驗。

---

## 14. AI 朋友圈

**狀態：`PLANNED` / `REFERENCE`**

產品方向可以參考 AI 社交 / Feed 類功能，但不應直接假定外部 GitHub Agents 或其他專案中的功能已經存在於 Nexus。

未來可以考慮：

- AI Moments 分享
- Agent 動態
- Skill 分享
- 使用者建立的 Agent / Skill 展示
- 社群互動

如果涉及公開分享，必須明確區分：

> 私人 Memory ≠ 公開 Feed

不能因為 AI 朋友圈而自動公開使用者的私人對話、檔案或記憶。

---

## 15. Voice

**狀態：`PLANNED` / `RECOVER`（依目前程式碼重新確認）**

目標：

- Speech-to-Text
- AI response
- Text-to-Speech
- Voice-triggered Agent tasks

Voice 應成為 Agent 的輸入方式之一，而不是另一套獨立聊天系統。

---

## 16. Malaysia-first

**狀態：`PLANNED`**

Nexus 的目標使用者包含馬來西亞手機使用者，因此 UX 應支援：

- 繁體 / 簡體中文視需求
- Bahasa Melayu
- English
- 中文 + English + BM 混合輸入

未來可以針對本地學生、文件、網站與常見手機使用情境做 Skill。

---

## 17. 儲存空間策略

Nexus 功能可以很多，但**不要因為怕 GB 而刪核心功能，也不要故意把 APK 做成 GB 級**。

建議：

```text
Core APK
   +
Dynamic / optional Skills
   +
Local database
   +
User files
   +
AI Moments media
   +
Cache
   +
Optional local models
```

其中真正可能讓空間快速增長的是：

- 圖片 / 影片
- Cache
- 使用者檔案
- 離線資料
- 未來本地 AI 模型

因此應提供：

- 儲存空間管理
- Cache 清理
- Media 管理
- Skill 移除 / 停用
- Model 管理

而不是刪掉 Nexus 的功能架構。

---

## 18. 開發路線圖

### Phase 1 — Nexus Foundation

- App branding
- Dashboard
- Agent Core 基礎
- Chat / Context
- Skill Registry
- Memory 基礎

### Phase 2 — True Agent

- Planner
- Task
- Tool
- Execute
- Observe
- Verify
- Retry
- Confirmation

### Phase 3 — Mobile Agent

- File Agent
- App Agent
- Browser Agent
- Notification / Android integration

### Phase 4 — Skills + Memory

- Skill install
- GitHub `SKILL.md`
- Skill permissions
- Long-term Memory
- Project Memory

### Phase 5 — Advanced Workspace

- Agent Studio
- Office Agent
- GitHub / Codex
- CI/CD Workflow Studio

### Phase 6 — Social / Moments

- AI Moments
- AI 朋友圈
- Skill sharing

### Phase 7 — Voice + Optimization

- STT
- TTS
- Background task UX
- Storage management
- Performance / battery optimization

---

## 19. 不應做的事情

1. 不要把 Native Android 專案改成 Vite / Web App 來迎合某個開發平台。
2. 不要因為外部 AI Studio / Agent 平台要求而大規模改寫 Android 架構。
3. 不要把 Gemini API key 寫死進 Git。
4. 不要為了「功能看起來很多」而讓 Agent 在沒有確認的情況下執行不可逆操作。
5. 不要把私人 Memory 自動公開到 AI 朋友圈。
6. 不要用 Root 作為 Nexus 的必要條件。
7. 不要因為未來可能很大就現在砍掉核心功能；先做好模組化與儲存管理。

---

## 20. 最終產品願景

```text
                         🤖 NEXUS
                     智能手機 AI Agent
                              │
          ┌───────────────────┼───────────────────┐
          ↓                   ↓                   ↓
       🧠 AI Core          🧩 Skills           🧠 Memory
          │                   │                   │
          ↓                   ↓                   ↓
     Gemini / Router     可擴充工具           長期記憶
          │                   │                   │
          └───────────────────┼───────────────────┘
                              ↓
                       📱 Mobile Agent
                              │
          ┌─────────────┬─────┼─────┬─────────────┐
          ↓             ↓     ↓     ↓             ↓
       📁 Files       📲 Apps 🌐 Web  💻 GitHub   📄 Office
                              │
                              ↓
                         👤 使用者手機

           不需要電腦 · 不需要 Root · 手機即 Agent
```

**Nexus 的核心原則：不是把 PC Agent 縮小成手機 App，而是從一開始就把「手機」當成 Agent 的主要執行環境。**
