package com.example.foresight;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.okhttp.Response;

import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;


public class AuthActivity extends Activity {

    //Input text du formulaire
    EditText mEditMail;
    EditText mEditPass;

    private BroadcastReceiver sendBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Verification qu'un utilisateur est deja connecté
        SharedPreferences pref = getSharedPreferences("SessionPref", 0);
        String sessionId = pref.getString("sessionId", null);
        if(sessionId != null){
            startActivity(new Intent(AuthActivity.this, MainActivity.class));
        }

        setContentView(R.layout.activity_auth);

        //Link de la variable avec l'element input du layout
        mEditMail = (EditText)findViewById(R.id.editTextTextEmailAddress);
        mEditPass = (EditText)findViewById(R.id.editTextTextPassword);

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
                                JSONObject jsonObject = new JSONObject(error);
                                Snackbar.make(view, jsonObject.getString("msg"), Snackbar.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                Snackbar.make(view, "Error 404", Snackbar.LENGTH_SHORT).show();
                            }
                            break;
                        default:
                            String response = intent.getStringExtra("response");
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                SharedPreferences pref = getSharedPreferences("SessionPref", 0);

                                String sessionId = pref.getString("sessionId", null);
                                if (sessionId == null) {
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putString("sessionId", jsonObject.getString("id"));
                                    editor.apply();
                                }
                                //Redirection vers page principal
                                startActivity(new Intent(AuthActivity.this, MainActivity.class));

                            } catch (JSONException e) {
                                Snackbar.make(view, "Error 404", Snackbar.LENGTH_SHORT).show();
                            }
                            break;
                    }
                }
            };
        }else{
            Snackbar.make(view, "Please Provide Email & Password", Snackbar.LENGTH_SHORT).show();
        }
    }


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
                                JSONObject jsonObject = new JSONObject(error);
                                Snackbar.make(view, jsonObject.getString("msg"), Snackbar.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                Snackbar.make(view, "Error 404", Snackbar.LENGTH_SHORT).show();
                            }
                            break;
                        default:
                            String response = intent.getStringExtra("response");
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                SharedPreferences pref = getSharedPreferences("SessionPref", 0);

                                String sessionId = pref.getString("sessionId", null);
                                if (sessionId == null) {
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putString("sessionId", jsonObject.getString("id"));
                                    editor.apply();
                                }
                                //Redirection vers page principal
                                startActivity(new Intent(AuthActivity.this, MainActivity.class));

                            } catch (JSONException e) {
                                Snackbar.make(view, "Error 404", Snackbar.LENGTH_SHORT).show();
                            }
                            break;
                    }
                }
            };
        }else{
            Snackbar.make(view, "Please Provide Email & Password", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }
}
