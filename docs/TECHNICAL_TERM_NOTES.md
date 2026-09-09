# Technical Term Notes

## Purpose

When explaining technical, scientific, programming, Android, AI, or engineering concepts, automatically add a short beginner-friendly note when a technical term first appears.

This is a project communication and documentation convention for Nexus development.

## Format

Use:

**Technical Name（这是什么/什么意思）**

Keep the explanation short and practical.

## Examples

- **Kotlin（编程语言：用于开发 Android App）**
- **API（接口：让不同软件互相沟通）**
- **Agent（智能代理：负责理解任务并执行操作）**
- **Repository（代码仓库：存放项目代码的地方）**
- **CI（自动检查：自动编译和测试代码）**
- **Gradle（项目构建工具：负责编译、依赖和打包）**
- **Jetpack Compose（Android UI 工具：用来制作 App 界面）**

## Rules

1. 第一次出现专业术语时自动解释。
2. 已经解释过的术语，后面可以直接使用。
3. 解释要简短、适合编程初学者。
4. 不改变原本的 technical name（专业名称）。
5. 不需要把每个普通英文单词都解释。
6. Science name（科学名称）和 technical name（专业名称）都适用。
7. 如果术语非常明显，可以使用一句非常短的解释。
8. 如果用户问「这个是什么」，再提供更详细的解释。
9. 说明代码、架构、Android、AI、网络、GitHub、科学或工程概念时，优先使用这个格式。
10. 这是解释与文档风格规范，不要求在源代码中的每个符号旁都添加注释。

## Language

- Prefer Traditional Chinese when communicating with the project owner.
- Keep original English technical names where useful for searching documentation and code.
- The note should normally be written in Chinese.

## Goal

The project owner can learn Nexus development while building the project, without needing to already understand professional terminology.

This convention should make technical discussions easier to follow while preserving accurate professional names.
