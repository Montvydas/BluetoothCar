package bluetoothStuff;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Created by Monte on 17/02/16.
 */
public class ManageConnectedThread implements Runnable {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    public int getBytes() {
        return bytes;
    }

    private int bytes;

    public ManageConnectedThread(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                // Send the obtained bytes to the UI activity
//                mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
//                        .sendToTarget();
            } catch (IOException e) {
                break;
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(String input) {
        try {
            byte[] bytes = input.getBytes(); // converts entered String into
            mmOutStream.write(bytes);
        } catch (IOException e) { }
    }

    public void write(int input) {
        try {
            // byte myByte = 0x00;
//            for (int i = 0; i < 8; i++){
//                myByte = (byte) (input << i);
//            }
            // short tmp = (short) (input & 0xFF);
            byte bytes = (byte) (input&0xFF);
            mmOutStream.write(bytes);
            Log.e("Send word int", "Int: " + input + ", Hex: 0x" + Integer.toHexString(input));
//            Log.e("Send word bytes", "Int: " + bytes + ", Hex: 0x" + Integer.toHexString(bytes));
        } catch (IOException e) { }
    }


    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}