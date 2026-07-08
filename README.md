# Screen Time Tracker

A privacy-focused, robust Android Screen Time Tracker designed to measure **actual screen-on time** system-wide, rather than individual app usage. 

### Why this exists
Native tools like Google's Digital Wellbeing track screen time per app. If a user deletes an app, its accumulated screen time often disappears from the daily total. This app acts as an independent system-level tracker, ensuring your total screen-on time is accurately preserved regardless of what you install or uninstall.

---

## 🚀 Features

*   **Total Screen-On Time Tracking:** Monitors system-wide display states rather than individual application packages.
*   **Persistent Logging:** App deletions do not impact or erase your historical screen-on data.
*   **2x1 Home Screen Widget:**
    *   Displays your current tracked screen time right on your home screen.
    *   **Quick-Refresh Button:** A dedicated button in the top corner to refresh the timer instantly without launching the main app.
    *   **Tap-to-Open:** Tapping anywhere else on the widget immediately opens the main application dashboard.

---

## 🛠️ How to Implement & Build This Yourself

If you want to clone this project or rebuild this logic from scratch, follow the structural guide below.

### 1. Prerequisites & Environment
*   **Android Studio** (Ladybug or newer)
*   **Minimum SDK:** API 26 (Android 8.0 Oreo) or higher (required for modern background restrictions and App Widgets)
*   **Language:** Kotlin

### 2. Core Components to Implement

#### A. Tracking the Screen State
To capture actual screen up-time without tracking individual apps, implement a background `Service` combined with a `BroadcastReceiver`.
*   Listen for `Intent.ACTION_SCREEN_ON` and `Intent.ACTION_SCREEN_OFF`.
*   When `SCREEN_ON` triggers, record the start timestamp using `SystemClock.elapsedRealtime()`.
*   When `SCREEN_OFF` triggers, calculate the duration (`currentTime - startTime`) and save it to a local database (**Room API**) or **DataStore**.

#### B. The 2x1 App Widget
Create an `AppWidgetProvider` class to handle the home screen widget behavior.
*   **Layout:** Design a `RemoteViews` layout measuring `2x1` cells. Place a localized `ImageButton` in the top right corner for the refresh action.
*   **Refresh Intent:** Bind a custom `PendingIntent` to the refresh button that sends a specific broadcast to your `AppWidgetProvider`. Inside `onReceive()`, recalculate the latest time from your database and call `appWidgetManager.updateAppWidget()`.
*   **Open App Intent:** Bind a standard activity-launching `PendingIntent` to the main background container of the widget layout.

### 3. Setup Instructions

1.  **Clone the Repository:**
    ```bash
    git clone [https://github.com/KamranUllahGul/Screen-Time-Tracker.git](https://github.com/KamranUllahGul/Screen-Time-Tracker.git)
    ```
2.  **Open in Android Studio:**
    Select **File > Open** and navigate to the cloned directory.
3.  **Sync Gradle:**
    Let Android Studio download the required dependencies (Kotlin Coroutines, Room Database, etc.).
4.  **Run the App:**
    Connect an Android device or emulator and click **Run**.
5.  **Add the Widget:**
    Go to your device's home screen, long-press, select **Widgets**, find **Screen Time Tracker**, and drag the `2x1` widget onto your screen.

---
