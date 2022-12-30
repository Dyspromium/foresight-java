package com.example.foresight;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;



public class SceanceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void goToAuthActivity() {
        Intent switchActivityIntent = new Intent(this, AuthActivity.class);
        startActivity(switchActivityIntent);
    }

}