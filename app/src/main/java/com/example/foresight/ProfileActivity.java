package com.example.foresight;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foresight.detector.ShakeDetector;
import com.google.firebase.auth.FirebaseAuth;


public class ProfileActivity extends Activity {

    //GET
    //intent.putExtra("apiEndpoint", "rest/v1/gym?select=*"); //replace with your own API endpoint
    //intent.putExtra("requestType", "GET"); //replace with your own API endpoint
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

    }
    public void signOut(View view) {

        //Clear des sharedPreferences
        SharedPreferences pref = getSharedPreferences("SessionPref", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove("sessionId");
        editor.apply();

        //Redirection vers page auth
        startActivity(new Intent(ProfileActivity.this, AuthActivity.class));

    }
}