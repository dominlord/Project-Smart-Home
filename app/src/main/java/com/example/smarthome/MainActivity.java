package com.example.smarthome;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    EditText taikhoanemail, mk;
    Button dangnhap, dangnhapDemo;
    TextView dangki , quenMK;
    CheckBox saveLogin;
    FirebaseAuth firebaseAuth;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        taikhoanemail = findViewById(R.id.email);
        mk = findViewById(R.id.mk);
        dangnhap = findViewById(R.id.login);
        saveLogin = findViewById(R.id.Savelogin);
        firebaseAuth = FirebaseAuth.getInstance();
        dangki = findViewById(R.id.dangki);
        quenMK = findViewById(R.id.forgetPass);
        dangnhapDemo = findViewById(R.id.fastlogin);

        quenMK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(MainActivity.this, QuenMKActivity.class);
                startActivity(intent1);
                finish();
            }
        });

        dangnhapDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(MainActivity.this, DangNhapDemoActivity.class);
                startActivity(intent2);
                finish();
            }
        });

        dangki.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DangkiActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //save password
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        boolean isChecked = sharedPreferences.getBoolean("saveLogin", false);

        if (isChecked) {
            taikhoanemail.setText(sharedPreferences.getString("email", ""));
            mk.setText(sharedPreferences.getString("password", ""));
            saveLogin.setChecked(true);
        }

        dangnhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = taikhoanemail.getText().toString();
                String password = mk.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(MainActivity.this, "Nhập Email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(MainActivity.this, "Nhập Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    if (user != null && user.isEmailVerified()) {
                                        Toast.makeText(MainActivity.this, "Chào mừng đến với trang chủ!", Toast.LENGTH_SHORT).show();

                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        if (saveLogin.isChecked()) {
                                            editor.putString("email", email);
                                            editor.putString("password", password);
                                            editor.putBoolean("saveLogin", true);
                                        } else {
                                            editor.clear();
                                        }
                                        editor.apply();

                                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Bạn chưa xác thực email. Vui lòng kiểm tra hộp thư!", Toast.LENGTH_LONG).show();
                                        firebaseAuth.signOut();
                                    }
                                } else {
                                    Toast.makeText(MainActivity.this, "Đăng nhập thất bại, hãy thử lại", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

    }
}








