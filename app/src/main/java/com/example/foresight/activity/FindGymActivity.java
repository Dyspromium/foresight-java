package com.example.foresight.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.example.foresight.fragment.MenuFragment;
import com.example.foresight.service.ApiCallIntentService;
import com.example.foresight.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class FindGymActivity extends AppCompatActivity {

    //Gestion de la permission d'acces à la géolocalisation
    int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;

    //Receiver des appels au service API
    private BroadcastReceiver sendBroadcastReceiver;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_find_gym);

        //Gestion de la bottom navigation bar
        FindGymActivity thisType = this;
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.search);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @SuppressLint("NonConstantResourceId")
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.home:
                                Intent switchActivityIntentz = new Intent(thisType, MainActivity.class);
                                startActivity(switchActivityIntentz);
                                break;
                            case R.id.search:
                                Intent switchActivityIntent = new Intent(thisType, FindGymActivity.class);
                                startActivity(switchActivityIntent);
                                break;
                            case R.id.settings:
                                Intent switchActivityIntents = new Intent(thisType, SettingsActivity.class);
                                startActivity(switchActivityIntents);
                                break;
                        }
                        return true;
                    }
                });

        //Init du menu top
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_menu, new MenuFragment());

        //Appel au service de gestion d'api pour recuperer les sessions
        Intent intent = new Intent(getApplicationContext(), ApiCallIntentService.class);
        intent.putExtra("apiEndpoint", "rest/v1/gym?select=*");
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
                            JSONArray gymList = new JSONArray (response);

                            //Vérification des permissions
                            if (ContextCompat.checkSelfPermission(thisType,
                                    Manifest.permission.ACCESS_FINE_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED) {

                                // Demande la permission
                                ActivityCompat.requestPermissions(thisType,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                            }

                            //Recuperation de latitude longitude
                            LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            //Récupération du container du layout
                            LinearLayout layout = (LinearLayout) findViewById(R.id.layoutGym);

                            for (int i=0; i < gymList.length(); i++) {
                                JSONObject gym = gymList.getJSONObject(i);
                                double gymLatitude = gym.getDouble("latitude ");
                                double gymLongitude = gym.getDouble("longitude");

                                double calculeDistance = calculeDistance(latitude, longitude, gymLatitude, gymLongitude);

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

                                TextView name = new TextView(thisType);
                                name.setText(gym.getString("name"));
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

                                TextView distance = new TextView(thisType);
                                distance.setText(calculeDistance + " Km");
                                distance.setTextSize(20);
                                distance.setTextColor(ContextCompat.getColor(context, R.color.gray));
                                wrapper.addView(distance);

                                layout.addView(wrapper);

                            }
                        } catch (JSONException e) {
                            Log.e("Error", e.toString());
                        }
                        break;
                }
            }
        };
        registerReceiver(sendBroadcastReceiver, filter);
    }

    //Retourne la distance entre 2 points en km
    public double calculeDistance(double lat1, double long1, double lat2, double long2) {
        double dLat = Math.toRadians(lat1 - lat2);
        double dLon = Math.toRadians(long1 - long2);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return round(6371 * c,2);
    }
    //Arrondi un chiffre du nombre de place apres la virgule
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(sendBroadcastReceiver != null){
            unregisterReceiver(sendBroadcastReceiver);
            sendBroadcastReceiver = null;
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }
}
