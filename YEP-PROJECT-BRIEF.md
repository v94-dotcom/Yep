# Yep. — Project Brief for Claude Code

## What is this document?

This is the complete project brief for **Yep.** — a mobile app that kills the "did I lock the door?" anxiety spiral. Hand this entire file to Claude Code as your starting prompt. Everything Claude Code needs to build the MVP is here.

---

## The App in One Sentence

Yep. lets you tap a button when you do routine things (lock door, turn off stove, take meds), so you can check later and see proof you actually did it — with an optional photo.

---

## Platform & Tech Stack

- **Target**: Android (Google Play Store)
- **IDE**: Android Studio
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Min SDK**: 26 (Android 8.0 — covers 95%+ of active devices)
- **Target SDK**: 35
- **Local storage**: Room database for items, daily records, and settings
- **Photo storage**: App-internal file storage (context.filesDir). Photos are private, never uploaded.
- **Notifications**: WorkManager for scheduled daily reminders + NotificationCompat
- **Navigation**: Jetpack Navigation Compose with bottom nav bar
- **Architecture**: MVVM (ViewModel + Repository + Room)
- **Dependency Injection**: Hilt
- **Camera**: CameraX for photo capture
- **Build system**: Gradle with Kotlin DSL (build.gradle.kts)

---

## Brand Identity

### Name & Tagline
- **Name**: Yep.
- **Tagline**: "The answer before the question."
- **Package name**: `com.yep.app` (or your preferred domain)

### Personality
Yep. is never anxious. It is the antidote to anxiety. The app is a calm friend who simply confirms: "Yes, you did." It never scolds, warns, guilt-trips, or uses exclamation marks.

### Voice Rules
- **We say**: "Yep, you locked the door." / "All good. Stove's off." / "Nice — 14 days."
- **We never say**: "Don't forget to..." / "WARNING: unconfirmed!" / "You missed 3 items!"
- No negative states. If something isn't confirmed yet, it says "Not yet today" — never "Missed" or "Failed."

### Colors
```kotlin
// Theme.kt / Color.kt
val GreenPrimary = Color(0xFF1D9E75)     // buttons, confirmed states, checkmarks
val GreenLight = Color(0xFF5DCAA5)       // accents, secondary elements
val Mint = Color(0xFFE1F5EE)             // confirmed card backgrounds
val Charcoal = Color(0xFF2C2C2A)         // primary text, dark mode background
val WarmGrayLight = Color(0xFFF1EFE8)    // unconfirmed button fills
val NeutralGray = Color(0xFF888780)      // secondary text
val BorderGray = Color(0xFFD3D1C7)       // dashed borders, dividers
val Surface = Color(0xFFFAFAF8)          // app background (light mode)
val DangerRed = Color(0xFFE24B4A)        // delete button only
val DangerRedLight = Color(0xFFFCEBEB)   // delete button background
val GreenDark = Color(0xFF0F6E56)        // confirmed subtitle text
val GreenDarkest = Color(0xFF085041)     // confirmed label text
```

### Dark Mode Colors
```kotlin
val DarkBackground = Color(0xFF2C2C2A)
val DarkSurface = Color(0xFF3A3A37)
val DarkTextPrimary = Color(0xFFF1EFE8)
val DarkTextSecondary = Color(0xFF888780)
val DarkBorder = Color(0xFF444441)
```

### Typography
- **Font**: DM Sans (import via Google Fonts / downloadable fonts, or bundle .ttf in res/font)
- **Weights**: 400 (body), 500 (labels), 600 (headings, numbers)
- Use Compose `Typography` with custom `TextStyle` definitions

### Corner Radius
- Cards & items: 16.dp
- Buttons inside cards: 12.dp
- Pill badges: 99.dp
- Circular elements: CircleShape

---

## Screens & Features (MVP)

### Screen 1: Today (Home)

This is the main screen. It shows all items for today.

**Layout:**
- Header: Yep. logo + app icon (left), edit button (subtle pencil icon, top-right). Confirmation counter badge "3/5" next to edit button.
- Date subtitle below header
- Scrollable LazyColumn of items
- "+ Add something" button at bottom

**Item States (Normal Mode):**

