package com.example.smarthome;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class RoomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_room);

        RelativeLayout buttonPK = findViewById(R.id.relativeLayoutButton);
        RelativeLayout buttonP1 = findViewById(R.id.relativeLayoutButton1);
        RelativeLayout buttonP2 = findViewById(R.id.relativeLayoutButton2);
        RelativeLayout buttonP3 = findViewById(R.id.relativeLayoutButton3);


        buttonPK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RoomActivity.this,PhongKhachActivity.class);
                startActivity(intent);
            }
        });

        buttonP1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(RoomActivity.this,PhongSo1Activity.class);
                startActivity(intent1);
            }
        });

        buttonP2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(RoomActivity.this,PhongSo2Activity.class);
                startActivity(intent2);
            }
        });

        buttonP3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent3 = new Intent(RoomActivity.this,NhietDoPhongActivity.class);
                startActivity(intent3);
            }
        });












        //chuyen dong filpper
        ViewFlipper viewFlipper1 = findViewById(R.id.viewFlipper1);
        ViewFlipper viewFlipper2 = findViewById(R.id.viewFlipper2);
        ViewFlipper viewFlipper3 = findViewById(R.id.viewFlipper3);
        ViewFlipper viewFlipper4 = findViewById(R.id.viewFlipper4);

        viewFlipper1.setInAnimation(this, android.R.anim.slide_in_left);
        viewFlipper2.setInAnimation(this, android.R.anim.slide_in_left);
        viewFlipper3.setInAnimation(this, android.R.anim.slide_in_left);
        viewFlipper4.setInAnimation(this, android.R.anim.slide_in_left);

        viewFlipper1.showNext();
        viewFlipper2.showNext();
        viewFlipper3.showNext();
        viewFlipper4.showNext();

        new Handler().postDelayed(() -> {
            viewFlipper1.stopFlipping();
            viewFlipper2.stopFlipping();
            viewFlipper3.stopFlipping();
            viewFlipper4.stopFlipping();
        }, 3000);


        //thanh dieu huong
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_room);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_room) {
                return true;
            } else if (itemId == R.id.bottom_user) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
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
}