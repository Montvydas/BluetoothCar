package com.monte.bluetoothcar;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Set;

/**
 * Created by Monte on 24/02/16.
 */

public class BluetoothActivity extends AppCompatActivity  implements AdapterView.OnItemClickListener {

    public static BluetoothAdapter bluetoothAdapter;
    public static Set<BluetoothDevice> pairedDevices;

    public static ArrayAdapter<String> btArrayAdapter;

    private ListView listDevices;

    //some constants
    final private int ACTIVITY_BLUETOOTH_ENABLE_CODE = 1;
    public static String DEVICE_ADDRESS = "com.monklu.bluetooth.deviceAddress";

    private final int MY_PERMISSIONS_REQUEST_BLUETOOTH = 1;
    private final int MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        getActionBar().setDisplayShowHomeEnabled(false);
//        getActionBar().setTitle("BluetoothCar!");

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH},
                    MY_PERMISSIONS_REQUEST_BLUETOOTH);
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH_ADMIN)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_ADMIN},
                    MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN);
        }


        checkAndEnableBluetooth();

        listDevices = (ListView) findViewById(R.id.listDevices);
        listDevices.setOnItemClickListener(this);

        btArrayAdapter = new ArrayAdapter<String>(BluetoothActivity.this, android.R.layout.simple_list_item_1);
        listDevices.setAdapter(btArrayAdapter);

        turnBluetoothPaired();

    }

    private void checkAndEnableBluetooth() {
        //check if the phone has a bluetooth
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Bluetooth is not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Device could not enable Bluetooth device (might not support)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        //Turn on Bluetooth on app
        if (!bluetoothAdapter.isEnabled()) {
            Intent turnBluetoothOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBluetoothOn, ACTIVITY_BLUETOOTH_ENABLE_CODE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bluetooth, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Enable Bluetooth if it's not enabled
            case R.id.action_enable:
                turnBluetoothOn();
                break;
            //Disable Bluetooth
            case R.id.action_disable:
                turnBluetoothOff();
                break;
            //Enable Bluetooth Visibility for some seconds
            case R.id.action_paired:
                turnBluetoothPaired();
                break;
            //Go into Bluetooth settings to pair with the device
            case R.id.action_configure:
                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        //Log.e("Bluetooth Visible", "" + resultCode);
        switch (requestCode) {
            case ACTIVITY_BLUETOOTH_ENABLE_CODE:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "Bluetooth Enabled!", Toast.LENGTH_SHORT).show();

                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "You will need Bluetooth to use this application!", Toast.LENGTH_SHORT).show();
                    //finish();
                }
                break;
            default:
        }
    }


    public void turnBluetoothOff() {
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
            Toast.makeText(getApplicationContext(), "Turned OFF", Toast.LENGTH_LONG).show();
        } else
            Toast.makeText(getApplicationContext(), "Already OFF", Toast.LENGTH_LONG).show();
    }

    public void turnBluetoothOn() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, ACTIVITY_BLUETOOTH_ENABLE_CODE);
        } else Toast.makeText(this, "Already ON", Toast.LENGTH_SHORT).show();
    }

    public void turnBluetoothPaired() {
        pairedDevices = bluetoothAdapter.getBondedDevices();

        if (btArrayAdapter != null)
            btArrayAdapter.clear();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                btArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        //bluetoothAdapter.disable();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.listDevices) {
            listDevices.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            listDevices.setItemChecked(position, true);         //set 'checked'

            String info = parent.getAdapter().getItem(position).toString();
            String address = info.substring(info.length() - 17);
            Log.e("Item selected", address);

            //if pressed on paired device, connect to that device

            Intent intent = new Intent(BluetoothActivity.this, GyroCarActivity.class);
            intent.putExtra(DEVICE_ADDRESS, address);
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_BLUETOOTH: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(getApplicationContext(), "You need Bluetooth for this App!", Toast.LENGTH_SHORT).show();
                    try {
                        Thread.sleep(1000);
                        finish();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN: {
                if (grantResults.length > 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(getApplicationContext(), "You need Bluetooth for this App!", Toast.LENGTH_SHORT).show();
                    try {
                        Thread.sleep(1000);
                        finish();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}