package simpleMqtt.simple;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Random;

public class Publisher {


  public static void main(String[] args) throws MqttException {

    String messageString = "Hello World from Java!";
    Random random = new Random();
    Integer inputId = random.nextInt(1000000);
    messageString=messageString+inputId;
    if (args.length == 2 ) {
      messageString = args[1];
    }


    System.out.println("== START PUBLISHER ==");


    MqttClient client = new MqttClient("tcp://broker.emqx.io:1883", MqttClient.generateClientId());
    client.connect();
    MqttMessage message = new MqttMessage();
    message.setPayload(messageString.getBytes());
    client.publish("iot_data", message);

    System.out.println("\tMessage '"+ messageString +"' to 'iot_data'");

    client.disconnect();

    System.out.println("== END PUBLISHER ==");

  }


}
