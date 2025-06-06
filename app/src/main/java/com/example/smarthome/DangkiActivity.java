package com.example.smarthome;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class DangkiActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private TextInputEditText taikhoanemail, mk, hoten, sodienthoai, madinhdanh;
    private Button dangki;
    private TextView dangnhap;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangki);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.ice_cream));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        taikhoanemail = findViewById(R.id.email);
        mk = findViewById(R.id.mk);
        hoten = findViewById(R.id.name);
        sodienthoai = findViewById(R.id.sdt);
        madinhdanh = findViewById(R.id.madinhdanh);
        dangki = findViewById(R.id.dangki);
        dangnhap = findViewById(R.id.login);
        progressBar = findViewById(R.id.progressBar);

        dangnhap.setOnClickListener(view -> {
            startActivity(new Intent(DangkiActivity.this, MainActivity.class));
            finish();
        });

        dangki.setOnClickListener(view -> {
            String email = taikhoanemail.getText().toString().trim();
            String password = mk.getText().toString().trim();
            String name = hoten.getText().toString().trim();
            String phone = sodienthoai.getText().toString().trim();
            String ID = madinhdanh.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ||
                    TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(ID)) {
                Toast.makeText(DangkiActivity.this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!phone.matches("^[0-9]{10}$")) {
                Toast.makeText(DangkiActivity.this, "Số điện thoại phải có đúng 10 chữ số!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidPassword(password)) {
                Toast.makeText(DangkiActivity.this, "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt!", Toast.LENGTH_LONG).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            dangki.setVisibility(View.INVISIBLE);
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        dangki.setVisibility(View.VISIBLE);
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                user.sendEmailVerification().addOnCompleteListener(verificationTask -> {
                                    if (verificationTask.isSuccessful()) {
                                        saveUserToFirestore(user.getUid(), email, name, phone,ID);

                                        Toast.makeText(DangkiActivity.this, "Vui lòng kiểm tra email để xác thực tài khoản!", Toast.LENGTH_LONG).show();
                                        firebaseAuth.signOut();
                                        startActivity(new Intent(DangkiActivity.this, MainActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(DangkiActivity.this, "Lỗi gửi email xác thực!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(DangkiActivity.this, "Đăng ký thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void saveUserToFirestore(String uid, String email, String name, String phone, String ID) {
        DocumentReference userRef = db.collection("users").document(uid);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().exists()) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("email", email);
                userData.put("hoTen", name);
                userData.put("soDienThoai", phone);
                userData.put("maDinhDanh", ID);
                userRef.set(userData);
            }
        });
    }

    private boolean isValidPassword(String password) {
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
        return password.matches(passwordPattern);
    }
}
