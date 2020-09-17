package nl.rubikscraft.bluebotcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.util.ArraySet;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    boolean connected = false;
    TextView myLabel;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    BluetoothSocket mmSocket;
    OutputStream mmOutputStream;

    public int speeds[] = {70, 80, 90, 100, 110, 120, 130, 140, 150};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        setContentView(R.layout.activity_main);


        final FrameLayout leftSeekbarLayout = (FrameLayout) findViewById(R.id.seekBarLeftLayout);

        leftSeekbarLayout.post(new Runnable() {
            @Override
            public void run() {
                int width = leftSeekbarLayout.getHeight();
                int height = leftSeekbarLayout.getMeasuredWidth();
                Log.d("hi", width + "  " + height);
                SeekBar leftSeekbar = (SeekBar) findViewById(R.id.seekBarLeft);
                ViewGroup.LayoutParams params = leftSeekbar.getLayoutParams();
                //params.height = height;
                params.width = width;
                leftSeekbar.setRotation(270);
                leftSeekbar.setLayoutParams(params);
            }
        });

        final FrameLayout rightSeekbarLayout = (FrameLayout) findViewById(R.id.seekBarLeftLayout);

        rightSeekbarLayout.post(new Runnable() {
            @Override
            public void run() {
                int width = rightSeekbarLayout.getMeasuredHeight();
                int height = rightSeekbarLayout.getMeasuredWidth();
                Log.d("hi", width + "  " + height);
                SeekBar rightSeekbar = (SeekBar) findViewById(R.id.seekBarRight);
                ViewGroup.LayoutParams params = rightSeekbar.getLayoutParams();
                //params.height = height;
                params.width = width;
                rightSeekbar.setRotation(270);
                rightSeekbar.setLayoutParams(params);
            }
        });

        final SeekBar rightSeekbar = (SeekBar) findViewById(R.id.seekBarRight);
        final SeekBar leftSeekbar = (SeekBar) findViewById(R.id.seekBarLeft);

        rightSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    int good = progress - 9;
                    if (good < 0) {
                        int index = good * -1;

                        if (index < 0)
                            index = 0;
                        if (index >= speeds.length)
                            index = speeds.length - 1;


                        sendData("R-" + speeds[index]);
                    } else if (good > 0) {
                        int index = good;

                        if (index < 0)
                            index = 0;
                        if (index >= speeds.length)
                            index = speeds.length - 1;


                        sendData("R+" + speeds[index]);
                    } else {
                        sendData("R+0");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                rightSeekbar.setProgress(9);
                try {
                    sendData("R+0");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        leftSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    int good = progress - 9;
                    if (good < 0) {
                        int index = good * -1;

                        if (index < 0)
                            index = 0;
                        if (index >= speeds.length)
                            index = speeds.length - 1;


                        sendData("L-" + speeds[index]);
                    } else if (good > 0) {
                        int index = good;

                        if (index < 0)
                            index = 0;
                        if (index >= speeds.length)
                            index = speeds.length - 1;


                        sendData("L+" + speeds[index]);
                    } else {
                        sendData("L+0");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                leftSeekbar.setProgress(9);
                try {
                    sendData("L+0");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        myLabel = (TextView) findViewById(R.id.label);
        Button openButton = (Button) findViewById(R.id.connectButton);
        Button biemButton = (Button) findViewById(R.id.biemButton);
        Button closeButton = (Button) findViewById(R.id.disconnectButton);

        //Open Button
        openButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                findBT();
            }
        });


        //Close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    closeBT();
                } catch (IOException ex) {
                } catch (InterruptedException ex) {

                }
            }
        });

        //Send Button
        biemButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    sendData("A0");
                } catch (IOException ex) {
                }
            }
        });
    }

    void findBT() {
        if (connected) return;

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            myLabel.setText("Bluetooth is turned off");
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        String[][] devices = new String[pairedDevices.size()][2];
        String[] names = new String[pairedDevices.size()];
        if (pairedDevices.size() > 0) {
            int i = 0;
            for (BluetoothDevice device : pairedDevices) {
                names[i] = device.getName() + " (" + device.getAddress() + ")";
                devices[i][0] = device.getName();
                devices[i][1] = device.getAddress();
                i++;
            }
        } else {
            myLabel.setText("No paired devices");
            return;
        }

        AlertDialog.Builder listalert = new AlertDialog.Builder(this);
        listalert.setTitle("Connect to").setItems(names, new MyOnClickListener(devices, mBluetoothAdapter));

        listalert.show();


    }

    void openBT(BluetoothDevice mmDevice) throws IOException {
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
        if (mmSocket.isConnected()) {
            mmSocket.close();
        }
        while (mmSocket.isConnected()) {
            Log.d("t", "Loopin");
        }
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();

        myLabel.setText("Connected");
        connected = true;
        sendData("R0");
        sendData("L0");
    }

    void sendData(String msg) throws IOException {
        if (connected) {
            msg += "\n";
            mmOutputStream.write(msg.getBytes());
        }
    }

    void closeBT() throws IOException, InterruptedException {
        if (connected) {
            mmOutputStream.flush();
            Thread.sleep(500);
            mmOutputStream.close();
            Thread.sleep(200);
            mmSocket.close();
            while (mmSocket.isConnected()) {
                Log.d("t", "Loopin");
            }
            mmSocket = null;
            mmOutputStream = null;
            myLabel.setText("Disconnected");
            connected = false;
        }
    }

    class MyOnClickListener implements DialogInterface.OnClickListener {

        String[][] devices;
        BluetoothAdapter bluetoothAdapter;

        public MyOnClickListener(String[][] devices, BluetoothAdapter bluetoothAdapter) {
            this.devices = devices;
            this.bluetoothAdapter = bluetoothAdapter;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            String address = devices[which][1];
            try {
                Log.d("t", "Connecting to " + address + " because " + which);
                openBT(bluetoothAdapter.getRemoteDevice(address));

            } catch (IOException e) {
                e.printStackTrace();
                myLabel.setText("Couldn't connect");
            }

        }
    }
}
