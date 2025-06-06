package com.example.smarthome;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
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

public class PhongSo2Activity extends AppCompatActivity {
    Switch denP2 , dieuhoaP2 , cuaP2 , quatP2 ;
    ImageView doorP2 , acP2 , lightP2 , fanP2 , backBtn2;
    private boolean lastLightStateP2, lastFanStateP2, lastAcStateP2, lastDoorStateP2;
    private WebSocket webSocket;
    SharedPreferences sharedPreferences;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean requestingData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_phong_so2);

        backBtn2 = findViewById(R.id.backBtn2);
        denP2 = findViewById(R.id.switch_light2);
        dieuhoaP2 = findViewById(R.id.switch_ac2);
        cuaP2 = findViewById(R.id.switch_door2);
        quatP2 = findViewById(R.id.switch_fan2);

        doorP2 = findViewById(R.id.doorButtonP2);
        acP2 = findViewById(R.id.acButtonP2);
        lightP2 = findViewById(R.id.lightButtonP2);
        fanP2 = findViewById(R.id.fanButtonP2);
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                autoUpdateSwitchState();
                handler.postDelayed(this, 2000);
            }
        }, 2000);

        ViewFlipper viewFlipperLightP2 = findViewById(R.id.viewFlipperLightP2);
        ViewFlipper viewFlipperDoorP2= findViewById(R.id.viewFlipperDoorP2);
        ViewFlipper viewFlipperACP2 = findViewById(R.id.viewFlipperACP2);
        ViewFlipper viewFlipperFanP2= findViewById(R.id.viewFlipperFanP2);


        viewFlipperLightP2.setInAnimation(this, android.R.anim.slide_in_left);
        viewFlipperDoorP2.setInAnimation(this, android.R.anim.slide_in_left);
        viewFlipperACP2.setInAnimation(this, android.R.anim.slide_in_left);
        viewFlipperFanP2.setInAnimation(this, android.R.anim.slide_in_left);

        viewFlipperLightP2.showNext();
        viewFlipperDoorP2.showNext();
        viewFlipperACP2.showNext();
        viewFlipperFanP2.showNext();


        new Handler().postDelayed(() -> {
            viewFlipperLightP2.stopFlipping();
            viewFlipperDoorP2.stopFlipping();
            viewFlipperACP2.stopFlipping();
            viewFlipperFanP2.stopFlipping();
        }, 3000);

        backBtn2.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
        });

        //luu trang thai tu ghi am
        boolean isLightP2On = sharedPreferences.getBoolean("denP2", false);
        if (isLightP2On) {
            lightP2.setImageResource(R.drawable.den);
            denP2.setChecked(isLightP2On);
        } else {
            lightP2.setImageResource(R.drawable.lightoff);
        }

        boolean isDoorP2On = sharedPreferences.getBoolean("doorP2", false);
        if (isDoorP2On) {
            doorP2.setImageResource(R.drawable.dor);
            cuaP2.setChecked(isDoorP2On);
        } else {
            doorP2.setImageResource(R.drawable.doroff);
        }

        boolean isACP2On = sharedPreferences.getBoolean("acP2", false);
        if (isACP2On) {
            acP2.setImageResource(R.drawable.ac);
            dieuhoaP2.setChecked(isACP2On);
        } else {
            acP2.setImageResource(R.drawable.acoff);
        }

        boolean isFanP2On = sharedPreferences.getBoolean("fanP2", false);
        if (isFanP2On) {
            fanP2.setImageResource(R.drawable.fan);
            quatP2.setChecked(isFanP2On);
        } else {
            fanP2.setImageResource(R.drawable.fanoff);
        }




        //switch
        denP2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                denP2.setOnCheckedChangeListener(null);
                denP2.setChecked(isChecked);
                denP2.setOnCheckedChangeListener(this);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("denP2", isChecked);
                editor.apply();

                Toast.makeText(PhongSo2Activity.this, isChecked ? "Đã bật" : "Đã tắt", Toast.LENGTH_SHORT).show();
                lightP2.setImageResource(isChecked ? R.drawable.den : R.drawable.lightoff);

                new Handler().postDelayed(() -> sendUpdatedState(), 400);
            }
        });

        dieuhoaP2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dieuhoaP2.setOnCheckedChangeListener(null);
                dieuhoaP2.setChecked(isChecked);
                dieuhoaP2.setOnCheckedChangeListener(this);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("acP2", isChecked);
                editor.apply();

                Toast.makeText(PhongSo2Activity.this, isChecked ? "Đã bật" : "Đã tắt", Toast.LENGTH_SHORT).show();
                acP2.setImageResource(isChecked ? R.drawable.ac : R.drawable.acoff);

                new Handler().postDelayed(() -> sendUpdatedState(), 400);
            }
        });

        cuaP2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cuaP2.setOnCheckedChangeListener(null);
                cuaP2.setChecked(isChecked);
                cuaP2.setOnCheckedChangeListener(this);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("doorP2", isChecked);
                editor.apply();

                Toast.makeText(PhongSo2Activity.this, isChecked ? "Đã bật" : "Đã tắt", Toast.LENGTH_SHORT).show();
                doorP2.setImageResource(isChecked ? R.drawable.dor : R.drawable.doroff);

                new Handler().postDelayed(() -> sendUpdatedState(), 400);
            }
        });

        quatP2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                quatP2.setOnCheckedChangeListener(null);
                quatP2.setChecked(isChecked);
                quatP2.setOnCheckedChangeListener(this);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("fanP2", isChecked);
                editor.apply();

                Toast.makeText(PhongSo2Activity.this, isChecked ? "Đã bật" : "Đã tắt", Toast.LENGTH_SHORT).show();
                fanP2.setImageResource(isChecked ? R.drawable.fan : R.drawable.fanoff);

                new Handler().postDelayed(() -> sendUpdatedState(), 400);
            }
        });
        connectWebSocket();

    }

    private void startRequestingData() {
        if (!requestingData || webSocket == null) return;
        webSocket.send("Requesting data");
        Log.d("PhongSo1Activity", "Sent: Requesting data");
        handler.postDelayed(() -> startRequestingData(), 2000);
    }


    private void sendUpdatedState() {
        try {
            boolean currentLightP2 = denP2.isChecked();
            boolean currentFanP2 = quatP2.isChecked();
            boolean currentAcP2 = dieuhoaP2.isChecked();
            boolean currentDoorP2 = cuaP2.isChecked();

            if (currentLightP2 == lastLightStateP2 && currentFanP2 == lastFanStateP2 &&
                    currentAcP2 == lastAcStateP2 && currentDoorP2 == lastDoorStateP2) {
                return;
            }

            JSONObject state = new JSONObject();
            JSONObject p2 = new JSONObject();
            p2.put("light", currentLightP2 ? 1 : 0);
            p2.put("fan", currentFanP2 ? 1 : 0);
            p2.put("ac", currentAcP2 ? 1 : 0);
            p2.put("door", currentDoorP2 ? 1 : 0);
            state.put("P2", p2);

            if (webSocket != null) {
                webSocket.send(state.toString());
            }

            lastLightStateP2 = currentLightP2;
            lastFanStateP2 = currentFanP2;
            lastAcStateP2 = currentAcP2;
            lastDoorStateP2 = currentDoorP2;

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
                Log.d("PhongSo2Activity", "WebSocket connected");
                requestingData = true;
                handler.post(() -> startRequestingData());

            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(text);
                        JSONObject p2 = jsonObject.getJSONObject("P2");

                        denP2.setChecked(p2.getInt("light") == 1);
                        quatP2.setChecked(p2.getInt("fan") == 1);
                        dieuhoaP2.setChecked(p2.getInt("ac") == 1);
                        cuaP2.setChecked(p2.getInt("door") == 1);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(1000, null);
                Log.d("PhongSo2Activity", "WebSocket closed");
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                Log.e("PhongSo2Activity", "WebSocket error: " + t.getMessage());
            }
        });
    }


    private void autoUpdateSwitchState() {
        boolean isLightP2On = sharedPreferences.getBoolean("denP2", false);
        if (denP2.isChecked() != isLightP2On) {
            denP2.setChecked(isLightP2On);
            lightP2.setImageResource(isLightP2On ? R.drawable.den : R.drawable.lightoff);
        }

        boolean isDoorP2On = sharedPreferences.getBoolean("doorP2", false);
        if (cuaP2.isChecked() != isDoorP2On) {
            cuaP2.setChecked(isDoorP2On);
            doorP2.setImageResource(isDoorP2On ? R.drawable.dor : R.drawable.doroff);
        }

        boolean isACP2On = sharedPreferences.getBoolean("acP2", false);
        if (dieuhoaP2.isChecked() != isACP2On) {
            dieuhoaP2.setChecked(isACP2On);
            acP2.setImageResource(isACP2On ? R.drawable.ac : R.drawable.acoff);
        }

        boolean isFanP2On = sharedPreferences.getBoolean("fanP2", false);
        if (quatP2.isChecked() != isFanP2On) {
            quatP2.setChecked(isFanP2On);
            fanP2.setImageResource(isFanP2On ? R.drawable.fan : R.drawable.fanoff);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (webSocket != null) {
            webSocket.close(1000, "Activity Destroyed");
            webSocket = null;
        }
        Log.d("PhongSo2Activity", "WebSocket đã được đóng");
    }
}