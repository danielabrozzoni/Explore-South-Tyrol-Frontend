package com.google.ar.sceneform.samples.solarsystem.Helper;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.ar.sceneform.math.Vector3;

public class GestureHelper {

    private float firstX, firstY, secondX, secondY;

    public void setTouchListener(View v, GestureListener gestureListener) {
        v.setOnTouchListener((view, motionEvent) -> {

            view.performClick();
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                Log.d("PinActivityForce", "DOWN");
                GestureHelper.this.firstX = motionEvent.getX();
                GestureHelper.this.firstY = motionEvent.getY();
                return true;
            }

            if(motionEvent.getAction() == MotionEvent.ACTION_UP) {

                Log.d("PinActivityForce", "UP");
                GestureHelper.this.secondX = motionEvent.getX();
                GestureHelper.this.secondY = motionEvent.getY();
                float dx = secondX - firstX;
                float dy = secondY - firstY;
                Vector3 force = new Vector3(dx, dy, (float) Math.sqrt(dx*dx + dy*dy));
                gestureListener.onForceSet(force);
                return true;
            }

            return false;

        });
    }

    public interface GestureListener {

        void onForceSet(Vector3 force);
    }
}