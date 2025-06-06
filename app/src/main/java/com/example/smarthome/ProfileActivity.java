package com.example.smarthome;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ProfileActivity extends AppCompatActivity {

    TextView emailTextView, nameTextView, idTextView, sdtTextView, StatusHouseTextView;
    FirebaseAuth auth;
    FirebaseFirestore db;
    SharedPreferences sharedPreferences;
    public static boolean hasLoaded = false;

    private int statusHouse = -1;
    private int lastStatusHouse = -1;

    private WebSocket webSocket;
    private Handler handler = new Handler(Looper.getMainLooper());
    ProgressBar progressBarStatus ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        emailTextView = findViewById(R.id.emailTextView);
        nameTextView = findViewById(R.id.nameTextView);
        idTextView = findViewById(R.id.idTextView);
        sdtTextView = findViewById(R.id.sdtTextView);
        progressBarStatus = findViewById(R.id.progressBarStatus);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        StatusHouseTextView = findViewById(R.id.StatusHouseTextView);
        connectWebSocket();

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            loadUserData(uid);

            String email = user.getEmail();
            emailTextView.setText(email);
        } else {
            emailTextView.setText("Chưa đăng nhập");
        }


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_user);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_user) {
                return true;
            } else if (itemId == R.id.bottom_room) {
                startActivity(new Intent(getApplicationContext(), RoomActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
                finish();
                return true;
            } else if (itemId == R.id.bottom_home) {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
                finish();
                return true;
            } else if (itemId == R.id.bottom_function) {
                startActivity(new Intent(getApplicationContext(), FunctionActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
                finish();
                return true;
            } else if (itemId == R.id.bottom_setting) {
                startActivity(new Intent(getApplicationContext(), SettingActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
                finish();
                return true;
            }
            return false;

        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!hasLoaded) {
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                String uid = user.getUid();
                loadUserData(uid);
                hasLoaded = true;
            }
        }
    }

    private void loadUserData(String uid) {
        String userEmail = auth.getCurrentUser().getEmail();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String hoTen = documentSnapshot.getString("hoTen");
                        String id = documentSnapshot.getString("maDinhDanh");
                        String sdt = documentSnapshot.getString("soDienThoai");

                        if ("quantrivien@gmail.com".equals(userEmail)) {
                            nameTextView.setText("Quản trị viên");
                            idTextView.setText("1");
                            sdtTextView.setText("0x000x0");
                        } else {
                            nameTextView.setText(hoTen);
                            idTextView.setText(id);
                            sdtTextView.setText(sdt);
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, "Không tìm thấy dữ liệu!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT).show();
                });
    }



    private void startAutoSending() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendUpdatedState();
                handler.postDelayed(this, 5000);
            }
        }, 5000);
    }

    private void sendUpdatedState() {
        try {
            if (statusHouse != lastStatusHouse) {
                if (webSocket == null) {
                    Log.e("WebSocket", "Không thể gửi: WebSocket chưa kết nối!");
                    return;
                }

                JSONObject state = new JSONObject();
                state.put("statusHouse", statusHouse);

                webSocket.send(state.toString());
                lastStatusHouse = statusHouse;

                Log.d("WebSocket", "Đã gửi trạng thái nhà.");
            } else {
                Log.d("WebSocket", "Trạng thái không thay đổi, không gửi.");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void connectWebSocket() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("ws://172.20.10.2:8765").build();
        handler.post(() -> progressBarStatus.setVisibility(View.VISIBLE));
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                runOnUiThread(() -> {
                    Log.d("ProfileActivity", "WebSocket connected");
                    handler.post(() -> progressBarStatus.setVisibility(View.GONE));
                    startAutoSending();
                });
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(text);
                        statusHouse = jsonObject.getInt("statusHouse");

                        if (statusHouse == 1) {
                            StatusHouseTextView.setText("Đang hoạt động");
                        } else if (statusHouse == 0) {
                            StatusHouseTextView.setText("Không hoạt động");
                        } else {
                            StatusHouseTextView.setText("Trạng thái không xác định");
                            Toast.makeText(ProfileActivity.this,"Không thể kết nối đến máy chủ, hãy kiểm tra lại mạng!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                runOnUiThread(() -> {
                    Log.e("ProfileActivity", "WebSocket error: " + t.getMessage());
                    handler.post(() -> progressBarStatus.setVisibility(View.GONE));
                    StatusHouseTextView.setText(getString(R.string.show_status));

                    Toast.makeText(ProfileActivity.this, "Không thể kết nối Server.\n" +
                            "Vui lòng kiểm tra lại mạng.", Toast.LENGTH_LONG).show();
                });
            }
        });
    }


}
