package com.example.trainingcompanion.ui.fragment.workouts;

import static android.Manifest.permission.BODY_SENSORS;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.trainingcompanion.MainActivity;
import com.example.trainingcompanion.R;
import com.example.trainingcompanion.extra.ToastUtilsKt;
import com.example.trainingcompanion.extra.heartrate.HeartRateListenerService;
import com.example.trainingcompanion.ui.viewmodel.workouts.MonitorViewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.S)
public class MonitorFragment extends Fragment {
    private TextView heartRateTextView;
    private ImageView bluetoothStatus;

    private static final String TAG = "HeartBeatFragment";
    private boolean useBuiltInSensors = false;

    private LineChart chart;
    private SensorManager sensorManager;
    private Sensor heartRateSensor;
    private SensorEventListener heartRateListener;
    private MonitorViewModel viewModel;
    private HeartRateListenerService service;


    private final HeartRateListenerService.HeartRateListener heartRateServiceListener = new HeartRateListenerService.HeartRateListener() {
        @Override
        public void onHeartRateChanged(float heartRate) {
            if (!useBuiltInSensors) {
                viewModel.updateHeartRate((int) heartRate);
                updateChart(heartRate);
                viewModel.addHeartRateData((int) heartRate);
                checkBluetoothStatus();
                viewModel.setServiceReceiving(true);
            }
        }

        @Override
        public void onReceivingStatusChanged(boolean isReceiving) {
            viewModel.setServiceReceiving(isReceiving);
            checkBluetoothStatus();
        }
    };

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                SharedPreferences preferences = requireActivity().getSharedPreferences("appPreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                if (isGranted) {
                    initHeartRateSensor();
                    editor.putBoolean("useBuiltInSensors", true);
                    editor.apply();
                } else {
                    ToastUtilsKt.toastLong(requireContext(), "Heart rate sensor permission denied.");
                    editor.putBoolean("useBuiltInSensors", false);
                    editor.apply();
                }
            });


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_monitor, container, false);

        LineData lineData = new LineData();
        chart = root.findViewById(R.id.chartView);
        chart.setData(lineData);
        configureChartAppearance();

        viewModel = new ViewModelProvider(requireActivity()).get(MonitorViewModel.class);
        SharedPreferences preferences = requireActivity().getSharedPreferences("appPreferences", Context.MODE_PRIVATE);
        useBuiltInSensors = preferences.getBoolean("useBuiltInSensors", true);

        heartRateTextView = root.findViewById(R.id.rate);
        LiveData<Integer> heartRateLiveData = viewModel.getHeartRate();
        heartRateLiveData.observe(getViewLifecycleOwner(), heartRate -> {
            heartRateTextView.setText(String.valueOf(heartRate));
        });


        ImageView resultsButton = root.findViewById(R.id.showResults);


        if (useBuiltInSensors) {
            //built in
            if (ContextCompat.checkSelfPermission(requireContext(), BODY_SENSORS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(BODY_SENSORS);
            }

            sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
            heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
            heartRateListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
                        float heartRate = event.values[0];

                        if (heartRate > 0) {
                            viewModel.updateHeartRate(Math.round(heartRate));
                            updateChart(heartRate);
                            viewModel.addHeartRateData((int) heartRate);

                            if (Objects.requireNonNull(viewModel.getHeartRateData().getValue()).size() > 20) {
                                resultsButton.setVisibility(View.VISIBLE);
                                resultsButton.setClickable(true);
                            }

                        } else {
                            Log.w(TAG, "Invalid heart rate value received: " + heartRate);
                        }
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    Toast.makeText(getContext(), "Recalibrating...", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Sensor accuracy changed: " + accuracy);
                }
            };

            resultsButton.setOnClickListener(v -> {
                viewModel.getAnalysis();
                showResults(viewModel.getAnalysis());
            });
        } else {
            //bluetooth
            LinearLayout layout = root.findViewById(R.id.connectionLayout);
            layout.setVisibility(View.VISIBLE);
            bluetoothStatus = root.findViewById(R.id.connectionStatus);
            ToastUtilsKt.toast(requireContext(), "Accessing bluetooth sensors");

            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) {
                service = activity.getService();
                if (service != null) {
                    service.setHeartRateListener(heartRateServiceListener);
                    viewModel.setServiceBound(true);
                    checkBluetoothStatus();
                }
            }
        }


        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                FragmentManager fragmentManager = getParentFragmentManager();
                assert getParentFragment() != null;
                View overlayFrameLayout = requireParentFragment().requireView().findViewById(R.id.overlayFrameLayout);
                if (overlayFrameLayout != null) {
                    overlayFrameLayout.setVisibility(View.GONE);
                    overlayFrameLayout.setClickable(false);
                }
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                fragmentTransaction
                        .remove(MonitorFragment.this)
                        .commit();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        return root;
    }

    private void showResults(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Results")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, id) -> dialog.dismiss());
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void checkBluetoothStatus() {
        if (!useBuiltInSensors)
            if (viewModel.serviceBound() && service != null && service.isReceiving() && viewModel.serviceReceiving()) {
                bluetoothStatus.setImageResource(R.drawable.bluetooth_connected);
            } else {
                bluetoothStatus.setImageResource(R.drawable.bluetooth_disconnected);
            }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (useBuiltInSensors)
            if (ContextCompat.checkSelfPermission(requireContext(), BODY_SENSORS)
                    == PackageManager.PERMISSION_GRANTED) {
                initHeartRateSensor();
            } else
                checkBluetoothStatus();
        else
            checkBluetoothStatus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (useBuiltInSensors && sensorManager != null) {
            sensorManager.unregisterListener(heartRateListener);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (useBuiltInSensors && sensorManager != null)
            sensorManager.unregisterListener(heartRateListener);
        viewModel.updateHeartRate(0);
    }

    private void initHeartRateSensor() {
        if (useBuiltInSensors)
            if (heartRateSensor != null && sensorManager != null) {
                sensorManager.registerListener(heartRateListener, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
                ToastUtilsKt.toast(requireContext(), "Initializing");
            } else {
                Toast.makeText(getContext(), "Heart rate sensor not available.", Toast.LENGTH_SHORT).show();
            }
    }


    private void updateChart(float heartRate) {
        LineData data = chart.getData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), heartRate), 0);
            data.notifyDataChanged();

            chart.notifyDataSetChanged();
            chart.setVisibleXRangeMaximum(10);
            chart.moveViewToX(data.getEntryCount());
        }
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Heart Rate");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.RED);
        set.setLineWidth(3f);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setFillAlpha(30);
        set.setFillColor(Color.RED);
        set.setHighLightColor(Color.rgb(244, 117, 117));
        return set;
    }

    private void configureChartAppearance() {
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setPinchZoom(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setEnabled(false);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(50f);
        leftAxis.setAxisMaximum(250f);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        Legend legend = chart.getLegend();
        legend.setTextColor(Color.WHITE);
    }
}
