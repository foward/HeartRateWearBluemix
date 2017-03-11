package com.ibm.watson.heartratesensor;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

public class HeartRateWearActivity extends Activity {

    private TextView mTextView;
    private HeartRateBroadcastReceiver heartRateBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_wear);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.heartRateTextView);
            }
        });
    }

    @Override
    protected void onStart() {
        // initialize the broadcast recieiver
        heartRateBroadcastReceiver = new HeartRateBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("heartRateAction");
        registerReceiver(heartRateBroadcastReceiver, intentFilter);

        // start HeartRateWearService
        startService(new Intent(this, HeartRateWearService.class));
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(heartRateBroadcastReceiver);
        stopService(new Intent(this, HeartRateWearService.class));
        super.onStop();
    }


    private class HeartRateBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // ahr
            Log.v(this.getClass().getName(), "Value Recieved");
            if (arg1.getAction().equals("heartRateAction")) {
                float hr = arg1.getFloatExtra("HeartRate", 0);
                if (HeartRateWearActivity.this.mTextView != null) {
                    HeartRateWearActivity.this.mTextView.setText("Heart Rate : " + hr);
                }
            }
        }
    }
}
