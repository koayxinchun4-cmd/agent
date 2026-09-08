# Nexus AI 前台 / 后台技術路線

## 1. 決策

Nexus AI 目前以 **原生 Android 前台**為優先，使用 Kotlin + Jetpack Compose。

Python 不會被拿來取代 Android App 的核心程式碼。未來如果需要獨立的 AI backend / server software，再評估使用 Python（例如 FastAPI）或其他合適的後端技術。

## 2. 為什麼前台先用 Kotlin

Nexus 是 mobile-first、phone-only、免 Root 的 Android AI Agent。Android 原生能力需要直接接觸：

- Jetpack Compose UI
- Android lifecycle
- Intent / Deep Link
- Storage Access Framework
- Notification / permission flows
- WorkManager / background tasks
- Voice / media APIs
- Android app integration

Kotlin 可以直接使用 Android SDK 與 Jetpack，因此目前是 Nexus 前台與 Agent runtime 的主要語言。

## 3. Python 的定位

Python 很適合：

- AI / ML 實驗
- RAG / data processing
- Web research backend
- FastAPI service
- Agent server orchestration
- 模型實驗與資料工具

但現階段不把 Python backend 加入核心 APK，避免在產品尚未完成前增加部署、網路、帳號、安全與維護複雜度。

## 4. 未來雙層架構

```text
                    Nexus AI
                       |
             +---------+---------+
             |                   |
       Android Frontend     Optional Backend
            Kotlin             Python / other
             |                   |
       Compose + Agent       FastAPI / AI services
       Android Tools         RAG / Research / Jobs
             |                   |
             +------ secure API--+
```

Backend 是 **FUTURE / OPTIONAL**，不是目前開發阻塞項。

## 5. 開發優先順序

### NOW — 前台與手機 Agent

- Nexus Home / navigation
- Chat UX
- Agent task UX
- Agent execution status
- Tool / Skill UI
- Memory UI
- Settings / model status
- Android permissions與原生整合
- 前台錯誤與 loading / empty states

### LATER — Agent 深化

- 真正 Model Router
- Tool calling
- 多步驟 Agent loop
- Verify / Retry / Re-plan
- GitHub / Web / File / App tools
- Skills registry

### FUTURE — Backend software

- Optional Python backend
- Remote jobs
- Server-side RAG
- Advanced model orchestration
- Shared/community services

## 6. 重要原則

不要為了「未來可能需要 backend」而提前把 Android 專案變成 client-server dependency。

Nexus 即使沒有 backend，也應該能保有完整的手機端核心體驗；backend 只能在真正有需求時作為可選能力加入。
