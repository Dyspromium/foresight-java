package com.example.foresight.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.app.Notification;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.foresight.api_class.Session;
import com.example.foresight.fragment.MenuFragment;
import com.example.foresight.notification.NotificationApp;
import com.example.foresight.service.ApiCallIntentService;
import com.example.foresight.R;
import com.example.foresight.detector.ShakeDetector;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //Detection du shaking
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    //Receiver des appels au service API
    private BroadcastReceiver sendBroadcastReceiver;

    //Session à gerer sur l'activité
    public ArrayList<Session> sessions = new ArrayList<>();
    public Session selectedSession;

    //Gestion des notifications
    private NotificationManagerCompat notificationManagerCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //Gestion de la bottom navigation bar
        MainActivity thisType = this;
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @SuppressLint("NonConstantResourceId")
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.home:
                                Intent switchActivityIntentz = new Intent(thisType, MainActivity.class);
                                startActivity(switchActivityIntentz);
                                overridePendingTransition(R.anim.hold, R.anim.fade_out);
                                break;
                            case R.id.search:
                                Intent switchActivityIntent = new Intent(thisType, FindGymActivity.class);
                                startActivity(switchActivityIntent);
                                overridePendingTransition(R.anim.hold, R.anim.fade_out);
                                break;
                            case R.id.settings:
                                Intent switchActivityIntents = new Intent(thisType, SettingsActivity.class);
                                startActivity(switchActivityIntents);
                                overridePendingTransition(R.anim.hold, R.anim.fade_out);
                                break;
                        }
                        return true;
                    }
                });

        //Initialisation du listener du shaking
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(count -> {
            Intent switchActivityIntent = new Intent(this, SessionActivity.class);
            switchActivityIntent.putExtra("session", (Parcelable) selectedSession);
            startActivity(switchActivityIntent);
        });

        //Init du menu top
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_menu, new MenuFragment());

        //Initialization Notifications
        this.notificationManagerCompat = NotificationManagerCompat.from(this);
        sendNotif();

        //Appel au service de gestion d'api pour recuperer les sessions
        Intent intent = new Intent(getApplicationContext(), ApiCallIntentService.class);
        intent.putExtra("apiEndpoint", "rest/v1/sessions?select=*");
        intent.putExtra("requestType", "GET");
        startService(intent);

        IntentFilter filter = new IntentFilter();
        filter.addAction("ACTION_API_RESPONSE");
        filter.addCategory(Intent.CATEGORY_DEFAULT);

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

                            //Element contenant les sessions sur le layout
                            LinearLayout layout = findViewById(R.id.layoutSession);
                            for (int i=0; i < sessionList.length(); i++) {

                                //recuperation d'une session
                                JSONObject sessionObject = sessionList.getJSONObject(i);
                                Session session = new Session(sessionObject);
                                selectedSession = session;

                                //Création de la boite l'enveloppant
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

                                String difficultyString = session.difficulty == 1 ? "Easy" : (session.difficulty == 2 ? "Medium" : "Hard");
                                TextView difficulty = new TextView(thisType);
                                difficulty.setText(difficultyString);
                                difficulty.setTextSize(20);
                                difficulty.setTextColor(ContextCompat.getColor(context, session.difficulty == 1 ? R.color.green : (session.difficulty == 2 ? R.color.yellow : R.color.red)));
                                wrapper.addView(difficulty);

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

    //Switch avec l'activité de session (boutton ou shaking)
    public void goToSessionActivity(View view) {
        Intent switchActivityIntent = new Intent(this, SessionActivity.class);
        switchActivityIntent.putExtra("session", (Parcelable) selectedSession);
        startActivity(switchActivityIntent);
    }

    //Switch avec l'activité de setting
    public void goToSettingsActivity(View view) {
        Intent switchActivityIntent = new Intent(this, SettingsActivity.class);
        startActivity(switchActivityIntent);
    }

    //Switch vers l'activité d'edit de session
    public void goToEditSession(View view) {
        Intent switchActivityIntent = new Intent(this, EditSessionActivity.class);
        switchActivityIntent.putExtra("session", (Parcelable) selectedSession);
        startActivity(switchActivityIntent);
    }

    //Switch avec l'activité pour trouver les salles de sports
    public void goToFindGym(View view) {
        Intent switchActivityIntent = new Intent(this, FindGymActivity.class);
        startActivity(switchActivityIntent);
    }

    //Envoie une notification
    public void sendNotif() {
        String title = "C'est l'heure de la scéance !";
        String message = "Dépense de l'énergie !";

        Notification notification = new NotificationCompat.Builder(this, NotificationApp.CHANNEL_1_ID)
                .setSmallIcon(R.drawable.logo_small)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();

        int notificationId = 1;
        this.notificationManagerCompat.notify(notificationId, notification);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
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
    public void onSensorChanged(SensorEvent sensorEvent) {}

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    //Animation sur le button back
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.hold, R.anim.hold);
    }
}