# RetroArcade

[English](./README.md) | [简体中文](./README.zh-CN.md)

RetroArcade is an Android project built around a simple idea: bring back the instant joy of old-school mini games.

It is not trying to be a large modern mobile game. Instead, it focuses on the kind of games that launch fast, explain themselves in seconds, and stay fun because their rules are clean and direct.

The current first playable version already includes 3 games:

- `Classic Snake`
- `Offline Dino`
- `Ball Downshaft`

## Highlights

- `Clear retro identity`: the project is intentionally inspired by keypad-phone games, Flash-era web games, and lightweight arcade classics.
- `Instant play`: offline, single-player, simple rules, fast feedback.
- `Expandable structure`: new games can be added without rewriting the whole app.
- `Open-source friendly`: gameplay logic is separated into clear `engine` and `ui` layers for easier maintenance.

## Current Games

### 1. Classic Snake

The pure classic Snake formula.

- Eat food to grow
- Crash into walls or yourself to lose
- Difficulty selection
- D-pad and swipe controls
- Multiple visual themes

### 2. Offline Dino

A Chromium offline runner inspired recreation.

- Classic monochrome visual direction
- Jump and duck controls
- High / mid / low flying birds
- Night mode, score display, and game-over presentation
- Chromium official sprite sheet used as the visual reference asset

### 3. Ball Downshaft

Inspired by early “Downshaft / falling ball platform” style games.

- Move the ball left and right
- Survive by landing on safe platforms
- Normal platforms are safe, spike platforms deal damage
- Heart pickups restore health when you are below max health
- Difficulty rises over time, but the pacing is tuned for longer runs

## Tech Stack

- `Kotlin`
- `Jetpack Compose`
- `Navigation Compose`
- `ViewModel`
- `DataStore`
- Single-activity architecture

## Project Structure

Main source folders:

- `app/src/main/java/com/retro/arcade/app`
- `app/src/main/java/com/retro/arcade/core`
- `app/src/main/java/com/retro/arcade/feature`

Game features are split into:

- `feature/snake`
- `feature/dino`
- `feature/downshaft`

Each game is generally organized into:

- `model`: state and configuration
- `engine`: pure gameplay logic
- `ui`: Compose screens and rendering

## Build Locally

### Requirements

- Android Studio
- JDK `17+`
- Android SDK `34`

### Run in Android Studio

1. Open the project folder in Android Studio
2. Wait for `Gradle Sync` to finish
3. Run the `app` module on an emulator or a real device

### Gradle Commands

macOS / Linux:

```bash
./gradlew assembleDebug
```

Windows:

```powershell
.\gradlew.bat assembleDebug
```

## Current Status

This repository is the first playable prototype of the project. The current version already includes:

- Home screen and game navigation
- First playable versions of 3 retro mini games
- Best-score persistence
- Basic gameplay engine tests

Planned future improvements may include:

- Sound and vibration feedback
- More retro games
- More complete localization
- Broader test coverage
- GitHub showcase screenshots and release notes

## Design Notes

The original direction document is here:

- `docs/retro-arcade-design.md`

## Assets and Attribution

- `Offline Dino` uses Chromium's offline runner sprite sheet as a visual reference asset
- See `NOTICE.md` for attribution details

## Why This Project Exists

If you miss the kind of games that have no ads, no stamina systems, no long onboarding, and no friction between opening the app and actually playing, this project is built exactly for that feeling.

It is not just a single-game demo. It is the foundation of a growing retro game box.
