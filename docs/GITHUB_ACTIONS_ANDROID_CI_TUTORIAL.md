# GitHub Actions Android CI 教學

> 給第一次接觸 GitHub Actions / Gradle CI 的人看的教學。
>
> 本教學以本專案的 `.github/workflows/android-ci.yml` 為例，教你看懂、修改，以及日後自己新增 CI 功能。

---

## 0. 先知道我們要做什麼

本專案的流程是：

```text
修改 Android 程式
      ↓
push 到 main
      ↓
GitHub Actions 自動啟動
      ↓
設定 JDK / Android SDK / Gradle
      ↓
./gradlew :app:assembleDebug
      ↓
產生 app-debug.apk
      ↓
上傳 GitHub Actions Artifact
      ↓
複製到 public/
      ↓
發布到 GitHub Pages
      ↓
手機可以直接打開網頁下載 APK
```

目前主要 Workflow：

```text
.github/
└── workflows/
    ├── android-ci.yml   ← 建置 APK + 發布 APK
    └── deploy.yml       ← 保留，但只允許手動執行
```

---

# 1. GitHub Actions 是什麼？

可以把 GitHub Actions 想成一台「GitHub 上的自動電腦」。

你把程式 push 上 GitHub 後，它可以自動幫你：

- 編譯 Android App
- 執行測試
- 產生 APK
- 上傳檔案
- 部署網站
- 執行其他指令

所以你不需要每次都在自己的電腦上手動執行全部步驟。

---

# 2. Workflow 檔案在哪裡？

GitHub Actions 的 Workflow 通常放在：

```text
.github/workflows/
```

例如：

```text
.github/workflows/android-ci.yml
```

副檔名可以是 `.yml` 或 `.yaml`。

---

# 3. `name`：Workflow 的名字

```yaml
name: Android CI
```

這只是 Workflow 顯示在 GitHub Actions 頁面上的名稱。

例如 GitHub Actions 頁面會看到：

```text
Android CI
```

你可以改成：

```yaml
name: Build Android APK
```

這不會直接改變 Android App 本身，只是改 Workflow 的名稱。

---

# 4. `on`：什麼時候執行？

目前：

```yaml
on:
  push:
    branches: [ "main" ]
  pull_request:
    branches: [ "main" ]
```

意思是：

### Push 到 main

```text
git push
   ↓
main 被更新
   ↓
Workflow 執行
```

### Pull Request 到 main

有人建立或更新 PR，準備合併到 `main` 時，也可以觸發 Workflow。

---

## 4.1 只想在 push 時執行

可以寫：

```yaml
on:
  push:
    branches: [ "main" ]
```

---

## 4.2 想手動執行

```yaml
on:
  workflow_dispatch:
```

這樣 GitHub Actions 頁面會出現 **Run workflow**。

---

# 5. `permissions`：Workflow 有什麼權限？

目前：

```yaml
permissions:
  contents: write
```

這讓 Workflow 可以對 Repository 的內容進行需要寫入權限的操作。

我們的 Pages 發布步驟需要這個權限設定。

> 注意：權限越少越好。只有確實需要寫入 Repository 時才給 `write`。

---

# 6. `jobs`：要做哪些工作？

```yaml
jobs:
  build:
```

可以理解成：

```text
jobs
 ↓
build 工作
```

一個 Workflow 可以有很多 Job，例如：

```yaml
jobs:
  build:
    ...

  test:
    ...

  deploy:
    ...
```

目前 Android 專案主要先做 `build`。

---

# 7. `runs-on`：在哪台電腦跑？

```yaml
runs-on: ubuntu-latest
```

意思是使用 GitHub 提供的 Ubuntu runner。

你可以把它想像成：

```text
GitHub
  ↓
開一台臨時 Linux 電腦
  ↓
執行你的 Workflow
```

---

# 8. `steps`：一步一步做事情

```yaml
steps:
  - ...
  - ...
  - ...
```

Workflow 裡真正執行工作的地方就是 `steps`。

可以把它想成教電腦做菜：

```text
Step 1：拿程式碼
Step 2：準備 Java
Step 3：準備 Android SDK
Step 4：準備 Gradle
Step 5：Build
Step 6：上傳 APK
Step 7：發布 APK
```

---

# 9. `uses` 和 `run` 的差別

這兩個一定要分清楚。

## `uses`

