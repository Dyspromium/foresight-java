package com.example.foresight;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;

import com.example.foresight.detector.ShakeDetector;



public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //Sensor shaking
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(count -> goToSceanceActivity());

        /*Intent intent = new Intent(getApplicationContext(), ApiCallIntentService.class);
        intent.putExtra("apiEndpoint", "rest/v1/gym?select=*"); //replace with your own API endpoint
        intent.putExtra("requestType", "GET"); //replace with your own API endpoint

         */

    }

    private void goToAuthActivity() {
        Intent switchActivityIntent = new Intent(this, AuthActivity.class);
        startActivity(switchActivityIntent);
    }

    private void goToSceanceActivity() {
        Intent switchActivityIntent = new Intent(this, SceanceActivity.class);
        startActivity(switchActivityIntent);
    }

    public void goToProfile(View view) {
        Intent switchActivityIntent = new Intent(this, ProfileActivity.class);
        startActivity(switchActivityIntent);
    }


    @Override
    public void onResume() {
        super.onResume();
        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        // Add the following line to unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}