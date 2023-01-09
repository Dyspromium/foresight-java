package com.example.foresight.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foresight.R;

public class SessionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_session);
    }

    private void goToAuthActivity() {
        Intent switchActivityIntent = new Intent(this, SignInActivity.class);
        startActivity(switchActivityIntent);
    }
}