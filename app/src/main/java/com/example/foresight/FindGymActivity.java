package com.example.foresight;

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
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class FindGymActivity extends Activity {

    int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;

    private BroadcastReceiver sendBroadcastReceiver;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_find_gym);

        Intent intent = new Intent(getApplicationContext(), ApiCallIntentService.class);
        intent.putExtra("apiEndpoint", "rest/v1/gym?select=*");
        intent.putExtra("requestType", "GET");
        startService(intent);

        IntentFilter filter = new IntentFilter();
        filter.addAction("ACTION_API_RESPONSE");
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        FindGymActivity thisType = this;

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
                            JSONArray gymList = new JSONArray (response);

                            if (ContextCompat.checkSelfPermission(thisType,
                                    Manifest.permission.ACCESS_FINE_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED) {

                                // Demande la permission
                                ActivityCompat.requestPermissions(thisType,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                            }

                            // Obtenez un référence au LocationManager
                            LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

                            // Obtenez la dernière position connue
                            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                            // Obtenez les informations de localisation à partir de la position
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            LinearLayout layout = (LinearLayout) findViewById(R.id.layoutGym);

                            for (int i=0; i < gymList.length(); i++) {
                                JSONObject gym = gymList.getJSONObject(i);
                                double gymLatitude = gym.getDouble("latitude ");
                                double gymLongitude = gym.getDouble("longitude");

                                double calculeDistance = calculeDistance(latitude, longitude, gymLatitude, gymLongitude);

                                LinearLayout wrapper = new LinearLayout(thisType);

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
