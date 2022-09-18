package com.example.foresight;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class FirebaseService extends Service {
    private static final String LOG_TAG = "FirebaseService";
    private final IBinder mBinder = new MyBinder();

    private FirebaseAuth mAuth;

    private String lastError;

    @Override
    public void onCreate() {
        super.onCreate();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public String getLastError(){
        return lastError;
    }


    public boolean getAuthentificatedUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null;
    }

    public boolean signIn(String email, String password) {
        if(!email.equals("") || !password.equals("")){
            mAuth.signInWithEmailAndPassword(email, password);
            if(getAuthentificatedUser()){
                return true;
            }
            lastError = "No matching account";
            return false;
        }
        lastError = "Please Provide Email & Password";
        return false;
    }

    public boolean signUp(String email, String password) {
        if(!email.equals("") || !password.equals("")){
            mAuth.createUserWithEmailAndPassword(email, password);
            if(getAuthentificatedUser()){
                return true;
            }
            lastError = "Account already exist";
            return false;
        }
        lastError = "Please Provide Email & Password";
        return false;
    }



    public class MyBinder extends Binder {
        FirebaseService getService() {
            return FirebaseService.this;
        }
    }
}