package com.example.wear;

import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BODY_SENSORS;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity {

    private BluetoothAdapter bluetoothAdapter;
    private ImageView bluetoothStatusImage;
    private boolean connection = false;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private SensorManager sensorManager;
    private SensorEventListener heartRateSensorListener;
    private TextView bpmTextView;
    private DataClient dataClient;
    private float currentHeartRate = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bpmTextView = findViewById(R.id.bpm);

        if (!checkPermissions()) {
            requestPermissions();
        } else {
            bluetoothStatusImage = findViewById(R.id.connectionStatus);
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            //change if not using emulator
            bluetoothStatusImage.setImageResource(R.drawable.bluetooth_connected);

            dataClient = Wearable.getDataClient(this);
            connection = true;
            startHeartRateMonitor();


            /**
             * Android Studio doesn't support bluetooth adapters:
             * BluetoothAdapter.getDefaultAdapter()  -->  returns 'null'
             *
             * The code below works on physical watches running WearOS to check whether
             * bluetooth is enabled and any device is connected to set an image in
             * layout according to results.
             */
        /*
        bluetoothAdapter.getProfileProxy(this, new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                checkBluetoothStatus();
            }
            @Override
            public void onServiceDisconnected(int profile) {
                checkBluetoothStatus();
            }
        }, BluetoothProfile.STATE_CONNECTED);
         */
        }
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, BODY_SENSORS) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{BODY_SENSORS, BLUETOOTH, BLUETOOTH_ADMIN, BLUETOOTH_CONNECT},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startHeartRateMonitor();
            } else {
                showToastAndFinish("Permission denied");
            }
        }
    }

    private void showToastAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void checkBluetoothStatus() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            bluetoothStatusImage.setImageResource(R.drawable.bluetooth_off);
            connection = false;
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions();
                return;
            }
            int connectionState = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP);
            if (connectionState == BluetoothProfile.STATE_CONNECTED) {
                bluetoothStatusImage.setImageResource(R.drawable.bluetooth_connected);
                connection = true;
            } else {
                bluetoothStatusImage.setImageResource(R.drawable.bluetooth_disconnected);
                connection = false;
            }
        }
    }

    private void startHeartRateMonitor() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        if (heartRateSensor == null) {
            showToastAndFinish("Heart rate sensor not available");
            return;
        }

        heartRateSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
                    currentHeartRate = event.values[0];
                    bpmTextView.setText(String.valueOf((int) currentHeartRate));
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        sensorManager.registerListener(heartRateSensorListener, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        if(connection)
            handler.post(sendHeartRateData);
    }

    @SuppressLint("VisibleForTests")
    private void sendDataToPhone(int heartRate) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/heart_rate");
        putDataMapRequest.getDataMap().putLong("current_time_millis", System.currentTimeMillis());
        putDataMapRequest.getDataMap().putInt("heart_rate", heartRate);

        dataClient.putDataItem(putDataMapRequest.asPutDataRequest())
                .addOnSuccessListener(
                        unused -> Log.d("Wear", "Sent heart rate: " + heartRate))
                .addOnFailureListener(
                        e -> Log.e("Wear", "Failed to send heart rate: " + heartRate, e));
    }

    private final Runnable sendHeartRateData = new Runnable() {
        @Override
        public void run() {
            sendDataToPhone((int) currentHeartRate);
            handler.postDelayed(this, 1000); // Send data every second
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(heartRateSensorListener);
        }
        handler.removeCallbacks(sendHeartRateData);
    }
}
