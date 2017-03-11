package com.ibm.watson.heartratesensor;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

public class HeartRateMobileWearableListenerService extends WearableListenerService {
    public HeartRateMobileWearableListenerService() {
    }

        @Override
        public void onDataChanged(DataEventBuffer dataEvents) {
            Log.d(this.getClass().getName(), "onDataChanged()");
            for (DataEvent dataEvent : dataEvents) {
                if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                    DataItem dataItem = dataEvent.getDataItem();
                    Uri uri = dataItem.getUri();
                    String path = uri.getPath();
                    if (path.equals("/sensor/heartRate")) {
                        DataMap map = DataMapItem.fromDataItem(dataItem).getDataMap();
                        long timestamp = map.getLong("timestamp");
                        float value = map.getFloat("value");
                        Intent intent = new Intent();
                        intent.setAction("heartRateAction");
                        intent.putExtra("HeartRate", value);
                        sendBroadcast(intent);
                    }
                }
            }
        }

}