```yaml
uses: actions/checkout@v4
```

代表使用別人已經寫好的 GitHub Action。

例如：

```yaml
uses: actions/checkout@v4
```

就是使用 Checkout Action。

## `run`

```yaml
run: ./gradlew :app:assembleDebug
```

代表直接在 runner 的終端機執行指令。

簡單記：

```text
uses = 使用現成工具
run  = 自己下指令
```

---

# 10. Checkout：把 Repository 拿到 runner

```yaml
- uses: actions/checkout@v4
```

沒有這一步，runner 不會自動擁有你的 Repository 原始碼。

完成後，runner 才可以看到：

```text
app/
gradle/
gradlew
build.gradle.kts
gradle/libs.versions.toml
...
```

---

# 11. 設定 JDK 17

```yaml
- name: Set up JDK 17
  uses: actions/setup-java@v4
  with:
    java-version: '17'
    distribution: 'temurin'
```

Android / Gradle 建置需要 Java。

這裡指定 Java 17。

### `with` 是什麼？

```yaml
with:
  java-version: '17'
```

`with` 就是把設定參數傳給 `uses` 使用的 Action。

所以：

```text
uses
 ↓
actions/setup-java
 ↓
with
 ↓
Java 17
```

---

# 12. 設定 Android SDK

```yaml
- name: Set up Android SDK
  uses: android-actions/setup-android@v3
```

這會準備 Android 建置需要的 SDK 環境。

---

# 13. 設定 Gradle

```yaml
- name: Set up Gradle
  uses: gradle/actions/setup-gradle@v3
```

這是為 Gradle 建置準備環境與快取等功能。

---

# 14. 讓 `gradlew` 可以執行

```yaml
- name: Make Gradle Wrapper executable
  run: chmod +x ./gradlew
```

Linux 上檔案需要 executable 權限才能直接執行。

所以：

```bash
chmod +x ./gradlew
```

就是「允許執行 `gradlew`」。

---

# 15. 建立 `.env`

目前 Workflow 有：

```yaml
- name: Create .env file with GitHub Secret
  run: echo "GEMINI_API_KEY=${{ secrets.GEMINI_API_KEY }}" > .env
```

這裡有兩個重要概念。

## GitHub Secret

```text
secrets.GEMINI_API_KEY
```

代表 GitHub Repository Settings 裡設定的 Secret。

## `.env`

這一步把 Secret 放進 runner 的 `.env`，讓 Gradle Secrets Plugin 可以讀取。

> 不要把真正的 API Key 寫進 `.yml`、`.kt`、README 或 commit。真正的 Secret 應放在 GitHub Secrets。

---

# 16. Build Debug APK ⭐

最重要的一步：

```yaml
- name: Build Debug APK
  run: ./gradlew :app:assembleDebug --stacktrace --no-daemon
```

真正的核心指令是：

```bash
./gradlew :app:assembleDebug
```

意思是叫 Gradle 建立 Debug APK。

產物通常會在：

```text
app/build/outputs/apk/debug/app-debug.apk
```

---

## 16.1 `--stacktrace` 是什麼？

```bash
--stacktrace
```

如果 Build 失敗，可以提供比較完整的錯誤資訊，方便除錯。

---

## 16.2 `--no-daemon` 是什麼？

```bash
--no-daemon
```

表示這次 Gradle 建置不使用長時間留在背景的 Gradle Daemon。

在 CI 環境常見這種寫法。

---

# 17. Build Scan：如果你想看建置分析

如果你說的 **Build Scan** 是 Gradle 的 Build Scan，可以在 Build 指令最後加：

```bash
--scan
```

例如：

```yaml
- name: Build Debug APK
  run: ./gradlew :app:assembleDebug --stacktrace --no-daemon --scan
```

它和普通的 Build 不一樣：

```text
普通 Build
↓
只負責建置

Build + --scan
↓
建置
+
建立 Gradle Build Scan
```

> Build Scan 可能涉及把建置資訊上傳到 Gradle 的服務。使用前應了解其資料分享與隱私設定。

---

# 18. Upload Artifact

```yaml
- name: Upload APK artifact
  uses: actions/upload-artifact@v4
  with:
    name: app-debug
    path: app/build/outputs/apk/debug/app-debug.apk
```

這一步把 APK 保存成 GitHub Actions Artifact。

你可以在：

