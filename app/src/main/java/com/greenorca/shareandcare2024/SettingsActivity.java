package com.greenorca.shareandcare2024;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        SharedPreferences loginPrefs = getSharedPreferences("user_prefs",MODE_PRIVATE);
        EditText txtuser = findViewById(R.id.txtUser);
        EditText txtPasswd = findViewById(R.id.txtPasswd);
        txtuser.setText(loginPrefs.getString("user", ""));
        txtPasswd.setText(loginPrefs.getString("passwd", ""));
    }

    public void save(View v){
        SharedPreferences loginPrefs = getSharedPreferences("user_prefs",MODE_PRIVATE);
        SharedPreferences.Editor edi = loginPrefs.edit();
        EditText txtuser = findViewById(R.id.txtUser);
        EditText txtPasswd = findViewById(R.id.txtPasswd);
        edi.putString("user", txtuser.getText().toString());
        edi.putString("passwd", txtPasswd.getText().toString());
        edi.commit();
        setResult(1);
        finish();
    }

}