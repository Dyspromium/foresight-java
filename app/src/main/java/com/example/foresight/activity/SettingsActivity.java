package com.example.foresight.activity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.foresight.R;
import com.example.foresight.database.ProfileDatabase;
import com.example.foresight.fragment.MenuFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;


public class SettingsActivity extends AppCompatActivity {

    int REQUEST_IMAGE_CAPTURE = 1;

    EditText mEditName;
    EditText mEditAge;
    EditText mEditSize;
    EditText mEditWeight;
    ImageView profilePicture;

    int profile_id = 1;

    View tmpView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Récuperation de la configuration actuel du téléphone pour determiner son format (paysage/portrait)
        Configuration configuration = getResources().getConfiguration();
        int orientation = configuration.orientation;

        //Si portrait on met le layout portrait sinon landscape
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_settings_landscape);
        } else{
            setContentView(R.layout.activity_settings_portrait);
        }


        //Gestion de la bottom navigation bar
        SettingsActivity thisType = this;

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.settings);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @SuppressLint("NonConstantResourceId")
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.home:
                                Intent switchActivityIntentz = new Intent(thisType, MainActivity.class);
                                startActivity(switchActivityIntentz);
                                overridePendingTransition(R.anim.hold, R.anim.fade_out);
                                break;
                            case R.id.search:
                                Intent switchActivityIntent = new Intent(thisType, FindGymActivity.class);
                                startActivity(switchActivityIntent);
                                overridePendingTransition(R.anim.hold, R.anim.fade_out);
                                break;
                            case R.id.settings:
                                Intent switchActivityIntents = new Intent(thisType, SettingsActivity.class);
                                startActivity(switchActivityIntents);
                                overridePendingTransition(R.anim.hold, R.anim.fade_out);
                                break;
                        }
                        return true;
                    }
                });

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
            if(imageBitmap != null){
                profilePicture.setImageBitmap(imageBitmap);
            }

            mEditName.setText(name);
            mEditAge.setText( String.valueOf(age));
            mEditSize.setText( String.valueOf(size));
            mEditWeight.setText( String.valueOf(weight));
        }
        cursor.close();
        db.close();
    }

    //Trigger si le format du téléphone change (portrait/paysage)
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        //Attribue un layout en fonction de l'orientation
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_settings_landscape);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            setContentView(R.layout.activity_settings_portrait);
        }
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
        tmpView = view;
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
        Snackbar.make(view, "Profile has been updated", Snackbar.LENGTH_SHORT).show();
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
            Snackbar.make(tmpView, "Profile Picture has been updated", Snackbar.LENGTH_SHORT).show();

        }
    }

    //Animation sur le button back
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.hold, R.anim.hold);
    }
}