```text
GitHub
→ Actions
→ 某次 Workflow Run
→ Artifacts
```

看到：

```text
app-debug
```

Artifact 下載通常會是 ZIP 包裝檔。

---

# 19. 為什麼還要 `public/`？

因為我們不只想讓 GitHub Actions 保存 APK。

我們還希望：

```text
手機
 ↓
GitHub Pages 網頁
 ↓
直接點 APK
```

所以建立：

```yaml
- name: Prepare APK for GitHub Pages
  run: |
    mkdir -p public
    cp app/build/outputs/apk/debug/app-debug.apk public/app-debug.apk
```

---

# 20. `run: |` 是什麼？

```yaml
run: |
  command1
  command2
  command3
```

代表下面可以寫多行 Shell 指令。

例如：

```yaml
run: |
  mkdir -p public
  cp app-debug.apk public/app-debug.apk
```

等於依序執行：

```bash
mkdir -p public
cp app-debug.apk public/app-debug.apk
```

---

# 21. `mkdir -p public`

```bash
mkdir -p public
```

建立 `public` 資料夾。

結果：

```text
public/
```

---

# 22. `cp`：複製 APK

```bash
cp app/build/outputs/apk/debug/app-debug.apk public/app-debug.apk
```

意思是：

```text
原本：
app/build/outputs/apk/debug/app-debug.apk

複製到：
public/app-debug.apk
```

所以：

```text
public/
└── app-debug.apk
```

---

# 23. `index.html`

我們另外建立一個簡單網頁：

```html
<h1>Agent APK</h1>
<p><a href="./app-debug.apk">Download app-debug.apk</a></p>
```

所以 Pages 網頁可以顯示：

```text
Agent APK

Download app-debug.apk
```

點連結就是 APK。

---

# 24. 發布 GitHub Pages

```yaml
- name: Publish APK to GitHub Pages
  uses: peaceiris/actions-gh-pages@v3
  with:
    github_token: ${{ secrets.GITHUB_TOKEN }}
    publish_dir: ./public
```

這一步把：

```text
public/
├── index.html
└── app-debug.apk
```

發布到 GitHub Pages。

---

# 25. `github_token` 是什麼？

```yaml
github_token: ${{ secrets.GITHUB_TOKEN }}
```

這不是你自己建立的 API Key。

GitHub Actions 會提供這個 Workflow Token，讓 Workflow 可以在 Repository 裡做被允許的操作。

所以不要把自己的 GitHub Personal Access Token 寫進 Workflow。

---

# 26. `publish_dir` 是什麼？

```yaml
publish_dir: ./public
```

意思是：

> 把 `./public` 裡面的內容拿去發布。

因此：

```text
public/index.html
public/app-debug.apk
```

會成為 Pages 的內容。

---

# 27. `deploy.yml` 為什麼改成手動？

我們目前讓它使用：

```yaml
on:
  workflow_dispatch:
```

意思是只有你手動按 **Run workflow** 才執行。

原因是目前 `android-ci.yml` 已經負責：

```text
Build APK
 ↓
Prepare public/
 ↓
Publish Pages
```

如果另一個 Workflow 也在每次 push 時自動發布，就可能互相覆蓋 Pages。

所以正常流程只需要：

```text
push main
 ↓
android-ci.yml
 ↓
Build
 ↓
Publish Pages
```

---

# 28. 以後想改 Workflow，要改哪裡？

## 想改觸發條件

看：

```yaml
on:
```

## 想改 Android Build

看：

```yaml
run: ./gradlew ...
```

## 想改 APK 放哪裡

看：

```yaml
cp ... public/...
```

## 想改 Pages 發布內容

看：

```yaml
publish_dir: ./public
```

## 想改下載網頁

修改：

```text
public/index.html
```

---

# 29. Debug APK 和 Release APK

目前：

```bash
./gradlew :app:assembleDebug
```

產生 Debug APK。

以後如果設定好 Release signing，可以使用：

```bash
./gradlew :app:assembleRelease
```

兩者用途不同：

```text
Debug
↓
開發 / 測試

Release
↓
正式發布
```

不要只因為檔名變成 `release` 就直接當成已經適合正式發布；Release 還需要正確的 signing / build 設定。

---

# 30. 最常見的錯誤

## 錯誤 1：YAML 縮排錯了

YAML 很重視縮排。

