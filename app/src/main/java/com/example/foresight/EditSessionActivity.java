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


public class EditSessionActivity extends Activity {

    int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;

    private BroadcastReceiver sendBroadcastReceiver;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_session);

        String sessionId = getIntent().getStringExtra("sessionId");
        Log.e("Error 404", sessionId);


        Intent intent = new Intent(getApplicationContext(), ApiCallIntentService.class);
        intent.putExtra("apiEndpoint", "rest/v1/rpc/get_exercices_by_session");
        intent.putExtra("requestType", "POST");
        intent.putExtra("params", "{\"session_id\": "+sessionId+"}"); //replace with your own API endpoint
        startService(intent);

        IntentFilter filter = new IntentFilter();
        filter.addAction("ACTION_API_RESPONSE");
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        EditSessionActivity thisType = this;

        //Recuperation du code retour de la requete
        sendBroadcastReceiver = new BroadcastReceiver() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onReceive(Context context, Intent intent) {

                //Recuperation du code retour de la requete
                String code = intent.getStringExtra("code");
                Log.e("Error 404", code);

                switch (code) {
                    case "500":
                        break;
                    case "400":
                        String error = intent.getStringExtra("response");
                        try {
                            JSONObject jsonObject = new JSONObject(error);
                            Log.e("Error", String.valueOf(jsonObject));

                        } catch (JSONException e) {
                            Log.e("Error 404", "Can't parse JSON");
                        }
                        break;
                    default:
                        String response = intent.getStringExtra("response");

                        try {
                            JSONArray exerciceList = new JSONArray (response);
                            Log.e("Error", String.valueOf(exerciceList));



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
