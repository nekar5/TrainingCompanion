package com.example.trainingcompanion.ui.fragment;

import static android.Manifest.permission.BODY_SENSORS;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.trainingcompanion.R;
import com.example.trainingcompanion.databinding.FragmentSettingsBinding;
import com.example.trainingcompanion.ui.viewmodel.SettingsViewModel;

import java.util.Objects;

public class SettingsFragment extends Fragment implements Inform {

    private SettingsViewModel viewModel;
    private Sensor heartRateSensor;
    private SensorManager sensorManager;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch sensorToggle;

    private FragmentSettingsBinding binding;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(getLayoutInflater());
        binding.setLifecycleOwner(this);
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        binding.setViewModel(viewModel);
        viewModel.setInform(this);
        viewModel.checkUser();

        Button resetDataButton = binding.resetAllDataButton;
        resetDataButton.setOnClickListener(v -> {
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        viewModel.resetAllDatabase();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
            builder.setIcon(R.drawable.info_circle_icon)
                    .setTitle("Reset app data")
                    .setMessage("This will reset all app data. Are you sure?")
                    .setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener)
                    .show()
                    .getWindow()
                    .setBackgroundDrawableResource(R.color.light_grey);
            ;
        });

        Button resetDefaultData = binding.resetDefaultDataButton;
        resetDefaultData.setOnClickListener(v -> {
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        viewModel.resetToDefaults();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
            builder.setIcon(R.drawable.info_circle_icon)
                    .setTitle("Reset default data")
                    .setMessage("This will reset app data to default values (Workouts, Exercises). Are you sure?")
                    .setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener)
                    .show()
                    .getWindow()
                    .setBackgroundDrawableResource(R.color.light_grey);
            ;
        });

        Button resetUserData = binding.resetUserDataButton;
        resetUserData.setOnClickListener(v -> {
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        viewModel.resetUserData();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
            builder.setIcon(R.drawable.info_circle_icon)
                    .setTitle("Reset user data")
                    .setMessage("This will reset user data. Are you sure?")
                    .setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener)
                    .show()
                    .getWindow()
                    .setBackgroundDrawableResource(R.color.light_grey);
            ;
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_view,
                Objects.requireNonNull(viewModel.getChoices()));


        binding.selectedSex.setAdapter(adapter);
        binding.selectedSex.setOnItemClickListener((parent, view, position, id) -> {
            String selectedSex = (String) parent.getItemAtPosition(position);
            viewModel.setSelectedSex(selectedSex);
        });

        sensorToggle = binding.sensorToggle;
        SharedPreferences preferences = requireActivity().getSharedPreferences("appPreferences", Context.MODE_PRIVATE);
        boolean useBuiltInSensors = preferences.getBoolean("useBuiltInSensors", false);
        sensorToggle.setChecked(useBuiltInSensors);

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        sensorToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (heartRateSensor == null) {
                    onFailure("Heart rate sensor not available on this device");
                    sensorToggle.setChecked(false);
                } else {
                    if (ContextCompat.checkSelfPermission(requireContext(), BODY_SENSORS)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{BODY_SENSORS}, 1);
                        sensorToggle.setChecked(false);
                    } else {
                        sensorToggle.setChecked(true);
                        savePreference(true);
                    }
                }
            } else {
                savePreference(false);
            }
        });

        return binding.getRoot();
    }

    private void savePreference(boolean useBuiltInSensors) {
        SharedPreferences preferences = requireActivity().getSharedPreferences("appPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("useBuiltInSensors", useBuiltInSensors);
        editor.apply();
    }

    @Override
    public void onSuccess(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFailure(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }
}