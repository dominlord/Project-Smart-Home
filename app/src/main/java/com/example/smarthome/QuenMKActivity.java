package com.example.smarthome;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

public class QuenMKActivity extends AppCompatActivity {
    EditText QuenEmail;
    ImageView closeBtn;
    Button reset;
    FirebaseAuth auth;
    ProgressBar progressBar;
    String FindEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quen_mkactivity);

        QuenEmail = findViewById(R.id.findEmail);
        closeBtn = findViewById(R.id.closeBtn);
        reset = findViewById(R.id.reset);
        progressBar = findViewById(R.id.progressBar);
        auth = FirebaseAuth.getInstance();

        closeBtn.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FindEmail = QuenEmail.getText().toString().trim();
                if (!TextUtils.isEmpty(FindEmail)) {
                    ResetPassword();
                } else {
                    QuenEmail.setError("Vui lòng nhập email");
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void ResetPassword() {
        progressBar.setVisibility(View.VISIBLE);
        reset.setVisibility(View.INVISIBLE);

        auth.sendPasswordResetEmail(FindEmail).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(QuenMKActivity.this, "Mật khẩu đã được gửi qua email", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(QuenMKActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(QuenMKActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
                reset.setVisibility(View.VISIBLE);
            }
        });
    }
}
