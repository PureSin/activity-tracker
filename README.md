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
    3. **`PARTIAL_COMPLETION`**: `intervals_completed` > 0 AND `intervals_completed` < `total_intervals_in_activity`. (User ended early, or app closed prematurely after at least one interval).
    4. **`NO_ACTIVITY_STARTED`**: No log entry for the activity on a given day, or `intervals_completed` was 0.
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

## Building and Testing

### Build the app
```
./gradlew assembleDebug
```

### Run unit tests
```
./gradlew test
```

### Run instrumentation tests
```
./gradlew connectedAndroidTest
```
