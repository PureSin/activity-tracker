# Activity Tracker 

## Product Requirements Document: Activity Tracker

This document outlines the core requirements for the  Activity Tracker Android app, focusing on activity definition, execution, and refined progress tracking.

### 1. Goal

To create a simple Android application that allows users to define and execute custom interval-based activities (e.g., workouts, study sessions) and track their completion with detailed progress.

### 2. Core Functionality

### 2.1. Activity Management

- **Activity List (Home Screen):** Displays all user-defined activities. Tapping an activity navigates to its details.
- **Activity Detail View:** Shows the activity's name and a sequential list of its intervals.
    - Each **interval** has:
        - An optional `name` (string).
        - A `duration` (integer) with a `duration_unit` ("seconds" or "minutes").
        - An optional `rest_duration` (integer) with a `rest_duration_unit` ("seconds" or "minutes").
    - **Actions:** "Start Activity," "Edit Activity" (via config), "Delete Activity."
- **Activity & Interval Definition:** All activities and their intervals are defined externally via a **YAML configuration file**.
    - **YAML Structure:** The file will contain a top-level `activities` list. Each activity object will have a `name` and an `intervals` list. Each interval object will have the properties defined above.
    - **Example YAML (simplified):**YAML
        
        `activities:
          - name: 7 Min Workout
            intervals:
              - name: Jumping Jacks
                duration: 30
                duration_unit: seconds
                rest_duration: 5
                rest_duration_unit: seconds
              - name: Wall Sit
                duration: 45
                duration_unit: seconds`
        
    - **Parsing:** The app will parse this YAML file using the **Kaml library** at startup or when activities are loaded.
    - **File Location:** The YAML config file should be easily accessible in a user-friendly location (e.g., app-specific public directory or `Downloads` folder).

### 2.2. Activity Execution (Timer)

- **Interval Timer View:** Displays the current interval's name, a countdown timer, and a progress indicator (e.g., progress bar).
- **Actions:** **"Pause/Resume"** and **"Skip"** (to next interval).
- **Rest Periods:** Automatically begins a rest timer after each interval if `rest_duration` is defined.
- **Auditory Cues:** Distinct sounds for interval start/end, rest start/end, and activity completion. Progress beeps (e.g., last 3 seconds of an interval).
- **Screen On:** The device screen remains on while an activity timer is active.

### 2.3. Activity Tracking

The app will log each activity session with detailed completion status, including overall duration and percentage progress.

- **Data Logged per Session:**
    - `activity_name`: String
    - `start_timestamp`: Long (Unix milliseconds)
    - `end_timestamp`: Long (Unix milliseconds) - *This captures the actual start and end time, including any pauses.*
    - `total_intervals_in_activity`: Int
    - `intervals_completed`: Int
    - `overall_progress_percentage`: Float (Calculated as `(intervals_completed / total_intervals_in_activity) * 100`).
    - `had_pauses`: Boolean (True if the timer was paused at any point during the session).
- **Completion Status Classification (for daily summary/display):**
    1. **`COMPLETED_FULL`**: `intervals_completed` == `total_intervals_in_activity` AND `had_pauses` is `false`.
    2. **`COMPLETED_FULL_WITH_PAUSE`**: `intervals_completed` == `total_intervals_in_activity` AND `had_pauses` is `true`.
    3. **`COMPLETED_EARLY`**: `completion_type` == `CompletionType.EARLY` (User manually finished activity early).
    4. **`PARTIAL_COMPLETION`**: All other cases, including when `intervals_completed` < `total_intervals_in_activity` or when `total_intervals_in_activity` == 0.
- **Display:** History or summary views will show daily activity counts categorized by these statuses, along with overall duration and percentage progress for each logged session.

### 3. Technical Details

- **Platform:** Android (API 24+ recommended).
- **Language:** Kotlin.
- **Configuration Parsing:** **Kaml** library (`com.charleskorn.kaml:kaml`).
    - Kotlin `data class`es will map directly to YAML structure.
    - Use `kotlinx.serialization` for serialization annotations.
- **Local Data Storage:** **Room Persistence Library** for activity session logs. This will provide a robust, queryable database for tracking history.
    - Define a Room `Entity` for the session log with the fields listed under "Data Logged per Session."
- **Timer Implementation:** Android `CountDownTimer` or a custom coroutine-based timer for precise interval management.
- **User Interface:** XML layouts with Jetpack Compose (optional, for modern UI) or traditional Views. Focus on clean, readable design with clear visual and auditory feedback.
- **File I/O:** Standard Android APIs for reading the YAML config file from external storage. Permissions for storage access will be required.

## 🚀 Development & Deployment

### Quick Start
```bash
# Build, install, and launch debug APK (complete workflow)
./gradlew deployDebug

# Build, install, and launch release APK
./gradlew deployRelease
```

