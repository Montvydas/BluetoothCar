package com.monte.bluetoothcar;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import java.util.UUID;

import bluetoothStuff.*;


/**
 * Created by Monte on 24/02/16.
 */
public class CarActivity extends Activity {

    public BluetoothDevice device;                          //device to be connected to
    public ConnectThread connectThread;                     //connection thread
    private ManageConnectedThread mmManagegedConnection;    //send & receive thread

    private int WIDTH;                                      //screen width for the aplication
    private int HEIGHT;                                     //same for height

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car);              //set layout
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //always keep orientation in portrait
        getScreenSize();                                    //gets the WIDTH and HEIGHT
        initialiseBluetooth();                              //Bluetooth stuff
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

        UUID deviceUUID = device.getUuids()[0].getUuid();
        Log.e("UUID", deviceUUID + "");
        connectThread = new ConnectThread(device, deviceUUID);
        connectThread.start();
    }

    //stop connection thread to save battery when application closed
    @Override
    protected void onStop() {
        super.onStop();
        mmManagegedConnection.cancel();
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

    //gets screen size
    private void getScreenSize (){
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        WIDTH = metrics.widthPixels;
        HEIGHT = metrics.heightPixels;

        Log.e("WIDTH=", WIDTH + "");
        Log.e("HEIGHT=", HEIGHT + "");
    }

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

                String message = String.format("x%4.2fy%4.2f\n", event.getX()/WIDTH, event.getY()/HEIGHT);
                Log.e(":)", message);               //output this on Logcat
//                sendResponse(message);              //send through bluetooth
                break;
            case MotionEvent.ACTION_UP:
                message = "x0.00y0.00\n";
//                message = "x0.00";
                Log.e(":)", message);               //output this on Logcat
//                sendResponse(message);              //send through bluetooth
               // return false;
//                message = "y0.00";
//                sendResponse(message);
                break;
        }
        return super.onTouchEvent(event);
    }
}
