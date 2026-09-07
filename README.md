# 🤖 Nexus AI 智能助手 (Nexus Android AI Agent)

[![Android CI/CD](https://github.com/OWNER/REPO/actions/workflows/android-ci.yml/badge.svg)](https://github.com/OWNER/REPO/actions/workflows/android-ci.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4.svg?logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Gemini](https://img.shields.io/badge/AI%20Engine-Google%20Gemini-4E79A7.svg?logo=google&logoColor=white)](https://ai.google.dev/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

> 基于 Google Gemini 模型的全能移动端 AI 智能体助手，深度融合 Jetpack Compose Material 3 现代化设计，具备**智能技能成长系统、Office 多功能工作台、Codex 编程助手、GitHub 远程联动与 AI 朋友圈技能共享中心**。

---

## ✨ 核心特性 (Features)

### 🧠 1. 多模态 AI 大脑 (Gemini AI Engine)
- **多模型无缝切换**：深度适配 Gemini 2.5 Flash 及长文本高推理模型。
- **动态上下文记忆**：基于 Room 本地数据库实现的长期语义记忆库与会话持久化。
- **实时语音交互**：集成 Android SpeechRecognizer 实时语音流转文字与本地 Text-to-Speech 朗读反馈。

### 📊 2. Office AI 生产力工作台 (Office Agent Studio)
- **文档智能生成与重构**：专业报告、邮件公文、PRD 需求文档、演讲大纲一键生成与润色。
- **电子表格与公式演算**：支持数据报表构建、财务测算模型、Excel/CSV 格式化与常用函数校验。
- **PPT 演示文稿结构化编排**：演讲逻辑规划、Slide 幻灯片分段大纲生成与排版建议。
- **一键分享与导出**：一键拷贝 Markdown/富文本或通过 Android 系统分享面板发送到办公应用。

### 💻 3. Codex 编程特工 (Developer Toolkit)
- **代码生成与语法高亮**：支持 Kotlin、Python、JavaScript、Java、SQL 等主流语言代码生成。
- **Bug 根因分析与重构**：深入解析异常堆栈，提供精准修复方案与单元测试用例。
- **代码运行与沙箱验证**：直接在移动端查看解析结果与运行逻辑。

### 🔄 4. GitHub 深度联动与 Git 运维 (GitHub Integration)
- **OAuth 2.0 & Token 安全授权**：支持浏览器端标准 OAuth 回调与个人访问令牌 (PAT) 加密安全存取。
- **仓库信息与代码浏览**：查看仓库分支、Commits、文件目录结构与 README 详情。
- **一键 Commit & Push**：支持向指定分支提交代码变更或创建全新文件。
- **Pull Request & Issue 管理**：在移动端轻松创建、审查 Pull Request，快速登记 Issue 缺陷。

### 📈 5. 智能体技能成长中枢与后台观察者 (Skill Mastery & Task Observer)
- **全域技能经验体系 (XP & Rank)**：从「初阶智能体」进阶到「全域大师」，每个技能拥有专属熟练度等级与经验条。
- **后台任务完成观察者 (`TaskCompletionObserver`)**：在完成 Commit 推送、PR 创建、文档生成等实战任务后，自动派发事件并增益技能掌握度。
- **技能一键导出至 GitHub**：将掌握的技能规范一键导出为 `SKILL.md` 并 Push 至公开 GitHub 仓库。

### 🌐 6. AI 朋友圈与开源技能中心 (AI Moments & Skill Hub)
- **动态心得广场**：分享智能体进化突破、开源动态与使用体验，支持点赞与外部社交分享。
- **开源技能一键下载**：从社区动态或指定 GitHub 仓库一键克隆安装扩展技能并即刻调用实战。

---

## 🔒 安全性与隐私设计 (Security & Privacy)

本项目严格遵循 Android 安全最佳实践与开源准则：

1. **密钥零硬编码 (Zero Hardcoded Secrets)**：
   - Gemini API Key 与第三方敏感配置通过 **Secrets Gradle Plugin** 从 `.env` 文件或构建环境变量安全注入 `BuildConfig`。
   - 仓库已配置标准的 `.gitignore`，`.env`、`local.properties`、`debug.keystore` 等敏感文件**绝不提交**至版本控制中。
2. **凭据本地安全存储**：
   - GitHub Token 与用户敏感配置存储于设备本地私有应用沙箱 (`Encrypted SharedPreferences` / `Room Database`)，不上传任何第三方未授权服务器。
3. **最小权限原则 (Least Privilege)**：
   - 仅申请网络访问 (`INTERNET`) 与本地语音交互 (`RECORD_AUDIO`) 等必要权限，麦克风权限采用 Compose 动态运行时按需申请机制。

---

## 🛠️ 技术架构 (Tech Stack)

| 模块 | 技术选型 | 说明 |
| :--- | :--- | :--- |
| **语言** | Kotlin 2.0+ | 纯 Kotlin 编写，现代函数式与协程支持 |
| **UI 框架** | Jetpack Compose (M3) | 声明式 UI，完整遵循 Material Design 3 规范 |
| **状态管理** | ViewModel + StateFlow | MVVM 架构，响应式单向数据流 |
| **本地持久化** | Room + SQLite (KSP) | 本地数据库缓存、会话历史、长期记忆、技能掌握度 |
| **网络请求** | Retrofit 2 + OkHttp 3 + Moshi | RESTful API 异步网络通信与 JSON 序列化解析 |
| **AI 模型** | Google Gemini API (Firebase AI) | Gemini 2.5 Flash / 2.5 Pro 多模态模型驱动 |
| **图片加载** | Coil Compose | 异步高效图片加载与圆角缓存处理 |
| **CI/CD** | GitHub Actions | 自动化单元测试、代码编译检查与 APK 构建交付 |

---

## 🚀 快速上手与本地构建 (Getting Started)

### 1. 克隆代码仓库
```bash
git clone https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
cd YOUR_REPO_NAME
```

### 2. 配置环境变量与 API Key
在项目根目录复制 `.env.example` 并重命名为 `.env`：
```bash
cp .env.example .env
```
打开 `.env` 文件，填入您的 Google Gemini API Key（可在 [Google AI Studio](https://aistudio.google.com/app/apikey) 免费申请）：
```properties
GEMINI_API_KEY=AIzaSy...your_actual_gemini_api_key...
```

### 3. 构建与运行

#### 使用 Gradle 命令行编译：
- **运行单元测试**：
  ```bash
  ./gradlew testDebugUnitTest
  ```
- **生成 Debug APK**：
  ```bash
  ./gradlew assembleDebug
  ```
  *生成的 APK 文件路径：`app/build/outputs/apk/debug/app-debug.apk`*

#### 使用 Android Studio 运行：
1. 使用 **Android Studio Iguana / Jellyfish / Ladybug+** 打开项目。
2. 等待 Gradle Sync 完成（确保 JDK 版本为 **Java 17**）。
3. 选择连接的 Android 物理设备或模拟器（Android 7.0 / API 24+），点击 **Run 'app'** (Shift + F10)。

---

## 🤖 CI/CD 持续集成 (GitHub Actions)

项目已内置 `.github/workflows/android-ci.yml` 自动化流水线：
- **触发条件**：向 `main` / `master` 分支 push 代码或提交 Pull Request。
- **流水线任务**：
  1. 自动化检出代码并配置 JDK 17 环境；
  2. 自动载入 `.env.example` 缺省安全环境配置；
  3. 执行 `testDebugUnitTest` 自动化测试；
  4. 构建生成 Debug APK 并保存为 Artifacts 供直接下载测试。

---

## 📂 项目结构规范 (Project Structure)

```text
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── data/
│   │   │   │   │   ├── api/           # Retrofit GitHub API 接口与数据模型
│   │   │   │   │   ├── local/         # Room Entity, DAO, 数据库与掌握度模型
│   │   │   │   │   └── repository/    # 核心数据仓库与业务中枢
│   │   │   │   ├── service/           # TaskCompletionObserver 后台任务观察者
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/    # Compose UI 组件 (Office Studio, Skills, Moments)
│   │   │   │   │   ├── theme/         # Material 3 颜色与排版主题
│   │   │   │   │   ├── AgentMainScreen.kt # 主交互界面
│   │   │   │   │   └── AgentViewModel.kt  # 全局业务状态机
│   │   │   │   └── MainActivity.kt    # 主入口 Activity
│   │   │   ├── res/                   # 矢量图、字符串、尺寸与清单资源
│   │   │   └── AndroidManifest.xml    # 清单配置与权限声明
│   │   └── test/                      # JVM 单元测试与 Robolectric 测试
│   └── build.gradle.kts               # App 级 Gradle 构建脚本
├── .github/
│   └── workflows/
│       └── android-ci.yml             # GitHub Actions CI/CD 流水线
├── .env.example                       # 环境变量模板
├── .gitignore                         # 忽略文件规则
├── gradle/libs.versions.toml          # Version Catalog 统一依赖管理
└── README.md                          # 项目中英文说明文档
```

---

## 📄 开源许可证 (License)

本项目基于 [Apache License 2.0](LICENSE) 协议开源。欢迎提交 Issue 与 Pull Request 共同完善！
