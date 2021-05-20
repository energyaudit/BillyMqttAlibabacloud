package com.aliyun.iot;

import org.eclipse.paho.mqttv5.client.IMqttMessageListener;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.MqttSubscription;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.eclipse.paho.mqttv5.common.packet.UserProperty;

import java.util.ArrayList;
import java.util.List;

class Mqtt5PostPropertyMessageListener implements IMqttMessageListener {

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        System.out.println("reply topic  : " + topic);
        System.out.println("reply payload: " + message.toString());
    }
}
/**
 * MQTT5 协议的设备接入示例
 */
public class Mqtt5App {
    public static void main(String[] args) throws MqttException {
        String productKey = "a112bibTSZ7";
        String deviceName = "example2";
        String deviceSecret = "06bdaca8e660fee3e1ca7e4a7a3047ac";

        //计算Mqtt建联参数
        MqttSign sign = new MqttSign();
        sign.calculate(productKey, deviceName, deviceSecret);

        System.out.println("username: " + sign.getUsername());
        System.out.println("password: " + sign.getPassword());
        System.out.println("clientid: " + sign.getClientid());

        //使用Paho连接阿里云物联网平台
//        String port = "443";
        String port = "1883";

        //您可登录物联网平台控制台，在实例概览页，找到并单击对应实例，进入实例详情页，查看实例终端节点
//        String broker = "ssl://" + "${企业版实例下接入域名}" + ":" + port;//ssl may change to tcp as your system designed to
        String broker = "ssl://" + "iot.eclipse.org" + ":" + port;


        MemoryPersistence persistence = new MemoryPersistence();
        try {
            //Paho Mqtt 客户端
            MqttClient sampleClient = new MqttClient(broker, sign.getClientid(), persistence);

            //Paho Mqtt 连接参数
            MqttConnectionOptions connOpts = new MqttConnectionOptions();
            connOpts.setCleanStart(true);
            connOpts.setKeepAliveInterval(180);
            connOpts.setUserName(sign.getUsername());
            connOpts.setPassword(sign.getPassword().getBytes());

            //Paho Mqtt5 设备上报QoS1&QoS2消息流控
            connOpts.setReceiveMaximum(5);

            //Paho Mqtt5 设备接收的最大报文长度
            connOpts.setMaximumPacketSize(1024L);

            IMqttToken iMqttToken = sampleClient.connectWithResult(connOpts);
            System.out.println("broker: " + broker + " Connected");

            //Paho Mqtt5 服务端可用能力列表
            System.out.println("broker available ability, retain is available: " + iMqttToken.getResponseProperties().isRetainAvailable());
            System.out.println("broker available ability, shared subscription is available: " + iMqttToken.getResponseProperties().isSharedSubscriptionAvailable());

            //Paho Mqtt 消息订阅
            String topicReply = "/sys/" + productKey + "/" + deviceName + "/thing/event/property/post_reply";
            MqttSubscription[] subscriptions = new MqttSubscription[] {new MqttSubscription(topicReply)};
            IMqttMessageListener[] subscriptionListeners = new IMqttMessageListener[] {
                new Mqtt5PostPropertyMessageListener()};
            sampleClient.subscribe(subscriptions, subscriptionListeners);
            System.out.println("subscribe: " + topicReply);

            //Paho Mqtt 消息发布
            String topic = "/sys/" + productKey + "/" + deviceName + "/thing/event/property/post";
            String content = "{\"id\":\"1\",\"version\":\"1.0\",\"params\":{\"LightSwitch\":1}}";
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(0);

            //Paho Mqtt5 用户属性
            MqttProperties properties = new MqttProperties();
            List<UserProperty> userPropertys = new ArrayList<>();
            userPropertys.add(new UserProperty("key1", "value1"));
            properties.setUserProperties(userPropertys);
            message.setProperties(properties);

            sampleClient.publish(topic, message);
            System.out.println("publish: " + content);

            Thread.sleep(2000);

            //Paho Mqtt 断开连接
            sampleClient.disconnect();
            System.out.println("Disconnected");
            System.exit(0);
        } catch (MqttException e) {
            System.out.println("reason " + e.getReasonCode());
            System.out.println("msg " + e.getMessage());
            System.out.println("cause " + e.getCause());
            System.out.println("excep " + e);
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
