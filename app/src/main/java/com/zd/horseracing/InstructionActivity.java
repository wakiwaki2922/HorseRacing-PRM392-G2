package com.zd.horseracing;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Displays the instruction screen for the horse racing game. Provides a "Next" button to proceed
 * to the main game activity (`MainActivity`). Handles background music and disables the back button.
 */
public class InstructionActivity extends AppCompatActivity {
    private Button btnNext;
    private MediaPlayer bgMusic;
    private boolean isActivityDestroyed = false; // Flag to track if activity is destroyed

    /**
     * Called when the activity is first created. Initializes the UI, sets up the "Next" button's
     * click listener to navigate to `MainActivity`, starts the background music, and disables
     * the back button.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Otherwise it is null.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instruction_layout);

        initializeBackgroundMusic(); // Initialize and start background music
        initializeNextButton();    // Initialize the "Next" button
        disableBackButton();     // Disable the back button
    }

    /**
     * Initializes and starts the background music.  Handles potential exceptions during MediaPlayer setup.
     */
    private void initializeBackgroundMusic() {
        try {
            bgMusic = MediaPlayer.create(this, R.raw.guildlinebeat);
            if (bgMusic != null) { // Check for null (e.g., if resource is missing)
                bgMusic.setLooping(true); // Loop the background music
                bgMusic.start();    // Start playing the music
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception (best practice)
            // Consider adding a Toast message or other UI feedback if music fails
        }
    }

    /**
     * Initializes the "Next" button and sets its click listener to navigate to the MainActivity.
     * Includes a null check for the button to prevent potential crashes.
     */
    private void initializeNextButton() {
        btnNext = findViewById(R.id.btnNext);
        if (btnNext != null) { // Null check to prevent crashes
            btnNext.setOnClickListener(view -> navigateToMainActivity());
        }
    }

    /**
     * Navigates to the MainActivity and finishes the current InstructionActivity.
     */
    private void navigateToMainActivity() {
        Intent intentToMain = new Intent(InstructionActivity.this, MainActivity.class);
        startActivity(intentToMain);
        finish(); // Finish InstructionActivity to prevent going back
    }

    /**
     * Disables the back button by adding a callback that does nothing. This prevents
     * the user from accidentally navigating back from the instruction screen.
     */
    private void disableBackButton() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Do nothing (back button is disabled)
            }
        });
    }

    /**
     * Called when the activity is paused. Pauses the background music to conserve resources.
     * We pause when the activity is no longer visible to the user.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (bgMusic != null && bgMusic.isPlaying()) {
            bgMusic.pause(); // Pause music playback
        }
    }

    /**
     * Called when the activity is resumed (becomes visible again).  Resumes background music
     * playback *only if* the activity was not destroyed.  This ensures we don't restart
     * music if the user navigated away and came back via a new activity instance.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (bgMusic != null && !isActivityDestroyed) { // Check if activity was destroyed
            bgMusic.start(); // Resume music playback
        }
    }

    /**
     * Called when the activity is being destroyed.  Releases the MediaPlayer resources to prevent
     * memory leaks.  Sets the `isActivityDestroyed` flag to prevent resuming music in `onResume()`.
     */
    @Override
    protected void onDestroy() {
        isActivityDestroyed = true; // Set flag to indicate destruction
        if (bgMusic != null) {
            try {
                if (bgMusic.isPlaying()) {
                    bgMusic.stop(); // Stop playback
                }
                bgMusic.release(); // Release resources
                bgMusic = null; // Set to null for garbage collection
            } catch (Exception e) {
                e.printStackTrace(); // Log any exceptions
            }
        }
        super.onDestroy();
    }
}