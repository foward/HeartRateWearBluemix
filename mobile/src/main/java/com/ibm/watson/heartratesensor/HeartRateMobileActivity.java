package com.ibm.watson.heartratesensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.ibm.watson.heartratesensor.utils.MqttHandler;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

public class HeartRateMobileActivity extends AppCompatActivity {

    private HeartRateBroadcastReceiver heartRateBroadcastReceiver;
    private boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_mobile);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // ahr
        heartRateBroadcastReceiver = new HeartRateBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("heartRateAction");
        registerReceiver(heartRateBroadcastReceiver, intentFilter);

        MqttHandler.getInstance(this).connect(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                connected = true;
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
            }
        });
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub

        try{
            if(heartRateBroadcastReceiver!=null)
                unregisterReceiver(heartRateBroadcastReceiver);
        }catch(Exception e)
        {

        }
        super.onDestroy();

    }

    @Override
    protected void onStop(){
    super.onStop();
        MqttHandler.getInstance(this).disconnect(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                connected = false;
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
            }
        });
    }

    private class HeartRateBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // ahr
           // Log.v(this.getClass().getName(), "Value Received");
            if (arg1.getAction().equals("heartRateAction")) {
                float hr = arg1.getFloatExtra("HeartRate", 0);
                long ts = arg1.getLongExtra("timestamp", 0);
                if(hr != 0) {
                    Log.v(this.getClass().getName(), "HeartRate Mobile" + hr);
                    ((TextView) HeartRateMobileActivity.this.findViewById(R.id.heartRateTextView)).setText("Heart Rate : " + hr);

                    if (connected)
                        MqttHandler.getInstance(HeartRateMobileActivity.this).publish(ts, hr);
                }

            }
        }
    }
}
