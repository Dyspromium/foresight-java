package com.example.foresight;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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

import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;


public class AuthActivity extends Activity {


    private FirebaseService mFirebaseService;
    boolean mServiceBound = false;

    EditText mEditMail;
    EditText mEditPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_auth);

        mEditMail = (EditText)findViewById(R.id.editTextTextEmailAddress);
        mEditPass = (EditText)findViewById(R.id.editTextTextPassword);
    }



    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, FirebaseService.class);
        startService(intent);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mServiceBound) {
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            FirebaseService.MyBinder myBinder = (FirebaseService.MyBinder) service;
            mFirebaseService = myBinder.getService();
            mServiceBound = true;

            if(mFirebaseService.getAuthentificatedUser()){
                startActivity(new Intent(AuthActivity.this, MainActivity.class));
            }
        }
    };



    public void signIn(View view) {
        if(mFirebaseService.signIn(mEditMail.getText().toString(),mEditPass.getText().toString())){
            startActivity(new Intent(AuthActivity.this, MainActivity.class));
        }else{
            Snackbar.make(view, mFirebaseService.getLastError(), Snackbar.LENGTH_SHORT).show();
        }
    }

    public void signUp(View view) {
        if(mFirebaseService.signUp(mEditMail.getText().toString(),mEditPass.getText().toString())){
            startActivity(new Intent(AuthActivity.this, MainActivity.class));
        }else{
            Snackbar.make(view, mFirebaseService.getLastError(), Snackbar.LENGTH_SHORT).show();
        }
    }
}
