# RetroArcade

[English](./README.md) | [中文](./README.zh-CN.md)

RetroArcade 是一个把童年怀旧小游戏收进 Android 手机上的小项目。

这个项目的想法很简单：没有广告、没有体力、没有账号系统，也没有复杂的新手引导。打开应用，直接开始玩。

## 当前游戏

- `Classic Snake`
  最朴素的经典贪吃蛇，规则简单，支持不同操作方式。

- `Offline Dino`
  参考 Chrome 小恐龙做的跑酷小游戏，包含跳跃、下蹲、飞鸟和夜间切换。

- `Ball Downshaft`
  灵感来自早期 Flash 时代“下100层”一类小游戏的下落求生玩法。

- `Classic Tetris`
  保留经典规则的俄罗斯方块，没有 Hold、没有 Ghost Piece、没有 Hard Drop。

- `Classic Minesweeper`
  参考 Windows XP 风格制作的经典扫雷。

## 这个项目想做成什么

它不是一个追求“大而全”的手游项目。

我更想做的是一个真正像“游戏盒”一样的东西，里面收着一些：

- 打开就能玩
- 上手不费劲
- 规则很直接
- 玩很多次也不会腻

的老式小游戏。

## 技术栈

- Kotlin
- Jetpack Compose
- Navigation Compose
- ViewModel
- DataStore

## 构建与运行

环境要求：

- Android Studio
- JDK 17+
- Android SDK 34

在 Android Studio 中运行：

1. 用 Android Studio 打开当前目录
2. 等待 Gradle Sync 完成
3. 运行 `app` 模块到模拟器或真机

Gradle 命令：

```bash
./gradlew assembleDebug
```

Windows:

```powershell
.\gradlew.bat assembleDebug
```

## 项目结构

主要代码目录：

- `app/src/main/java/com/retro/arcade/app`
- `app/src/main/java/com/retro/arcade/core`
- `app/src/main/java/com/retro/arcade/feature`

当前游戏模块：

- `feature/snake`
- `feature/dino`
- `feature/downshaft`
- `feature/tetris`
- `feature/minesweeper`

每个游戏尽量独立组织，方便后续继续改玩法、修 bug、加新游戏。

## 当前状态

现在这是项目的第二个可玩版本。

已经完成的内容：

- 5 款可玩的小游戏
- 首页统一入口
- 最佳分数 / 最佳时间保存
- 一些核心规则的基础测试

后面可能会继续做：

- 音效和振动
- 视觉和操作细节打磨
- 更多怀旧小游戏
- 更完整的测试覆盖

## 素材与说明

- `Offline Dino` 使用了 Chromium 离线小恐龙的 sprite sheet 作为视觉参考素材
- 相关说明见 [NOTICE.md](./NOTICE.md)

## 为什么开源

因为这些小游戏很值得留下来。

它们简单、直接、耐玩，而且很多年后再玩，依然有那种很纯粹的乐趣。这个项目就是我把这种感觉重新整理到一起的一种方式。
