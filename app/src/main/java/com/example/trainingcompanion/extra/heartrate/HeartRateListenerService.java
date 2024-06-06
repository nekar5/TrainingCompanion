package com.example.trainingcompanion.extra.heartrate;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;

public class HeartRateListenerService extends Service implements DataClient.OnDataChangedListener {

    private ArrayList<Integer> history;
    private boolean recordHistory;
    private boolean isReceiving = false;
    private final Handler handler = new Handler();
    private boolean newDataReceived = false;
    private static final String TAG = "HeartRateListenerService";
    private HeartRateListener heartRateListener;

    public interface HeartRateListener {
        void onHeartRateChanged(float heartRate);

        void onReceivingStatusChanged(boolean isReceiving);
    }

    private final Runnable checkReceivingStatusRunnable = new Runnable() {
        @Override
        public void run() {
            if (!newDataReceived) {
                isReceiving = false;
                if (heartRateListener != null) {
                    heartRateListener.onReceivingStatusChanged(isReceiving);
                }
                //Log.e(TAG, "No new data received, set isReceiving to false");
            } else {
                newDataReceived = false;
                isReceiving = true;
            }
            handler.postDelayed(this, 3000);
        }
    };

    public class LocalBinder extends android.os.Binder {
        public HeartRateListenerService getService() {
            return HeartRateListenerService.this;
        }
    }

    private final IBinder binder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        history = new ArrayList<>();
        recordHistory = true;
        Wearable.getDataClient(this).addListener(this);
        handler.postDelayed(checkReceivingStatusRunnable, 3000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Wearable.getDataClient(this).removeListener(this);
        handler.removeCallbacks(checkReceivingStatusRunnable);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (path.equals("/heart_rate")) {
                    DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                    int heartRate = dataMap.getInt("heart_rate");
                    Log.w(TAG, "Received heart rate: " + heartRate);
                    if (heartRateListener != null) {
                        heartRateListener.onHeartRateChanged(heartRate);
                        isReceiving = true;
                        if (recordHistory)
                            history.add(heartRate);
                        newDataReceived = true;
                    }
                }
            }
        }
    }

    public boolean isReceiving() {
        return isReceiving;
    }

    public void resetHistory() {
        history = new ArrayList<>();
    }

    public void pauseHistory() {
        recordHistory = false;
    }

    public void resumeHistory() {
        recordHistory = true;
    }

    public ArrayList<Integer> getHistory() {
        if (history != null) {
            return history;
        }
        return new ArrayList<>();
    }

    public void setHeartRateListener(HeartRateListener listener) {
        this.heartRateListener = listener;
    }
}
