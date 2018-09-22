package com.google.ar.sceneform.samples.solarsystem.Helper;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

import static android.content.Context.SENSOR_SERVICE;

public class CompassHelper {

    private static CompassHelper mInstance;

    private SensorManager mSensorManager;

    private float mCurrentDegree = -1;

    private float mCurrentRadians = -1;

    private Sensor mAccelerometer, mMagnetometer;

    private float[] mLastAccelerometer = new float[3], mLastMagnetometer = new float[3];

    private float[] mR = new float[9];

    private float[] mOrientation = new float[3];

    private boolean mLastAccelerometerSet = false, mLastMagnetometerSet = false;

    private Activity mActivity;

    public static CompassHelper getInstance(Activity activity, Runnable r) {
        if(mInstance == null)
            mInstance = new CompassHelper(activity, r);
        return mInstance;
    }

    private CompassHelper(Activity activity, Runnable r){
        mActivity = activity;
        init(r);
    }

    public SensorEventListener getSensorEventListener(Runnable r) {
        return new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                if(mCurrentDegree != -1)
                    return;

                if (event.sensor == mAccelerometer) {
                    System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
                    mLastAccelerometerSet = true;
                } else if (event.sensor == mMagnetometer) {
                    System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
                    mLastMagnetometerSet = true;
                }

                if (mLastAccelerometerSet && mLastMagnetometerSet) {
                    SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
                    SensorManager.getOrientation(mR, mOrientation);
                    float azimuthInRadians = mOrientation[0];
                    float azimuthInDegress = (float)(Math.toDegrees(azimuthInRadians)+360)%360;
                    mCurrentRadians = azimuthInRadians;
                    mCurrentDegree = -azimuthInDegress;
                    Toast.makeText(mActivity, String.format("%s", mCurrentDegree), Toast.LENGTH_SHORT).show();
                    mSensorManager.unregisterListener(this);
                    Log.d("CompassHelper", String.format("%s", mCurrentDegree));
                    r.run();
                }

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
    }

    private void init(Runnable r) {
        mSensorManager = (SensorManager) mActivity.getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(getSensorEventListener(r), mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(getSensorEventListener(r), mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public float getCurrentDegree() {
        return mCurrentDegree;
    }

    public float getCurrentRadians() {
        return mCurrentRadians;
    }
}
