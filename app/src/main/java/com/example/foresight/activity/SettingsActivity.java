package com.example.foresight.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.foresight.R;
import com.example.foresight.database.ProfileDatabase;
import com.example.foresight.fragment.MenuFragment;

import java.io.ByteArrayOutputStream;


public class SettingsActivity extends AppCompatActivity {

    int REQUEST_IMAGE_CAPTURE = 1;

    EditText mEditName;
    EditText mEditAge;
    EditText mEditSize;
    EditText mEditWeight;
    ImageView profilePicture;

    int profile_id = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_menu, new MenuFragment());

        mEditName = (EditText)findViewById(R.id.editTextName);
        mEditAge = (EditText)findViewById(R.id.editTextAge);
        mEditSize = (EditText)findViewById(R.id.editTextSize);
        mEditWeight = (EditText)findViewById(R.id.editTextWeight);
        profilePicture = (ImageView)findViewById(R.id.profilePicture);

        // Querying the database
        ProfileDatabase profile = new ProfileDatabase(this);
        SQLiteDatabase db = profile.getReadableDatabase();
        Cursor cursor = db.query("profile", null, "id="+profile_id, null, null, null, null);
        while (cursor.moveToNext()) {
            // Process the result set here
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("name"));
            @SuppressLint("Range") int age = cursor.getInt(cursor.getColumnIndex("age"));
            @SuppressLint("Range") int size = cursor.getInt(cursor.getColumnIndex("size"));
            @SuppressLint("Range") int weight = cursor.getInt(cursor.getColumnIndex("weight"));
            @SuppressLint("Range") byte[] imageData = cursor.getBlob(cursor.getColumnIndex("image_data"));
            Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            profilePicture.setImageBitmap(imageBitmap);

            Log.e("debug", name);
            mEditName.setText(name);
            mEditAge.setText( String.valueOf(age));
            mEditSize.setText( String.valueOf(size));
            mEditWeight.setText( String.valueOf(weight));
        }
        cursor.close();
        db.close();
    }
    public void signOut(View view) {

        //Clear des sharedPreferences
        SharedPreferences pref = getSharedPreferences("SessionPref", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove("sessionId");
        editor.apply();

        //Redirection vers page auth
        startActivity(new Intent(SettingsActivity.this, SignInActivity.class));
        finish();

    }

    public void openCamera(View view){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void saveData(View view){
        ProfileDatabase profile = new ProfileDatabase(this);
        SQLiteDatabase db = profile.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("name", mEditName.getText().toString());
        values.put("age", mEditAge.getText().toString());
        values.put("size", mEditSize.getText().toString());
        values.put("weight", mEditWeight.getText().toString());
        String selection = "id="+profile_id;
        String[] selectionArgs = {};
        db.update("profile", values, selection, selectionArgs);
        db.close();
        startActivity(new Intent(SettingsActivity.this, MainActivity.class));
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            profilePicture.setImageBitmap(imageBitmap);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] bitmapData = stream.toByteArray();

// Storing the byte array in a SQLite database
            ProfileDatabase profile = new ProfileDatabase(this);
            SQLiteDatabase db = profile.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("image_data", bitmapData);
            String selection = "id=" + profile_id;
            String[] selectionArgs = {};
            db.update("profile", values, selection, selectionArgs);
        }
    }

    public void goToSettingsActivity(View view) {
        Intent switchActivityIntent = new Intent(this, SettingsActivity.class);
        startActivity(switchActivityIntent);
    }
}