package com.example.trainingcompanion.extra;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.Chronometer;

import com.example.trainingcompanion.R;

import java.sql.Time;

import kotlin.Triple;

public class CustomChronometer extends Chronometer {
    CustomChronometer nextChronometer = null;
    Triple notificationData = null;
    boolean isRunning = false;
    boolean isFinished = true;
    int time;
    long pausedTime = 0;

    public CustomChronometer(Context context) {
        super(context);
    }

    public CustomChronometer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomChronometer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomChronometer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void pause() {
        pausedTime = SystemClock.elapsedRealtime() - this.getBase();
        super.stop();
        isRunning = true;
    }

    public void resume() {
        this.setBase(SystemClock.elapsedRealtime() - pausedTime);
        super.start();
    }

    @Override
    public void start() {
        this.setNewBase();
        super.start();
        this.setStopper();
        isRunning = true;
    }

    @Override
    public void stop() {
        isRunning = false;
        super.stop();
        if (notificationData != null) {
            if (super.getText().equals("00:00")) {
                isFinished = true;
                this.notifyStop();
            }
        }
        if (nextChronometer != null) {
            this.nextChronometer.start();
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void skip() {
        this.setText(R.string.zero_time);
        this.stop();
    }

    public void reduceTime(int reduced) {
        super.stop();
        isRunning = true;
        setTime(time - reduced);
        this.start();
    }

    public long getRemainingTimeLong() {
        Time t = Time.valueOf("00:" + this.getText().toString());
        return t.getTime();
    }

    public void setNewBase() {
        this.setBase(SystemClock.elapsedRealtime() + time * 60000L + 100L);//+ 100L
    }

    private void setStopper() {
        this.setOnChronometerTickListener(chronometer -> {
            if (chronometer.getText().equals("00:00")) {
                chronometer.stop();
            }
        });
    }

    public void setNext(CustomChronometer cc) {
        nextChronometer = cc;
    }

    public void setNotification(Activity activity, String title, String text) {
        notificationData = new Triple(activity, title, text);
    }

    private void notifyStop() {
        NotificationHelper.sendNotification((Activity) notificationData.getFirst(),
                (String) notificationData.getSecond(),
                (String) notificationData.getThird());
    }
}
