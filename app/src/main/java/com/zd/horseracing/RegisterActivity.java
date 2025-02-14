package com.zd.horseracing;

import android.content.Intent;
import android.content.SharedPreferences;
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

public class RegisterActivity extends AppCompatActivity {
    private EditText etEmail, etPassword, etConfirmPassword;
    private ImageView togglePassword;
    private boolean isPasswordVisible = false;
    private Button btnRegister;
    private SharedPreferences sharedPreferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        togglePassword = findViewById(R.id.ivTogglePassword);
        btnRegister = findViewById(R.id.btnRegister);

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        togglePassword.setOnClickListener(view -> {
            isPasswordVisible = !isPasswordVisible;
            togglePasswordVisibility(etPassword, togglePassword, isPasswordVisible);
        });

        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (!validateInput(email, password, confirmPassword)) {
                return;
            }

            if (isAccountExists(email)) {
                Toast.makeText(RegisterActivity.this, "Tài khoản đã tồn tại!", Toast.LENGTH_SHORT).show();
                return;
            }

            saveAccount(email, password);

            // Hiển thị thông báo đăng ký thành công
            Toast.makeText(RegisterActivity.this, "Đăng ký thành công! Đang chuyển đến trang đăng nhập...", Toast.LENGTH_SHORT).show();

            // Trì hoãn chuyển màn hình 1 giây để Toast hiển thị đủ lâu
            new android.os.Handler().postDelayed(() -> {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.putExtra("email", email);
                intent.putExtra("password", password);
                startActivity(intent);
                finish();
            }, 1000); // 1000ms = 1 giây
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

    private boolean validateInput(String email, String password, String confirmPassword) {
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
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

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Xác nhận mật khẩu không khớp!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void saveAccount(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Lấy danh sách email đã lưu
        Set<String> savedEmails = sharedPreferences.getStringSet("emails", new HashSet<>());

        // Thêm email mới vào danh sách
        savedEmails.add(email);

        // Lưu danh sách email mới
        editor.putStringSet("emails", savedEmails);

        // Lưu mật khẩu cho từng email riêng biệt
        editor.putString(email + "_password", password);

        editor.apply();
    }



    private boolean isAccountExists(String email) {
        Set<String> savedEmails = sharedPreferences.getStringSet("emails", new HashSet<>());

        return savedEmails.contains(email);
    }




}
