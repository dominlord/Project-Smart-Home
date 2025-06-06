package com.example.smarthome;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;


public class DangNhapDemoActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private TextInputEditText taikhoanemail, mk;
    private Button dangnhapnhanh;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_nhap_demo);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.ice_cream));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        taikhoanemail = findViewById(R.id.email1);
        mk = findViewById(R.id.mk1);
        dangnhapnhanh = findViewById(R.id.dangnhapnhanh);
        progressBar = findViewById(R.id.progressBar);

        taikhoanemail.setText("quantrivien@gmail.com");
        mk.setText("qtv123");

        progressBar.setVisibility(View.GONE);

        dangnhapnhanh.setOnClickListener(view -> {
            String email = taikhoanemail.getText().toString().trim();
            String password = mk.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(DangNhapDemoActivity.this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            dangnhapnhanh.setVisibility(View.INVISIBLE);

            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        dangnhapnhanh.setVisibility(View.VISIBLE);

                        if (task.isSuccessful()) {
                            String uid = firebaseAuth.getCurrentUser().getUid();

                            saveUserToFirestore(uid, email);


                            Intent intent = new Intent(DangNhapDemoActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(DangNhapDemoActivity.this, "Đăng nhập thất bại! Kiểm tra lại thông tin.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void saveUserToFirestore(String uid, String email) {
        DocumentReference userRef = db.collection("users").document(uid);
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);

        userRef.set(user, SetOptions.merge())
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(DangNhapDemoActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(DangNhapDemoActivity.this, "Đăng nhập thất bại!", Toast.LENGTH_SHORT).show());
    }





}
