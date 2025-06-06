package com.example.smarthome;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class HenGioActivity extends AppCompatActivity {
    private static final Logger log = LoggerFactory.getLogger(HenGioActivity.class);
    private static final String PREFS_TIMER = "TIMER_PREF";
    private static final String KEY_END_TIME = "endTime";
    private static final String KEY_DEVICES_JSON = "selectedDevices";
    private static final String KEY_ACTION = "actionBat";

    private Button btnChonThoiGian, btnHenGio, btnChonThietBi, btnStopAll;
    private TextView txtThoiGianHen, txtThietBi, txtCountdown;
    private ImageView backHenGio;
    private int selectedHour = 0, selectedMinute = 0;

    private SharedPreferences sharedPreferences;  // original AppPrefs
    private SharedPreferences prefsTimer;        // để lưu timer
    private Handler handler = new Handler();
    private Set<String> selectedDevices = new HashSet<>();
    private WebSocket webSocket;
    private CountDownTimer countDownTimer;
    private Vibrator vibrator;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hen_gio);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.ice_cream));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // Ánh xạ view
        btnChonThoiGian = findViewById(R.id.btnChonThoiGian);
        btnHenGio = findViewById(R.id.btnHenGio);
        btnChonThietBi = findViewById(R.id.btnChonThietBi);
        btnStopAll = findViewById(R.id.btnStopAll);
        txtThoiGianHen = findViewById(R.id.txtThoiGianHen);
        txtThietBi = findViewById(R.id.txtThietBi);
        txtCountdown = findViewById(R.id.txtCountdown);
        backHenGio = findViewById(R.id.backHenGio);

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefsTimer = getSharedPreferences(PREFS_TIMER, MODE_PRIVATE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        connectWebSocket();
        setupListeners();
        restoreState();  // khôi phục timer và thiết bị
    }

    private void setupListeners() {
        backHenGio.setOnClickListener(v -> finish());

        btnChonThoiGian.setOnClickListener(v -> {
            if (isTimerActive()) {
                Toast.makeText(this, "Đang có hẹn giờ, vui lòng chờ kết thúc!", Toast.LENGTH_SHORT).show();
                return;
            }
            Calendar now = Calendar.getInstance();
            int hour = now.get(Calendar.HOUR_OF_DAY);
            int minute = now.get(Calendar.MINUTE);
            new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
                selectedHour = hourOfDay;
                selectedMinute = minuteOfHour;
                txtThoiGianHen.setText("Hẹn sau: " + selectedHour + " giờ " + selectedMinute + " phút");
            }, hour, minute, true).show();
        });

        btnChonThietBi.setOnClickListener(v -> {
            if (isTimerActive()) {
                Toast.makeText(this, "Đang có hẹn giờ, không thể chọn thiết bị mới!", Toast.LENGTH_SHORT).show();
                return;
            }
            showDeviceSelectionDialog();
        });

        btnHenGio.setOnClickListener(v -> {
            if (isTimerActive()) {
                Toast.makeText(this, "Bạn phải chờ hẹn giờ hiện tại kết thúc trước khi đặt mới.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedDevices.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn thiết bị!", Toast.LENGTH_SHORT).show();
                return;
            }
            long totalMillis = (selectedHour * 60 + selectedMinute) * 60_000L;
            if (totalMillis <= 0) {
                Toast.makeText(this, "Thời gian không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }
            showActionDialog(totalMillis);
        });

        // Nút dừng toàn bộ hẹn giờ
        btnStopAll.setOnClickListener(v -> {
            if (countDownTimer != null) countDownTimer.cancel();
            prefsTimer.edit().clear().apply();
            txtCountdown.setText("00:00:00");
            btnChonThoiGian.setEnabled(true);
            btnChonThietBi.setEnabled(true);
            btnHenGio.setEnabled(true);
            Toast.makeText(this, "Đã dừng hẹn giờ", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean isTimerActive() {
        long endTime = prefsTimer.getLong(KEY_END_TIME, -1);
        return endTime > System.currentTimeMillis();
    }

    private void showActionDialog(long totalMillis) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn hành động khi hẹn giờ");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        CheckBox cbBat = new CheckBox(this), cbTat = new CheckBox(this);
        cbBat.setText("Bật thiết bị"); cbTat.setText("Tắt thiết bị");
        cbBat.setOnCheckedChangeListener((b, c) -> { if (c) cbTat.setChecked(false); });
        cbTat.setOnCheckedChangeListener((b, c) -> { if (c) cbBat.setChecked(false); });
        layout.addView(cbBat); layout.addView(cbTat);
        builder.setView(layout);

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            if (!cbBat.isChecked() && !cbTat.isChecked()) {
                Toast.makeText(this, "Vui lòng chọn hành động Bật hoặc Tắt!", Toast.LENGTH_SHORT).show();
                return;
            }
            final boolean isBat = cbBat.isChecked();
            saveTimerPrefs(totalMillis, isBat);
            startTimer(totalMillis, isBat);
            btnChonThoiGian.setEnabled(false);
            btnChonThietBi.setEnabled(false);
            btnHenGio.setEnabled(false);
            new AlertDialog.Builder(this)
                    .setTitle("Xác Nhận")
                    .setMessage("Đã hẹn " + (isBat?"bật":"tắt") + " " + selectedDevices.size() +
                            " thiết bị sau " + selectedHour + " giờ " + selectedMinute + " phút.")
                    .setPositiveButton("OK", null).show();
        });
        builder.setNegativeButton("Hủy", (d, w) -> { if (countDownTimer != null) countDownTimer.cancel(); });
        builder.show();
    }

    private void saveTimerPrefs(long millisInFuture, boolean isBat) {
        long endTime = System.currentTimeMillis() + millisInFuture;
        SharedPreferences.Editor editor = prefsTimer.edit();
        editor.putLong(KEY_END_TIME, endTime);
        editor.putBoolean(KEY_ACTION, isBat);
        String json = gson.toJson(new ArrayList<>(selectedDevices));
        editor.putString(KEY_DEVICES_JSON, json);
        editor.apply();
    }

    private void restoreState() {
        long endTime = prefsTimer.getLong(KEY_END_TIME, -1);
        if (endTime > System.currentTimeMillis()) {
            String json = prefsTimer.getString(KEY_DEVICES_JSON, null);
            if (json != null) {
                Type type = new TypeToken<ArrayList<String>>(){}.getType();
                ArrayList<String> list = gson.fromJson(json, type);
                selectedDevices.clear(); selectedDevices.addAll(list);
                txtThietBi.setText("Đã chọn: " + selectedDevices);
            }
            long timeLeft = endTime - System.currentTimeMillis();
            boolean isBat = prefsTimer.getBoolean(KEY_ACTION, true);
            startTimer(timeLeft, isBat);
            btnChonThoiGian.setEnabled(false);
            btnChonThietBi.setEnabled(false);
            btnHenGio.setEnabled(false);
        } else {
            prefsTimer.edit().clear().apply();
        }
    }

    private void startTimer(long millisInFuture, boolean isBat) {
        if (countDownTimer != null) countDownTimer.cancel();
        countDownTimer = new CountDownTimer(millisInFuture, 1000) {
            @Override public void onTick(long ms) {
                int h = (int)(ms/3600000);
                int m = (int)((ms%3600000)/60000);
                int s = (int)((ms%60000)/1000);
                txtCountdown.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", h,m,s));
            }

            @Override public void onFinish() {
                txtCountdown.setText("00:00:00");
                vibrate();
                executeAction(isBat); // ✅ truyền isBat vào đây
                btnChonThoiGian.setEnabled(true);
                btnChonThietBi.setEnabled(true);
                btnHenGio.setEnabled(true);
                prefsTimer.edit().clear().apply();

                // ✅ Gửi trạng thái mới về server
                sendUpdatedState();

                // ✅ Hiển thị xác nhận
                String actionDone = isBat ? "bật" : "tắt";
                Toast.makeText(HenGioActivity.this,
                        "Đã " + actionDone + " " + selectedDevices.toString(),
                        Toast.LENGTH_SHORT).show();
            }
        }.start();
    }


    private void vibrate() {
        if (vibrator!=null) {
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(500);
            }
        }
    }

    private void executeAction(boolean isBat) {
        SharedPreferences.Editor ed = sharedPreferences.edit();
        for (String device: selectedDevices) {
            ed.putBoolean(device, isBat);
        }
        ed.apply();
        sendUpdatedState();
        Toast.makeText(this, "Đã " + (isBat?"bật":"tắt") + " " + selectedDevices, Toast.LENGTH_SHORT).show();
    }


    private void showDeviceSelectionDialog() {
        String[] devices = {"Đèn Phòng Khách","Điều Hòa Phòng Khách","Cửa Phòng Khách","Quạt Phòng Khách",
                "Đèn Phòng Số 1","Điều Hòa Phòng Số 1","Cửa Phòng Số 1","Quạt Phòng Số 1",
                "Đèn Phòng Số 2","Điều Hòa Phòng Số 2","Cửa Phòng Số 2","Quạt Phòng Số 2"};
        String[] keys = {"denPK","acPK","doorPK","fanPK",
                "denP1","acP1","doorP1","fanP1",
                "denP2","acP2","doorP2","fanP2"};
        boolean[] checked = new boolean[devices.length];
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn Thiết Bị");
        builder.setMultiChoiceItems(devices, checked, (d, i, c) -> {
            if (c) selectedDevices.add(keys[i]); else selectedDevices.remove(keys[i]);
        });
        builder.setPositiveButton("OK", (d, w) -> {
            if (selectedDevices.isEmpty()) txtThietBi.setText("Chưa chọn thiết bị");
            else txtThietBi.setText("Đã chọn: " + selectedDevices);
        });
        builder.setNegativeButton("Hủy", (d, w) -> d.dismiss());
        builder.show();
    }

    private void connectWebSocket() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("ws://172.20.10.2:8765").build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override public void onOpen(WebSocket ws, okhttp3.Response r) { Log.d("HenGio", "connected"); }
            @Override public void onClosing(WebSocket ws, int c, String r) { ws.close(1000, null); }
            @Override public void onFailure(WebSocket ws, Throwable t, okhttp3.Response r) { Log.e("HenGio", "WS error: "+t.getMessage()); }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocket != null) webSocket.close(1000, "Destroyed");
        if (vibrator != null) vibrator.cancel();
        if (countDownTimer != null) countDownTimer.cancel();
    }

    private void sendUpdatedState() {
        SharedPreferences sp = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        JSONObject state = new JSONObject();
        JSONObject pk = new JSONObject(), p1 = new JSONObject(), p2 = new JSONObject();
        try {
            for (String device : selectedDevices) {
                boolean on = sp.getBoolean(device, false);
                pk.put(device, on?1:0);
                p1.put(device, on?1:0);
                p2.put(device, on?1:0);
            }
            state.put("PK", pk);
            state.put("P1", p1);
            state.put("P2", p2);
            if (webSocket != null) webSocket.send(state.toString());
        } catch (JSONException e) { e.printStackTrace(); }
    }
}