1. **Unconfirmed** — White card, dashed border (BorderGray), item label in Charcoal, subtitle "Not yet today" in light gray. Right side: empty circle. Tapping anywhere on the card expands it.

2. **Expanded (choosing)** — White card, solid green border (2dp, GreenPrimary). Shows item label at top, then two side-by-side buttons below:
   - Left button: Green background (GreenPrimary), white checkmark icon + "Yep" label. Tapping confirms immediately.
   - Right button: Mint background, camera icon + "Yep + photo" label. Tapping opens the camera.
   - Tapping outside the card collapses it back to unconfirmed.

3. **Confirmed (no photo)** — Mint background. Label in GreenDarkest. Subtitle "Yep — [time]" in GreenDark. Right side: solid green circle with white checkmark.

4. **Confirmed (with photo)** — Same as confirmed, plus a small "photo" pill badge next to the timestamp. Tapping the card opens the photo viewer.

**Edit Mode:**

Triggered by tapping the pencil icon in the header. The header shows "Edit items" title and "Done" text button (green).

Each item in edit mode shows:
- The item label (center)
- **Uncheck button** (left side): Only visible on confirmed items. Undo icon on light gray background. Tapping resets item to unconfirmed and removes associated photo.
- **Delete button** (right side): Red trash icon on DangerRedLight background. Tapping shows inline confirmation: card turns light red with "Cancel" and "Delete" buttons.

Tapping "Done" exits edit mode.

**Adding Items:**
- Tapping "+ Add something" shows inline TextField with "Add" button and close button.
- New item appears at the bottom in unconfirmed state.
- Items persist via Room database.

### Screen 2: Camera (Photo Proof)

Full-screen camera view using CameraX. Opens when user taps "Yep + photo."

**Layout:**
- Dark background
- Top bar: close button (X, top-left), item name in a green pill badge (center)
- CameraX Preview in center
- Bottom: "Skip" text button (left) + large shutter button (center)

**Flow:**
1. Camera opens. Request CAMERA permission if not yet granted (use Accompanist permissions or ActivityResultContracts).
2. User taps shutter → capture image with ImageCapture → save to app-internal storage (context.filesDir/photos/).
3. Brief white flash overlay animation → checkmark animation → auto-navigate back to Today screen.
4. "Skip" confirms the item without a photo.

**Photo Storage Rules:**
- Save to: `context.filesDir/photos/{itemId}_{timestamp}.jpg`
- Photos auto-delete after 24 hours (cleanup in ViewModel on app launch)
- Not visible in device gallery (app-internal storage)
- No cloud upload, no sharing

### Screen 3: Photo Viewer

Opens when user taps a confirmed item that has a photo.

**Layout:**
- Dark background
- Top bar: back arrow (left), item name + time (center)
- Photo displayed large with rounded corners (load with Coil)
- Timestamp badge overlay on photo (bottom-right)
- Green confirmation badge at bottom

### Screen 4: History

Shows past days' confirmation records.

**Layout:**
- Today's summary card at top (Mint background): date, "X of Y confirmed", percentage
- LazyColumn of past days (white cards, light border)
- Each day shows: date, "X of Y" subtitle, row of small dots (green = confirmed, gray = not)

**Data:**
- Read from Room DailyRecord table
- Keep 30 days of history
- Show "All clear" with full green dots for perfect days

### Screen 5: Streaks

Gamification / habit reinforcement screen.

**Layout:**
- Large circle at top: current streak number + "days" label
- "Current streak" title + encouraging subtitle
- 2-column grid: "Best streak" card + "Completion rate" card
- Weekly dot row: M T W T F S S with green fills for completed days

**Streak Logic:**
- A day is "complete" if ALL items were confirmed
- Streak breaks if any day has unconfirmed items by midnight (device local time)
- Best streak = longest consecutive run ever
- Stored in Room

### Bottom Navigation

Three tabs using NavigationBar (Material 3):
- **Today** (checkmark icon) — home screen
- **History** (calendar icon) — past records
- **Streaks** (clock icon) — streak tracker

Active tab: GreenPrimary. Inactive: NeutralGray.

---

## Notifications (MVP — Optional for User)

During onboarding, ask: "Want a gentle reminder?" with time picker.

