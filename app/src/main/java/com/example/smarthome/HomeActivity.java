package com.example.smarthome;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeActivity extends AppCompatActivity {

    private TextView tvStatus, tvTemp, tvHumidity, tvWind, tvCity;
    private LottieAnimationView lottieWeather;
    private final String API_KEY = "4f0922c85ab68519188945c9ded49d1b";
    private String CITY = "Hanoi";
    RelativeLayout BackgroundWeather ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.ice_cream));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        tvStatus = findViewById(R.id.tvStatus);
        tvTemp = findViewById(R.id.tvTemp);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWind = findViewById(R.id.tvWindSpeed);
        lottieWeather = findViewById(R.id.lottieWeather);
        tvCity = findViewById(R.id.tvCity);
        BackgroundWeather = findViewById(R.id.BackgroundWeather);

        SharedPreferences preferences = getSharedPreferences("weather_prefs", MODE_PRIVATE);

        tvStatus.setText(preferences.getString("status", "Đang tải..."));
        tvTemp.setText(preferences.getString("temp", "--°C"));
        tvHumidity.setText(preferences.getString("humidity", "--%"));
        tvWind.setText(preferences.getString("wind", "-- m/s"));

        CITY = preferences.getString("last_city", "Hanoi");
        tvCity.setText(CITY);


        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        boolean isNight = (hour >= 18 || hour < 6);


        if (isNight) {
            BackgroundWeather.setBackgroundResource(R.drawable.nightbackground);
        } else {
            BackgroundWeather.setBackgroundResource(R.drawable.sunbackground);
        }

        getWeather();

        SearchView searchViewCity = findViewById(R.id.searchViewCity);
        searchViewCity.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.trim().isEmpty()) {
                    CITY = query.trim();
                    getWeather();
                    tvCity.setText(CITY);

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("last_city", CITY);
                    editor.apply();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        //xử lí thanh điều hướng
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_home) {
                return true;
            } else if (itemId == R.id.bottom_user) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
                finish();
                return true;
            } else if (itemId == R.id.bottom_room) {
                startActivity(new Intent(getApplicationContext(), RoomActivity.class));
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

    private void getWeather() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherAPI api = retrofit.create(WeatherAPI.class);
        Call<WeatherResponse> call = api.getWeather(CITY, API_KEY, "metric");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse data = response.body();

                    SharedPreferences preferences = getSharedPreferences("weather_prefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();

                    editor.putString("temp", "Nhiệt độ:\n " + data.main.temp + "°C");
                    editor.putString("humidity", "Độ ẩm:\n " + data.main.humidity + "%");
                    editor.putString("wind", "Tốc độ gió:\n " + data.wind.speed + " m/s");
                    editor.apply();

                    Calendar calendar = Calendar.getInstance();
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    boolean isNight = (hour >= 18 || hour < 6);
                    String condition = data.weather[0].main.toLowerCase();
                    int humidity = data.main.humidity;


                    String status;
                    if (humidity < 30) {
                        status = getString(R.string.weather_dry);
                    } else if (humidity > 80) {
                        status = getString(R.string.weather_humid);
                    } else {
                        switch (condition) {
                            case "clear":
                                status = getString(R.string.weather_clear);
                                break;
                            case "clouds":
                                status = getString(R.string.weather_clouds);
                                break;
                            case "rain":
                                status = getString(R.string.weather_rain);
                                break;
                            default:
                                status = getString(R.string.weather_unknown);
                                break;
                        }
                    }

                    String prefix = isNight ? getString(R.string.weather_night) : getString(R.string.weather_day);
                    tvStatus.setText(prefix + status);



                    int animRes;
                    if (isNight) {
                        if ("rain".equals(condition)) animRes = R.raw.rainymoon_anim;
                        else animRes = R.raw.moon_anim;
                    } else {
                        switch (condition) {
                            case "clear": animRes = R.raw.sun_anim; break;
                            case "clouds": animRes = R.raw.cloundsun_anim; break;
                            case "rain": animRes = R.raw.rainy_anim; break;
                            default: animRes = R.raw.sun_anim; break;
                        }
                    }
                    lottieWeather.setAnimation(animRes);
                    lottieWeather.playAnimation();

                    tvTemp.setText(getString(R.string.temp_label, data.main.temp));
                    tvHumidity.setText(getString(R.string.humidity_label, data.main.humidity));
                    tvWind.setText(getString(R.string.wind_label, data.wind.speed));

                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                tvTemp.setText("Lỗi!");
            }
        });
    }
}