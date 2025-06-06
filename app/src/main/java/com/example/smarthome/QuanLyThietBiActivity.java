package com.example.smarthome;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class QuanLyThietBiActivity extends AppCompatActivity implements OnDeviceStateChangedListener {

    private RecyclerView recyclerView;
    private DeviceAdapter adapter;
    private List<HashMap<String, Object>> deviceList = new ArrayList<>();
    private Button btnCreate, btnDelete;
    private boolean isDeleteMode = false;
    private WebSocket webSocket;
    private ImageView backQuanLy;
    private TextView DeleteMode;
    private HashMap<Integer, Boolean> webSocketNodes = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_thiet_bi);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.ice_cream));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        recyclerView = findViewById(R.id.recyclerView);
        btnCreate = findViewById(R.id.btnCreate);
        btnDelete = findViewById(R.id.btnDelete);
        backQuanLy = findViewById(R.id.backBtnQuanLy);
        DeleteMode = findViewById(R.id.DeleteMode);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DeviceAdapter(deviceList, this::onDeviceClick, this);
        recyclerView.setAdapter(adapter);

        btnCreate.setOnClickListener(v -> createNewDevice());
        btnDelete.setOnClickListener(v -> toggleDeleteMode());
        backQuanLy.setOnClickListener(v -> finishWithAnimation());

        connectWebSocket();
        loadDeviceList();
    }

    private void toggleDeleteMode() {
        isDeleteMode = !isDeleteMode;
        DeleteMode.setText(isDeleteMode
                ? getString(R.string.delete_mode_on)
                : getString(R.string.delete_mode_off));

        Toast.makeText(this, isDeleteMode ? "Chế độ xóa được bật" : "Chế độ xóa tắt", Toast.LENGTH_SHORT).show();
        adapter.notifyDataSetChanged();
    }

    private void finishWithAnimation() {
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
    }

    private void createNewDevice() {
        Set<Integer> usedIds = new HashSet<>();
        for (HashMap<String, Object> device : deviceList) {
            usedIds.add((Integer) device.get("id"));
        }

        int newDeviceId = 1;
        while (usedIds.contains(newDeviceId)) {
            newDeviceId++;
        }
        newDeviceId = newDeviceId;
        webSocketNodes.put(newDeviceId, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nhập tên thiết bị mới");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        int finalNewDeviceId = newDeviceId;
        builder.setPositiveButton("OK", (dialog, which) -> {
            String newDeviceName = input.getText().toString().trim();
            if (newDeviceName.isEmpty()) {
                Toast.makeText(this, "Tên thiết bị không được để trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            HashMap<String, Object> newDevice = new HashMap<>();
            newDevice.put("id", finalNewDeviceId);
            newDevice.put("name", newDeviceName);
            newDevice.put("isOn", false);

            deviceList.add(newDevice);
            adapter.notifyDataSetChanged();
            saveDeviceList();
            Toast.makeText(this, "Đã thêm thiết bị: " + newDeviceName, Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void onDeviceClick(HashMap<String, Object> device) {
        if (isDeleteMode) {
            showDeleteDialog(device);
        }
    }

    private void showDeleteDialog(HashMap<String, Object> device) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có muốn xóa " + device.get("name") + " không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deviceList.remove(device);
                    webSocketNodes.put((Integer) device.get("id"), false);
                    adapter.clearSelection();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Đã xóa " + device.get("name"), Toast.LENGTH_SHORT).show();
                    sendUpdatedState();
                    saveDeviceList();
                    if (deviceList.isEmpty()) isDeleteMode = false;
                })
                .setNegativeButton("Hủy", (dialog, which) -> adapter.clearSelection())
                .show();
    }

    @Override
    public void onDeviceStateChanged() {
        for (HashMap<String, Object> device : deviceList) {
            int id = (int) device.get("id");
            boolean isOn = (boolean) device.get("isOn");
            webSocketNodes.put(id, isOn);
        }
        sendUpdatedState();
        saveDeviceList();
    }

    private void saveDeviceList() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        JSONArray jsonArray = new JSONArray();
        for (HashMap<String, Object> device : deviceList) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", device.get("id"));
                jsonObject.put("name", device.get("name"));
                jsonObject.put("isOn", device.get("isOn"));
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        editor.putString("deviceList", jsonArray.toString());
        editor.apply();
    }

    private void loadDeviceList() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        String jsonString = sharedPreferences.getString("deviceList", null);
        deviceList.clear();

        if (jsonString != null) {
            try {
                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    HashMap<String, Object> device = new HashMap<>();
                    device.put("id", jsonObject.getInt("id"));
                    device.put("name", jsonObject.getString("name"));
                    device.put("isOn", jsonObject.getBoolean("isOn"));
                    deviceList.add(device);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi khi đọc danh sách thiết bị!", Toast.LENGTH_SHORT).show();
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void sendUpdatedState() {
        try {
            JSONObject state = new JSONObject();
            JSONObject NODE = new JSONObject();
            for (Integer id : webSocketNodes.keySet()) {
                boolean isOn = webSocketNodes.get(id);
                NODE.put("node" + id, isOn ? 1 : 0);
            }
            state.put("NODE", NODE);
            if (webSocket != null) {
                webSocket.send(state.toString());
            }
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
                Log.d("WebSocket", "Connected");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(text);
                        JSONArray nodesArray = jsonObject.getJSONArray("nodes");
                        for (int i = 0; i < nodesArray.length(); i++) {
                            JSONObject nodeObject = nodesArray.getJSONObject(i);
                            int id = nodeObject.getInt("id");
                            boolean isOn = nodeObject.getBoolean("isOn");
                            for (HashMap<String, Object> device : deviceList) {
                                if ((Integer) device.get("id") == id) {
                                    device.put("isOn", isOn);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.e("WebSocket", "Lỗi khi xử lý tin nhắn: " + e.getMessage());
                    }
                });
            }
        });
    }
}