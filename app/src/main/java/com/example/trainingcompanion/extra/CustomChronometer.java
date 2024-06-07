package com.example.trainingcompanion.extra;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.sql.Time;

import kotlin.Triple;

public class CustomChronometer extends androidx.appcompat.widget.AppCompatTextView {
    private Triple<Activity, String, String> notificationData = null;
    private boolean isRunning = false;
    private boolean isPaused = false;
    private int time;
    private CountDownTimer countDownTimer;
    private CustomChronometer nextChronometer;
    private PowerManager.WakeLock wakeLock;
    private long remainingTime = 0;

    public CustomChronometer(Context context) {
        super(context);
        initWakeLock(context);
    }

    public CustomChronometer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initWakeLock(context);
    }

    public CustomChronometer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initWakeLock(context);
    }

    private void initWakeLock(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TrainingCompanion::CustomChronometerWakeLock");
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
        setText(formatTime(time * 60100L));
    }

    public void start() {
        if (isPaused) {
            isPaused = false;
            isRunning=false;
        } else {
            remainingTime = time * 60000L;
        }
        if (!isRunning) {
            isRunning = true;
            countDownTimer = new CountDownTimer(remainingTime, 1000) {
                @SuppressLint("DefaultLocale")
                @Override
                public void onTick(long millisUntilFinished) {
                    remainingTime = millisUntilFinished;
                    setText(formatTime(millisUntilFinished));
                }

                @Override
                public void onFinish() {
                    stop();
                }
            }.start();
            acquireWakeLock();
        }
    }

    private String formatTime(long millis) {
        long totalSeconds = millis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        String formattedTime;
        if (hours >= 1) {
            formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            formattedTime = String.format("%02d:%02d", minutes, seconds);
        }
        return formattedTime;
    }

    public void stop() {
        if (isRunning) {
            if (countDownTimer != null) {
                countDownTimer.cancel();

                if (!isPaused) {
                    isRunning = false;
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                        if (nextChronometer != null) {
                            nextChronometer.start();
                        }
                        if (notificationData != null) {
                            notifyStop();
                        }
                    }
                    releaseWakeLock();
                }
            }
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setNotification(Activity activity, String title, String text) {
        notificationData = new Triple<>(activity, title, text);
        NotificationHelper.createNotificationChannel(activity);
    }

    private void notifyStop() {
        NotificationHelper.sendNotification(notificationData.getFirst(),
                notificationData.getSecond(),
                notificationData.getThird());
    }

    private void acquireWakeLock() {
        if (wakeLock != null && !wakeLock.isHeld()) {
            wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
        }
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    public void skip() {
        setText("00:00");
        isPaused = false;
        stop();
    }

    public void pause() {
        isPaused = true;
        stop();
        isRunning = true;
    }

    public void resume() {
        start();
    }

    public void setNext(CustomChronometer nextChronometer) {
        this.nextChronometer = nextChronometer;
    }

    public long getRemainingTimeMillis() {
        Time t = Time.valueOf("00:" + getText().toString());
        return t.getTime();
    }

    public void reset() {
        if(isRunning){
            isPaused=false;
            stop();
        }
        setText(formatTime(time * 60100L));
    }
}