- Use WorkManager with PeriodicWorkRequest for daily reminder at user's chosen time
- Notification text rotates between calm messages:
  - "Quick check before you head out?"
  - "Tap your morning list — takes 5 seconds."
  - "Your future self will thank you."
- NEVER use urgent/alarming language
- Create a NotificationChannel on app start

---

## Data Model (Room Entities)

### Item
```kotlin
@Entity(tableName = "items")
data class Item(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val label: String,
    val sortOrder: Int,
    val createdAt: Long = System.currentTimeMillis()
)
```

### Confirmation
```kotlin
@Entity(tableName = "confirmations")
data class Confirmation(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val itemId: String,
    val date: String,          // "2026-04-02"
    val confirmedAt: Long,
    val photoPath: String?     // local file path or null
)
```

### DailyRecord (derived — query, not table)
```kotlin
data class DailyRecord(
    val date: String,
    val confirmedCount: Int,
    val totalItems: Int
)
```

### UserSettings
```kotlin
@Entity(tableName = "settings")
data class UserSettings(
    @PrimaryKey val id: Int = 0,      // singleton row
    val reminderEnabled: Boolean = false,
    val reminderTime: String = "08:30",
    val onboardingComplete: Boolean = false,
    val theme: String = "system"       // "light", "dark", "system"
)
```

### StreakData
```kotlin
@Entity(tableName = "streak")
data class StreakData(
    @PrimaryKey val id: Int = 0,      // singleton row
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastCompleteDate: String? = null
)
```

### DAO
```kotlin
@Dao
interface YepDao {
    // Items
    @Query("SELECT * FROM items ORDER BY sortOrder ASC")
    fun getAllItems(): Flow<List<Item>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: Item)
    
    @Delete
    suspend fun deleteItem(item: Item)
    
    @Query("UPDATE items SET sortOrder = :order WHERE id = :id")
    suspend fun updateSortOrder(id: String, order: Int)

    // Confirmations
    @Query("SELECT * FROM confirmations WHERE date = :date")
    fun getConfirmationsForDate(date: String): Flow<List<Confirmation>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfirmation(confirmation: Confirmation)
    
    @Query("DELETE FROM confirmations WHERE itemId = :itemId AND date = :date")
    suspend fun removeConfirmation(itemId: String, date: String)

    // Settings & Streak
    @Query("SELECT * FROM settings WHERE id = 0")
    fun getSettings(): Flow<UserSettings?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: UserSettings)
    
    @Query("SELECT * FROM streak WHERE id = 0")
    fun getStreak(): Flow<StreakData?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStreak(streak: StreakData)
}
```

### Database
```kotlin
@Database(
    entities = [Item::class, Confirmation::class, UserSettings::class, StreakData::class],
    version = 1
)
abstract class YepDatabase : RoomDatabase() {
    abstract fun yepDao(): YepDao
}
```

---

## Onboarding (First Launch)

Keep it to 3 screens max using a HorizontalPager. No account creation. No sign-up. No email.

**Screen 1**: "You know that feeling..." — Brief relatable description of the problem.

**Screen 2**: "One tap. Total peace of mind." — Show the tap interaction with a demo item.

**Screen 3**: "What do you check every day?" — Pre-filled suggestions with checkboxes:
  - Locked the door
  - Stove off
  - Unplugged iron
  - Took medication
  - Closed windows
  - Turned off lights
  - Fed the pet
  - Custom: [TextField]

User taps "Start" → selected items inserted into Room → navigate to Today screen.

---

## Daily Reset Logic

- On app open, check if the current date differs from the last recorded date.
- If new day: save yesterday's record, update streak, clear today's confirmations.
- Delete photos older than 24 hours from `context.filesDir/photos/`.
- Also schedule this via WorkManager as a daily background task in case the app isn't opened.

---

## Animations & Micro-interactions

Use Compose animation APIs: `animateContentSize()`, `AnimatedVisibility`, `animateColorAsState`, etc.

- **Tap to expand**: `AnimatedVisibility` + `animateContentSize()`, 150ms
- **Confirm**: Animated checkmark icon (scale from 0 to 1), 300ms spring
- **Card transition** (unconfirmed → confirmed): `animateColorAsState` on background, 200ms
- **Camera flash**: White overlay `Box` with `animateFloatAsState` on alpha
- **Edit mode enter/exit**: `AnimatedVisibility` on edit controls, 200ms
- **Delete confirmation**: `animateColorAsState` to DangerRedLight, 150ms
- **Tab switch**: `Crossfade` composable

