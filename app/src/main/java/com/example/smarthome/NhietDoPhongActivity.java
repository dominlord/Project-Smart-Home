package com.example.smarthome;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

public class NhietDoPhongActivity extends AppCompatActivity {
    ImageView backBtn4 ;
    TextView HumiText , TempText , FireWarningText, RainText, BrightnessText ;
    private double temperature = 0;
    private double brightness = 0 ;
    private double humidity = 0;
    private double rain = 0 ;
    private int fireWarning = 0;
    private WebSocket webSocket;
    private Handler handler = new Handler(Looper.getMainLooper());
    ProgressBar progressBarFireWarning , progressBarHumi , progressBarTemp , progressBarRain, progressBarBrightness ;

    private double lastTemperature = -1, lastHumidity = -1;
    private int lastFireWarning = -1;
    private double lastRain = -1;
    private double lastBrightness = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nhiet_do_phong);

        backBtn4 = findViewById(R.id.backBtn4);
        TempText = findViewById(R.id.TempText);
        FireWarningText = findViewById(R.id.FireText);
        HumiText = findViewById(R.id.HumiText);
        RainText = findViewById(R.id.RainText);
        BrightnessText = findViewById(R.id.BrightnessText);

        progressBarHumi = findViewById(R.id.progressBarHumi);
        progressBarTemp = findViewById(R.id.progressBarTemp);
        progressBarFireWarning = findViewById(R.id.progressBarFireWarning);
        progressBarRain = findViewById(R.id.progressBarRain);
        progressBarBrightness = findViewById(R.id.progressBarBrightness);


        backBtn4.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
        });

        connectWebSocket();
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
            if (temperature != lastTemperature || humidity != lastHumidity || fireWarning != lastFireWarning || brightness != lastBrightness) {
                if (webSocket == null) {
                    Log.e("WebSocket", "Không thể gửi: WebSocket chưa kết nối!");
                    return;
                }

                JSONObject state = new JSONObject();
                state.put("temperature", temperature);
                state.put("humidity", humidity);
                state.put("fireWarning", fireWarning);
                state.put("rain", rain);
                state.put("brightness_level", brightness);

                webSocket.send(state.toString());
                lastTemperature = temperature;
                lastHumidity = humidity;
                lastFireWarning = fireWarning;
                lastBrightness = brightness;
                lastRain = rain;

                Log.d("WebSocket", "Đã gửi dữ liệu cập nhật.");
            } else {
                Log.d("WebSocket", "Dữ liệu không thay đổi, không gửi.");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void connectWebSocket() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("ws://172.20.10.2:8765").build();
        handler.post(() -> progressBarFireWarning.setVisibility(View.VISIBLE));
        handler.post(() -> progressBarHumi.setVisibility(View.VISIBLE));
        handler.post(() -> progressBarTemp.setVisibility(View.VISIBLE));
        handler.post(() -> progressBarRain.setVisibility(View.VISIBLE));
        handler.post(() -> progressBarBrightness.setVisibility(View.VISIBLE));

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                runOnUiThread(() -> {
                    Log.d("NhieDoPhongActivity", "WebSocket connected");
                    handler.post(() -> progressBarFireWarning.setVisibility(View.GONE));
                    handler.post(() -> progressBarHumi.setVisibility(View.GONE));
                    handler.post(() -> progressBarTemp.setVisibility(View.GONE));
                    handler.post(() -> progressBarRain.setVisibility(View.GONE));
                    handler.post(() -> progressBarBrightness.setVisibility(View.GONE));

                    startAutoSending();
                });
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(text);
                        temperature = jsonObject.getDouble("temperature");
                        humidity = jsonObject.getDouble("humidity");
                        fireWarning = jsonObject.getInt("fireWarning");
                        rain = jsonObject.getDouble("rain");
                        brightness = jsonObject.getDouble("brightness_level");

                       TempText.setText(temperature + "°C");
                        HumiText.setText(humidity + "%");
                        RainText.setText(rain + "%");
                        BrightnessText.setText(brightness + "%");
                        String fireStatus = fireWarning == 1
                                ? getString(R.string.fire_danger)
                                : getString(R.string.fire_normal);

                        if (Integer.parseInt(fireStatus) == 1) {
                            FireWarningText.setText(getString(R.string.fire_warning, fireStatus));
                        } else {
                            FireWarningText.setText(getString(R.string.binh_thuong));
                        }




                        if (fireWarning == 1) {
                            Toast.makeText(NhietDoPhongActivity.this, "Cảnh báo cháy! Vui lòng kiểm tra!", Toast.LENGTH_LONG).show();
                        }

                        if (temperature > 30) {
                            Toast.makeText(NhietDoPhongActivity.this, "Nhiệt độ cao! (" + temperature + "°C)", Toast.LENGTH_SHORT).show();
                        }

                        if (lastTemperature != -1 && (temperature - lastTemperature) > 5) {
                            Toast.makeText(NhietDoPhongActivity.this, "Nhiệt độ tăng bất thường!", Toast.LENGTH_SHORT).show();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                runOnUiThread(() -> {
                    Log.e("NhieDoPhongActivity", "WebSocket error: " + t.getMessage());
                    handler.post(() -> progressBarFireWarning.setVisibility(View.GONE));
                    handler.post(() -> progressBarHumi.setVisibility(View.GONE));
                    handler.post(() -> progressBarTemp.setVisibility(View.GONE));
                    handler.post(() -> progressBarRain.setVisibility(View.GONE));
                    handler.post(() -> progressBarBrightness.setVisibility(View.GONE));

                    Toast.makeText(NhietDoPhongActivity.this, "Không thể kết nối Server.\n" +
                            "Vui lòng kiểm tra lại mạng.", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

}