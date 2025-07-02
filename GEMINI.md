# Gemini Project Memory

This file contains a summary of the project to help Gemini assist you better.

## Project Overview

- **Project Name:** Activity Tracker
- **Platform:** Android
- **Language:** Kotlin
- **GitHub Repository:** https://github.com/PureSin/activity-tracker

## Core Technologies

- **UI:** Jetpack Compose
- **Data Persistence:** Room Database
- **Configuration:** YAML file parsed with the Kaml library
- **Navigation:** Jetpack Navigation for Compose
- **Testing:** JUnit, Espresso, and Compose Test Rule for instrumentation tests.

## Key Features

- **Activity Definition:** Activities and their intervals are defined in a `activities.yaml` file located in `app/src/main/assets`.
- **UI:** The app has three main screens:
    - `ActivityListScreen`: Displays a list of activities from the YAML file.
    - `ActivityDetailScreen`: Shows the details of a selected activity.
    - `TimerScreen`: A screen for executing an activity with a countdown timer.
- **Data Logging:** Activity sessions are logged to a Room database. The `ActivitySession` entity stores details about each session.
- **Auditory Cues:** The app uses `MediaPlayer` to play sounds for timer events (placeholder sounds are in `app/src/main/res/raw`).
- **Screen On:** The app keeps the screen on during an activity.

## Build and Test Commands

- **Build the app:** `./gradlew assembleDebug`
- **Run unit tests:** `./gradlew test`
- **Run instrumentation tests:** `./gradlew connectedAndroidTest`
