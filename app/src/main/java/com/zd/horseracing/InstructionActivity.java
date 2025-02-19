package com.zd.horseracing;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Displays the instruction screen for the horse racing game.  Provides a "Next" button to proceed
 * to the main game activity (`MainActivity`).  Handles background music and disables the back button.
 */
public class InstructionActivity extends AppCompatActivity {
    Button btnNext;
    MediaPlayer bgMusic;

    /**
     * Called when the activity is first created.  Initializes the UI, sets up the "Next" button's
     * click listener to navigate to `MainActivity`, starts the background music, and disables the back button.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Otherwise it is null.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instruction_layout);

        // Initialize and start background music
        bgMusic = MediaPlayer.create(this, R.raw.guildlinebeat);
        bgMusic.setLooping(true);
        bgMusic.start();

        // Initialize the "Next" button and set its click listener
        btnNext = (Button) findViewById(R.id.btnNext);
        btnNext.setOnClickListener(view -> {
            Intent intentToMain  = new Intent(InstructionActivity.this, MainActivity.class);
            startActivity(intentToMain);
            finish(); // Finish InstructionActivity to prevent going back to it
        });

        // Disable back button
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Do nothing to prevent going back
            }
        });
    }

    /**
     * Called when the activity is destroyed. Stops the background music to release resources.
     */
    @Override
    protected void onDestroy() {
        bgMusic.stop(); // Stop the music
        super.onDestroy();
    }
}