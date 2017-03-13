package com.ibm.watson.heartratesensor.utils;

import android.content.Context;

import android.os.Build;
import android.support.annotation.RequiresApi;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by friveros on 11/03/2017.
 */

public class MqttHandler implements MqttCallback {

    private static MqttHandler instance;
    private MqttAndroidClient mqttClient;
    Context context;

    private static String ORG = "hudvi4";
    private static String DEVICE_TYPE = "Wear";
    private static String DEVICE_ID = "myWatch";
    private static String TOKEN = "+PEi&j)1GR9n(rmpUx";
    private static String TOPIC = "iot-2/evt/hr/fmt/json";

    private MqttHandler(Context context) {
        this.context = context;
    }


    public static MqttHandler getInstance(Context context) {
        if (instance == null) {
            instance = new MqttHandler(context);
        }
        return instance;
    }


    @Override
    public void connectionLost(Throwable throwable) {
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    }

    public void connect(IMqttActionListener listener) {
        if (!isConnected()) {
            String iotPort = "1883";
            String iotHost = ORG+".messaging.internetofthings.ibmcloud.com";
            String iotClientId = "d:"+ORG+":"+DEVICE_TYPE+":"+DEVICE_ID;

            String connectionUri = "tcp://" + iotHost + ":" + iotPort;

            if (mqttClient != null) {
                mqttClient.unregisterResources();
                mqttClient = null;
            }

            mqttClient = new MqttAndroidClient(context, connectionUri, iotClientId);
            mqttClient.setCallback(this);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setUserName("use-token-auth");
            options.setPassword(TOKEN.toCharArray());

            try {
                mqttClient.connect(options, context, listener);
            } catch (MqttException e) {

            }
        }
    }

    public void disconnect(IMqttActionListener listener) {
        if (isConnected()) {
            try {
                mqttClient.disconnect(context, listener);
                mqttClient = null;
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }


    public void publish(long timestamp, float heartRateValue) {
        if (isConnected()) {

            //Format the Json String
            JSONObject heartRateObj = new JSONObject();
            JSONObject jsonObj = new JSONObject();
            try {
                heartRateObj.put("heartRate", heartRateValue);
                heartRateObj.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(new Date()));
                jsonObj.put("d", heartRateObj);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }


          //  String msg = "{'timestamp':"+timestamp+",'heartRate':"+heartRateValue+"}";
            MqttMessage mqttMsg = new MqttMessage(jsonObj.toString().getBytes());
            mqttMsg.setRetained(false);
            mqttMsg.setQos(0);
            try {
                mqttClient.publish(TOPIC, mqttMsg);
            } catch (Exception e) {

            }



        }
    }

    private boolean isConnected() {
        if (mqttClient != null) {
            return mqttClient.isConnected();
        }
        return false;
    }
}