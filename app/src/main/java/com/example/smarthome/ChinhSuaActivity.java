package com.example.smarthome;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ChinhSuaActivity extends AppCompatActivity {
    LinearLayout logoutButton, languageButton;
    SharedPreferences sharedPreferences;
    FirebaseAuth auth;
    ImageView BackBtnChinhSua;

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String lang = prefs.getString("app_language", "vi");
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chinh_sua);

        // Log ngôn ngữ hiện tại
        String currentLang = getResources().getConfiguration().locale.getLanguage();
        Log.d("LanguageCheck", "Current Language: " + currentLang);

        logoutButton = findViewById(R.id.logoutButton);
        languageButton = findViewById(R.id.languageButton);
        BackBtnChinhSua = findViewById(R.id.backBtnChinhSua);
        auth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        BackBtnChinhSua.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
        });



        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            clearSavedCredentials();
            Intent intent = new Intent(ChinhSuaActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        languageButton.setOnClickListener(v -> showLanguageDialog());


    }

    private void clearSavedCredentials() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    private void showLanguageDialog() {
        final String[] languages = {getString(R.string.vietnamese), getString(R.string.english)};
        final String[] langCodes = {"vi", "en"};

        new android.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_language))
                .setItems(languages, (dialog, which) -> {
                    String selectedLangCode = langCodes[which];
                    saveLanguagePreference(selectedLangCode);

                    // Áp dụng ngôn ngữ mới
                    LocaleHelper.setLocale(this, selectedLangCode);

                    // Khởi động lại Activity hiện tại
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .create()
                .show();
    }



    private void saveLanguagePreference(String langCode) {
        SharedPreferences.Editor editor = getSharedPreferences("LoginPrefs", MODE_PRIVATE).edit();
        editor.putString("app_language", langCode);
        editor.apply();
    }
}
