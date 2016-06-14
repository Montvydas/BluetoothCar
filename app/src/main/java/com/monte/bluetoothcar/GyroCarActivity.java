package com.monte.bluetoothcar;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

import bluetoothStuff.*;

/**
 * Created by Monte on 14/06/16.
 */
public class GyroCarActivity extends Activity implements SensorEventListener{

    public BluetoothDevice device;                          //device to be connected to
    public ConnectThread connectThread;                     //connection thread
    private ManageConnectedThread mmManagegedConnection;    //send & receive thread     //same for height

    private TextView yawText;
    private TextView pitchText;
    private TextView rollText;

    private SensorManager mSensorManager;   //sensorManager object
    private Sensor rSensor; //rotation vector sensor

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gyro_car);              //set layout
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //always keep orientation in portrait
        initialiseBluetooth();                              //Bluetooth stuff
        initialiseViews();
        initialiseSensors();
    }


    private void initialiseViews(){
        yawText = (TextView) findViewById(R.id.yawText);
        pitchText = (TextView) findViewById(R.id.pitchText);
        rollText = (TextView) findViewById(R.id.rollText);
    }

    //Initialise bluetooth stuff
    private void initialiseBluetooth() {
        Intent intent = getIntent();
        String deviceAddress = intent.getStringExtra(BluetoothActivity.DEVICE_ADDRESS); //gets passed device MAC address

        device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
        Log.e("Device Address is", deviceAddress);
    }

    private void initialiseSensors (){
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);  //Sensor manager service
        rSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);//used for showing the rotation on each axis
    }

    //Need to start connection thread again when awake
    @Override
    protected void onResume() {
        super.onResume();

        mSensorManager.registerListener(this, rSensor, SensorManager.SENSOR_DELAY_GAME);

        UUID deviceUUID = device.getUuids()[0].getUuid();
        Log.e("UUID", deviceUUID + "");
        connectThread = new ConnectThread(device, deviceUUID);
        connectThread.start();
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(this);
        super.onPause();
    }

    //stop connection thread to save battery when application closed
    @Override
    protected void onStop() {
        super.onStop();
        if (mmManagegedConnection != null)
            mmManagegedConnection.cancel();
        if (connectThread != null)
            connectThread.cancel();
        try {
            connectThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //sends response through bluetooth, firstly gets a socket
    public void sendResponse(String sendWord) {
        if (connectThread.getSocket() != null && connectThread.getSocket().isConnected()) { //check if device is still connected
            if (mmManagegedConnection == null) {
                mmManagegedConnection = new ManageConnectedThread(connectThread.getSocket());   //get bluetooth socket
            }
            mmManagegedConnection.write(sendWord);                                          //send the work
        }
        //mmManagegedConnection.write(string.getBytes(Charset.forName("UTF-8")));
    }

    private void calculateRotationOrientation (){
        float[] rotationMatrix = new float[16];
        float[] orientation = new float[3];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rawRotationValues);   //calculate the rotation matrix for the rotation vector
        SensorManager.getOrientation(rotationMatrix, orientation);  //get orientation from the rotation matrix

        rawYaw     = ((float) mod(orientation[0] + TWO_PI,TWO_PI) );   //get radians in the correct region
        rawPitch = ((float) mod(orientation[1] + TWO_PI,TWO_PI) );
        rawRoll  = ((float) mod(orientation[2] + TWO_PI,TWO_PI) );

        float yaw   = rawYaw * 180/PI + yawOffset;
        float pitch = rawPitch * 180/PI + pitchOffset;    //in degrees
        float roll  = rawRoll * 180/PI + rollOffset;


        yawText.setText(String.format("%.2f ˚", yaw));
        pitchText.setText(String.format("%.2f ˚", pitch));
        rollText.setText(String.format("%.2f ˚", roll));
//        Log.e("degrees:", "yaw= " + yaw + " pitch= " + pitch + " roll= " + roll);
    }
    private double mod(double a, double b){ //functions calculates the mod
        return a % b;
    }

    float rawYaw = 0.0f;
    float rawPitch = 0.0f;
    float rawRoll = 0.0f;
    private final static float PI = (float) Math.PI;
    private final static float TWO_PI = PI*2;

    float yawOffset = 0.0f;
    float pitchOffset = 0.0f;
    float rollOffset = 0.0f;

    // method CalibrateGyro is called when click the calibrate button
    public void calibrateRotationSensors (View view){
        float[] rotationMatrix = new float[16];
        float[] orientation = new float[3];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rawRotationValues);   //calculate the rotation matrix for the rotation vector
        SensorManager.getOrientation(rotationMatrix, orientation);  //get orientation from the rotation matrix


        yawOffset = ((float) mod(orientation[0] + TWO_PI,TWO_PI) - rawYaw)*180/PI;
        pitchOffset = ((float) mod(orientation[1] + TWO_PI,TWO_PI) - rawPitch)*180/PI;
        rollOffset = ((float) mod(orientation[2] + TWO_PI,TWO_PI) - rawRoll)*180/PI;
//        Log.e("offset=", offset + "");                                                          // initialisation
//        Log.e ("mag degree=", Math.toDegrees(Values[0])+"");
        Toast.makeText(this, "Hold the phone in car non-moving position...", Toast.LENGTH_SHORT).show();
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
}
