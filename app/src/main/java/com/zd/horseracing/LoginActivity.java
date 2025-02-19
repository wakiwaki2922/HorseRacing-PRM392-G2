package com.zd.horseracing;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
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
 * `LoginActivity` handles user login and registration navigation.  It manages user input for email and password,
 *  provides password visibility toggling, and validates user credentials against stored data (SharedPreferences)
 *  and default credentials.  It also integrates with a `RegisterActivity` for new user registration.
 */
public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private ImageView togglePassword;
    private boolean isPasswordVisible = false;
    private Button btnLogin;
    private TextView btnDontHaveAccount;
    private SharedPreferences sharedPreferences;

    private static final String DEFAULT_EMAIL = "user@m.c";
    private static final String DEFAULT_PASSWORD = "123456";
    private MediaPlayer musicBg;

    /**
     * Called when the activity is destroyed.  Stops and releases the background music MediaPlayer.
     */
    @Override
    protected void onDestroy() {
        musicBg.stop();
        super.onDestroy();
    }

    /**
     * Called when the activity is first created. Initializes UI components, sets up click listeners,
     * retrieves SharedPreferences, handles the back button, and starts background music.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize background music
        musicBg = MediaPlayer.create(this, R.raw.loginbackground);
        musicBg.setLooping(true);
        musicBg.start();

        // Initialize UI elements
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        togglePassword = findViewById(R.id.ivTogglePassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnDontHaveAccount = findViewById(R.id.btnDontHaveAccount);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // Set click listener for password visibility toggle
        togglePassword.setOnClickListener(view -> {
            isPasswordVisible = !isPasswordVisible;
            togglePasswordVisibility(etPassword, togglePassword, isPasswordVisible);
        });

        // Set click listener for "Don't have an account?" text
        btnDontHaveAccount.setOnClickListener(view -> {
            Intent intentMain = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intentMain);
            finish(); // Finish LoginActivity so user can't go back to it after registering
        });

        // Set click listener for login button
        btnLogin.setOnClickListener(view -> {
            String emailInput = etEmail.getText().toString().trim();
            String passwordInput = etPassword.getText().toString().trim();

            if (validateInput(emailInput, passwordInput)) {
                if (checkAccount(emailInput, passwordInput)) {
                    Intent intentToInstruction = new Intent(LoginActivity.this, InstructionActivity.class);
                    startActivity(intentToInstruction);
                    finish(); // Finish LoginActivity after successful login
                } else {
                    Toast.makeText(this, "Incorrect password or account!", Toast.LENGTH_SHORT).show();
                }
            }
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
     * Validates the user's email and password input. Checks for empty fields, email format,
     * password length, and if the account exists.
     *
     * @param email    The user's email address.
     * @param password The user's password.
     * @return True if the input is valid, false otherwise.
     */
    private boolean validateInput(String email, String password) {
        Set<String> savedEmails = sharedPreferences.getStringSet("emails", new HashSet<>());

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter all information!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!email.contains("@")) {
            Toast.makeText(this, "Invalid email!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Incorrect password or account!", Toast.LENGTH_SHORT).show(); //Same message as login failure for security.
            return false;
        }

        if (!savedEmails.contains(email) && !email.equals(DEFAULT_EMAIL)) {
            Toast.makeText(this, "Account does not exist!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Checks if the provided email and password match the stored credentials.  Checks against
     * both default credentials and credentials stored in SharedPreferences.
     *
     * @param email    The user's email address.
     * @param password The user's password.
     * @return True if the account credentials are valid, false otherwise.
     */
    private boolean checkAccount(String email, String password) {
        Set<String> savedEmails = sharedPreferences.getStringSet("emails", new HashSet<>());

        // Check if the email is in the list and the password is correct
        return (email.equals(DEFAULT_EMAIL) && password.equals(DEFAULT_PASSWORD)) ||
                (savedEmails.contains(email) && password.equals(sharedPreferences.getString(email + "_password", "")));
    }
}