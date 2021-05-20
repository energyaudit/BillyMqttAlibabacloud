package com.aliyun.iot;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.*;

class Mqtt3PostPropertyMessageListener implements IMqttMessageListener {
    @Override
    public void messageArrived(String var1, MqttMessage var2) throws Exception {
        System.out.println("reply topic  : " + var1);
        System.out.println("reply payload: " + var2.toString());
    }
}

/**
 * MQTT3.1 协议的设备接入示例
 */
public class App
{
    public static void main( String[] args )
    {
        String productKey = "a1X2bEnP82z";
        String deviceName = "example1";
        String deviceSecret = "ga7XA6KdlEeiPXQPpRbAjOZXwG8ydgSe";

        //计算Mqtt建联参数
        MqttSign sign = new MqttSign();
        sign.calculate(productKey, deviceName, deviceSecret);

        System.out.println("username: " + sign.getUsername());
        System.out.println("password: " + sign.getPassword());
        System.out.println("clientid: " + sign.getClientid());

        //使用Paho连接阿里云物联网平台
        String port = "443";
        String broker = "ssl://" + productKey + ".iot-as-mqtt.cn-shanghai.aliyuncs.com" + ":" + port;
        MemoryPersistence persistence = new MemoryPersistence();
        try{
            //Paho Mqtt 客户端
            MqttClient sampleClient = new MqttClient(broker, sign.getClientid(), persistence);

            //Paho Mqtt 连接参数
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setKeepAliveInterval(180);
            connOpts.setUserName(sign.getUsername());
            connOpts.setPassword(sign.getPassword().toCharArray());
            sampleClient.connect(connOpts);
            System.out.println("broker: " + broker + " Connected");

            //Paho Mqtt 消息订阅
            String topicReply = "/sys/" + productKey + "/" + deviceName + "/thing/event/property/post_reply";
            sampleClient.subscribe(topicReply, new Mqtt3PostPropertyMessageListener());
            System.out.println("subscribe: " + topicReply);

            //Paho Mqtt 消息发布
            String topic = "/sys/" + productKey + "/" + deviceName + "/thing/event/property/post";
            String content = "{\"id\":\"1\",\"version\":\"1.0\",\"params\":{\"LightSwitch\":1}}";
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(0);
            sampleClient.publish(topic, message);
            System.out.println("publish: " + content);

            Thread.sleep(2000);

            //Paho Mqtt 断开连接
            sampleClient.disconnect();
            System.out.println("Disconnected");
            System.exit(0);
        }catch (MqttException e) {
            System.out.println("reason " + e.getReasonCode());
            System.out.println("msg " + e.getMessage());
            System.out.println("loc " + e.getLocalizedMessage());
            System.out.println("cause " + e.getCause());
            System.out.println("excep " + e);
            e.printStackTrace();
        }catch (InterruptedException e ) {
            e.printStackTrace();
        }
    }
}