# RetroArcade

[English](./README.md) | [简体中文](./README.zh-CN.md)

RetroArcade 是一个围绕“怀旧小游戏”方向构建的 Android 项目。

它想做的不是复杂的大型现代手游，而是把那种打开就能玩、几秒钟就能理解规则、靠纯粹玩法让人停不下来的老式小游戏重新带回手机里。

当前第一版已经可以直接游玩 3 款游戏：

- `Classic Snake`
- `Offline Dino`
- `Ball Downshaft`

## 项目亮点

- `怀旧气质明确`：整体方向就是按键机时代、Flash 时代、小网页小游戏时代的轻量快乐。
- `打开即玩`：单机离线、规则简单、反馈直接。
- `持续扩展`：项目结构已经为后续新增游戏预留好空间。
- `适合开源维护`：玩法逻辑拆分成清晰的 `engine` 与 `ui` 层，方便后续维护和继续迭代。

## 当前游戏

### 1. Classic Snake

最经典的贪吃蛇玩法。

- 吃到食物就变长
- 撞墙或撞到自己就失败
- 支持难度选择
- 支持方向键和滑动操作
- 支持多种主题风格

### 2. Offline Dino

参考 Chromium 离线小恐龙制作的还原版。

- 经典黑白灰视觉方向
- 支持跳跃和下蹲
- 包含高空 / 中空 / 低空飞鸟
- 有夜间切换、分数显示和结算呈现
- 使用 Chromium 官方 sprite sheet 作为视觉参考素材

### 3. Ball Downshaft

灵感来自早期“下100层 / 小球平台下落”一类老游戏。

- 控制小球左右移动
- 在平台之间不断下落求生
- 普通平台安全，刺平台会扣血
- 心形血包在生命未满时可以回血
- 难度会逐渐提升，但节奏已经做过放缓调整，更适合连续游玩

## 技术栈

- `Kotlin`
- `Jetpack Compose`
- `Navigation Compose`
- `ViewModel`
- `DataStore`
- 单 Activity 架构

## 项目结构

主要代码目录：

- `app/src/main/java/com/retro/arcade/app`
- `app/src/main/java/com/retro/arcade/core`
- `app/src/main/java/com/retro/arcade/feature`

游戏功能拆分为：

- `feature/snake`
- `feature/dino`
- `feature/downshaft`

每个游戏基本按以下结构组织：

- `model`：状态与配置
- `engine`：纯玩法逻辑
- `ui`：Compose 页面与绘制

## 本地构建

### 环境要求

- Android Studio
- JDK `17+`
- Android SDK `34`

### 在 Android Studio 中运行

1. 用 Android Studio 打开项目目录
2. 等待 `Gradle Sync` 完成
3. 运行 `app` 模块到模拟器或真机

### Gradle 命令

macOS / Linux:

```bash
./gradlew assembleDebug
```

Windows:

```powershell
.\gradlew.bat assembleDebug
```

## 当前状态

这个仓库现在是项目的第一版可玩原型，已经完成：

- 首页与游戏导航
- 3 款怀旧小游戏的首个可玩版本
- 最高分持久化
- 基础玩法引擎测试

后续可以继续补充：

- 音效和振动反馈
- 更多怀旧小游戏
- 更完整的多语言支持
- 更全面的测试覆盖
- GitHub 展示截图和版本发布说明

## 设计文档

项目最初的方向设计稿见：

- `docs/retro-arcade-design.md`

## 素材与说明

- `Offline Dino` 使用了 Chromium 的离线小恐龙 sprite sheet 作为视觉参考素材
- 相关署名说明见 `NOTICE.md`

## 为什么做这个项目

如果你也怀念那种没有广告、没有体力、没有复杂教程，打开之后立刻就能开始玩的小游戏体验，这个项目就是为这种感觉而做的。

它不是一个单一游戏 Demo，而是一个可以持续扩展的“怀旧游戏盒”基础版本。
