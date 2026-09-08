# 🤖 Nexus AI 智能助手

基于 **Android + Kotlin + Jetpack Compose + Google Gemini API** 的移动端 AI 智能助手项目。

> **独立项目声明 / Independence Notice**
>
> Nexus AI 是一个独立开发的个人项目，**与 Marvis AI、MyNexusAI（https://app.mynexusai.com/）及其运营方、开发者或相关品牌不存在官方关联、合作、授权或隶属关系**。
>
> 本项目在产品设计与技术探索过程中，可能参考公开可见的 AI Agent、移动端 AI 助手及相关产品的通用设计理念；这不代表复制其专有代码、资产、品牌或未公开技术，也不代表得到相关项目方的认可或背书。
>
> “Marvis AI”、“MyNexusAI”及其他第三方名称、商标和产品标识归其各自权利人所有。本项目不使用这些名称作为自己的产品名称或品牌标识。

## ✨ 核心特性

- 🧠 Gemini AI 对话能力，并支持无有效 API Key 时的本地自主引擎回退
- 🎨 Jetpack Compose + Material 3 UI
- 💾 Room + KSP 本地数据与会话持久化
- 🌐 Retrofit + Moshi + OkHttp 网络通信
- 💻 Kotlin / Java / Python / JavaScript / SQL 等编程辅助
- 📄 Office 文档、表格、PPT 等内容生成与整理能力
- 🔄 GitHub 仓库、Commit、Issue、Pull Request 等联动能力
- 📈 技能成长与任务完成观察机制

## 🛠️ 技术栈

| 模块 | 技术 |
| :--- | :--- |
| 语言 | Kotlin 2.2.10 |
| Android | Android Gradle Plugin 8.7.3 / compileSdk 35 |
| UI | Jetpack Compose / Material 3 |
| 状态管理 | ViewModel + StateFlow |
| 数据库 | Room 2.7.1 + KSP |
| 网络 | Retrofit 2 + OkHttp + Moshi |
| AI | Google Gemini API |
| 异步 | Kotlin Coroutines |
| CI | GitHub Actions |

## 🚀 快速开始

### 1. 克隆仓库

```bash
git clone https://github.com/koayxinchun4-cmd/agent.git
cd agent
```

### 2. 配置 Gemini API Key（可选）

项目使用 Secrets Gradle Plugin 从根目录 `.env` 读取 Gemini API Key。

复制模板：

```bash
cp .env.example .env
```

然后编辑 `.env`：

```env
GEMINI_API_KEY=你的_GEMINI_API_KEY
```

`.env` 已被 `.gitignore` 忽略，**不要将真实 API Key 提交到 GitHub**。

如果没有有效的 API Key，应用会回退到本地自主引擎。

> 如果 API Key 曾经被公开发布或直接提交到仓库，请先在 Google AI Studio / Google Cloud 中撤销旧 Key 并重新生成。

## 📦 本地构建

环境要求：

- JDK 17
- Android SDK 35
- Android Studio（建议使用较新的稳定版）

运行单元测试：

```bash
./gradlew testDebugUnitTest
```

生成 Debug APK：

```bash
./gradlew :app:assembleDebug
```

APK 输出位置：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 🤖 GitHub Actions

Android CI 工作流位于：

```text
.github/workflows/android-ci.yml
```

当前工作流会在：

- `main` 分支 push
- 针对 `main` 的 Pull Request

时自动执行 Android Debug APK 构建，并将 `app-debug.apk` 上传为 GitHub Actions Artifact。

### GitHub Secret

如果希望 CI 构建时使用 Gemini API，请在：

**Repository → Settings → Secrets and variables → Actions**

创建以下 Repository Secret：

```text
GEMINI_API_KEY
```

CI 会在构建过程中临时生成 `.env`，不会要求把真实 API Key 写进仓库。

## 🔒 安全说明

- 不要提交 `.env`。
- 不要把真实 Gemini API Key 写进 Kotlin、Gradle、README 或其他源码文件。
- `.env.example` 只保留占位符。
- GitHub Actions 使用 `GEMINI_API_KEY` Secret 注入构建环境。
- GitHub Token 等用户凭据应使用安全的本地存储机制，不应硬编码。

## 📂 项目结构

```text
├── app/
│   ├── src/main/
│   │   ├── java/com/example/       # Android/Kotlin 源码
│   │   ├── res/                    # Android 资源
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml          # Version Catalog
├── .github/
│   └── workflows/
│       └── android-ci.yml          # Android CI
├── .env.example                    # API Key 模板
├── .gitignore
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
└── README.md
```

## 🎯 构建目标

项目当前的主要验证目标是确保以下命令能够成功执行：

```bash
./gradlew :app:assembleDebug
```

并通过 GitHub Actions 持续验证 Android 项目的可构建性，同时生成可下载的 Debug APK Artifact。

## 📄 License

本项目采用 **GNU General Public License v3.0 (GPLv3)** 开源。

Nexus AI 是免费且开放源码的项目，欢迎任何人使用、研究、修改、Fork 和贡献，但所有 GPLv3-covered 代码的再发布和衍生版本都必须遵守 GPLv3 的适用条款，并保留适用的版权、许可证、署名及修改说明。

### Attribution / 归属

原始项目版权归 **koayxinchun4-cmd 和项目贡献者**所有。使用、Fork 或再发布 Nexus AI 的代码时，不得删除适用的原始版权和许可证声明，也不得将 Nexus AI 原作者或贡献者创作的代码虚假声称为自己独立原创。

### Independent forks and data

基于 Nexus AI 创建的 Fork、应用、服务和商业项目可以独立运营。其用户数据、聊天记录、文件、业务数据、内容、运营行为和其他独立产生的数据，不会仅因为使用 Nexus AI 就自动成为 Nexus AI 项目的数据，也不会自动要求向 Nexus AI 原项目公开。

这些独立项目及其运营行为不代表 Nexus AI 或其原作者、贡献者；除非另有明确书面说明，Nexus AI 项目不对第三方 Fork、应用、服务或其数据与行为背书或承担责任。

完整许可证说明请参阅仓库中的 `LICENSE` 文件及 GNU GPLv3 官方文本。

## 🤝 Community

**Nexus AI is free and open source — everyone is welcome to join us. ❤️**

欢迎提交 Issue、Pull Request、功能建议、Bug 修复和新的 Agent / Skill。贡献请先阅读 `CONTRIBUTING.md`。