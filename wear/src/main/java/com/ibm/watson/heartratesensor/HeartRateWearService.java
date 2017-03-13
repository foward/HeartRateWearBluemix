package com.ibm.watson.heartratesensor;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static android.content.Context.SENSOR_SERVICE;

public class HeartRateWearService extends Service implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor heartRateSensor;
    private ScheduledExecutorService heartRateScheduler;
    private GoogleApiClient googleApiClient;
    private ExecutorService executorService;

    public HeartRateWearService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getHeartRateValues();
        executorService = Executors.newCachedThreadPool();
    }

    private boolean isConnected() {
        if (googleApiClient == null)
            googleApiClient = new GoogleApiClient.Builder(this.getApplication().getApplicationContext()).addApi(Wearable.API).build();

        if (googleApiClient.isConnected()) {
            return true;
        }

        ConnectionResult result = googleApiClient.blockingConnect(15000, TimeUnit.MILLISECONDS);
        return result.isSuccess();
    }

    private void sendToMobile(final SensorEvent event) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {

                PutDataMapRequest dataMap = PutDataMapRequest.create("/sensor/heartRate");
                dataMap.getDataMap().putLong("timestamp", event.timestamp);
                dataMap.getDataMap().putFloat("value", event.values[0]);
                PutDataRequest putDataRequest = dataMap.asPutDataRequest();
                if (isConnected()) {
                    Wearable.DataApi.putDataItem(googleApiClient, putDataRequest).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(DataApi.DataItemResult dataItemResult) {
                            Log.v(this.getClass().getName(), "Sending heartRate: " + dataItemResult.getStatus().isSuccess());
                        }
                    });
                }
            }
        });
    }



    private void getHeartRateValues() {
        sensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        if (heartRateSensor != null) {
            heartRateScheduler = Executors.newScheduledThreadPool(1);
            heartRateScheduler.scheduleAtFixedRate(
                    new Runnable() {
                        @Override
                        public void run() {
                            sensorManager.registerListener(HeartRateWearService.this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException e) {
                            }
                            sensorManager.unregisterListener(HeartRateWearService.this, heartRateSensor);
                        }
                    }, 3, 15, TimeUnit.SECONDS);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (sensorManager != null)
            sensorManager.unregisterListener(this);
        heartRateScheduler.shutdown();

        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // sends an Intent to the Activity
        Intent intent = new Intent();
        intent.setAction("heartRateAction");
        intent.putExtra("HeartRate", event.values[0]);
        if( event.values[0] >0 ) {
            Log.v(this.getClass().getName(), "HeartRate change: " + event.values[0]);
            sendBroadcast(intent);
            sendToMobile(event);
        }else{
            Log.v(this.getClass().getName(), "HeartRate 0.0");
        }
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(this.getClass().getName(), "onAccuracyChanged - accuracy: " + accuracy);
    }
}