---

## Accessibility

- All interactive elements have minimum 48.dp touch targets (Material 3 default)
- All icons use `contentDescription` for TalkBack
- Confirmed/unconfirmed states communicated via `semantics { stateDescription = "..." }`
- Color contrast meets WCAG AA
- Support dynamic type scaling

---

## Project Structure

```
app/src/main/java/com/yep/app/
├── MainActivity.kt                    # Single Activity, setContent with Compose
├── YepApplication.kt                  # Hilt Application class
├── di/
│   └── DatabaseModule.kt              # Hilt module providing Room DB + DAO
├── data/
│   ├── YepDatabase.kt                 # Room database
│   ├── YepDao.kt                      # All queries
│   ├── entities/
│   │   ├── Item.kt
│   │   ├── Confirmation.kt
│   │   ├── UserSettings.kt
│   │   └── StreakData.kt
│   └── repository/
│       └── YepRepository.kt           # Repository wrapping DAO
├── ui/
│   ├── theme/
│   │   ├── Color.kt                   # Brand colors
│   │   ├── Type.kt                    # DM Sans typography
│   │   └── Theme.kt                   # Material 3 theme (light + dark)
│   ├── navigation/
│   │   └── YepNavigation.kt           # NavHost + bottom bar setup
│   ├── today/
│   │   ├── TodayScreen.kt             # Main screen composable
│   │   ├── TodayViewModel.kt          # ViewModel for Today
│   │   ├── ItemCard.kt                # Single item (all states)
│   │   ├── ItemCardEdit.kt            # Item in edit mode
│   │   └── AddItemInput.kt            # Add item composable
│   ├── camera/
│   │   ├── CameraScreen.kt            # CameraX viewfinder + capture
│   │   └── CameraViewModel.kt
│   ├── photo/
│   │   └── PhotoViewerScreen.kt       # Full-screen photo view
│   ├── history/
│   │   ├── HistoryScreen.kt
│   │   └── HistoryViewModel.kt
│   ├── streaks/
│   │   ├── StreaksScreen.kt
│   │   └── StreaksViewModel.kt
│   └── onboarding/
│       ├── OnboardingScreen.kt        # HorizontalPager onboarding
│       └── OnboardingViewModel.kt
├── util/
│   ├── PhotoManager.kt                # Save/delete/cleanup photos
│   ├── DateUtils.kt                   # Date formatting helpers
│   └── NotificationHelper.kt          # Channel creation + scheduling
└── worker/
    └── DailyResetWorker.kt            # WorkManager task for midnight reset

app/src/main/res/
├── font/
│   ├── dm_sans_regular.ttf
│   ├── dm_sans_medium.ttf
│   └── dm_sans_semibold.ttf
├── mipmap-*/
│   └── ic_launcher.png                # App icon (green square, white checkmark)
└── values/
    └── strings.xml
```

---

## Key Dependencies (build.gradle.kts)

```kotlin
// Compose BOM
implementation(platform("androidx.compose:compose-bom:2025.01.00"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.ui:ui-tooling-preview")

// Navigation
implementation("androidx.navigation:navigation-compose:2.8.0")

// Room
implementation("androidx.room:room-runtime:2.7.0")
implementation("androidx.room:room-ktx:2.7.0")
ksp("androidx.room:room-compiler:2.7.0")

// Hilt
implementation("com.google.dagger:hilt-android:2.51")
ksp("com.google.dagger:hilt-compiler:2.51")
implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

// CameraX
implementation("androidx.camera:camera-core:1.4.0")
implementation("androidx.camera:camera-camera2:1.4.0")
implementation("androidx.camera:camera-lifecycle:1.4.0")
implementation("androidx.camera:camera-view:1.4.0")

// Coil (image loading)
implementation("io.coil-kt:coil-compose:2.7.0")

// WorkManager
implementation("androidx.work:work-runtime-ktx:2.10.0")

// Accompanist (permissions)
implementation("com.google.accompanist:accompanist-permissions:0.36.0")
```

