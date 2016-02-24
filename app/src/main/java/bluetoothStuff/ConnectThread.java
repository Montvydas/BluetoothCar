package bluetoothStuff;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Created by Monte on 6/3/2015.
 */
public class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;

    public BluetoothSocket getSocket() {
        return mmSocket;
    }

    private final BluetoothDevice mmDevice;


    public ConnectThread(BluetoothDevice device, UUID MY_UUID) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        Log.e("Thread created", "one!");

        BluetoothSocket tmp = null;
        mmDevice = device;

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) { }
        mmSocket = tmp;
    }

    public void run() {
        // Cancel discovery because it will slow down the connection
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                mmSocket.close();
            } catch (IOException closeException) { }
            return;
        }
        // Do work to manage the connection (in a separate thread)
        //manageConnectedSocket(mmSocket);
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }

    public void manageConnectedSocket (BluetoothSocket mmSocket){
        String string = "hello";

    }

}