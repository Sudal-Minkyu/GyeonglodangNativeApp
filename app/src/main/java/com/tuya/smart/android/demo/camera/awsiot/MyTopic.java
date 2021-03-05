package com.tuya.smart.android.demo.camera.awsiot;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;
import com.tuya.smart.android.demo.camera.CameraPanelActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class MyTopic extends AWSIotTopic {

    private CameraPanelActivity cameraPanelActivity;

    public MyTopic(String topic, AWSIotQos qos, CameraPanelActivity cameraPanelActivity) {
        super(topic, qos);
        this.cameraPanelActivity = cameraPanelActivity;
    }

    public MyTopic(String topic, AWSIotQos qos) {
        super(topic, qos);
    }

    public void datasend(String data) {
        cameraPanelActivity.doorOn(data);
    }

    @Override
    public void onMessage(AWSIotMessage message) {
        try {
//            System.out.println("Subscribe topic :"+ message.getTopic() +"    Message : "+ message.getStringPayload());
//            Log.e("받아오기 : ","받아오기 성공!");
            final JSONObject json = new JSONObject(message.getStringPayload());
            final String doordata = json.getString("door1");
            this.datasend(doordata);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}