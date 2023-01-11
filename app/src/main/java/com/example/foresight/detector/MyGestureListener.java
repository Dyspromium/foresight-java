package com.example.foresight.detector;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velX, float velY) {
        Log.e("debug", "SWIPE");
        return true;
    }
}