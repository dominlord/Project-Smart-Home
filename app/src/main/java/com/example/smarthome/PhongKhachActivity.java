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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class PhongKhachActivity extends AppCompatActivity {
    Switch denPK, dieuhoaPK, cuaPK, quatPK;
    ImageView doorPK, acPK, lightPK, fanPK, ImageViewPK , backBtn;
    private boolean lastLightStatePK, lastFanStatePK, lastAcStatePK, lastDoorStatePK;
    private WebSocket webSocket , CameraPKWebsocket;
    SharedPreferences sharedPreferences;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean requestingData = false;
    private ProgressBar progressBarPK;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_phong_khach);

        backBtn = findViewById(R.id.backBtn);
        denPK = findViewById(R.id.switch_light);
        dieuhoaPK = findViewById(R.id.switch_ac);
        cuaPK = findViewById(R.id.switch_door);
        quatPK = findViewById(R.id.switch_fan);
        progressBarPK = findViewById(R.id.progressBarPK);


        doorPK = findViewById(R.id.doorButtonPK);
        acPK = findViewById(R.id.acButtonPK);
        lightPK = findViewById(R.id.lightButtonPK);
        fanPK = findViewById(R.id.fanButtonPK);
        ImageViewPK = findViewById(R.id.imageViewPK);
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        backBtn.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
        });

        //quet hen gio
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                autoUpdateSwitchState();
                handler.postDelayed(this, 2000);
            }
        }, 2000);

        ViewFlipper viewFlipperLightPK = findViewById(R.id.viewFlipperLightPK);
        ViewFlipper viewFlipperDoorPK = findViewById(R.id.viewFlipperDoorPK);
        ViewFlipper viewFlipperACPK = findViewById(R.id.viewFlipperACPK);
        ViewFlipper viewFlipperFanPK = findViewById(R.id.viewFlipperFanPK);


        viewFlipperLightPK.setInAnimation(this, android.R.anim.slide_in_left);
        viewFlipperDoorPK.setInAnimation(this, android.R.anim.slide_in_left);
        viewFlipperACPK.setInAnimation(this, android.R.anim.slide_in_left);
        viewFlipperFanPK.setInAnimation(this, android.R.anim.slide_in_left);

        viewFlipperLightPK.showNext();
        viewFlipperDoorPK.showNext();
        viewFlipperACPK.showNext();
        viewFlipperFanPK.showNext();


        new Handler().postDelayed(() -> {
            viewFlipperLightPK.stopFlipping();
            viewFlipperDoorPK.stopFlipping();
            viewFlipperACPK.stopFlipping();
            viewFlipperFanPK.stopFlipping();
        }, 3000);





        //luu trang thai tu ghi am
        boolean isLightPKOn = sharedPreferences.getBoolean("denPK", false);
        if (isLightPKOn) {
            lightPK.setImageResource(R.drawable.den);
            denPK.setChecked(isLightPKOn);
        } else {
            lightPK.setImageResource(R.drawable.lightoff);
        }

        boolean isDoorPKOn = sharedPreferences.getBoolean("doorPK", false);
        if (isDoorPKOn) {
            doorPK.setImageResource(R.drawable.dor);
            cuaPK.setChecked(isDoorPKOn);
        } else {
            doorPK.setImageResource(R.drawable.doroff);
        }

        boolean isACPKOn = sharedPreferences.getBoolean("acPK", false);
        if (isACPKOn) {
            acPK.setImageResource(R.drawable.ac);
            dieuhoaPK.setChecked(isACPKOn);
        } else {
            acPK.setImageResource(R.drawable.acoff);
        }

        boolean isFanPKOn = sharedPreferences.getBoolean("fanPK", false);
        if (isFanPKOn) {
            fanPK.setImageResource(R.drawable.fan);
            quatPK.setChecked(isFanPKOn);
        } else {
            fanPK.setImageResource(R.drawable.fanoff);
        }




        //switch
        denPK.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                denPK.setOnCheckedChangeListener(null);
                denPK.setChecked(isChecked);
                denPK.setOnCheckedChangeListener(this);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("denPK", isChecked);
                editor.apply();


                Toast.makeText(PhongKhachActivity.this, isChecked ? "Đã bật" : "Đã tắt", Toast.LENGTH_SHORT).show();
                lightPK.setImageResource(isChecked ? R.drawable.den : R.drawable.lightoff);

                new Handler().postDelayed(() -> sendUpdatedState(), 400);
            }
        });


        dieuhoaPK.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dieuhoaPK.setOnCheckedChangeListener(null);
                dieuhoaPK.setChecked(isChecked);
                dieuhoaPK.setOnCheckedChangeListener(this);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("acPK", isChecked);
                editor.apply();

                Toast.makeText(PhongKhachActivity.this, isChecked ? "Đã bật" : "Đã tắt", Toast.LENGTH_SHORT).show();
                acPK.setImageResource(isChecked ? R.drawable.ac : R.drawable.acoff);

                new Handler().postDelayed(() -> sendUpdatedState(), 400);
            }
        });


        cuaPK.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cuaPK.setOnCheckedChangeListener(null);
                cuaPK.setChecked(isChecked);
                cuaPK.setOnCheckedChangeListener(this);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("doorPK", isChecked);
                editor.apply();

                Toast.makeText(PhongKhachActivity.this, isChecked ? "Đã bật" : "Đã tắt", Toast.LENGTH_SHORT).show();
                doorPK.setImageResource(isChecked ? R.drawable.dor : R.drawable.doroff);

                new Handler().postDelayed(() -> sendUpdatedState(), 400);
            }
        });


        quatPK.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                quatPK.setOnCheckedChangeListener(null);
                quatPK.setChecked(isChecked);
                quatPK.setOnCheckedChangeListener(this);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("fanPK", isChecked);
                editor.apply();

                Toast.makeText(PhongKhachActivity.this, isChecked ? "Đã bật" : "Đã tắt", Toast.LENGTH_SHORT).show();
                fanPK.setImageResource(isChecked ? R.drawable.fan : R.drawable.fanoff);

                new Handler().postDelayed(() -> sendUpdatedState(), 400);
            }
        });


        connectWebSocket();
        connectCameraWebSocket();
    }

    private void sendUpdatedState() {
        try {
            boolean currentLightPK = denPK.isChecked();
            boolean currentFanPK = quatPK.isChecked();
            boolean currentAcPK = dieuhoaPK.isChecked();
            boolean currentDoorPK = cuaPK.isChecked();

            if (currentLightPK == lastLightStatePK && currentFanPK == lastFanStatePK &&
                    currentAcPK == lastAcStatePK && currentDoorPK == lastDoorStatePK) {
                return;
            }

            JSONObject state = new JSONObject();
            JSONObject pk = new JSONObject();
            pk.put("light", currentLightPK ? 1 : 0);
            pk.put("fan", currentFanPK ? 1 : 0);
            pk.put("ac", currentAcPK ? 1 : 0);
            pk.put("door", currentDoorPK ? 1 : 0);
            state.put("PK", pk);

            if (webSocket != null) {
                webSocket.send(state.toString());
            }

            lastLightStatePK = currentLightPK;
            lastFanStatePK = currentFanPK;
            lastAcStatePK = currentAcPK;
            lastDoorStatePK = currentDoorPK;

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
                Log.d("PhongKhachActivity", "WebSocket connected");
                handler.post(() -> startRequestingData());
                requestingData = true;
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(text);
                        JSONObject pk = jsonObject.getJSONObject("PK");

                        denPK.setChecked(pk.getInt("light") == 1);
                        quatPK.setChecked(pk.getInt("fan") == 1);
                        dieuhoaPK.setChecked(pk.getInt("ac") == 1);
                        cuaPK.setChecked(pk.getInt("door") == 1);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(1000, null);
                Log.d("PhongKhachActivity", "WebSocket closed");
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                Log.e("PhongKhachActivity", "WebSocket error: " + t.getMessage());
            }
        });
    }

    private void startRequestingData() {
        if (!requestingData || webSocket == null) return;
        webSocket.send("Requesting data");
        Log.d("PhongSo1Activity", "Sent: Requesting data");
        handler.postDelayed(() -> startRequestingData(), 2000);
    }





    //trang thai theo hen gio
    private void autoUpdateSwitchState() {
        boolean isLightPKOn = sharedPreferences.getBoolean("denPK", false);
        if (denPK.isChecked() != isLightPKOn) {
            denPK.setChecked(isLightPKOn);
            lightPK.setImageResource(isLightPKOn ? R.drawable.den : R.drawable.lightoff);
        }

        boolean isDoorPKOn = sharedPreferences.getBoolean("doorPK", false);
        if (cuaPK.isChecked() != isDoorPKOn) {
            cuaPK.setChecked(isDoorPKOn);
            doorPK.setImageResource(isDoorPKOn ? R.drawable.dor : R.drawable.doroff);
        }

        boolean isACPKOn = sharedPreferences.getBoolean("acPK", false);
        if (dieuhoaPK.isChecked() != isACPKOn) {
            dieuhoaPK.setChecked(isACPKOn);
            acPK.setImageResource(isACPKOn ? R.drawable.ac : R.drawable.acoff);
        }

        boolean isFanPKOn = sharedPreferences.getBoolean("fanPK", false);
        if (quatPK.isChecked() != isFanPKOn) {
            quatPK.setChecked(isFanPKOn);
            fanPK.setImageResource(isFanPKOn ? R.drawable.fan : R.drawable.fanoff);
        }
    }

    private void connectCameraWebSocket() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("ws://172.20.10.2:9000").build();
        handler.post(() -> progressBarPK.setVisibility(View.VISIBLE));

        CameraPKWebsocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                Log.d("CameraPKWebSocket", "Camera WebSocket connected");
                handler.post(() -> progressBarPK.setVisibility(View.GONE));

            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    byte[] imageBytes = android.util.Base64.decode(text, android.util.Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    handler.post(() -> {
                        if (bitmap != null) {
                            ImageViewPK.setImageBitmap(bitmap);
                        }
                    });
                } catch (Exception e) {
                    Log.e("CameraPKWebSocket", "Lỗi khi giải mã hình ảnh: " + e.getMessage());
                }
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(1000, null);
                Log.d("CameraPKWebSocket", "WebSocket closed");
                handler.post(() -> progressBarPK.setVisibility(View.GONE));
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {

                Log.e("CameraPKWebSocket", "WebSocket error: " + t.getMessage());

                handler.post(() -> {
                    progressBarPK.setVisibility(View.GONE);
                    Toast.makeText(PhongKhachActivity.this, "Không thể kết nối Server.\n" +
                                                                         "Vui lòng kiểm tra lại mạng.", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (CameraPKWebsocket != null) {
            CameraPKWebsocket.close(1000, "Activity Destroyed");
            CameraPKWebsocket = null;
        }
        if (webSocket != null) {
            webSocket.close(1000, "Activity Destroyed");
            webSocket = null;
        }
        Log.d("CameraCongActivity", "WebSocket đã được đóng");
    }



}
