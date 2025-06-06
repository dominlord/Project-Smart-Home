package com.example.smarthome;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.widget.ViewFlipper;


import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.java_websocket.client.WebSocketClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class FunctionActivity extends AppCompatActivity {
    private TextView RecordText;
    private LottieAnimationView recordButton;
    private boolean isSpeaking = false;
    private WebSocket webSocket;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_function);

        RelativeLayout buttonCamera = findViewById(R.id.relativeLayoutCamera);
        RelativeLayout buttonNode = findViewById(R.id.relativeLayoutNode);

        recordButton = findViewById(R.id.recordButton);
        RecordText = findViewById(R.id.RecordText);
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isSpeaking) {
                    isSpeaking = true;
                    recordButton.cancelAnimation();
                    recordButton.setAnimation(R.raw.voice_animation);
                    recordButton.playAnimation();
                    RecordText.setText(getString(R.string.ghi_am));
                    SpeakNow();
                }
            }
        });

        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FunctionActivity.this,CameraActivity.class);
                startActivity(intent);
            }
        });


        buttonNode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(FunctionActivity.this,QuanLyThietBiActivity.class);
                startActivity(intent2);
            }
        });

        connectWebSocket();


        //chuyen dong filpper
        ViewFlipper viewFlipperCamera = findViewById(R.id.viewFlipperCamera);
        ViewFlipper viewFlipperNode = findViewById(R.id.viewFlipperNode);


        viewFlipperCamera.setInAnimation(this, android.R.anim.slide_in_left);
        viewFlipperNode.setInAnimation(this, android.R.anim.slide_in_left);

        viewFlipperCamera.showNext();
        viewFlipperNode.showNext();

        new Handler().postDelayed(() -> {
            viewFlipperCamera.stopFlipping();
            viewFlipperNode.stopFlipping();
        }, 3000);



        //thanh dieu huong
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_function);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_function) {
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
            } else if (itemId == R.id.bottom_user) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
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











    //ghi am
    private void SpeakNow() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hãy nói lệnh điều khiển đèn...");
        startActivityForResult(intent, 111);
    }

    //trang thai cho ghi am
    private void saveLightStatus(boolean isOn) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("denPK", isOn);
        editor.apply();
    }

    private void saveLight1Status(boolean isOn) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("denP1", isOn);
        editor.apply();
    }

    private void saveLight2Status(boolean isOn) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("denP2", isOn);
        editor.apply();
    }

    private void saveDoorStatus(boolean isOn) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("doorPK", isOn);
        editor.apply();
    }

    private void saveDoor1Status(boolean isOn) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("doorP1", isOn);
        editor.apply();
    }

    private void saveDoor2Status(boolean isOn) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("doorP2", isOn);
        editor.apply();
    }

    private void saveACStatus(boolean isOn) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("acPK", isOn);
        editor.apply();
    }

    private void saveAC1Status(boolean isOn) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("acP1", isOn);
        editor.apply();
    }

    private void saveAC2Status(boolean isOn) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("acP2", isOn);
        editor.apply();
    }

    private void saveFanStatus(boolean isOn) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("fanPK", isOn);
        editor.apply();
    }

    private void saveFan1Status(boolean isOn) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("fanP1", isOn);
        editor.apply();
    }

    private void saveFan2Status(boolean isOn) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("fanP2", isOn);
        editor.apply();
    }

    private void saveGateStatus(boolean isOn) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("Gate", isOn);
        editor.apply();
    }

    private void saveGardenLightStatus(boolean isOn) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("GardenLight", isOn);
        editor.apply();
    }



    private void sendUpdatedState() {
        try {
            JSONObject state = new JSONObject();
            JSONObject pk = new JSONObject();
            JSONObject p1 = new JSONObject();
            JSONObject p2 = new JSONObject();
            JSONObject outdoor = new JSONObject();

            SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            boolean currentLightPK = sharedPreferences.getBoolean("denPK", false);
            boolean currentFanPK = sharedPreferences.getBoolean("fanPK", false);
            boolean currentAcPK = sharedPreferences.getBoolean("acPK", false);
            boolean currentDoorPK = sharedPreferences.getBoolean("doorPK", false);

            boolean currentLightP1 = sharedPreferences.getBoolean("denP1", false);
            boolean currentFanP1 = sharedPreferences.getBoolean("fanP1", false);
            boolean currentAcP1 = sharedPreferences.getBoolean("acP1", false);
            boolean currentDoorP1 = sharedPreferences.getBoolean("doorP1", false);

            boolean currentLightP2 = sharedPreferences.getBoolean("denP2", false);
            boolean currentFanP2 = sharedPreferences.getBoolean("fanP2", false);
            boolean currentAcP2 = sharedPreferences.getBoolean("acP2", false);
            boolean currentDoorP2 = sharedPreferences.getBoolean("doorP2", false);

            boolean currentGate = sharedPreferences.getBoolean("Gate", false);
            boolean currentGardenLight = sharedPreferences.getBoolean("GardenLight", false);

            // Gán trạng thái vào JSON
            pk.put("light", currentLightPK ? 1 : 0);
            pk.put("fan", currentFanPK ? 1 : 0);
            pk.put("ac", currentAcPK ? 1 : 0);
            pk.put("door", currentDoorPK ? 1 : 0);
            state.put("PK", pk);

            p1.put("light", currentLightP1 ? 1 : 0);
            p1.put("fan", currentFanP1 ? 1 : 0);
            p1.put("ac", currentAcP1 ? 1 : 0);
            p1.put("door", currentDoorP1 ? 1 : 0);
            state.put("P1", p1);

            p2.put("light", currentLightP2 ? 1 : 0);
            p2.put("fan", currentFanP2 ? 1 : 0);
            p2.put("ac", currentAcP2 ? 1 : 0);
            p2.put("door", currentDoorP2 ? 1 : 0);
            state.put("P2", p2);

            outdoor.put("Gate", currentGate ? 1 : 0);
            outdoor.put("GardenLight", currentGardenLight ? 1 : 0);
            state.put("outdoor", outdoor);



            if (webSocket != null) {
                webSocket.send(state.toString());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void connectWebSocket() {
        if (webSocket != null) {
            Log.d("FunctionActivity", "WebSocket đã kết nối trước đó");
            return;
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("ws://172.20.10.2:8765").build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                Log.d("FunctionActivity", "WebSocket connected");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(text);
                        logRoomState("PK", jsonObject.getJSONObject("PK"));
                        logRoomState("P1", jsonObject.getJSONObject("P1"));
                        logRoomState("P2", jsonObject.getJSONObject("P2"));
                        logRoomState("outdoor", jsonObject.getJSONObject("outdoor"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }

            private void logRoomState(String roomName, JSONObject roomData) throws JSONException {
                boolean lightState = roomData.getInt("light") == 1;
                boolean fanState = roomData.getInt("fan") == 1;
                boolean acState = roomData.getInt("ac") == 1;
                boolean doorState = roomData.getInt("door") == 1;

                boolean GateState = roomData.getInt("Gate") == 1;
                boolean GardenLightState = roomData.getInt("GardenLight") == 1;

                Log.d("FunctionActivity", roomName + " - Đèn: " + (lightState ? "Bật" : "Tắt"));
                Log.d("FunctionActivity", roomName + " - Quạt: " + (fanState ? "Bật" : "Tắt"));
                Log.d("FunctionActivity", roomName + " - Điều hòa: " + (acState ? "Bật" : "Tắt"));
                Log.d("FunctionActivity", roomName + " - Cửa: " + (doorState ? "Mở" : "Đóng"));
                Log.d("FunctionActivity", roomName + " - Cổng chính: " + (GateState ? "Mở" : "Đóng"));
                Log.d("FunctionActivity", roomName + " - Đèn vườn: " + (GardenLightState ? "Bật" : "Tắt"));
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(1000, null);
                Log.d("FunctionActivity", "WebSocket closed");
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                Log.e("FunctionActivity", "WebSocket error: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            webSocket.close(1000, "Activity Destroyed");
            webSocket = null;
            Log.d("FunctionActivity", "WebSocket đã được đóng");
        }
    }





    //ghi am
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 111 && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && result.size() > 0) {
                String command = result.get(0).toLowerCase(Locale.ROOT);

                //phong khach activity
                if (command.contains("đèn phòng khách sáng")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("denPK", true);
                    editor.apply();
                    sendUpdatedState();

                    saveLightStatus(true);
                    Toast.makeText(this, "Đèn phòng khách đã bật", Toast.LENGTH_SHORT).show();
                } else if (command.contains("đèn phòng khách tắt")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("denPK", false);
                    editor.apply();
                    sendUpdatedState();

                    saveLightStatus(false);
                    Toast.makeText(this, "Đèn phòng khách đã tắt", Toast.LENGTH_SHORT).show();
                }

                if (command.contains("cửa phòng khách mở")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("doorPK", true);
                    editor.apply();
                    sendUpdatedState();

                    saveDoorStatus(true);
                    Toast.makeText(this, "Cửa phòng khách đã mở", Toast.LENGTH_SHORT).show();
                } else if (command.contains("cửa phòng khách đóng")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("doorPK", false);
                    editor.apply();
                    sendUpdatedState();

                    saveDoorStatus(false);
                    Toast.makeText(this, "Cửa phòng khách đã đóng", Toast.LENGTH_SHORT).show();
                }

                if (command.contains("điều hòa phòng khách bật")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("acPK", true);
                    editor.apply();
                    sendUpdatedState();

                    saveACStatus(true);
                    Toast.makeText(this, "Điều hòa phòng khách đã bật", Toast.LENGTH_SHORT).show();
                } else if (command.contains("điều hòa phòng khách tắt")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("acPK", false);
                    editor.apply();
                    sendUpdatedState();

                    saveACStatus(false);
                    Toast.makeText(this, "Điều hòa phòng khách đã tắt", Toast.LENGTH_SHORT).show();
                }

                if (command.contains("quạt phòng khách bật")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("fanPK", true);
                    editor.apply();
                    sendUpdatedState();

                    saveFanStatus(true);
                    Toast.makeText(this, "Quạt phòng khách đã bật", Toast.LENGTH_SHORT).show();
                } else if (command.contains("quạt phòng khách tắt")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("fanPK", false);
                    editor.apply();
                    sendUpdatedState();

                    saveFanStatus(false);
                    Toast.makeText(this, "Quạt phòng khách đã tắt", Toast.LENGTH_SHORT).show();
                }

                //phong so1 activity
                if (command.contains("đèn phòng số 1 sáng")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("denP1", true);
                    editor.apply();
                    sendUpdatedState();

                    saveLight1Status(true);
                    Toast.makeText(this, "Đèn phòng số 1 đã bật", Toast.LENGTH_SHORT).show();
                } else if (command.contains("đèn phòng số 1 tắt")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("denP1", false);
                    editor.apply();
                    sendUpdatedState();

                    saveLight1Status(false);
                    Toast.makeText(this, "Đèn phòng số 1 đã tắt", Toast.LENGTH_SHORT).show();
                }

                if (command.contains("cửa phòng số 1 mở")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("doorP1", true);
                    editor.apply();
                    sendUpdatedState();

                    saveDoor1Status(true);
                    Toast.makeText(this, "Cửa phòng số 1 đã mở", Toast.LENGTH_SHORT).show();
                } else if (command.contains("cửa phòng số 1 đóng")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("doorP1", false);
                    editor.apply();
                    sendUpdatedState();

                    saveDoor1Status(false);
                    Toast.makeText(this, "Cửa phòng số 1 đã đóng", Toast.LENGTH_SHORT).show();
                }

                if (command.contains("điều hòa phòng số 1 bật")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("acP1", true);
                    editor.apply();
                    sendUpdatedState();

                    saveAC1Status(true);
                    Toast.makeText(this, "Điều hòa số 1 đã bật", Toast.LENGTH_SHORT).show();
                } else if (command.contains("điều hòa phòng số 1 tắt")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("acP1", false);
                    editor.apply();
                    sendUpdatedState();

                    saveAC1Status(false);
                    Toast.makeText(this, "Điều hòa phòng số 1 đã tắt", Toast.LENGTH_SHORT).show();
                }

                if (command.contains("quạt phòng số 1 bật")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("fanP1", true);
                    editor.apply();
                    sendUpdatedState();

                    saveFan1Status(true);
                    Toast.makeText(this, "Quạt phòng số 1 đã bật", Toast.LENGTH_SHORT).show();
                } else if (command.contains("quạt phòng số 1 tắt")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("fanP1", false);
                    editor.apply();
                    sendUpdatedState();

                    saveFan1Status(false);
                    Toast.makeText(this, "Quạt phòng số 1 đã tắt", Toast.LENGTH_SHORT).show();
                }

                //phong so2 activity
                if (command.contains("đèn phòng số 2 sáng")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("denP2", true);
                    editor.apply();
                    sendUpdatedState();

                    saveLight2Status(true);
                    Toast.makeText(this, "Đèn phòng số 2 đã bật", Toast.LENGTH_SHORT).show();
                } else if (command.contains("đèn phòng số 2 tắt")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("denP2", false);
                    editor.apply();
                    sendUpdatedState();

                    saveLight2Status(false);
                    Toast.makeText(this, "Đèn phòng số 2 đã tắt", Toast.LENGTH_SHORT).show();
                }

                if (command.contains("cửa phòng số 2 mở")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("doorP2", true);
                    editor.apply();
                    sendUpdatedState();

                    saveDoor2Status(true);
                    Toast.makeText(this, "Cửa phòng số 2 đã mở", Toast.LENGTH_SHORT).show();
                } else if (command.contains("cửa phòng số 2 đóng")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("doorP2", false);
                    editor.apply();
                    sendUpdatedState();

                    saveDoor2Status(false);
                    Toast.makeText(this, "Cửa phòng số 2 đã đóng", Toast.LENGTH_SHORT).show();
                }

                if (command.contains("điều hòa phòng số 2 bật")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("acP2", true);
                    editor.apply();
                    sendUpdatedState();

                    saveAC2Status(true);
                    Toast.makeText(this, "Điều hòa số 2 đã bật", Toast.LENGTH_SHORT).show();
                } else if (command.contains("điều hòa phòng số 2 tắt")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("acP2", false);
                    editor.apply();
                    sendUpdatedState();

                    saveAC2Status(false);
                    Toast.makeText(this, "Điều hòa phòng số 2 đã tắt", Toast.LENGTH_SHORT).show();
                }

                if (command.contains("quạt phòng số 2 bật")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("fanP2", true);
                    editor.apply();
                    sendUpdatedState();

                    saveFan2Status(true);
                    Toast.makeText(this, "Quạt phòng số 2 đã bật", Toast.LENGTH_SHORT).show();
                } else if (command.contains("quạt phòng số 2 tắt")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("fanP2", false);
                    editor.apply();
                    sendUpdatedState();

                    saveFan2Status(false);
                    Toast.makeText(this, "Quạt phòng số 2 đã tắt", Toast.LENGTH_SHORT).show();
                }

                //outdoor activity
                if (command.contains("cổng chính mở")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("Gate", true);
                    editor.apply();
                    sendUpdatedState();

                    saveGateStatus(true);
                    Toast.makeText(this, "Cổng chính đã mở", Toast.LENGTH_SHORT).show();
                } else if (command.contains("cổng chính đóng")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("Gate", false);
                    editor.apply();
                    sendUpdatedState();

                    saveGateStatus(false);
                    Toast.makeText(this, "cổng chính đã đóng", Toast.LENGTH_SHORT).show();
                }

                if (command.contains("đèn vườn bật")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("GardenLight", true);
                    editor.apply();
                    sendUpdatedState();

                    saveGardenLightStatus(true);
                    Toast.makeText(this, "Đèn vườn đã bật", Toast.LENGTH_SHORT).show();
                } else if (command.contains("đèn vườn tắt")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("GardenLight", false);
                    editor.apply();
                    sendUpdatedState();

                    saveGardenLightStatus(false);
                    Toast.makeText(this, "Đèn vườn đã tắt", Toast.LENGTH_SHORT).show();
                }

            }
        }

        isSpeaking = false;
        recordButton.cancelAnimation();
        recordButton.setAnimation(R.raw.voice);
        recordButton.playAnimation();
        RecordText.setText("Điều khiển bằng giọng nói");
    }





}