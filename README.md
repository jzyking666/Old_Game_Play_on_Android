# RetroArcade

[English](./README.md) | [Chinese](./README.zh-CN.md)

RetroArcade is a small Android collection of retro games I grew up with.

The idea is simple: no ads, no stamina, no account system, no complicated onboarding. Just open the app and start playing.

## Current Games

- `Classic Snake`
  A straightforward old-school Snake with simple rules and multiple control styles.

- `Offline Dino`
  A Chrome Dino inspired runner with jump, duck, birds, night mode, and a visual style that stays close to the original feel.

- `Ball Downshaft`
  A survival descent game inspired by old Flash-era "downshaft" style games.

- `Classic Tetris`
  A stripped-back classic Tetris with no hold, no ghost piece, and no hard drop.

- `Classic Minesweeper`
  A Windows XP style minesweeper with classic difficulties, flagging, and quick open support.

## What This Project Is Trying To Be

This is not meant to become a huge "feature-rich" game app.

The goal is to build a clean retro game box around games that:

- start fast
- feel familiar right away
- are simple to understand
- still feel good after dozens of runs

## Tech Stack

- Kotlin
- Jetpack Compose
- Navigation Compose
- ViewModel
- DataStore

## Build And Run

Requirements:

- Android Studio
- JDK 17+
- Android SDK 34

Run in Android Studio:

1. Open this folder in Android Studio
2. Let Gradle Sync finish
3. Run the `app` module on a device or emulator

Gradle:

```bash
./gradlew assembleDebug
```

Windows:

```powershell
.\gradlew.bat assembleDebug
```

## Project Structure

Main source folders:

- `app/src/main/java/com/retro/arcade/app`
- `app/src/main/java/com/retro/arcade/core`
- `app/src/main/java/com/retro/arcade/feature`

Game features:

- `feature/snake`
- `feature/dino`
- `feature/downshaft`
- `feature/tetris`
- `feature/minesweeper`

Each game is kept fairly separate so rules, rendering, and future fixes stay manageable.

## Current State

This is the second playable version of the project.

What is already in:

- 5 playable games
- home navigation between games
- saved best scores / best time
- basic engine tests for core rules

What I may keep improving:

- sound and vibration
- more polish on visuals and controls
- more retro games
- better test coverage

## Assets And Attribution

- `Offline Dino` uses Chromium's offline runner sprite sheet as a visual reference asset
- See [NOTICE.md](./NOTICE.md) for attribution details

## Why I'm Open-Sourcing It

Because a lot of these small games are worth keeping around.

They are simple, direct, and fun in a way that still holds up. This project is my way of collecting that feeling in one app.
