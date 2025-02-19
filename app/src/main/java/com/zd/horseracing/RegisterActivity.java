package com.zd.horseracing;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles user registration.  Allows users to enter an email, password, and confirm password,
 * validates the input, checks for existing accounts, and saves the new account information
 * using SharedPreferences. Navigates to the LoginActivity upon successful registration.
 */
public class RegisterActivity extends AppCompatActivity {
    private EditText etEmail, etPassword, etConfirmPassword;
    private ImageView togglePassword;
    private boolean isPasswordVisible = false;
    private Button btnRegister;
    private SharedPreferences sharedPreferences;
    private MediaPlayer musicBg;

    /**
     * Called when the activity is destroyed. Stops and releases the background music MediaPlayer.
     */
    @Override
    protected void onDestroy() {
        musicBg.stop(); // Stop the background music
        super.onDestroy();
    }

    /**
     * Called when the activity is first created.  Initializes UI components, sets up click listeners,
     * retrieves SharedPreferences, and handles registration logic.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize background music
        musicBg = MediaPlayer.create(this, R.raw.loginbackground);
        musicBg.setLooping(true);
        musicBg.start();

        // Initialize UI elements
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        togglePassword = findViewById(R.id.ivTogglePassword);
        btnRegister = findViewById(R.id.btnRegister);

        // Get SharedPreferences instance
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // Set click listener for password visibility toggle
        togglePassword.setOnClickListener(view -> {
            isPasswordVisible = !isPasswordVisible;
            togglePasswordVisibility(etPassword, togglePassword, isPasswordVisible);
        });

        // Set click listener for register button
        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // Validate user input
            if (!validateInput(email, password, confirmPassword)) {
                return; // Stop if input is invalid
            }

            // Check if account already exists
            if (isAccountExists(email)) {
                Toast.makeText(RegisterActivity.this, "Account already exists!", Toast.LENGTH_SHORT).show();
                return; // Stop if account exists
            }

            // Save the new account
            saveAccount(email, password);

            // Display success message
            Toast.makeText(RegisterActivity.this, "Registration successful! Redirecting to login...", Toast.LENGTH_SHORT).show();

            // Delay the screen transition by 1 second so the Toast is visible long enough
            new android.os.Handler().postDelayed(() -> {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.putExtra("email", email);  //Optional: Pass email/pass to login.
                intent.putExtra("password", password);
                startActivity(intent);
                finish(); // Finish RegisterActivity to prevent going back
            }, 1000); // 1000ms = 1 second
        });
    }

    /**
     * Toggles the visibility of the password in the EditText.
     *
     * @param editText   The EditText containing the password.
     * @param toggleIcon The ImageView used to toggle visibility.
     * @param isVisible  True if the password should be visible, false otherwise.
     */
    private void togglePasswordVisibility(EditText editText, ImageView toggleIcon, boolean isVisible) {
        if (isVisible) {
            // Show password
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            editText.setTransformationMethod(null); // Remove password transformation
            toggleIcon.setImageResource(R.drawable.ic_eye_open);
        } else {
            // Hide password
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            editText.setTransformationMethod(new PasswordTransformationMethod());
            toggleIcon.setImageResource(R.drawable.ic_eye_closed);
        }
        editText.setSelection(editText.getText().length()); // Move cursor to the end
    }

    /**
     * Validates the user's input for email, password, and confirm password.
     * Checks for empty fields, email format, password length, and password confirmation match.
     *
     * @param email           The user's email address.
     * @param password        The user's password.
     * @param confirmPassword The user's confirmation password.
     * @return True if the input is valid, false otherwise.
     */
    private boolean validateInput(String email, String password, String confirmPassword) {
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please enter all information!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!email.contains("@")) {
            Toast.makeText(this, "Invalid email!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Password confirmation does not match!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Saves the new account information (email and password) to SharedPreferences.
     * Stores the email in a Set to keep track of registered emails, and stores the password
     * associated with the email using a key derived from the email.
     *
     * @param email    The user's email address.
     * @param password The user's password.
     */
    private void saveAccount(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Get the set of saved emails
        Set<String> savedEmails = sharedPreferences.getStringSet("emails", new HashSet<>());

        // Add the new email to the set
        savedEmails.add(email);

        // Save the updated set of emails
        editor.putStringSet("emails", savedEmails);

        // Save the password associated with the email
        editor.putString(email + "_password", password);

        editor.apply(); // Apply the changes
    }


    /**
     * Checks if an account with the given email already exists in SharedPreferences.
     *
     * @param email The email address to check.
     * @return True if the account exists, false otherwise.
     */
    private boolean isAccountExists(String email) {
        Set<String> savedEmails = sharedPreferences.getStringSet("emails", new HashSet<>());
        return savedEmails.contains(email);
    }
}