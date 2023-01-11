package com.example.foresight.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.example.foresight.R;


public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

    }
    public void signOut(View view) {

        //Clear des sharedPreferences
        SharedPreferences pref = getSharedPreferences("SessionPref", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove("sessionId");
        editor.apply();

        //Redirection vers page auth
        startActivity(new Intent(SettingsActivity.this, SignInActivity.class));

    }
}