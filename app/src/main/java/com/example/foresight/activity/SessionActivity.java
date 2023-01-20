package com.example.foresight.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.foresight.R;
import com.example.foresight.api_class.Exercice;
import com.example.foresight.api_class.Session;
import com.example.foresight.detector.OnSwipeTouchListener;
import com.example.foresight.fragment.MenuFragment;
import com.example.foresight.service.ApiCallIntentService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SessionActivity extends AppCompatActivity {

    private static final float SWIPE_THRESHOLD = 100;
    private static final float SWIPE_VELOCITY_THRESHOLD = 500;

    Session session;
    int state = 0;
    private BroadcastReceiver sendBroadcastReceiver;

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = getIntent().getParcelableExtra("session");

        setContentView(R.layout.activity_session);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_menu, new MenuFragment());

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView title = findViewById(R.id.sessionTitle);
        title.setText("Session " + session.name);

        fetchExerciceJoin();
    }

    private void fetchExerciceJoin(){
        Intent intent = new Intent(getApplicationContext(), ApiCallIntentService.class);
        intent.putExtra("apiEndpoint", "rest/v1/rpc/get_exercices_by_session");
        intent.putExtra("requestType", "POST");
        intent.putExtra("params", "{\"session_id\": \""+session.fk_session+"\"}"); //replace with your own API endpoint
        startService(intent);

        IntentFilter filter = new IntentFilter();
        filter.addAction("ACTION_API_RESPONSE");
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        SessionActivity thisType = this;

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
                            Log.e("Error 404", String.valueOf(jsonObject));

                        } catch (JSONException e) {
                            Log.e("Error 404", "Can't parse JSON");
                        }
                        break;
                    default:
                        String response = intent.getStringExtra("response");
                        try {
                            JSONArray exerciceList = new JSONArray (response);

                            for (int i=0; i < exerciceList.length(); i++) {
                                JSONObject exerciceData = exerciceList.getJSONObject(i);

                                Exercice exercice = new Exercice(exerciceData);
                                session.exercices.add(exercice);
                            }
                            updateScreen();
                        } catch (JSONException e) {
                            Log.e("Error", e.toString());
                        }
                        break;
                }
            }
        };
        registerReceiver(sendBroadcastReceiver, filter);
    }

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    private void updateScreen(){
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) LinearLayout exerciceContainer = findViewById(R.id.exerciceState);


        exerciceContainer.setOnTouchListener(new OnSwipeTouchListener(SessionActivity.this) {
            public void onSwipeRight() {
                removeSessionState();
            }
            public void onSwipeLeft() {
                addSessionState();
            }
        });

        exerciceContainer.removeAllViews();

        TextView exerciceName = new TextView(this);
        exerciceName.setText((state+1) +" - " +session.exercices.get(state).name);
        exerciceName.setTextSize(25);
        exerciceName.setTextColor(ContextCompat.getColor(this, R.color.gray));
        exerciceContainer.addView(exerciceName);

        TextView exerciceDesc = new TextView(this);
        exerciceDesc.setText(session.exercices.get(state).description);
        exerciceDesc.setTextSize(18);
        exerciceDesc.setTextColor(ContextCompat.getColor(this, R.color.gray));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, (int) (10 * getResources().getDisplayMetrics().density), 0, (int) (15 * getResources().getDisplayMetrics().density));
        exerciceDesc.setLayoutParams(layoutParams);
        exerciceContainer.addView(exerciceDesc);


        for (int i = 0; i < session.exercices.get(state).set; i++) {
            TextView exerciceSet= new TextView(this);
            exerciceSet.setText(session.exercices.get(state).rep + " rep "+session.exercices.get(state).weight+"Kg");
            exerciceSet.setTextSize(20);
            exerciceSet.setTextColor(ContextCompat.getColor(this, R.color.gray));
            exerciceContainer.addView(exerciceSet);
        }
    }

    @SuppressLint("SetTextI18n")
    public void addSessionState() {
        state++;
        Log.e("debug", String.valueOf(state));
        Log.e("debug", String.valueOf(session.exercices.size()));
        if(state == session.exercices.size()){
            Intent switchActivityIntent = new Intent(this, MainActivity.class);
            startActivity(switchActivityIntent);
            overridePendingTransition(R.anim.hold, R.anim.hold);
        } else if(state == session.exercices.size()-1){
            Button buttonState = findViewById(R.id.buttonState);
            buttonState.setText(getResources().getString(R.string.finish));
            buttonState.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToMainActivity();
                }
            });
            updateScreen();
        }else{
            updateScreen();
        }
    }

    @SuppressLint("SetTextI18n")
    public void removeSessionState() {
        if(state == session.exercices.size()-1){
            Button buttonState = findViewById(R.id.buttonState);
            buttonState.setText(getResources().getString(R.string.next));
            buttonState.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateSessionState(v);
                }
            });
        }
        if(state >0) {
            state--;
        }
        updateScreen();
    }

    private void goToMainActivity() {
        Intent switchActivityIntent = new Intent(this, MainActivity.class);
        startActivity(switchActivityIntent);
        overridePendingTransition(R.anim.hold, R.anim.hold);

    }

    //Animation sur le button back
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.hold, R.anim.hold);
    }

    public void updateSessionState(View view) {
        state++;
        if(state == session.exercices.size()-1){
            Button buttonState = findViewById(R.id.buttonState);
            buttonState.setText(getResources().getString(R.string.finish));
            buttonState.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToMainActivity();
                }
            });
        }
        updateScreen();
    }
}