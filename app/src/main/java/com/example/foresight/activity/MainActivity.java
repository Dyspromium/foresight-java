package com.example.foresight.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.foresight.api_class.Session;
import com.example.foresight.service.ApiCallIntentService;
import com.example.foresight.R;
import com.example.foresight.detector.ShakeDetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    private BroadcastReceiver sendBroadcastReceiver;

    public ArrayList<Session> sessions = new ArrayList<>();
    public Session selectedSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //Sensor shaking
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(count -> {
            Intent switchActivityIntent = new Intent(this, SessionActivity.class);
            startActivity(switchActivityIntent);
        });

        Intent intent = new Intent(getApplicationContext(), ApiCallIntentService.class);
        intent.putExtra("apiEndpoint", "rest/v1/sessions?select=*");
        intent.putExtra("requestType", "GET");
        startService(intent);

        IntentFilter filter = new IntentFilter();
        filter.addAction("ACTION_API_RESPONSE");
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        MainActivity thisType = this;

        //Recuperation du code retour de la requete
        sendBroadcastReceiver = new BroadcastReceiver() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onReceive(Context context, Intent intent) {

                //Recuperation du code retour de la requete
                String code = intent.getStringExtra("code");

                switch (code) {
                    case "500":
                        break;
                    case "400":
                        String error = intent.getStringExtra("response");
                        try {
                            JSONObject jsonObject = new JSONObject(error);
                        } catch (JSONException e) {
                            Log.e("Error 404", "Can't parse JSON");
                        }
                        break;
                    default:
                        String response = intent.getStringExtra("response");

                        try {
                            JSONArray sessionList = new JSONArray (response);

                            LinearLayout layout = findViewById(R.id.layoutSession);

                            for (int i=0; i < sessionList.length(); i++) {

                                JSONObject sessionObject = sessionList.getJSONObject(i);
                                Session session = new Session(sessionObject);
                                selectedSession = session;

                                LinearLayout wrapper = new LinearLayout(thisType);
                                wrapper.setPadding((int) (15 * getResources().getDisplayMetrics().density),
                                        (int) (7 * getResources().getDisplayMetrics().density),
                                        (int) (15 * getResources().getDisplayMetrics().density),
                                        (int) (7 * getResources().getDisplayMetrics().density));
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                );
                                layoutParams.setMargins(0, 0, 0, (int) (15 * getResources().getDisplayMetrics().density));
                                wrapper.setLayoutParams(layoutParams);
                                wrapper.setBackground(ContextCompat.getDrawable(context, R.drawable.element_border));
                                wrapper.setOnClickListener(v -> {
                                    for (int j=0; j< sessions.size(); j++) {
                                        if(sessions.get(j).fk_session == session.fk_session){
                                            selectedSession = sessions.get(j);
                                            LinearLayout selected = findViewById(selectedSession.fk_session);
                                            selected.setBackground(ContextCompat.getDrawable(context, R.drawable.element_border_selected));
                                        }
                                        else{
                                            LinearLayout selected = findViewById(sessions.get(j).fk_session);
                                            selected.setBackground(ContextCompat.getDrawable(context, R.drawable.element_border));
                                        }
                                    }
                                    //goToEditSession(String.valueOf(session.fk_session), session.name);
                                });
                                wrapper.setId(session.fk_session);

                                TextView name = new TextView(thisType);
                                name.setText(session.name);
                                name.setTextSize(20);
                                name.setTextColor(ContextCompat.getColor(context, R.color.gray));
                                wrapper.addView(name);

                                View separator = new View(thisType);
                                separator.setLayoutParams(
                                        new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                1,
                                                1f
                                        )
                                );
                                wrapper.addView(separator);

                                ImageView icon = new ImageView(thisType);
                                icon.setLayoutParams(
                                        new LinearLayout.LayoutParams(
                                                (int) (30 * getResources().getDisplayMetrics().density),
                                                (int) (30 * getResources().getDisplayMetrics().density)
                                        )
                                );
                                icon.setBackground(ContextCompat.getDrawable(context, R.drawable.edit_icon));
                                wrapper.addView(icon);

                                layout.addView(wrapper);

                                sessions.add(session);
                            }

                            LinearLayout selected = findViewById(selectedSession.fk_session);
                            selected.setBackground(ContextCompat.getDrawable(context, R.drawable.element_border_selected));


                        } catch (JSONException e) {
                            Log.e("Error", e.toString());
                        }
                        break;
                }
            }
        };
        registerReceiver(sendBroadcastReceiver, filter);

    }

    public void goToSessionActivity(View view) {
        Intent switchActivityIntent = new Intent(this, SessionActivity.class);
        switchActivityIntent.putExtra("session", (Parcelable) selectedSession);
        startActivity(switchActivityIntent);
    }
    public void goToSettingsActivity(View view) {
        Intent switchActivityIntent = new Intent(this, SettingsActivity.class);
        startActivity(switchActivityIntent);
    }

    public void goToEditSession(String id, String name) {
        Intent switchActivityIntent = new Intent(this, EditSessionActivity.class);
        switchActivityIntent.putExtra("sessionId", id);
        switchActivityIntent.putExtra("sessionName", name);
        startActivity(switchActivityIntent);
    }

    public void goToFindGym(View view) {
        Intent switchActivityIntent = new Intent(this, FindGymActivity.class);
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
        if(sendBroadcastReceiver != null){
            unregisterReceiver(sendBroadcastReceiver);
            sendBroadcastReceiver = null;
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(sendBroadcastReceiver != null){
            unregisterReceiver(sendBroadcastReceiver);
            sendBroadcastReceiver = null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


}