package com.zd.horseracing;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles user registration. Allows users to enter an email address, password,
 * and confirm password.  Validates the input, checks for existing accounts,
 * and saves the new account information using SharedPreferences.  Navigates
 * to the LoginActivity upon successful registration.
 */
public class RegisterActivity extends AppCompatActivity {

    // UI elements
    private EditText etEmail, etPassword, etConfirmPassword;
    private ImageView togglePassword;
    private boolean isPasswordVisible = false; // Flag to track password visibility
    private Button btnRegister;

    // Shared preferences for storing user data
    private SharedPreferences sharedPreferences;
    private MediaPlayer musicBg;

    // Constants for validation and shared preferences
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String EMAIL_SET_KEY = "emails";

    /**
     * Called when the activity is first created. Initializes UI components,
     * sets up click listeners, retrieves SharedPreferences, and handles
     * registration logic.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down, this Bundle contains
     *                           the data it most recently supplied in
     *                           onSaveInstanceState(Bundle). Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeViews(); // Initialize UI elements
        setupMusic();      // Initialize and start background music
        setupClickListeners(); // Set up click listeners for UI interactions
    }

    /**
     * Initializes UI components and retrieves SharedPreferences.
     */
    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        togglePassword = findViewById(R.id.ivTogglePassword);
        btnRegister = findViewById(R.id.btnRegister);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    /**
     * Initializes and starts the background music. Handles potential exceptions
     * during MediaPlayer setup.
     */
    private void setupMusic() {
        try {
            musicBg = MediaPlayer.create(this, R.raw.loginbackground);
            if (musicBg != null) { // Check for null (e.g., if resource is missing)
                musicBg.setLooping(true); // Loop the background music
                musicBg.start(); // Start playing
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception (best practice)
        }
    }

    /**
     * Sets up click listeners for the interactive UI elements:
     * - Password visibility toggle button.
     * - Register button.
     * Adds listeners for immediate saving on EditText.
     */
    private void setupClickListeners() {

        etEmail.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                etPassword.requestFocus(); // Move focus
                return true;
            }
            return false;
        });

        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                etConfirmPassword.requestFocus();
                return true;
            }
            return false;
        });

        // Set up password visibility toggle
        togglePassword.setOnClickListener(view -> {
            isPasswordVisible = !isPasswordVisible; // Toggle visibility flag
            togglePasswordVisibility(etPassword);  // Update password field
            togglePasswordVisibility(etConfirmPassword); // Update confirm password field
        });

        // Set up register button
        btnRegister.setOnClickListener(v -> handleRegistration());
    }

    /**
     * Handles the user registration process. Retrieves user input, validates it,
     * checks for existing accounts, saves the new account information, and
     * navigates to the LoginActivity.
     */
    private void handleRegistration() {
        String email = etEmail.getText().toString().trim(); // Get email, remove whitespace
        String password = etPassword.getText().toString().trim(); // Get password, remove whitespace
        String confirmPassword = etConfirmPassword.getText().toString().trim(); // Get confirm password

        // Validate user input
        if (!validateInput(email, password, confirmPassword)) {
            return; // Stop if input is invalid
        }

        // Check if an account with the given email already exists
        if (isAccountExists(email)) {
            showToast("Account already exists!");
            return; // Stop if account exists
        }

        // Save the new account information
        saveAccount(email, password);

        // Display success message
        showToast("Registration successful! Redirecting to login...");

        // Navigate to LoginActivity after a short delay (1 second)
        new android.os.Handler().postDelayed(() -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.putExtra("email", email); // Optional: Pass email to LoginActivity
            intent.putExtra("password", password); // Optional: Pass password
            startActivity(intent);
            finish(); // Finish RegisterActivity to prevent going back
        }, 1000); // 1000ms = 1 second
    }

    /**
     * Toggles the visibility of the password in the given EditText. Updates
     * the input type, transformation method, and the visibility toggle icon.
     * Preserves the cursor position.
     *
     * @param editText The EditText containing the password.
     */
    private void togglePasswordVisibility(EditText editText) {
        int selectionStart = editText.getSelectionStart(); // Preserve cursor start
        int selectionEnd = editText.getSelectionEnd(); // Preserve cursor end
        if (isPasswordVisible) {
            // Show password
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            editText.setTransformationMethod(null); // Remove password transformation
            togglePassword.setImageResource(R.drawable.ic_eye_open); // Show "eye open" icon
        } else {
            // Hide password
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            editText.setTransformationMethod(new PasswordTransformationMethod()); // Apply password mask
            togglePassword.setImageResource(R.drawable.ic_eye_closed); // Show "eye closed" icon
        }
        editText.setSelection(selectionStart, selectionEnd);  //Restore cursor position.
    }

    /**
     * Validates the user's input for email, password, and confirm password.
     * Checks for:
     * - Empty fields
     * - Valid email format (using Patterns.EMAIL_ADDRESS)
     * - Minimum password length
     * - Password and confirm password match
     *
     * @param email           The user's email address.
     * @param password        The user's password.
     * @param confirmPassword The user's confirmation of the password.
     * @return True if the input is valid, false otherwise.
     */
    private boolean validateInput(String email, String password, String confirmPassword) {
        // Check for empty fields
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
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
            showToast("Password must be at least " + MIN_PASSWORD_LENGTH + " characters long!");
            return false;
        }

        // Check if password and confirm password match
        if (!password.equals(confirmPassword)) {
            showToast("Password confirmation does not match!");
            return false;
        }

        return true; // All validations passed
    }

    /**
     * Saves the new account information (email and password) to SharedPreferences.
     * Stores the email in a Set to keep track of registered emails, and stores
     * the password associated with the email using a key derived from the email.
     *
     * @param email    The user's email address.
     * @param password The user's password.
     */
    private void saveAccount(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Get the existing set of registered emails (or create a new one if it doesn't exist)
        Set<String> savedEmails = new HashSet<>(sharedPreferences.getStringSet(EMAIL_SET_KEY, new HashSet<>()));

        // Add the new email to the set
        savedEmails.add(email);

        // Save the updated set of emails
        editor.putStringSet(EMAIL_SET_KEY, savedEmails);

        // Save the password, using the email as part of the key
        editor.putString(email + "_password", password);

        editor.apply(); // Apply the changes to SharedPreferences
    }


    /**
     * Checks if an account with the given email already exists in SharedPreferences.
     *
     * @param email The email address to check.
     * @return True if an account with the email exists, false otherwise.
     */
    private boolean isAccountExists(String email) {
        Set<String> savedEmails = sharedPreferences.getStringSet(EMAIL_SET_KEY, new HashSet<>());
        return savedEmails.contains(email); // Check if the email is in the set
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
     * Called when the activity is paused. Pauses the background music.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (musicBg != null && musicBg.isPlaying()) {
            musicBg.pause(); // Pause music playback
        }
    }

    /**
     * Called when the activity is resumed.  Resumes background music playback.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (musicBg != null) {
            musicBg.start(); // Resume music playback
        }
    }

    /**
     * Called when the activity is being destroyed. Releases the MediaPlayer resources to prevent memory leaks.
     */
    @Override
    protected void onDestroy() {
        if (musicBg != null) {
            try {
                if (musicBg.isPlaying()) {
                    musicBg.stop(); // Stop playback
                }
                musicBg.release(); // Release resources

            } catch (Exception e){
                e.printStackTrace();
            }
            finally {
                musicBg = null; // Set to null for garbage collection
            }
        }
        super.onDestroy();
    }
}