例如：

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
```

不要隨便改成：

```yaml
jobs:
build:
runs-on: ubuntu-latest
```

---

## 錯誤 2：路徑寫錯

如果 APK 實際在：

```text
app/build/outputs/apk/debug/app-debug.apk
```

但你寫：

```text
app/build/outputs/apk/app-debug.apk
```

Copy 就會失敗。

---

## 錯誤 3：Gradle Build 失敗

先看：

```text
GitHub
→ Actions
→ 失敗的 Run
→ build
```

找到第一個真正的錯誤，而不是只看最後一行。

---

## 錯誤 4：Secret 不存在

如果 Workflow 使用：

```text
secrets.GEMINI_API_KEY
```

Repository 必須有對應的 GitHub Secret。

不要把真正的 Key 直接貼到公開 Repository。

---

# 31. 「我改一個檔案，會發生什麼？」

例如你修改：

```text
app/src/main/java/.../MainActivity.kt
```

然後：

```text
git add .
 ↓
git commit
 ↓
git push
```

GitHub 收到 push：

```text
on.push
 ↓
Workflow 啟動
 ↓
checkout
 ↓
JDK
 ↓
Android SDK
 ↓
Gradle
 ↓
assembleDebug
 ↓
app-debug.apk
 ↓
Artifact
 ↓
public/
 ↓
GitHub Pages
```

所以你不需要手動重新 Build 再上傳 APK。

---

# 32. 以後如何「更新」而不是 Delete？

對 GitHub 裡的文字檔，例如：

```text
.github/workflows/android-ci.yml
```

正常做法就是直接編輯：

```text
Open file
 ↓
Edit
 ↓
修改內容
 ↓
Commit changes
```

如果使用 GitHub API / Connector：

```text
先取得目前檔案 SHA
        ↓
update_file
        ↓
新的 commit
```

不要為了修改檔案先 Delete 再 Create。

> 注意：這個「update」概念是指 Repository 裡的文字檔。APK 本身是二進位檔，不能用普通的文字 `create_file/update_file` 方式處理。

---

# 33. 建議你學習的順序

如果你想自己慢慢學 GitHub Actions，可以按照：

```text
① name
 ↓
② on
 ↓
③ jobs
 ↓
④ runs-on
 ↓
⑤ steps
 ↓
⑥ uses
 ↓
⑦ run
 ↓
⑧ with
 ↓
⑨ Artifact
 ↓
⑩ GitHub Pages
 ↓
⑪ Secrets
 ↓
⑫ Build Scan
```

先完全看懂這個專案的 `android-ci.yml`，再自己從零寫一個 Workflow，會比較容易。

---

# 34. 一張圖記住全部

```text
┌───────────────────────────────┐
│       GitHub Repository        │
│                               │
│   push main                   │
└───────────────┬───────────────┘
                ↓
┌───────────────────────────────┐
│       GitHub Actions           │
│                               │
│  checkout                      │
│     ↓                          │
│  JDK 17                        │
│     ↓                          │
│  Android SDK                   │
│     ↓                          │
│  Gradle                        │
│     ↓                          │
│  assembleDebug                 │
└───────────────┬───────────────┘
                ↓
        app-debug.apk
                │
        ┌───────┴────────┐
        ↓                ↓
     Artifact         public/
                          │
                   ┌──────┴──────┐
                   ↓             ↓
              index.html    app-debug.apk
                   │             │
                   └──────┬──────┘
                          ↓
                   GitHub Pages
                          ↓
                    📱 下載 APK
```

---

## 最後記住

```text
Workflow = 自動化流程

on       = 什麼時候跑
jobs     = 做什麼工作
steps    = 一步一步怎麼做
uses     = 使用現成 Action
run      = 執行 Shell 指令
with     = 傳設定給 Action
Artifact = 保存建置產物
Pages    = 發布網頁 / 檔案
Secrets  = 保存敏感資料
```

你以後看到任何 `.github/workflows/*.yml`，都可以先用這幾個問題拆解它：

1. **什麼時候跑？** → `on`
2. **在哪裡跑？** → `runs-on`
3. **做什麼？** → `jobs`
4. **每一步做什麼？** → `steps`
5. **是現成工具還是 Shell 指令？** → `uses` / `run`
6. **產生什麼檔案？** → `path`
7. **最後發布去哪裡？** → `publish_dir` / deploy step
