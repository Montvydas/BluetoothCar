package com.monte.bluetoothcar;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.widget.Toast;

/**
 * Created by monte on 14/06/16.
 */
public class RotationValues implements SensorEventListener{
    private Context context;
    private SensorManager mSensorManager;   //sensorManager object
    private Sensor rSensor; //rotation vector sensor

    private float yaw;
    private float pitch;
    private float roll;

    public RotationValues (Context context){
        this.context = context; //first need to get context
        initialiseSensors ();   //then use it to initialise sensors
    }

    public float getYaw() {
        return yaw;
    }
    public float getPitch() {
        return pitch;
    }
    public float getRoll() {
        return roll;
    }

    private void initialiseSensors (){
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);  //Sensor manager service
        rSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);//used for showing the rotation on each axis
    }

    float [] rawRotationValues;
    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()){
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
                rawRotationValues = event.values.clone();
                calculateRotationOrientation(); //calculate the degrees for all rotations
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void calculateRotationOrientation (){
        float[] rotationMatrix = new float[16];
        float[] orientation = new float[3];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rawRotationValues);   //calculate the rotation matrix for the rotation vector
        SensorManager.getOrientation(rotationMatrix, orientation);  //get orientation from the rotation matrix

        rawYaw     = ((float) mod(orientation[0] + TWO_PI,TWO_PI) );   //get radians in the correct region
        rawPitch = ((float) mod(orientation[1] + TWO_PI,TWO_PI) );
        rawRoll  = ((float) mod(orientation[2] + TWO_PI,TWO_PI) );

        yaw   = rawYaw * 180/PI + yawOffset;
        pitch = rawPitch * 180/PI + pitchOffset;    //in degrees
        roll  = rawRoll * 180/PI + rollOffset;
    }
    private double mod(double a, double b){ //functions calculates the mod
        return a % b;
    }

    private float rawYaw = 0.0f;
    private float rawPitch = 0.0f;
    private float rawRoll = 0.0f;
    private final static float PI = (float) Math.PI;
    private final static float TWO_PI = PI*2;

    private float yawOffset = 0.0f;
    private float pitchOffset = 0.0f;
    private float rollOffset = 0.0f;

    // method CalibrateGyro is called when click the calibrate button
    private void calibrateRotationSensors (){
        float[] rotationMatrix = new float[16];
        float[] orientation = new float[3];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rawRotationValues);   //calculate the rotation matrix for the rotation vector
        SensorManager.getOrientation(rotationMatrix, orientation);  //get orientation from the rotation matrix


        yawOffset = ((float) mod(orientation[0] + TWO_PI,TWO_PI) - rawYaw)*180/PI;
        pitchOffset = ((float) mod(orientation[1] + TWO_PI,TWO_PI) - rawPitch)*180/PI;
        rollOffset = ((float) mod(orientation[2] + TWO_PI,TWO_PI) - rawRoll)*180/PI;
    }

    public void unregisterListener (){
        mSensorManager.unregisterListener(this);
    }
    public void registerListener (int delayValue){
        mSensorManager.registerListener(this, rSensor, delayValue);
    }
}
