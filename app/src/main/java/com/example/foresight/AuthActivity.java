package com.example.foresight;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;


public class AuthActivity extends Activity {

    private FirebaseAuth mAuth;

    EditText mEditMail;
    EditText mEditPass;
    CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            goToMainActivity();
        }
        setContentView(R.layout.activity_auth);
        mAuth = FirebaseAuth.getInstance();

        mEditMail = (EditText)findViewById(R.id.editTextTextEmailAddress);
        mEditPass = (EditText)findViewById(R.id.editTextTextPassword);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
    }

    private void goToMainActivity() {
        Intent switchActivityIntent = new Intent(this, MainActivity.class);
        startActivity(switchActivityIntent);
    }


    public void signIn(View view) {
        if(!mEditMail.getText().toString().equals("") || !mEditPass.getText().toString().equals("")){
            mAuth.signInWithEmailAndPassword(mEditMail.getText().toString(), mEditPass.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            goToMainActivity();
                        } else {
                            System.out.println("Not found");
                        }
                    }
                }
            );
        }else{
            System.out.println("Please provide mail & password");
        }
    }

    public void signUp(View view) {
        if(!mEditMail.getText().toString().equals("") || !mEditPass.getText().toString().equals("")){

            mAuth.createUserWithEmailAndPassword(mEditMail.getText().toString(), mEditPass.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            goToMainActivity();
                        } else {
                            System.out.println("Impossible to create");
                        }
                    }
                }
            );
        }else{
            System.out.println("Please provide mail & password");
        }
    }
}
