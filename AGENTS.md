# AGENTS.md

## 0. 环境

当前的环境是:

- OS: windows11
- 命令行: 可能是cmd或者powershell
- python编程环境：你可以使用uv来使用和管理python虚拟环境，如果本项目需要依赖python辅助构建、测试你应该使用uv在本项目的./.venv创建专属虚拟环境；推荐使用已经存在的python3.12版本
- nodejs编程环境：你可以使用npm、npx、node等相关命令来运行js脚本
- Java编程环境：当前是openjdk 17
  - gradle：使用项目本地的gradle.bat
- android编程环境：可以使用android cli来管理安卓项目；android sdk目录在D:\soft\Android\Sdk，你可以通过环境遍历ANDROID_HOME获悉
- c/c++：我在`C:\msys64`安装了MSYS2；`C:\msys64\mingw64\bin`已经加入了PATH环境变量，其下的`gcc` `g++` `clang` `clang++` `cmake` `clang-cl` `gdb`等可执行程序可直接调用；`C:\msys64\usr\bin`也已经加入了PATH环境变量

## 项目协作约定

本项目目录名为 `J4Ledger`，是通过 Android CLI 创建的 Android 工程。后续开发时先阅读当前目录下的文档和构建配置，再按实际代码状态执行改动。

## 工具使用约定

- Android 相关诊断、SDK 管理、设备、模拟器、截图、布局检查和官方文档查询，优先考虑 `android` CLI。
- 构建、测试和依赖解析优先使用项目自带 Gradle Wrapper：`.\gradlew.bat`。
- 不要求所有任务都强制走 Android CLI；如果 Gradle、IDE 生成配置或项目脚本是更准确的入口，应按项目现状选择工具。
- 查询 Android 官方能力、API 和最佳实践时，可使用 `android docs search` 和 `android docs fetch`。
- 使用 `android describe --project_dir=.` 可以识别工程结构和构建产物，但该命令可能触发 Gradle 分析并生成 `build/` 目录内容。

## 文档维护约定

- `README.md`：记录项目简介、环境要求、启动方式、常用命令和目录说明。
- `CHANGELOG.md`：记录功能、修复、文档和工程化变更。用户可见行为、工程结构或协作约定变化时应更新。
- `技术文档.md`：记录技术栈、模块结构、架构说明、关键依赖和扩展设计。
- `关键链路.md`：记录创建、开发、构建、运行、测试、发布等关键流程。

## 变更要求

- 修改功能代码时，同步判断是否需要更新 `README.md`、`技术文档.md` 和 `关键链路.md`。
- 调整构建、运行、SDK、依赖、目录结构或项目命名时，同步更新相关文档。
- 完成功能、修复、文档或工程配置变更后，在 `CHANGELOG.md` 添加对应记录。
- 保持文档内容简洁、可执行，命令示例优先使用 PowerShell 可直接运行的形式。
- 不要假设目录名、Gradle 项目名、应用显示名、包名和 `applicationId` 必须始终一致；涉及这些命名变更时，应明确修改范围并同步验证。