---

## What NOT to Build (MVP Scope Control)

Do NOT include these in the MVP. They're ideas for v2+:

- User accounts / sign-in / cloud sync
- Sharing or social features
- Multiple "routines" (morning, evening, leaving work) — MVP has one flat list
- Smart home integrations
- Wear OS companion
- Home screen widget (design the data layer for it, but don't build yet)
- AI suggestions ("you usually forget the stove on Mondays")
- In-app purchases or subscriptions (MVP is 100% free)
- Analytics or tracking SDKs (respect user privacy completely)

---

## Build Order (Suggested Sequence for Claude Code)

**IMPORTANT: After every step, the app must be runnable.** Do not build components in isolation. Each step should result in visible, working UI when you run the app.

1. **Project scaffold**: Android Studio project with Kotlin, Jetpack Compose, Material 3, Hilt setup. App launches to a screen with bottom nav (Today, History, Streaks tabs). Each tab shows its name as placeholder text. **Verify: app runs, tabs switch.**

2. **Theme & colors**: Implement Color.kt, Type.kt (DM Sans), Theme.kt with light/dark variants. Apply theme to the scaffold. **Verify: app shows branded colors and fonts.**

3. **Data layer**: Room database with Item, Confirmation, UserSettings, StreakData entities. YepDao, YepRepository, Hilt DatabaseModule. **Verify: app still runs (no UI change yet, but DB initializes without crash).**

4. **Today screen — basic**: TodayViewModel + TodayScreen. Show hardcoded default items in a LazyColumn. Each item shows label and "Not yet today" subtitle. Header with Yep. logo and date. **Verify: app runs, Today tab shows items.**

5. **Today screen — confirm flow**: Tap item → expands with "Yep" and "Yep + photo" buttons. Tap "Yep" → item turns green with timestamp. Data persists in Room. **Verify: can confirm items, they stay confirmed after app restart.**

6. **Today screen — add & edit**: Add item input at bottom. Edit mode with pencil button in header. Uncheck and delete functionality. **Verify: can add, uncheck, delete items.**

7. **Camera flow**: CameraScreen with CameraX. Permission handling. Photo capture → save to internal storage → confirm item with photoPath. **Verify: "Yep + photo" button works end to end.**

8. **Photo viewer**: PhotoViewerScreen loads photo from internal storage with Coil. Shows timestamp overlay. **Verify: tapping photo badge on confirmed item opens viewer.**

9. **History screen**: HistoryViewModel queries past DailyRecords. Shows list with dot indicators. **Verify: History tab shows data.**

10. **Streaks screen**: StreaksViewModel calculates current/best streak. Shows streak circle and weekly dots. **Verify: Streaks tab shows data.**

11. **Daily reset**: DailyResetWorker + on-app-open check. Saves yesterday's record, clears today, updates streak, deletes old photos. **Verify: next day (or simulate date change), items reset.**

12. **Onboarding**: OnboardingScreen with HorizontalPager. Item picker. Saves selected items to Room. Only shows on first launch. **Verify: fresh install shows onboarding, subsequent launches skip it.**

13. **Notifications**: NotificationHelper + WorkManager scheduling. Optional daily reminder at user-chosen time. **Verify: notification appears at scheduled time.**

14. **Polish**: Animations (AnimatedVisibility, animateColorAsState), haptic feedback on confirm (HapticFeedbackType.LongPress), final QA.

---

## Reference

The interactive prototype is in `yep-prototype.jsx` (React component). Use it ONLY as a visual reference for spacing, colors, states, and flow. Do NOT use any React/TypeScript code — this is a native Android project using Kotlin and Jetpack Compose.

The prototype demonstrates:
- All item states (unconfirmed, expanded, confirmed, confirmed with photo)
- Camera flow with flash animation
- Photo viewer
- History and Streaks tabs
- Edit mode with uncheck and delete
- Add item functionality

Match the prototype's look and feel as closely as possible using Compose UI.

---

## One Last Thing

The entire philosophy of this app is: **one tap, total peace of mind.** Every feature decision should be filtered through that lens. If something adds complexity without adding calm, cut it. If something makes the user think more, cut it. The app should feel like taking a deep breath.
