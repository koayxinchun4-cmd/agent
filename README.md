# 🤖 Nexus AI

A mobile-first AI Agent for Android, built with **Kotlin, Jetpack Compose, and Gemini**.

Nexus is designed to understand user goals, decompose tasks, select models and tools, execute safely, verify results, and improve through Skills and Memory.

## ✨ What Nexus Is Building

- 🧠 **Agent Core** — intent understanding, task decomposition, planning, model routing, tool calling, verification, and recovery.
- 📱 **Mobile Agent** — Android-safe File and App execution with explicit permissions and user confirmation for risky actions.
- 🤖 **Multi-Model Intelligence** — Gemini and a provider abstraction designed for additional models and local fallback paths.
- 🧰 **Skills + Memory** — reusable skills, conversation/project/task memory, and user preferences with local-first persistence.
- 🌐 **Real-World Agents** — Web Research, Office, GitHub, and coding-oriented workflows.
- 🧭 **Agent Timeline UI** — a structured execution timeline that shows understanding, planning, execution, verification, and completion.
- 🌍 **Multilingual UX** — English-first project content with support planned for Traditional Chinese, Bahasa Melayu, and mixed-language input.

## 🖼️ UI Concept

![Nexus AI UI concept](docs/images/nexus-ui-mockup.jpg)

The interface is designed around a task timeline rather than chat alone, making agent activity and execution state easier to understand on a phone.

## 🧱 Architecture Direction

```text
User Goal
   ↓
Intent Understanding
   ↓
Task Decomposition
   ↓
Planner
   ↓
Model Router ─────→ AI Providers / Local Models
   ↓
Tool Registry
   ↓
Permission + Risk Checks
   ↓
Tool Execution
   ↓
Verification
   ↓
Result / Recovery
```

The Android app remains **phone-first, permission-aware, and Root-free**. Android framework behavior is kept behind explicit boundaries so pure application logic can remain fast and testable on the JVM.

> **Independent Project Notice**
>
> Nexus AI is an independent personal project. It is not officially affiliated with, partnered with, authorized by, or endorsed by Marvis AI, MyNexusAI, or their operators, developers, or related brands.

## 🛠️ Tech Stack

| Area | Technology |
| :--- | :--- |
| Language | Kotlin 2.2.10 |
| Android | Android Gradle Plugin 8.7.3 / compileSdk 35 |
| UI | Jetpack Compose / Material 3 |
| State | ViewModel + StateFlow |
| Database | Room 2.7.1 + KSP |
| Networking | Retrofit 2 + OkHttp + Moshi |
| AI | Google Gemini API + provider abstraction |
| Async | Kotlin Coroutines |
| CI | GitHub Actions |

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/koayxinchun4-cmd/agent.git
cd agent
```

### 2. Configure Gemini API Key (optional)

Nexus uses the Secrets Gradle Plugin to read the Gemini API key from a root `.env` file.

```bash
cp .env.example .env
```

Then edit `.env`:

```env
GEMINI_API_KEY=your_GEMINI_API_KEY
```

`.env` is ignored by Git. **Never commit a real API key to GitHub.**

If no valid API key is available, Nexus can fall back to its local autonomous engine path where supported.

## 📦 Local Build

Requirements:

- JDK 17
- Android SDK 35
- A recent stable Android Studio release is recommended

Run JVM unit tests:

```bash
./gradlew testDebugUnitTest
```

Build a Debug APK:

```bash
./gradlew :app:assembleDebug
```

APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 🤖 GitHub Actions

The Android CI workflow is located at `.github/workflows/android-ci.yml`.

The workflow validates pushes to `main` and Pull Requests targeting `main`. The validation path includes unit tests, lint, Debug APK packaging, APK validation, and Gradle wrapper checks.

For CI builds that use Gemini, configure the repository secret:

```text
GEMINI_API_KEY
```

CI injects the secret into a temporary `.env` during the build. The real key is not stored in the repository.

## 🔐 Security & Execution Principles

- Never commit `.env` or real API keys.
- Never hard-code Gemini or GitHub credentials in source code.
- User credentials and tokens must use secure local storage and stay within the integration that needs them.
- Android permissions are explicit: tools do not bypass or silently request permissions.
- Risky or irreversible actions require user confirmation before execution.
- File operations validate paths and SAF boundaries before opening resources.
- App execution validates package names and uses explicit Android launch intents.
- **Pure logic is tested on the JVM. Android-specific behavior is validated in Android Runtime.**

See [`docs/engineering-principles.md`](docs/engineering-principles.md) for the JVM vs Android Runtime testing rule.

## 📂 Project Structure

```text
├── app/
│   ├── src/main/
│   │   ├── java/com/example/       # Android/Kotlin source
│   │   ├── res/                    # Android resources
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml          # Version Catalog
├── .github/
│   └── workflows/
│       └── android-ci.yml          # Android CI
├── docs/
│   ├── engineering-principles.md  # Engineering and testing rules
│   └── images/
│       └── nexus-ui-mockup.jpg     # UI concept
├── .env.example                    # API key template
├── .gitignore
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
└── README.md
```

## 🗺️ Roadmap

The current roadmap is maintained in [`CURRENT_ROADMAP.md`](CURRENT_ROADMAP.md).

Major milestones:

1. **Agent Core Reliability**
2. **Mobile Agent**
3. **Multi-Model Intelligence**
4. **Skills + Memory**
5. **Real-World Work Agents**
6. **Nexus Agent Studio**
7. **Product Experience**

Development follows small, verifiable delivery gates: implement one capability, add focused tests, run CI, confirm the product contract, then continue.

## 📚 Documentation

- [`CURRENT_ROADMAP.md`](CURRENT_ROADMAP.md) — product direction and delivery priorities
- [`docs/engineering-principles.md`](docs/engineering-principles.md) — engineering and testing boundaries
- [`docs/external-references.md`](docs/external-references.md) — external ecosystem references and integration notes
- [`CONTRIBUTING.md`](CONTRIBUTING.md) — contribution guidelines

## 📄 License

Nexus AI is released under the **GNU General Public License v3.0 (GPLv3)**.

Nexus AI is free and open source. You may use, study, modify, fork, and contribute to the project, subject to the applicable GPLv3 terms.

Original project copyright belongs to **koayxinchun4-cmd and project contributors**. Applicable copyright, license, attribution, and modification notices must be preserved when redistributing covered code.

Independent forks, applications, services, and commercial projects may operate independently. Their user data, conversations, files, business data, content, and operational behavior do not automatically become Nexus AI project data merely because they use Nexus AI.

See [`LICENSE`](LICENSE) and the official GNU GPLv3 text for the complete license terms.

## 🤝 Community

**Nexus AI is free and open source — everyone is welcome to join us.**

Issues, Pull Requests, feature ideas, bug fixes, and new Agent / Skill contributions are welcome. Please read [`CONTRIBUTING.md`](CONTRIBUTING.md) before contributing.
