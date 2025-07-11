# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Activity Tracker is an Android application for managing and executing interval-based activities (workouts, study sessions, etc.). The app uses YAML configuration files to define activities and their intervals, implements a timer system with audio cues, and tracks session data in a Room database.

## Development Commands

### Build Commands
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean
```

### Testing Commands
```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run tests for specific build variant
./gradlew testDebugUnitTest
```

### Development Tasks
```bash
# Install debug APK on connected device
./gradlew installDebug

# Run lint checks
./gradlew lint

# Generate test coverage report
./gradlew jacocoTestReport
```

### Deployment Commands
```bash
# Build, install, and launch debug APK (complete workflow)
./gradlew deployDebug

# Build, install, and launch release APK
./gradlew deployRelease

# Alternative: Manual command for build, install, and launch
./gradlew assembleDebug installDebug && adb shell am start -n com.example.kelvinma.activitytracker/.MainActivity
```

## Architecture Overview

### Core Components

- **Data Layer**: Room database for session tracking, YAML-based activity configuration
- **UI Layer**: Jetpack Compose with navigation between three main screens
- **Timer System**: CountDownTimer-based implementation with audio feedback
- **Repository Pattern**: ActivityRepository handles YAML parsing and data access

### Key Data Models

- `Activity`: Represents a complete activity with name and intervals list
- `Interval`: Individual timed segments with duration, optional rest periods
- `ActivitySession`: Database entity tracking session completion data
- `TimerViewModel`: Manages timer state and session lifecycle

### Navigation Structure

The app uses Jetpack Navigation with three main destinations:
- `activityList`: Home screen showing all available activities
- `activityDetail/{activityName}`: Details view for a specific activity
- `timer/{activityName}`: Timer execution screen

### Configuration System

Activities are defined in YAML files located in `app/src/main/assets/`. Each YAML file represents a single activity with this structure:

```yaml
name: "Activity Name"
intervals:
  - name: "Interval Name"
    duration: 30
    duration_unit: "seconds"
    rest_duration: 10
    rest_duration_unit: "seconds"
```

The app automatically discovers and loads all `.yaml` files from the assets directory using the Kaml library.

### Audio System

The timer uses MediaPlayer for audio feedback:
- `interval_start.mp3`: Played when an interval begins
- `interval_end.mp3`: Played when an interval completes
- `progress_beep.mp3`: Played during final 3 seconds of intervals
- `activity_complete.mp3`: Played when entire activity finishes

Audio files are located in `app/src/main/res/raw/`.

### Database Schema

Session tracking uses Room with a single entity:
- `ActivitySession`: Stores completion data including timestamps, progress percentage, pause status
- Database name: `activity_tracker_database`
- Single DAO: `ActivitySessionDao` for CRUD operations

### Testing Strategy

- **Unit Tests**: Located in `app/src/test/` for data models and business logic
- **Instrumented Tests**: Located in `app/src/androidTest/` for UI components and database operations
- **Screen Testing**: Uses Compose testing framework with screenshot comparison capabilities

## Key Technical Details

### Dependencies
- **UI**: Jetpack Compose with Material 3 design system
- **Database**: Room persistence library with KSP annotation processing
- **Configuration**: Kaml library for YAML parsing with kotlinx.serialization
- **Navigation**: Jetpack Navigation for Compose
- **Testing**: JUnit, Espresso, and Compose UI testing libraries

### Build Configuration
- **Target SDK**: 34
- **Min SDK**: 28
- **Compile SDK**: 35
- **Java Version**: 11
- **Kotlin Version**: 1.9.24

### Package Structure
```
com.example.kelvinma.activitytracker/
├── data/              # Data models, database, and repository
├── ui/
│   ├── activitydetail/    # Activity detail screen
│   ├── activitylist/      # Activity list screen
│   ├── timer/             # Timer screen and ViewModel
│   └── theme/             # Compose theme configuration
└── MainActivity.kt        # App entry point and navigation setup
```

## Development Notes

### Adding New Activities
1. Create a new YAML file in `app/src/main/assets/`
2. Follow the existing YAML structure format
3. Activities are automatically discovered on app startup

### Modifying Timer Behavior
- Timer logic is centralized in `TimerViewModel`
- Audio cues are configurable via the `playSound()` method
- Session data is automatically persisted to Room database

### Testing Considerations
- Instrumented tests require a connected Android device or emulator
- Screen tests use snapshot comparison for UI validation
- Database tests use in-memory Room database for isolation