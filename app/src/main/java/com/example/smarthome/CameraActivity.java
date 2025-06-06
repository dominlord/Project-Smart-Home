package com.example.smarthome;


import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.material.floatingactionbutton.FloatingActionButton;


import org.json.JSONException;
import org.json.JSONObject;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;


public class CameraActivity extends AppCompatActivity {


    private ImageView imageViewCong, backBtnCamera;
    private WebSocket cameraWebSocket;
    private WebSocket statusWebSocket;
    private ProgressBar progressBarCameraCong;


    Switch switch_gate, switch_gardenLight;
    SeekBar seekBar_outLight;
    private boolean lastGardenLightState, lastGateState;
    private int lastOutLightState;
    SharedPreferences sharedPreferences;
    private Handler handler = new Handler(Looper.getMainLooper());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera);


        imageViewCong = findViewById(R.id.imageViewCong);
         backBtnCamera = findViewById(R.id.backBtnCameraCong);
         progressBarCameraCong = findViewById(R.id.progressBarCameraCong);


        switch_gate = findViewById(R.id.switch_gate);
        switch_gardenLight = findViewById(R.id.switch_gardenLight);
        seekBar_outLight = findViewById(R.id.OutLightSeekBar);
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);


        connectCameraWebSocket();
        connectStatusWebSocket();


        backBtnCamera.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
        });


        boolean isGateOn = sharedPreferences.getBoolean("Gate", false);
        boolean isGardenLightOn = sharedPreferences.getBoolean("GardenLight", false);
        int outLightValue = sharedPreferences.getInt("OutLight", 0);


        switch_gate.setChecked(isGateOn);
        switch_gardenLight.setChecked(isGardenLightOn);
        seekBar_outLight.setProgress(outLightValue);


        lastGateState = isGateOn;
        lastGardenLightState = isGardenLightOn;
        lastOutLightState = outLightValue;


        switch_gate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("Gate", isChecked);
            editor.apply();
            Toast.makeText(CameraActivity.this, isChecked ? "Đã bật" : "Đã tắt", Toast.LENGTH_SHORT).show();
            handler.postDelayed(this::sendUpdatedState, 400);
        });


        switch_gardenLight.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("GardenLight", isChecked);
            editor.apply();
            Toast.makeText(CameraActivity.this, isChecked ? "Đã bật" : "Đã tắt", Toast.LENGTH_SHORT).show();
            handler.postDelayed(this::sendUpdatedState, 400);
        });


        seekBar_outLight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d("SeekBar", "Giá trị hiện tại: " + progress);
            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}


            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int brightness = seekBar.getProgress();


                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("OutLight", brightness);
                editor.apply();


                sendUpdatedState();
            }
        });
    }


    private void connectCameraWebSocket() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("ws://172.20.10.2:8000").build();
        cameraWebSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                Log.d("CameraCongWebSocket", "Camera WebSocket connected");
            }


            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d("CameraCongWebSocket", "Đã nhận dữ liệu từ server. Độ dài chuỗi base64: " + text.length());
                Bitmap bitmap = decodeBase64ToBitmap(text);
                runOnUiThread(() -> imageViewCong.setImageBitmap(bitmap));
            }


            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(1000, null);
                Log.d("CameraCongWebSocket", "WebSocket closed");
            }


            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                Log.e("CameraCongWebSocket", "WebSocket error: " + t.getMessage());
            }
        });
    }


    private void connectStatusWebSocket() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("ws://172.20.10.2:8765").build();
        handler.post(() -> progressBarCameraCong.setVisibility(View.VISIBLE));

        statusWebSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                Log.d("StatusWebSocket", "Status WebSocket connected");
                handler.post(() -> progressBarCameraCong.setVisibility(View.GONE));
            }


            @Override
            public void onMessage(WebSocket webSocket, String text) {
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(text);
                        JSONObject Outdoor = jsonObject.getJSONObject("Outdoor");


                        switch_gate.setChecked(Outdoor.getInt("Gate") == 1);
                        switch_gardenLight.setChecked(Outdoor.getInt("GardenLight") == 1);
                        seekBar_outLight.setProgress(Outdoor.getInt("OutLight"));


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }


            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(1000, null);
                Log.d("StatusWebSocket", "WebSocket closed");
                handler.post(() -> progressBarCameraCong.setVisibility(View.GONE));
            }


            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                Log.e("StatusWebSocket", "WebSocket error: " + t.getMessage());
                handler.post(() -> {
                    progressBarCameraCong.setVisibility(View.GONE);
                    Toast.makeText(CameraActivity.this, "Không thể kết nối Server.\n" +
                            "Vui lòng kiểm tra lại mạng.", Toast.LENGTH_LONG).show();
                });
            }
        });
    }


    private Bitmap decodeBase64ToBitmap(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }


    private void sendUpdatedState() {
        try {
            boolean currentGate = switch_gate.isChecked();
            boolean currentGardenLight = switch_gardenLight.isChecked();
            int currentOutLight = seekBar_outLight.getProgress();


            if (currentGate == lastGateState && currentGardenLight == lastGardenLightState &&
                    currentOutLight == lastOutLightState) {
                return;
            }


            JSONObject state = new JSONObject();
            JSONObject Outdoor = new JSONObject();
            Outdoor.put("Gate", currentGate ? 1 : 0);
            Outdoor.put("GardenLight", currentGardenLight ? 1 : 0);
            Outdoor.put("OutLight", currentOutLight);
            state.put("Outdoor", Outdoor);


            if (statusWebSocket != null) {
                statusWebSocket.send(state.toString());
            }


            lastGateState = currentGate;
            lastGardenLightState = currentGardenLight;
            lastOutLightState = currentOutLight;


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraWebSocket != null) {
            cameraWebSocket.close(1000, "Activity Destroyed");
            cameraWebSocket = null;
        }
        if (statusWebSocket != null) {
            statusWebSocket.close(1000, "Activity Destroyed");
            statusWebSocket = null;
        }
        Log.d("CameraCongActivity", "WebSocket đã được đóng");
    }
}

