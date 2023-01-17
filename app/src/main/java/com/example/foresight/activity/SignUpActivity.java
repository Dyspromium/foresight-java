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
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.foresight.service.ApiCallIntentService;
import com.example.foresight.R;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;


public class SignUpActivity extends Activity {

    //Input text du formulaire
    EditText mEditMail;
    EditText mEditPass;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Verification qu'un utilisateur est deja connecté
        SharedPreferences pref = getSharedPreferences("SessionPref", 0);
        String sessionId = pref.getString("sessionId", null);
        if(sessionId != null){
            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
        }

        //Récuperation de la configuration actuel du téléphone pour determiner son format (paysage/portrait)
        Configuration configuration = getResources().getConfiguration();
        int orientation = configuration.orientation;

        //Si portrait on met le layout portrait sinon landscape
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_sign_up_landscape);
        } else{
            setContentView(R.layout.activity_sign_up_portrait);
        }

        //Link de la variable avec l'element input du layout
        mEditMail = (EditText)findViewById(R.id.editTextEmailAddress);
        mEditPass = (EditText)findViewById(R.id.editTextPassword);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_sign_up_landscape);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            setContentView(R.layout.activity_sign_up_portrait);
        }
    }

    //Methode pour tenter de s'inscrire
    public void signUp(View view) {

        //Vérification que les inputs sont bien remplis
        if(!mEditMail.getText().toString().equals("") && !mEditPass.getText().toString().equals("")){

            //Appel du service d'api
            Intent intent = new Intent(getApplicationContext(), ApiCallIntentService.class);
            intent.putExtra("apiEndpoint", "auth/v1/signup"); //replace with your own API endpoint
            intent.putExtra("requestType", "POST"); //replace with your own API endpoint
            intent.putExtra("params", "{\"email\": \""+mEditMail.getText().toString()+"\", \"password\": \""+mEditPass.getText().toString()+"\"}"); //replace with your own API endpoint
            startService(intent);

            //Reception du broadcast
            IntentFilter filter = new IntentFilter();
            filter.addAction("ACTION_API_RESPONSE");
            filter.addCategory(Intent.CATEGORY_DEFAULT);

            //Recuperation du code retour de la requete
            BroadcastReceiver sendBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    //Recuperation du code retour de la requete
                    String code = intent.getStringExtra("code");

                    switch (code) {
                        case "500":
                            Snackbar.make(view, "Error contacting server", Snackbar.LENGTH_SHORT).show();
                            break;
                        case "400":
                            //Recuperation de l'erreur 400
                            String error = intent.getStringExtra("response");
                            try {
                                JSONObject jsonObject = new JSONObject(error);
                                Snackbar.make(view, jsonObject.getString("msg"), Snackbar.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                Snackbar.make(view, "Error 404 - Can't parse JSON", Snackbar.LENGTH_SHORT).show();
                            }
                            break;
                        default:
                            //Recuperation de la réponse
                            String response = intent.getStringExtra("response");
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                JSONObject userObject = new JSONObject(jsonObject.getString("user"));

                                //Ajout aux données en cache de la session
                                SharedPreferences pref = getSharedPreferences("SessionPref", 0);
                                String sessionId = pref.getString("sessionId", "");
                                if (sessionId.equals("")) {
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putString("sessionId", userObject.getString("id"));
                                    editor.apply();
                                }

                                //Redirection vers page principal
                                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                finish();

                            } catch (JSONException e) {
                                Snackbar.make(view, e.toString(), Snackbar.LENGTH_SHORT).show();
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

    //Redirige vers l'activité de connection
    public void goToSignInActivity(View view) {
        finish();
    }
}
