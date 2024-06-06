package com.example.trainingcompanion;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.trainingcompanion.databinding.ActivityMainBinding;
import com.example.trainingcompanion.extra.NotificationHelper;
import com.example.trainingcompanion.extra.heartrate.HeartRateListenerService;
import com.example.trainingcompanion.ui.fragment.HistoryFragment;
import com.example.trainingcompanion.ui.fragment.SettingsFragment;
import com.example.trainingcompanion.ui.fragment.exercises.AddExerciseFragment;
import com.example.trainingcompanion.ui.fragment.exercises.ExercisesFragment;
import com.example.trainingcompanion.ui.fragment.workouts.AddWorkoutFragment;
import com.example.trainingcompanion.ui.fragment.workouts.MonitorFragment;
import com.example.trainingcompanion.ui.fragment.workouts.WorkoutFragment;
import com.example.trainingcompanion.ui.fragment.workouts.WorkoutsFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;
    private boolean backPressedOnce = false;
    private HeartRateListenerService service;
    private boolean serviceBound = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder serv) {
            HeartRateListenerService.LocalBinder binder = (HeartRateListenerService.LocalBinder) serv;
            service = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
            }
        }

        SharedPreferences preferences = this.getSharedPreferences("appPreferences", Context.MODE_PRIVATE);
        if (!preferences.contains("useBuiltInSensors")) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("useBuiltInSensors", false);
            editor.apply();
        }

        Intent intent = new Intent(this, HeartRateListenerService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        /*navigation*/
        TextView toWorkoutsMenu = findViewById(R.id.textViewWorkout);
        toWorkoutsMenu.setOnClickListener(v -> loadFragment(new WorkoutsFragment()));

        TextView toExercisesMenu = findViewById(R.id.textViewExcercises);
        toExercisesMenu.setOnClickListener(v -> loadFragment(new ExercisesFragment()));

        TextView toHistoryMenu = findViewById(R.id.textViewHistory);
        toHistoryMenu.setOnClickListener(v -> loadFragment(new HistoryFragment()));

        TextView toSettingsMenu = findViewById(R.id.textViewSettings);
        toSettingsMenu.setOnClickListener(v -> loadFragment(new SettingsFragment()));

        OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FragmentManager fragmentManager = getSupportFragmentManager();
                int backStackEntryCount = fragmentManager.getBackStackEntryCount();
                if (backStackEntryCount > 0) {
                    Fragment currentFragment = fragmentManager.findFragmentById(R.id.mainFragmentContainerView);
                    if (currentFragment != null) {
                        fragmentManager.beginTransaction().remove(currentFragment).commit();
                        fragmentManager.popBackStack();
                    }
                } else {
                    if (backPressedOnce) {
                        finish();
                        return;
                    }
                    backPressedOnce = true;
                    Toast.makeText(MainActivity.this, "Press back again to exit", Toast.LENGTH_SHORT).show();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            backPressedOnce = false;
                        }
                    }, 2000);
                }
            }
        };

        onBackPressedDispatcher.addCallback(this, callback);
    }

    public void loadFragment(Fragment fragment) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.mainFragmentContainerView);
        if (currentFragment == null || !currentFragment.getClass().equals(fragment.getClass())) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                    .replace(R.id.mainFragmentContainerView, fragment);

            if (!(currentFragment instanceof AddWorkoutFragment) &&
                    !(currentFragment instanceof AddExerciseFragment) &&
                    !(currentFragment instanceof WorkoutFragment) &&
                    !(currentFragment instanceof MonitorFragment)) {
                transaction.addToBackStack(null);
            }
            transaction.commit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                NotificationHelper.createNotificationChannel(this);
            } else {
                Toast.makeText(MainActivity.this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
    }

    public HeartRateListenerService getService() {
        return service;
    }

    public boolean isServiceBound() {
        return serviceBound;
    }
}