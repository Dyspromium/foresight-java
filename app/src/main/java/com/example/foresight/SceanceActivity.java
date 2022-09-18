package com.example.foresight;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class SceanceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            setContentView(R.layout.sceance_main);
        } else {
            Intent switchActivityIntent = new Intent(this, AuthActivity.class);
            startActivity(switchActivityIntent);
        }
    }

    private void goToAuthActivity() {
        Intent switchActivityIntent = new Intent(this, AuthActivity.class);
        startActivity(switchActivityIntent);
    }



}