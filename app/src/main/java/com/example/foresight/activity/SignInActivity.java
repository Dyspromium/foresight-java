package com.example.foresight.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import com.example.foresight.service.ApiCallIntentService;
import com.example.foresight.R;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;


public class SignInActivity extends Activity {

    //Input text du formulaire
    EditText mEditMail;
    EditText mEditPass;

    private BroadcastReceiver sendBroadcastReceiver;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Verification qu'un utilisateur est deja connecté
        SharedPreferences pref = getSharedPreferences("SessionPref", 0);
        String sessionId = pref.getString("sessionId", null);
        if(sessionId != null){
            startActivity(new Intent(SignInActivity.this, MainActivity.class));
        }

        Configuration configuration = getResources().getConfiguration();
        int orientation = configuration.orientation;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_sign_in_landscape);
        } else{
            setContentView(R.layout.activity_sign_in_portrait);
        }

        //Link de la variable avec l'element input du layout
        mEditMail = (EditText)findViewById(R.id.editTextEmailAddress);
        mEditPass = (EditText)findViewById(R.id.editTextPassword);

    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Log.e("debug", "config update");
        Log.e("debug", String.valueOf(newConfig.orientation));

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_sign_in_landscape);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            setContentView(R.layout.activity_sign_in_portrait);
        }
    }

    public void signIn(View view) {

        //Vérification que les inputs sont bien remplis
        if(!mEditMail.getText().toString().equals("") && !mEditPass.getText().toString().equals("")){

            //Appel du service d'api
            Intent intent = new Intent(getApplicationContext(), ApiCallIntentService.class);
            intent.putExtra("apiEndpoint", "auth/v1/token?grant_type=password"); //replace with your own API endpoint
            intent.putExtra("requestType", "POST"); //replace with your own API endpoint
            intent.putExtra("params", "{\"email\": \""+mEditMail.getText().toString()+"\", \"password\": \""+mEditPass.getText().toString()+"\"}"); //replace with your own API endpoint
            startService(intent);

            //Reception du broadcast
            IntentFilter filter = new IntentFilter();
            filter.addAction("ACTION_API_RESPONSE");
            filter.addCategory(Intent.CATEGORY_DEFAULT);

            sendBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    //Recuperation du code retour de la requete
                    String code = intent.getStringExtra("code");

                    switch (code) {
                        case "500":
                            Snackbar.make(view, "Error contacting server", Snackbar.LENGTH_SHORT).show();
                            break;
                        case "400":
                            String error = intent.getStringExtra("response");
                            try {
                                Log.e("debug", error);
                                JSONObject jsonObject = new JSONObject(error);
                                Snackbar.make(view, jsonObject.getString("msg"), Snackbar.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                Snackbar.make(view, "Error 404", Snackbar.LENGTH_SHORT).show();
                            }
                            break;
                        default:
                            String response = intent.getStringExtra("response");

                            Log.e("debug", response);
                            try {
                                JSONObject jsonObject = new JSONObject(response);

                                SharedPreferences pref = getSharedPreferences("SessionPref", 0);

                                String sessionId = pref.getString("sessionId", null);
                                if (sessionId == null) {
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putString("sessionId", jsonObject.getJSONObject("user").getString("id"));
                                    editor.apply();
                                }
                                //Redirection vers page principal
                                startActivity(new Intent(SignInActivity.this, MainActivity.class));

                            } catch (JSONException e) {
                                Snackbar.make(view, Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_SHORT).show();
                            }
                            break;
                    }
                }
            };
            registerReceiver(sendBroadcastReceiver, filter);
        }else{
            Snackbar.make(view, "Please Provide Email & Password", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void goToSignUpActivity(View view) {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }


}