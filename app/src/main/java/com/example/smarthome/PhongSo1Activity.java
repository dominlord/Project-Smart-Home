package com.example.smarthome;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class PhongSo1Activity extends AppCompatActivity {
    Switch denP1 , dieuhoaP1 , cuaP1 , quatP1 ;
    ImageView doorP1 , acP1 , lightP1 , fanP1, ImageViewPSo1, backBtn1;
    private boolean lastLightStateP1, lastFanStateP1, lastAcStateP1, lastDoorStateP1;
    private WebSocket webSocket, CameraP1Websocket;
    SharedPreferences sharedPreferences;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean requestingData = false;
    private ProgressBar progressBarP1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_phong_so1);

        backBtn1 = findViewById(R.id.backBtn1);
        denP1 = findViewById(R.id.switch_light1);
        dieuhoaP1 = findViewById(R.id.switch_ac1);
        cuaP1 = findViewById(R.id.switch_door1);
        quatP1 = findViewById(R.id.switch_fan1);
        progressBarP1 = findViewById(R.id.progressBarP1);

        doorP1 = findViewById(R.id.doorButtonP1);
        acP1 = findViewById(R.id.acButtonP1);
        lightP1 = findViewById(R.id.lightButtonP1);
        fanP1 = findViewById(R.id.fanButtonP1);
        ImageViewPSo1 = findViewById(R.id.imageViewPSo1);
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                autoUpdateSwitchState();
                handler.postDelayed(this, 2000);
            }
        }, 2000);

        ViewFlipper viewFlipperLightP1 = findViewById(R.id.viewFlipperLightP1);
        ViewFlipper viewFlipperDoorP1 = findViewById(R.id.viewFlipperDoorP1);
        ViewFlipper viewFlipperACP1 = findViewById(R.id.viewFlipperACP1);
        ViewFlipper viewFlipperFanP1 = findViewById(R.id.viewFlipperFanP1);


        viewFlipperLightP1.setInAnimation(this, android.R.anim.slide_in_left);
        viewFlipperDoorP1.setInAnimation(this, android.R.anim.slide_in_left);
        viewFlipperACP1.setInAnimation(this, android.R.anim.slide_in_left);
        viewFlipperFanP1.setInAnimation(this, android.R.anim.slide_in_left);

        viewFlipperLightP1.showNext();
        viewFlipperDoorP1.showNext();
        viewFlipperACP1.showNext();
        viewFlipperFanP1.showNext();


        new Handler().postDelayed(() -> {
            viewFlipperLightP1.stopFlipping();
            viewFlipperDoorP1.stopFlipping();
            viewFlipperACP1.stopFlipping();
            viewFlipperFanP1.stopFlipping();
        }, 3000);

        backBtn1.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
        });

        //luu trang thai tu ghi am
        boolean isLightP1On = sharedPreferences.getBoolean("denP1", false);
        if (isLightP1On) {
            lightP1.setImageResource(R.drawable.den);
            denP1.setChecked(isLightP1On);
        } else {
            lightP1.setImageResource(R.drawable.lightoff);
        }

        boolean isDoorP1On = sharedPreferences.getBoolean("doorP1", false);
        if (isDoorP1On) {
            doorP1.setImageResource(R.drawable.dor);
            cuaP1.setChecked(isDoorP1On);
        } else {
            doorP1.setImageResource(R.drawable.doroff);
        }

        boolean isACP1On = sharedPreferences.getBoolean("acP1", false);
        if (isACP1On) {
            acP1.setImageResource(R.drawable.ac);
            dieuhoaP1.setChecked(isACP1On);
        } else {
            acP1.setImageResource(R.drawable.acoff);
        }

        boolean isFanP1On = sharedPreferences.getBoolean("fanP1", false);
        if (isFanP1On) {
            fanP1.setImageResource(R.drawable.fan);
            quatP1.setChecked(isFanP1On);
        } else {
            fanP1.setImageResource(R.drawable.fanoff);
        }

        //switch
        denP1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                denP1.setOnCheckedChangeListener(null);
                denP1.setChecked(isChecked);
                denP1.setOnCheckedChangeListener(this);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("denP1", isChecked);
                editor.apply();

                Toast.makeText(PhongSo1Activity.this, isChecked ? "Đã bật" : "Đã tắt", Toast.LENGTH_SHORT).show();
                lightP1.setImageResource(isChecked ? R.drawable.den : R.drawable.lightoff);

                new Handler().postDelayed(() -> sendUpdatedState(), 400);
            }
        });

        dieuhoaP1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dieuhoaP1.setOnCheckedChangeListener(null);
                dieuhoaP1.setChecked(isChecked);
                dieuhoaP1.setOnCheckedChangeListener(this);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("acP1", isChecked);
                editor.apply();

                Toast.makeText(PhongSo1Activity.this, isChecked ? "Đã bật" : "Đã tắt", Toast.LENGTH_SHORT).show();
                acP1.setImageResource(isChecked ? R.drawable.ac : R.drawable.acoff);

                new Handler().postDelayed(() -> sendUpdatedState(), 400);
            }
        });

        cuaP1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cuaP1.setOnCheckedChangeListener(null);
                cuaP1.setChecked(isChecked);
                cuaP1.setOnCheckedChangeListener(this);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("doorP1", isChecked);
                editor.apply();

                Toast.makeText(PhongSo1Activity.this, isChecked ? "Đã bật" : "Đã tắt", Toast.LENGTH_SHORT).show();
                doorP1.setImageResource(isChecked ? R.drawable.dor : R.drawable.doroff);

                new Handler().postDelayed(() -> sendUpdatedState(), 400);
            }
        });

        quatP1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                quatP1.setOnCheckedChangeListener(null);
                quatP1.setChecked(isChecked);
                quatP1.setOnCheckedChangeListener(this);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("fanP1", isChecked);
                editor.apply();

                Toast.makeText(PhongSo1Activity.this, isChecked ? "Đã bật" : "Đã tắt", Toast.LENGTH_SHORT).show();
                fanP1.setImageResource(isChecked ? R.drawable.fan : R.drawable.fanoff);

                new Handler().postDelayed(() -> sendUpdatedState(), 400);
            }
        });

        connectWebSocket();
        connectCameraWebSocket();
    }

    private void sendUpdatedState() {
        try {
            boolean currentLightP1 = denP1.isChecked();
            boolean currentFanP1 = quatP1.isChecked();
            boolean currentAcP1 = dieuhoaP1.isChecked();
            boolean currentDoorP1 = cuaP1.isChecked();

            if (currentLightP1 == lastLightStateP1 && currentFanP1 == lastFanStateP1 &&
                    currentAcP1 == lastAcStateP1 && currentDoorP1 == lastDoorStateP1) {
                return;
            }

            JSONObject state = new JSONObject();
            JSONObject p1 = new JSONObject();
            p1.put("light", currentLightP1 ? 1 : 0);
            p1.put("fan", currentFanP1 ? 1 : 0);
            p1.put("ac", currentAcP1 ? 1 : 0);
            p1.put("door", currentDoorP1 ? 1 : 0);
            state.put("P1", p1);

            if (webSocket != null) {
                webSocket.send(state.toString());
            }

            lastLightStateP1 = currentLightP1;
            lastFanStateP1 = currentFanP1;
            lastAcStateP1 = currentAcP1;
            lastDoorStateP1 = currentDoorP1;

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void connectWebSocket() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("ws://172.20.10.2:8765").build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                Log.d("PhongSo1Activity", "WebSocket connected");
                handler.post(() -> startRequestingData());
                requestingData = true;
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(text);
                        JSONObject p1 = jsonObject.getJSONObject("P1");

                        denP1.setChecked(p1.getInt("light") == 1);
                        quatP1.setChecked(p1.getInt("fan") == 1);
                        dieuhoaP1.setChecked(p1.getInt("ac") == 1);
                        cuaP1.setChecked(p1.getInt("door") == 1);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(1000, null);
                Log.d("PhongSo1Activity", "WebSocket closed");
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                Log.e("PhongSo1Activity", "WebSocket error: " + t.getMessage());
            }
        });
    }

    private void startRequestingData() {
        if (!requestingData || webSocket == null) return;
        webSocket.send("Requesting data");
        Log.d("PhongSo1Activity", "Sent: Requesting data");
        handler.postDelayed(() -> startRequestingData(), 2000);
    }



    private void autoUpdateSwitchState() {
        boolean isLightP1On = sharedPreferences.getBoolean("denP1", false);
        if (denP1.isChecked() != isLightP1On) {
            denP1.setChecked(isLightP1On);
            lightP1.setImageResource(isLightP1On ? R.drawable.den : R.drawable.lightoff);
        }

        boolean isDoorP1On = sharedPreferences.getBoolean("doorP1", false);
        if (cuaP1.isChecked() != isDoorP1On) {
            cuaP1.setChecked(isDoorP1On);
            doorP1.setImageResource(isDoorP1On ? R.drawable.dor : R.drawable.doroff);
        }

        boolean isACP1On = sharedPreferences.getBoolean("acP1", false);
        if (dieuhoaP1.isChecked() != isACP1On) {
            dieuhoaP1.setChecked(isACP1On);
            acP1.setImageResource(isACP1On ? R.drawable.ac : R.drawable.acoff);
        }

        boolean isFanP1On = sharedPreferences.getBoolean("fanP1", false);
        if (quatP1.isChecked() != isFanP1On) {
            quatP1.setChecked(isFanP1On);
            fanP1.setImageResource(isFanP1On ? R.drawable.fan : R.drawable.fanoff);
        }
    }

    private void connectCameraWebSocket() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("ws://172.20.10.2:9000").build();
        handler.post(() -> progressBarP1.setVisibility(View.VISIBLE));

        CameraP1Websocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                Log.d("CameraP1WebSocket", "Camera WebSocket connected");
                handler.post(() -> progressBarP1.setVisibility(View.GONE));
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    byte[] imageBytes = android.util.Base64.decode(text, android.util.Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    handler.post(() -> {
                        if (bitmap != null) {
                            ImageViewPSo1.setImageBitmap(bitmap);
                        }
                    });
                } catch (Exception e) {
                    Log.e("CameraP1WebSocket", "Lỗi khi giải mã hình ảnh: " + e.getMessage());
                }
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(1000, null);
                Log.d("CameraP1WebSocket", "WebSocket closed");
                handler.post(() -> progressBarP1.setVisibility(View.GONE));
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                Log.e("CameraP1WebSocket", "WebSocket error: " + t.getMessage());

                handler.post(() -> {
                    progressBarP1.setVisibility(View.GONE);
                    Toast.makeText(PhongSo1Activity.this, "Không thể kết nối Server.\n" +
                                                                       "Vui lòng kiểm tra lại mạng.", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (CameraP1Websocket != null) {
            CameraP1Websocket.close(1000, "Activity Destroyed");
            CameraP1Websocket = null;
        }
        if (webSocket != null) {
            webSocket.close(1000, "Activity Destroyed");
            webSocket = null;
        }
        Log.d("CameraCongActivity", "WebSocket đã được đóng");
    }

}