### Build Commands
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean
```

### Installation & Deployment
```bash
# Install debug APK on connected device
./gradlew installDebug

# Build, install, and launch (manual approach)
./gradlew assembleDebug installDebug && adb shell am start -n com.example.kelvinma.activitytracker/.MainActivity

# Custom deployment tasks (recommended)
./gradlew deployDebug    # Development workflow
./gradlew deployRelease  # Production deployment
```

### Testing Commands
```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run tests for specific build variant
./gradlew testDebugUnitTest

# Run lint checks
./gradlew lint
```

### Quality Assurance Pipeline
```bash
# Complete QA workflow
./gradlew clean test lint assembleDebug

# Pre-release validation
./gradlew clean test lint assembleRelease
```

## 📱 Current Features

### ✅ Implemented
- **Session Tracking**: Room database with comprehensive activity completion logging
- **Analytics Dashboard**: Advanced metrics and insights with visual charts
- **Material 3 UI**: Modern design with custom app icon and visual feedback
- **Timer System**: CountDownTimer with audio cues and pause/resume functionality
- **Activity Management**: YAML-based configuration with multiple workout types
- **Progress Indicators**: Real-time progress tracking and daily completion status
- **Workout Content**: 
  - Complete 7-minute workout (12 exercises from ACSM study)
  - Comprehensive stretch routine (10 exercises with progressive timing)
  - Study session (Pomodoro-style)

### 🎯 Key Features
- **Duration Display**: Smart time formatting in activity list (e.g., "7m 10s")
- **Daily Completion**: Green checkmarks for activities completed today
- **Database Integration**: Persistent session history with timestamps and progress
- **Reactive UI**: Real-time updates using Flow and collectAsState
- **Custom Icon**: Professional stopwatch-themed app icon
- **Comprehensive Testing**: Unit tests and UI tests with database validation

## 📊 Analytics Dashboard

The Activity Tracker includes a comprehensive analytics screen that provides detailed insights into your activity performance and progress patterns.

### Overview Metrics
- **Total Sessions**: Complete count of all activity sessions performed
- **Completion Rate**: Overall percentage of activities completed successfully
- **Current Streak**: Number of consecutive days with completed activities
- **Time Invested**: Total time spent across all activities with smart formatting

### Completion Analysis
**Visual Breakdown Chart** showing distribution of session outcomes:
- 🟢 **Completed Full**: Activities finished without pauses (optimal performance)
- 🟡 **Completed Full with Pause**: Activities finished but with pauses during execution
- 🟠 **Completed Early**: Activities ended before all intervals were finished
- 🔵 **Partial Completion**: Some intervals completed but session ended early
- 🔴 **Incomplete**: Sessions started but no intervals completed

### Activity Performance Tracking
Per-activity detailed statistics including:
- **Completion Rate**: Success percentage for each specific activity
- **Session Count**: Total attempts vs. successful completions
- **Average Progress**: Mean completion percentage across all attempts
- **Time Spent**: Total duration invested in each activity type

### Streak Analysis
- **Current Streak**: Live tracking of consecutive completion days
- **Longest Streak**: Personal best streak record
- **Weekly Patterns**: Completion trends over recent weeks
- **Consistency Insights**: Performance stability analysis

### Smart Insights & Recommendations
AI-powered suggestions based on your activity patterns:
- 🎯 **Performance Insights**: Recognition of excellent completion rates (80%+)
- 📈 **Improvement Suggestions**: Recommendations for low completion rates (<50%)
- 🏃 **Pattern Recognition**: Early completion trends and duration adjustments
- 💪 **Motivation Boosts**: Encouragement for streak rebuilding and fresh starts

### Data Visualization
- **Responsive Charts**: Visual representation of completion breakdowns
- **Progress Indicators**: Circular progress displays for key metrics
- **Color-Coded Status**: Intuitive color scheme for quick status recognition
- **Real-Time Updates**: Live data refresh with pull-to-refresh functionality

### Navigation & Usability
- **Accessible from Activity List**: Quick access via navigation
- **Back Navigation**: Seamless return to main app flow
- **Refresh Control**: Manual data refresh capability
- **Error Handling**: Graceful degradation with partial data loading

The analytics system automatically tracks all activity sessions and provides actionable insights to help improve consistency, identify patterns, and maintain motivation for long-term fitness and productivity goals.

### 🏗️ Architecture
- **MVVM Pattern**: ViewModel with database integration
- **Room Database**: Activity session persistence with reactive queries
- **Jetpack Compose**: Modern UI with Material 3 design system
- **Repository Pattern**: Clean data access layer
- **YAML Configuration**: Flexible activity definition system
- **Analytics Engine**: Advanced data processing with completion status classification
