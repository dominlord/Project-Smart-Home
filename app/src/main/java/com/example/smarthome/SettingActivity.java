package com.example.smarthome;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.java_websocket.WebSocket;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocketListener;

public class SettingActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);


        RelativeLayout buttonHenGio = findViewById(R.id.relativeLayoutHenGio);
        RelativeLayout buttonSetting = findViewById(R.id.relativeLayoutSetting);


        buttonHenGio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this,HenGioActivity.class);
                startActivity(intent);
            }
        });

        buttonSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(SettingActivity.this,ChinhSuaActivity.class);
                startActivity(intent1);
            }
        });



        ViewFlipper viewFlipperHenGio = findViewById(R.id.viewFlipperHenGio);
        ViewFlipper viewFlipperSetting = findViewById(R.id.viewFlipperSetting);


        viewFlipperHenGio.setInAnimation(this, android.R.anim.slide_in_left);
        viewFlipperSetting.setInAnimation(this, android.R.anim.slide_in_left);

        viewFlipperHenGio.showNext();
        viewFlipperSetting.showNext();


        new Handler().postDelayed(() -> {
            viewFlipperHenGio.stopFlipping();
            viewFlipperSetting.stopFlipping();
        }, 3000);




        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_setting);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_setting) {
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
            } else if (itemId == R.id.bottom_user) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
                finish();
                return true;
            }
            return false;

        });
    }


}