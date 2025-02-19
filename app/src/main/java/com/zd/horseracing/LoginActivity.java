package com.zd.horseracing;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashSet;
import java.util.Set;

/**
 * The LoginActivity handles user authentication and navigation to the registration screen.
 * It manages user input for email and password, provides password visibility toggling,
 * and validates credentials against stored data (SharedPreferences) and default credentials.
 */
public class LoginActivity extends AppCompatActivity {

    // UI elements
    private EditText etEmail, etPassword;
    private ImageView togglePassword;
    private boolean isPasswordVisible = false; // Flag to track password visibility
    private Button btnLogin;
    private TextView btnDontHaveAccount;

    // Shared preferences for storing user data
    private SharedPreferences sharedPreferences;
    private MediaPlayer musicBg;

    // Constants for default credentials and shared preferences keys
    private static final String DEFAULT_EMAIL = "user@m.c";
    private static final String DEFAULT_PASSWORD = "123456";
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String EMAIL_SET_KEY = "emails";

    /**
     * Called when the activity is being destroyed.  Releases resources held by the MediaPlayer.
     * It's important to release MediaPlayer to avoid memory leaks.
     */
    @Override
    protected void onDestroy() {
        if (musicBg != null) {
            try {
                musicBg.stop(); // Stop playback
                musicBg.release(); // Release resources
            } catch (Exception e) {
                e.printStackTrace(); // Log any exceptions
            } finally {
                musicBg = null; // Set to null for garbage collection
            }
        }
        super.onDestroy();
    }

    /**
     * Called when the activity is first created. This is where most initialization should go:
     * calling setContentView(int) to inflate the activity's UI, using findViewById(int)
     * to programmatically interact with widgets in the UI, and configuring event listeners.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down then this Bundle contains the data it most recently
     *                           supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews(); // Initialize UI elements
        initializeMusic(); // Initialize and start background music
        setupListeners();  // Set up event listeners for UI interactions
        disableBackButton(); // Disable the back button to prevent navigation issues
    }

    /**
     * Initializes UI components and retrieves SharedPreferences.
     */
    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        togglePassword = findViewById(R.id.ivTogglePassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnDontHaveAccount = findViewById(R.id.btnDontHaveAccount);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    /**
     * Initializes and starts the background music.  Uses a try-catch block to handle
     * potential exceptions during MediaPlayer setup.
     */
    private void initializeMusic() {
        try {
            musicBg = MediaPlayer.create(this, R.raw.loginbackground);
            if (musicBg != null) {  // Check for null (e.g., if resource is missing)
                musicBg.setLooping(true); // Set the music to loop
                musicBg.start(); // Start playing the music
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log any exceptions that occur
        }
    }

    /**
     * Sets up click listeners for the interactive UI elements:
     * - Password visibility toggle button.
     * - "Don't have an account?" text (navigates to registration).
     * - Login button.
     */
    private void setupListeners() {
        togglePassword.setOnClickListener(v -> togglePasswordVisibility());
        btnDontHaveAccount.setOnClickListener(v -> navigateToRegister());
        btnLogin.setOnClickListener(v -> handleLogin());
    }

    /**
     * Toggles the visibility of the password field.  Updates the input type and
     * the visibility toggle icon accordingly.  Preserves the cursor position.
     */
    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible; // Toggle the visibility flag
        int selectionStart = etPassword.getSelectionStart(); // Get current cursor start position
        int selectionEnd = etPassword.getSelectionEnd();     // Get current cursor end position

        if (isPasswordVisible) {
            // Show password
            etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            togglePassword.setImageResource(R.drawable.ic_eye_open); // Update icon
        } else {
            // Hide password
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            etPassword.setTransformationMethod(new PasswordTransformationMethod()); // Apply password mask
            togglePassword.setImageResource(R.drawable.ic_eye_closed); // Update icon
        }

        // Restore cursor position after changing input type
        etPassword.setSelection(selectionStart, selectionEnd);
    }

    /**
     * Navigates to the RegisterActivity and finishes the current activity.
     */
    private void navigateToRegister() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        finish(); // Prevent user from returning to login screen via back button
    }

    /**
     * Handles the login process.  Retrieves user input, validates it,
     * and checks credentials.  If successful, navigates to the InstructionActivity.
     */
    private void handleLogin() {
        String emailInput = etEmail.getText().toString().trim(); // Get email, remove whitespace
        String passwordInput = etPassword.getText().toString().trim(); // Get password, remove whitespace

        if (validateInput(emailInput, passwordInput)) { // Validate the user's input
            if (checkAccount(emailInput, passwordInput)) { // Check credentials
                Intent intent = new Intent(LoginActivity.this, InstructionActivity.class);
                startActivity(intent);
                finish(); // Prevent user from returning to login screen
            } else {
                showToast("Incorrect password or account!"); // Show error message
            }
        }
    }

    /**
     * Validates the user's email and password input.  Checks for:
     * - Empty fields
     * - Valid email format (using Patterns.EMAIL_ADDRESS)
     * - Minimum password length
     * - Account existence (in SharedPreferences or default credentials)
     *
     * @param email    The user's email address.
     * @param password The user's password.
     * @return True if the input is valid, false otherwise.
     */
    private boolean validateInput(String email, String password) {
        // Check for empty fields
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showToast("Please enter all information!");
            return false;
        }

        // Check for valid email format
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Invalid email!");
            return false;
        }

        // Check for minimum password length
        if (password.length() < MIN_PASSWORD_LENGTH) {
            showToast("Incorrect password or account!"); // Security: Don't reveal length requirement
            return false;
        }

        // Check if the account exists (either in SharedPreferences or as the default account)
        Set<String> savedEmails = sharedPreferences.getStringSet(EMAIL_SET_KEY, new HashSet<>());
        if (!savedEmails.contains(email) && !email.equals(DEFAULT_EMAIL)) {
            showToast("Account does not exist!");
            return false;
        }

        return true; // All validations passed
    }

    /**
     * Checks if the provided email and password match stored credentials.
     * Checks against both default credentials and credentials stored in SharedPreferences.
     *
     * @param email    The user's email address.
     * @param password The user's password.
     * @return True if the credentials are valid, false otherwise.
     */
    private boolean checkAccount(String email, String password) {
        Set<String> savedEmails = sharedPreferences.getStringSet(EMAIL_SET_KEY, new HashSet<>());

        // Check against default credentials OR SharedPreferences
        return (email.equals(DEFAULT_EMAIL) && password.equals(DEFAULT_PASSWORD)) ||
                (savedEmails.contains(email) &&
                        password.equals(sharedPreferences.getString(email + "_password", "")));
    }

    /**
     * Displays a short Toast message to the user. Includes a check to prevent showing
     * the Toast if the activity is finishing, which can cause a crash.
     *
     * @param message The message to display in the Toast.
     */
    private void showToast(String message) {
        if (!isFinishing()) { // Check if the activity is still active
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Disables the back button by adding a callback that does nothing when the back button is pressed.
     * This prevents the user from navigating back to previous activities in unexpected ways.
     */
    private void disableBackButton() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Do nothing (back button is disabled)
            }
        });
    }
}