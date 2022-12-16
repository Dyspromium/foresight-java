package com.example.foresight;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
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


public class AuthActivity extends Activity {

    EditText mEditMail;
    EditText mEditPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_auth);

        mEditMail = (EditText)findViewById(R.id.editTextTextEmailAddress);
        mEditPass = (EditText)findViewById(R.id.editTextTextPassword);
    }


    public void signIn(View view) {
        /*
        mFirebaseService.signIn(mEditMail.getText().toString(),mEditPass.getText().toString());
        System.out.println("alo");

         */
    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //This is where you will receive the result from IntentService
            System.out.println(intent.getAction());
        }
    };


    //GET
    //intent.putExtra("apiEndpoint", "rest/v1/gym?select=*"); //replace with your own API endpoint
    //intent.putExtra("requestType", "GET"); //replace with your own API endpoint
    public void signUp(View view) {
        Intent intent = new Intent(getApplicationContext(), ApiCallIntentService.class);
        intent.putExtra("apiEndpoint", "auth/v1/signup"); //replace with your own API endpoint
        intent.putExtra("requestType", "POST"); //replace with your own API endpoint
        intent.putExtra("params", "{\"email\": \""+mEditMail.getText().toString()+"\", \"password\": \""+mEditPass.getText().toString()+"\"}"); //replace with your own API endpoint
        startService(intent);
    }
}
