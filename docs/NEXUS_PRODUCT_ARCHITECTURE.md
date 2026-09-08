# Nexus 智能助手产品与架构路线

## 产品定义

Nexus 是一款 **Native Android + Kotlin + Jetpack Compose** 的移动端 AI Agent。

核心原则：

- 手机本身就是 Agent 的主要执行环境。
- 不要求电脑。
- 不要求 Root。
- 需要系统能力时，优先使用 Android 正规 API、Intent、Storage Access Framework、Accessibility 等用户明确授权的机制。
- Gemini 是 AI 大脑，但 Nexus 的长期目标不是只有聊天，而是具备规划、工具调用、记忆、Skills 和任务执行能力。
- 面向马来西亚用户，优先支持中文、Bahasa Melayu、English 及自然混合输入。

## 能力地图

### 已有基础

- Jetpack Compose UI
- Gemini API / Retrofit
- Room 本地聊天历史
- GitHub Actions 自动构建 APK
- GitHub Releases / Pages 发布链路
- Nexus 首页、对话页和能力中心
- 对话上下文与 Nexus system instruction

### 下一阶段

1. **Agent Core**
   - Task / Plan / Step 数据模型
   - Agent 状态机
   - 工具注册表（Tool Registry）
   - 用户确认机制
   - 执行结果与错误恢复

2. **Skills**
   - Skill Manifest
   - Skill Registry
   - 内置技能
   - 自定义技能
   - 技能权限与开关

3. **Mobile Agent**
   - 文件访问
   - Android Intent
   - App Launcher
   - Accessibility 辅助操作
   - Notification integration
   - 所有敏感动作必须有明确授权或确认

4. **Web Research**
   - 搜索入口
   - 页面读取
   - 多来源摘要
   - 来源记录

5. **Memory Engine**
   - 用户主动保存的长期记忆
   - 偏好
   - 重要事实
   - 会话摘要
   - 删除与查看记忆

6. **AI Moments / AI 朋友圈**
   - Agent 任务活动
   - 创作内容
   - 用户可控分享
   - 本地优先的数据策略

7. **Office Agent**
   - PDF / 文档 / 表格等移动端工作流
   - 学习资料整理
   - 内容总结与生成

8. **GitHub / Codex**
   - Device Code 登录
   - Repository 浏览
   - Issues / PR
   - 代码辅助
   - 手机端提交代码时必须有明确确认

9. **Voice**
   - Speech-to-Text
   - Text-to-Speech
   - Agent 语音任务

## 空间策略

Nexus 不应该为了“做大”而把所有资源硬塞进 APK。

目标是模块化：

```text
核心 APK
  + 按需启用的 Skills
  + 本地数据库 / Memory
  + 用户产生的 AI Moments 媒体
  + 可选模型或离线资源
```

因此未来即使整个 Nexus 数据目录达到 GB 级，也不代表核心 APK 必须达到 GB 级。

如果以后加入本地 AI 模型，模型应作为可选资源管理，而不是为了体积牺牲核心功能。

## 开发原则

### 不删功能，只替换基础设施

旧功能如果暂时不能实现，应保留产品入口、数据模型或接口，而不是直接删除。

### 不假装 Agent 已经能做某事

UI 可以提前展示规划中的能力，但必须标记为“规划中 / 需要授权 / 尚未接入”。

### Android 优先

不要把 Native Android 项目改造成 Vite / React / Web App 来迁就某个开发环境。

### 增量演进

每次大功能都应该能够独立编译、测试和回滚。

## 当前里程碑

**Milestone: Nexus Foundation**

当前目标不是一次完成所有 Agent 功能，而是先建立一个稳定的 Android Agent 基础，让后续 Skills、Memory、Mobile Agent、Web Research、AI Moments 等模块可以继续叠加。
