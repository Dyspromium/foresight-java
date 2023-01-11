package com.example.foresight.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foresight.R;
import com.example.foresight.api_class.Session;

public class SessionActivity extends AppCompatActivity {

    Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = getIntent().getParcelableExtra("session");

        Log.e("debug", session.name);

        setContentView(R.layout.activity_session);
    }

    private void goToAuthActivity() {
        Intent switchActivityIntent = new Intent(this, SignInActivity.class);
        startActivity(switchActivityIntent);
    }
}