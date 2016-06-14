package com.monte.bluetoothcar;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.UUID;

import bluetoothStuff.*;

/**
 * Created by Monte on 14/06/16.
 */
public class GyroCarActivity extends Activity {//implements SensorEventListener{

    public BluetoothDevice device;                          //device to be connected to
    public ConnectThread connectThread;                     //connection thread
    private ManageConnectedThread mmManagegedConnection;    //send & receive thread     //same for height

    private TextView yawText;
    private TextView pitchText;
    private TextView rollText;

    private RotationValues myRotations;     //*** needed

    private SeekBar speedSeekBar;
    private SeekBar angleSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gyro_car);              //set layout
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //always keep orientation in portrait
        initialiseBluetooth();                              //Bluetooth stuff
        initialiseViews();
        myRotations = new RotationValues(getApplicationContext());      //*** needed
    }

    private int direction = 0;
    private int speed = 0;  //0 - 255
    private int angle = 0;  //90 - 135

    private void initialiseSender (){
        final Handler h = new Handler();
        final int delay = 20; //milliseconds

        h.postDelayed(new Runnable(){
            public void run(){
                //do something

                yawText.setText(String.format("%.2f ˚", myRotations.getYaw()));
                pitchText.setText(String.format("%.2f ˚", myRotations.getPitch()));
                rollText.setText(String.format("%.2f ˚", myRotations.getRoll()));

                // Set the speed and the direction (forward/backward)
                float positionY = myRotations.getRoll();    //280 - middle; 235 - 280 - 325
                float positionX = myRotations.getPitch();   //0 - middle;   45 - 0 - 315

                if (positionY >= 235 && positionY <= 325){
                    if (positionY >= 280.0)
                        direction = 1;
                    else
                        direction = 0;
                    speed = (int) (Math.abs(positionY - 280) / 45.0 * 255.0);
                } else {
                    speed = 255;
                }

                // Set the angle
                if (positionX <= 90){
                    if (positionX <= 45)    //90˚ - 112.5˚
                        angle = (int) (112.5 - positionX/2);
                    else
                        angle = 90;
                }

                if (positionX >= 270){
                    if (positionX >= 315)
                        angle = (int) (135 - (positionX - 315)/2);  //112.5˚ - 135˚
                    else
                        angle = 135;
                }


                speedSeekBar.setProgress(speed);
                angleSeekBar.setProgress(angle - 90);

                // Set direction: 1 forward, 0 backward
                int val = 0;
                if (direction==1) {
                    val |= 0x80;
                }

                // Assign 3 bits to set angle
                val |= (((135-angle)/6) << 4) ;

                // Assign 4 bits to set speed
                if (speed >= 60) {
//                    speed -= 60;
                    val |= (speed-60) / 12;
                }
                else {
                    val = val & 0xf0;
                }
                
                if (enableCar) {
                    if (connectThread.getSocket() == null)
                        finish();
                    sendResponse(val);
                }

//                Log.e("values", "pitch= " + myRotations.getPitch() + " roll= " + myRotations.getRoll());
                h.postDelayed(this, delay);
            }
        }, delay);
    }

    private void initialiseViews(){
        yawText = (TextView) findViewById(R.id.yawText);
        pitchText = (TextView) findViewById(R.id.pitchText);
        rollText = (TextView) findViewById(R.id.rollText);

        speedSeekBar = (SeekBar) findViewById(R.id.speedSeekBar);
        angleSeekBar= (SeekBar) findViewById(R.id.angleSeekBar);
    }

    //Initialise bluetooth stuff
    private void initialiseBluetooth() {
        Intent intent = getIntent();
        String deviceAddress = intent.getStringExtra(BluetoothActivity.DEVICE_ADDRESS); //gets passed device MAC address

        device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
        Log.e("Device Address is", deviceAddress);
    }

    //Need to start connection thread again when awake
    int i = 0;
    @Override
    protected void onResume() {
        super.onResume();
        UUID deviceUUID = device.getUuids()[0].getUuid();
        Log.e("UUID", deviceUUID + "");
        connectThread = new ConnectThread(device, deviceUUID);
        connectThread.start();

        myRotations.registerListener();     //*** this needed
//        initialiseSender();                 //*** this is used to repeatedly send data
    }

    @Override
    protected void onPause() {
        super.onPause();
        myRotations.unregisterListener();
    }

    //stop connection thread to save battery when application closed
    @Override
    protected void onStop() {
        super.onStop();
        if (mmManagegedConnection != null)
            mmManagegedConnection.cancel();
        if (connectThread.getSocket() != null)
            connectThread.cancel();
        try {
            connectThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    //sends response through bluetooth, firstly gets a socket
    public void sendResponse(int sendInt) {
//        Log.e("Send word ", "Int: " + sendInt + ", Hex: 0x" + Integer.toHexString(sendInt));
        if (connectThread.getSocket() != null && connectThread.getSocket().isConnected()) { //check if device is still connected
            if (mmManagegedConnection == null) {
                mmManagegedConnection = new ManageConnectedThread(connectThread.getSocket());   //get bluetooth socket
            }
            mmManagegedConnection.write(sendInt);
            Log.e("Send word ", "Int: " + sendInt + ", Hex: 0x" + Integer.toHexString(sendInt));//send the work
        }
        //mmManagegedConnection.write(string.getBytes(Charset.forName("UTF-8")));
    }

    private boolean enableCar = false;
    public void startCar (View view){
        sendResponse(23);

        if (enableCar){
            ((Button) view).setText("START");
            enableCar = false;
        } else {
            ((Button) view).setText("STOP");
            enableCar = true;
        }
            
//        Log.e("values", "pitch= " + myRotations.getPitch() + " roll= " + myRotations.getRoll());
    }
}
