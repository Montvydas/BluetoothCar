package com.monte.bluetoothcar;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.UUID;

import bluetoothStuff.*;


/**
 * Created by Monte on 24/02/16.
 */
public class CarActivity extends Activity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener, View.OnTouchListener{

    public BluetoothDevice device;                          //device to be connected to
    public ConnectThread connectThread;                     //connection thread
    private ManageConnectedThread mmManagegedConnection;    //send & receive thread

    private int WIDTH;                                      //screen width for the aplication
    private int HEIGHT;                                     //same for height

    private TextView yawText;
    private TextView pitchText;
    private TextView rollText;

    private RotationValues myRotations;     //*** needed

    private SeekBar speedSeekBar;
    private SeekBar angleSeekBar;

    private Button startButton;
    private Button driftButton;

    private RadioGroup speedGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gyro_car_landscape);              //set layout
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //always keep orientation in portrait
        getScreenSize();                                    //gets the WIDTH and HEIGHT
        initialiseBluetooth();
        initialiseViews();      //Bluetooth stuff
        myRotations = new RotationValues(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void initialiseViews(){
        yawText = (TextView) findViewById(R.id.yawText);
        pitchText = (TextView) findViewById(R.id.pitchText);
        rollText = (TextView) findViewById(R.id.rollText);

        speedSeekBar = (SeekBar) findViewById(R.id.speedSeekBar);
        angleSeekBar= (SeekBar) findViewById(R.id.angleSeekBar);

        driftButton = (Button) findViewById(R.id.driftButton);
        driftButton.setOnTouchListener(this);

        startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(this);

        speedGroup = (RadioGroup) findViewById(R.id.speedGroup);
        speedGroup.setOnCheckedChangeListener(this);
    }
//Initialise bluetooth stuff
    private void initialiseBluetooth() {
        Intent intent = getIntent();
        String deviceAddress = intent.getStringExtra(BluetoothActivity.DEVICE_ADDRESS); //gets passed device MAC address

        device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
        Log.e("Device Address is", deviceAddress);
    }

    //Need to start connection thread again when awake
    @Override
    protected void onResume() {
        super.onResume();

        myRotations.registerListener();
        initialiseSender();

        UUID deviceUUID = device.getUuids()[0].getUuid();
        Log.e("UUID", deviceUUID + "");
        connectThread = new ConnectThread(device, deviceUUID);
        connectThread.start();
    }

    //stop connection thread to save battery when application closed
    @Override
    protected void onPause() {
        super.onPause();
        myRotations.unregisterListener();

        enableCar = false;
        startButton.setText("START");

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
    public void sendResponse(int sendWord) {
        if (connectThread.getSocket() != null && connectThread.getSocket().isConnected()) { //check if device is still connected
            if (mmManagegedConnection == null) {
                mmManagegedConnection = new ManageConnectedThread(connectThread.getSocket());   //get bluetooth socket
            }
            mmManagegedConnection.write(sendWord);                                          //send the work
        }
        //mmManagegedConnection.write(string.getBytes(Charset.forName("UTF-8")));
    }

    //gets screen size
    private void getScreenSize (){
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        WIDTH = metrics.widthPixels;
        HEIGHT = metrics.heightPixels;

        Log.e("WIDTH=", WIDTH + "");
        Log.e("HEIGHT=", HEIGHT + "");
    }

    /*
    //Magic is done in here!
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()){                  //listen for specific touch events
            case MotionEvent.ACTION_DOWN:           //if pressed down or moved, then send adjusted coordinates
                //return true;
            case MotionEvent.ACTION_MOVE:           //in the form of: x0.35 y0.54

                if (event.getY()/HEIGHT < 0.3){
                    sendResponse("red\n");
                    Log.e("colour=", "red");
                } else if (event.getY()/HEIGHT < 0.7 && event.getY()/HEIGHT > 0.3){
                    sendResponse("green\n");
                    Log.e("colour=", "green");
                } else {
                    sendResponse("blue\n");
                    Log.e("colour=", "blue");
                }


//                String message = String.format("x%4.2fy%4.2f\n", event.getX()/WIDTH, event.getY()/HEIGHT);
//                Log.e(":)", message);               //output this on Logcat
//                sendResponse(message);              //send through bluetooth
                break;
            case MotionEvent.ACTION_UP:
//                message = "x0.00y0.00\n";
//                message = "x0.00";
//                Log.e(":)", message);               //output this on Logcat
//                sendResponse(message);              //send through bluetooth
               // return false;
//                message = "y0.00";
//                sendResponse(message);
                break;
        }
        return super.onTouchEvent(event);
    }
*/
    private boolean prevDirection = true;
    private boolean direction = false;
    private int speed = 0;  //0 - 255
    private int angle = 112;  //90 - 135
    private int countBrakeLoops = 0;

    private void initialiseSender (){
        final Handler handler = new Handler();
        final int delay = 40; //milliseconds

        handler.postDelayed(new Runnable() {
            public void run() {
                //do something

                yawText.setText(String.format("%.2f ˚", myRotations.getYaw()));
                pitchText.setText(String.format("%.2f ˚", myRotations.getPitch()));
                rollText.setText(String.format("%.2f ˚", myRotations.getRoll()));

                // Set the speed and the direction (forward/backward)
                float positionY = myRotations.getRoll();    //280 - middle; 235 - 280 - 325
                float positionX = myRotations.getPitch();   //0 - middle;   45 - 0 - 315

                if (positionY >= 220 && positionY <= 340) {
                    if (positionY >= 280.0)
                        prevDirection = true;
                    else
                        prevDirection = false;
                }

                if (driftCar){
                    direction = !prevDirection;
                    countBrakeLoops++;
                    if (countBrakeLoops == 10)
                        driftCar = false;
                } else {
                    countBrakeLoops = 0;
                    direction = prevDirection;
                }


                if (positionY >= 220 && positionY <= 340) {
                    speed = (int) (Math.abs(positionY - 280) / 60.0 * maxSpeed);
                } else {
                    speed = maxSpeed;
                }

                // Set the angle
                if (positionX <= 90) {
                    if (positionX <= 45)
                        angle = (int) (112.5 + positionX / 2);      //112.5˚ - 135˚
                    else
                        angle = 135;
                }

                if (positionX >= 270) {
                    if (positionX >= 315)
                        angle = (int) (90 + (positionX - 315) / 2);  //90˚ - 112.5˚
                    else
                        angle = 90;
                }

                speedSeekBar.setProgress(speed);
                angleSeekBar.setProgress(135 - angle);

                if (!enableCar) {
                    speed = 0;
                    angle = 112;
                    direction = true;
                }

                // Set direction: 1 forward, 0 backward
                int val = 0;
                if (direction) {
                    val |= 0x80;
                }

                // Assign 3 bits to set angle
                val |= (((135 - angle) / 6) << 4);

                // Assign 4 bits to set speed
                if (speed >= 60) {
//                    speed -= 60;
                    val |= (speed - 60) / 12;
                } else {
                    val = val & 0xf0;
                }

                if (connectThread.getSocket() == null) {
                    finish();
                }

                sendResponse(val);

//                Log.e("values", "pitch= " + myRotations.getPitch() + " roll= " + myRotations.getRoll());
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    private boolean enableCar = false;

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.startButton:
                if (enableCar){ //make the car stop
                    enableCar = false;
                    startButton.setText("START");
                } else {
                    startButton.setText("STOP");
                    enableCar = true;
                }
                break;
        }
    }

    private int maxSpeed = 150;
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId){
            case R.id.speedSlow:
                maxSpeed = 100;
                break;
            case R.id.speedMedium:
                maxSpeed = 170;
                break;
            case R.id.speedHigh:
                maxSpeed = 250;
                break;
        }
        Log.i("New Max Speed:", maxSpeed + "");
    }

    private boolean driftCar = false;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()){
            case R.id.driftButton:
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    driftCar = true;
                } else if (event.getAction() == MotionEvent.ACTION_UP){
                    driftCar = false;
                }
                break;
        }
        return false;
    }
}