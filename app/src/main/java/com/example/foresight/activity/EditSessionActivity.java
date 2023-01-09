package com.example.foresight.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.foresight.api_class.Exercice;
import com.example.foresight.service.ApiCallIntentService;
import com.example.foresight.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class EditSessionActivity extends Activity {


    private BroadcastReceiver joinBroadcastReceiver;
    private BroadcastReceiver exerciceBroadcastReceiver;

    private List<Exercice> exercices;
    private String sessionId;
    private String sessionName;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_session);

        sessionId = getIntent().getStringExtra("sessionId");
        sessionName = getIntent().getStringExtra("sessionName");
        fetchExerciceJoin();
    }

    private void fetchExerciceJoin(){
        Intent intent = new Intent(getApplicationContext(), ApiCallIntentService.class);
        intent.putExtra("apiEndpoint", "rest/v1/rpc/get_exercices_by_session");
        intent.putExtra("requestType", "POST");
        intent.putExtra("params", "{\"session_id\": \""+sessionId+"\"}"); //replace with your own API endpoint
        startService(intent);

        IntentFilter filter = new IntentFilter();
        filter.addAction("ACTION_API_RESPONSE");
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        EditSessionActivity thisType = this;

        //Recuperation du code retour de la requete
        joinBroadcastReceiver = new BroadcastReceiver() {
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
                            Log.e("Error 404", String.valueOf(jsonObject));

                        } catch (JSONException e) {
                            Log.e("Error 404", "Can't parse JSON");
                        }
                        break;
                    default:
                        String response = intent.getStringExtra("response");
                        try {
                            JSONArray exerciceList = new JSONArray (response);

                            LinearLayout layout = findViewById(R.id.layoutExercices);
                            TextView title = findViewById(R.id.sessionTitle);
                            Log.e("debug", sessionName);
                            title.setText(sessionName);

                            for (int i=0; i < exerciceList.length(); i++) {

                                JSONObject exerciceData = exerciceList.getJSONObject(i);
                                Exercice exercice = new Exercice(exerciceData);

                                LinearLayout wrapper = new LinearLayout(thisType);
                                wrapper.setPadding((int) (15 * getResources().getDisplayMetrics().density),
                                        (int) (7 * getResources().getDisplayMetrics().density),
                                        (int) (15 * getResources().getDisplayMetrics().density),
                                        (int) (7 * getResources().getDisplayMetrics().density));
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                );
                                wrapper.setOrientation(LinearLayout.HORIZONTAL);
                                layoutParams.setMargins(0, 0, 0, (int) (15 * getResources().getDisplayMetrics().density));
                                wrapper.setLayoutParams(layoutParams);
                                wrapper.setBackground(ContextCompat.getDrawable(context, R.drawable.element_border));
                                wrapper.setOnClickListener(v -> {
                                    Log.e("debug", "alo");
                                });


                                TextView name = new TextView(thisType);
                                name.setText(exercice.name);
                                name.setTextSize(20);
                                name.setTextColor(ContextCompat.getColor(context, R.color.gray));
                                LinearLayout.LayoutParams name2params = new LinearLayout.LayoutParams(
                                        0,
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        2);
                                name.setLayoutParams(name2params);
                                wrapper.addView(name);

                                TextView rep = new TextView(thisType);
                                rep.setText(exercice.set+"X"+exercice.rep);
                                rep.setTextSize(20);
                                rep.setTextColor(ContextCompat.getColor(context, R.color.gray));
                                LinearLayout.LayoutParams rep2params = new LinearLayout.LayoutParams(
                                        0,
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        1);
                                rep.setLayoutParams(rep2params);
                                wrapper.addView(rep);


                                TextView weight = new TextView(thisType);
                                weight.setText(exercice.weight+"Kg");
                                weight.setTextSize(20);
                                weight.setTextColor(ContextCompat.getColor(context, R.color.gray));
                                LinearLayout.LayoutParams weight2params = new LinearLayout.LayoutParams(
                                        0,
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        1);
                                weight.setLayoutParams(weight2params);
                                wrapper.addView(weight);


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

                                exercices.add(exercice);

                            }
                        } catch (JSONException e) {
                            Log.e("Error", e.toString());
                        }
                        break;
                }
            }
        };
        registerReceiver(joinBroadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(joinBroadcastReceiver != null){
            unregisterReceiver(joinBroadcastReceiver);
            joinBroadcastReceiver = null;
        }
        if(exerciceBroadcastReceiver != null){
            unregisterReceiver(exerciceBroadcastReceiver);
            exerciceBroadcastReceiver = null;
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if(joinBroadcastReceiver != null){
            unregisterReceiver(joinBroadcastReceiver);
            joinBroadcastReceiver = null;
        }
        if(exerciceBroadcastReceiver != null){
            unregisterReceiver(exerciceBroadcastReceiver);
            exerciceBroadcastReceiver = null;
        }
    }
}
