package com.zd.horseracing;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private ImageView togglePassword;
    private boolean isPasswordVisible = false;
    private Button btnLogin;
    private TextView btnDontHaveAccount;
    private SharedPreferences sharedPreferences;

    private static final String DEFAULT_EMAIL = "user@gmail.com";
    private static final String DEFAULT_PASSWORD = "123456";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        togglePassword = findViewById(R.id.ivTogglePassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnDontHaveAccount = findViewById(R.id.btnDontHaveAccount);

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        String password = intent.getStringExtra("password");

        if (email != null && password != null) {
            etEmail.setText(email);
            etPassword.setText(password);
        }

        togglePassword.setOnClickListener(view -> {
            isPasswordVisible = !isPasswordVisible;
            togglePasswordVisibility(etPassword, togglePassword, isPasswordVisible);
        });

        btnDontHaveAccount.setOnClickListener(view -> {
            Intent intentMain = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intentMain);
            finish();
        });

        btnLogin.setOnClickListener(view -> {
            String emailInput = etEmail.getText().toString().trim();
            String passwordInput = etPassword.getText().toString().trim();

            if (validateInput(emailInput, passwordInput)) {
                if (checkAccount(emailInput, passwordInput)) {
                    Intent intentMain = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("email", emailInput);
                    startActivity(intentMain);
                    finish();
                } else {
                    Toast.makeText(this, "Tài khoản không tồn tại. Vui lòng đăng ký!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void togglePasswordVisibility(EditText editText, ImageView toggleIcon, boolean isVisible) {
        if (isVisible) {
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            editText.setTransformationMethod(null);
            toggleIcon.setImageResource(R.drawable.ic_eye_open);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            editText.setTransformationMethod(new PasswordTransformationMethod());
            toggleIcon.setImageResource(R.drawable.ic_eye_closed);
        }
        editText.setSelection(editText.getText().length());
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!email.contains("@")) {
            Toast.makeText(this, "Email không hợp lệ!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean checkAccount(String email, String password) {
        String savedEmail = sharedPreferences.getString("email", null);
        String savedPassword = sharedPreferences.getString("password", null);

        return (email.equals(DEFAULT_EMAIL) && password.equals(DEFAULT_PASSWORD)) ||
                (email.equals(savedEmail) && password.equals(savedPassword));
    }
}
