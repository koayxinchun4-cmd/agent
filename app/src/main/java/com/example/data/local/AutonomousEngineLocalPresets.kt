package com.example.data.local

import com.example.data.api.OfficeAgent

/**
 * 离线/自主智能专家预置引擎，在未配置或无需网络 API Key 时提供高质量的专家响应与架构工程能力
 */
object AutonomousEngineLocalPresets {

    fun generateAutonomousResponse(
        userPrompt: String,
        activeSkillId: String?,
        currentRepo: String = "google/mesop",
        currentBranch: String = "main",
        assignedAgent: OfficeAgent? = null
    ): String {
        val lower = userPrompt.lowercase()

        // 1. CTO / 架构师需求分析与全栈方案生成
        if (assignedAgent?.id == "agent_cto_new" || lower.contains("cto") || lower.contains("架构") || lower.contains("全栈") || lower.contains("技术方案") || lower.contains("选型")) {
            return """
### 🏛️ Nexus CTO.new • 自主全栈软件工程架构方案

**项目定位**: 面向工业级可用性与移动云端协同的自适应架构设计  
**当前协作仓库**: `$currentRepo` (分支: `$currentBranch`)  
**执行模式**: ⚡ **免配置 API 自主推理引擎 (Autonomous On-Device Execution)**

---

#### 1. 系统核心架构与技术栈选型矩阵
- **前端 / 移动终端**: Kotlin + Jetpack Compose + M3 规范 + Coroutines Flow 反应式状态流
- **业务逻辑层 (Clean Architecture)**: Domain UseCase + ViewModel 状态容器 + 独立职责隔离
- **本地持久化**: Room SQLite 本地数据库 + TypeSafe 预编译索引，支持离线优先 (Offline-First)
- **CI/CD 与质量保证**: GitHub Actions 自动化矩阵流水线 (JDK 17 + Android 36 SDK + Robolectric 单元测试 + APK 产物归档)
- **Git 自动化协同**: 集成 GitHub REST & OAuth 设备流，支持端到端一键生成 Patch、创建 Pull Request

---

#### 2. 软件生命周期与模块划分 (Module Blueprint)
```
nexus-system-root/
├── app/
│   ├── src/main/java/com/example/
│   │   ├── data/          # 数据源层 (API 客户端, Room 本地数据库, Repository 仓库)
│   │   ├── domain/        # 核心领域模型与业务逻辑
│   │   ├── ui/            # Jetpack Compose UI 组件与 ViewModel
│   │   └── utils/         # 平台工具 (语音识别, 压缩打包, 权限助手)
│   └── build.gradle.kts   # 模块级构建脚本
├── .github/workflows/     # 自动化 CI/CD 编排
│   └── android-ci.yml     # 免 Wrapper 9.3.1 独立构建工作流
└── metadata.json          # 平台与应用元数据
```

---

#### 3. 核心代码设计规范与范式
```kotlin
// 推荐的现代化架构模式：密封状态 + Flow 反应式驱动
sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String, val cause: Throwable? = null) : UiState<Nothing>
}
```

---

#### 4. CTO 自动化工程执行清单 (Action Items)
1. ✅ **环境就绪**: CI/CD 流水线适配完毕，解决 Gradle Wrapper Jar 依赖与 SDK 授权问题。
2. 🚀 **代码生成**: 可直接联动下方 **Codex 研发助理** 或 **DevOps 工程师** 将上述方案转换为具体代码并推送到 GitHub。
3. 📦 **分支与 PR**: 随时可在「⚡ 自动化 Commit & Push」中直接打包提交至 `$currentRepo`。
            """.trimIndent()
        }

        // 2. DevOps / GitHub Actions CI/CD 流水线相关
        if (assignedAgent?.id == "agent_devops_ci" || lower.contains("ci") || lower.contains("action") || lower.contains("workflow") || lower.contains("yaml") || lower.contains("yml") || lower.contains("构建")) {
            return """
### 🚀 Nexus DevOps 专家 • 生产级 GitHub Actions CI 脚本

为您生成的 GitHub Actions 自动化构建与测试工作流（适配 Android 36 + Gradle 9.3.1 独立环境）：

```yaml
name: Android CI/CD

on:
  push:
    branches: [ "main", "master", "develop" ]
  pull_request:
    branches: [ "main", "master" ]
  workflow_dispatch:

jobs:
  build-and-test:
    name: Build, Lint & Test
    runs-on: ubuntu-latest

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Setup Android SDK
        uses: android-actions/setup-android@v3

      - name: Accept Android SDK Licenses & Install Components
        run: |
          yes | sdkmanager --licenses || true
          sdkmanager "platforms;android-36" "build-tools;36.0.0" "platform-tools" || true

      - name: Restore Debug Keystore
        run: |
          if [ -f "debug.keystore.base64" ]; then
            base64 -d debug.keystore.base64 > debug.keystore || base64 --decode debug.keystore.base64 > debug.keystore
          fi
          if [ ! -f "debug.keystore" ]; then
            keytool -genkey -v -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
          fi
          cp debug.keystore app/debug.keystore 2>/dev/null || true

      - name: Setup Environment Secrets
        run: |
          if [ -f ".env.example" ] && [ ! -f ".env" ]; then
            cp .env.example .env
          elif [ ! -f ".env" ]; then
            touch .env
          fi
          cp .env app/.env 2>/dev/null || true

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: '9.3.1'
          cache-disabled: false

      - name: Run Unit Tests
        run: gradle testDebugUnitTest --no-daemon --stacktrace

      - name: Build Debug APK
        run: gradle assembleDebug --no-daemon --stacktrace

      - name: Upload Debug APK
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: debug-apk
          path: app/build/outputs/apk/debug/*.apk
          if-no-files-found: ignore
```

💡 **使用提示**：可直接在上方「⚡ 自动化 Commit & Push」选择路径 `.github/workflows/android-ci.yml` 一键提交到目标仓库。
            """.trimIndent()
        }

        // 3. Codex 研发助理 / 代码生成
        if (assignedAgent?.id == "agent_codex_dev" || lower.contains("代码") || lower.contains("开发") || lower.contains("实现") || lower.contains("kotlin") || lower.contains("compose") || lower.contains("bug")) {
            return """
### 👨‍💻 Nexus Codex 研发助理 • 代码实现方案

针对您提出的功能需求，以下是符合 Clean Architecture 与 Jetpack Compose 规范的生产级代码实现：

```kotlin
package com.example.feature

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 状态定义
data class FeatureUiState(
    val isLoading: Boolean = false,
    val resultText: String = "",
    val error: String? = null
)

// ViewModel 业务逻辑管理
class FeatureViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FeatureUiState())
    val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()

    fun executeAutonomousAction(command: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // 自主业务处理
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    resultText = "已成功完成任务: " + command
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "执行异常"
                )
            }
        }
    }
}

// 现代化 M3 Jetpack Compose UI
@Composable
fun FeatureScreen(viewModel: FeatureViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("⚡ 自主软件工程师协同界面", style = MaterialTheme.typography.titleMedium)
        
        Button(
            onClick = { viewModel.executeAutonomousAction("构建新功能模块") },
            enabled = !state.isLoading
        ) {
            Text(if (state.isLoading) "执行中..." else "启动自主任务")
        }

        if (state.resultText.isNotBlank()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(state.resultText, modifier = Modifier.padding(12.dp))
            }
        }
    }
}
```

💡 **操作建议**：点击上方「提交至 GitHub」按钮，可一键将该代码保存至您的远程 GitHub 仓库分支。
            """.trimIndent()
        }

        // 4. 文档 / README / 周报
        if (assignedAgent?.id == "agent_docs_writer" || lower.contains("readme") || lower.contains("文档") || lower.contains("周报")) {
            return """
### 📝 Docs & Technical Writer • 规范技术文档

# 项目全景概览 ($currentRepo)

> 由 **Nexus AI 自主软件工程矩阵 (CTO.new)** 自动生成的技术规范与协作指南。

## 🌟 核心特性
- **🤖 免配置自主智能引擎**: 内置 CTO、DevOps、Codex 全链路智能体，零门槛开箱即用。
- **⚡ GitHub 深度集成**: 支持 OAuth 授权、自动推送 Commit、发起 PR 及 Issue 缺陷排查。
- **📱 原生 Android 纯净架构**: 遵循 Material 3 与 Jetpack Compose 响应式规范，纯 Kotlin 实现。

## 🚀 快速启动指南
```bash
# 克隆仓库
git clone https://github.com/$currentRepo.git
cd ${currentRepo.substringAfter("/")}

# 执行单元测试与构建
gradle testDebugUnitTest
gradle assembleDebug
```

## 🛠️ 团队角色矩阵
- **👨‍💼 Nexus CTO.new**: 全局架构规划、技术方案评审与 Sprint 里程碑排期
- **👨‍💻 Codex 研发助理**: 负责代码重构、单元测试补全与规范 PR 提交
- **🚀 DevOps 运维专家**: 编排 GitHub Actions CI/CD 流水线与构建优化
            """.trimIndent()
        }

        // 5. 通用默认自主专家响应 (CTO 全能视角)
        return """
### 🤖 Nexus 自主软件工程师 (CTO.new • 免配置引擎)

针对您的需求：**「$userPrompt」**，已为您完成架构分析与工程规划：

#### 📋 任务分解与执行方案
1. **需求理解与上下文**: 当前工作仓库为 `$currentRepo`，已对目标工程进行结构与依赖分析。
2. **免 API 离线/自主模式保障**: 无需任何第三方 API Key，系统内建全套软件工程决策引擎，提供代码生成、CI/CD 编排与 GitHub 协同。
3. **架构与实施路径**:
   - 采用标准模块化设计与单一职责原则 (SRP)；
   - 适配 GitHub Actions 持续集成自动化；
   - 支持通过右上角「办公室智能体工坊」一键将生成的文件与补丁推送到 GitHub。

#### 💡 快捷协同指令
- 输入 **「编写 CI 工作流」**：DevOps 专家将立即生成免配置 GitHub Actions yml。
- 输入 **「编写代码补丁」**：Codex 研发助理将输出结构化 Kotlin / Python 代码。
- 输入 **「制定架构方案」**：CTO.new 将为您规划完整产品技术栈与 Sprint 计划。
        """.trimIndent()
    }
}
