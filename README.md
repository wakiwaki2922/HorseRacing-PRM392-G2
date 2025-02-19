# Horse Racing Game

This Android application simulates a horse racing game where users can place bets and watch races.

## Features

*   **User Authentication:** Login/registration with password visibility, secure credential storage (`SharedPreferences`), input validation, and default credentials for testing.
*   **Betting System:** Custom dialog for choosing horses and placing bets (validated amounts).  Displays total bet and updates user balance.  Includes a dialog to add money.
*   **Race Simulation:** Horse animations (`AnimationDrawable`, `SeekBar`), randomized progress, countdown, background music, sound effects, and a result dialog.
*   **UI/UX:** Instructions screen, disabled back button in key activities, toast messages, and lifecycle-aware resource management (`MediaPlayer`).
*   **Architecture:** Uses `ViewModel`, `LiveData`, `Repository`, and `SharedPreferences`.

## Classes

*   **`LoginActivity`:** User login.
*   **`RegisterActivity`:** User registration.
*   **`InstructionActivity`:** Game instructions.
*   **`MainActivity`:** Main game screen.
*   **`RaceViewModel`:** ViewModel for `MainActivity`. Manages the UI state, user interaction, and communication with the `RaceRepository`.  Handles user balance, bets, race start/stop/reset, and result calculation.
*   **`HorseBet`:** (Model) Represents a bet placed on a horse. Contains horse number and bet amount.
*   **`RaceRepository`:** Handles data operations, including managing the user's balance and current bets.  Provides an abstraction layer for data access.

## Libraries Used

*   androidx.appcompat:appcompat
*   com.google.android.material:material
*   androidx.activity:activity
*   androidx.constraintlayout:constraintlayout
*   androidx.lifecycle:lifecycle-viewmodel
*   androidx.lifecycle:lifecycle-livedata
*   androidx.core:core-ktx (implicitly)

## Setup

1.  Clone the repository.
2.  Open in Android Studio.
3.  Build and run.

## Notes

*   Includes sound and image resources in `res/raw` and `res/drawable`.
*   Background music loops using `MediaPlayer.setLooping(true)`.
*   Back button disabled in key activities.
*   Minimum SDK: 24.

## Improvements

*   **Network Integration:** Fetch data from a server, cloud saves.
*   **More Realistic Race Simulation:** Horse stats, track conditions, jockey skills.
*   **Betting Options:** More betting types.
*   **UI/UX Enhancements:**  Improved visuals, `RecyclerView` for horses.
*   **Sound Management:** Dedicated `SoundManager` class.
*   **Testing:** Unit and UI tests.
*   **Error Handling:** More robust error handling.
*   **Dependency Injection:** Hilt or Koin.
*   **Code Refactoring:** Cleaner, best-practice code.
*   **Kotlin Coroutines:** Replace `Handler`.
* **Data Persistence**: Currently uses `SharedPreferences` within the repository. Could be improved with a dedicated data source (Room Database for local storage or a remote API).