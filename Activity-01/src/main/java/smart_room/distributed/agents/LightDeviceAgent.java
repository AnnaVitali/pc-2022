package smart_room.distributed.agents;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttClient;
import smart_room.distributed.LightDeviceSimulator;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class LightDeviceAgent {

    private static final double T_LEVEL = 0.5;
    private static final String PRESENCE_DETECTED_TOPIC = "PresenceDetected";
    private static final String PRESENCE_NO_MORE_DETECTED_TOPIC = "PresenceNoMoreDetected";
    private static final String LIGHT_LEVEL_CHANGED_TOPIC = "LightLevelChanged";
    private MqttClient mqttClient;
    private final Map<String, Integer> topics = new HashMap<>();
    private boolean someoneIsInTheRoom = false;
    private double currentLuminosityLevel = 0.0;
    private LightDeviceSimulator lightDeviceSimulator;
    private Vertx vertx;

    public LightDeviceAgent(Vertx vertx){
        this.vertx = vertx;
        this.start();
    }

     private void start(){
        mqttClient = MqttClient.create(vertx);
         mqttClient.connect(8080, "localhost", s -> {
             System.out.println("Light Device connected");

             topics.put(PRESENCE_DETECTED_TOPIC, 1);
             topics.put(PRESENCE_NO_MORE_DETECTED_TOPIC, 1);
             topics.put(LIGHT_LEVEL_CHANGED_TOPIC, 1);

             mqttClient.publishHandler(t -> {
                         switch(t.topicName()){
                             case PRESENCE_DETECTED_TOPIC:
                                 presenceDetectedHandler();
                                 break;
                             case PRESENCE_NO_MORE_DETECTED_TOPIC:
                                 presenceNoMoreDetectedHandler();
                                 break;
                             case LIGHT_LEVEL_CHANGED_TOPIC:
                                 lightLevelChangedHandler(Double.parseDouble(t.payload().toString(Charset.defaultCharset())));
                                 break;
                         }
                     })
                     .subscribe(topics);
             this.initializeFrame();
         });
    }

    private void initializeFrame(){
        lightDeviceSimulator = new LightDeviceSimulator("MyLight");
        lightDeviceSimulator.init();
    }

    private void lightLevelChangedHandler(double newLevel){
        currentLuminosityLevel = newLevel;
        if(someoneIsInTheRoom && currentLuminosityLevel < T_LEVEL){
            lightDeviceSimulator.on();
        }else{
            lightDeviceSimulator.off();
        }
    }

    private void presenceDetectedHandler(){
        someoneIsInTheRoom = true;
        if(currentLuminosityLevel < T_LEVEL){
            lightDeviceSimulator.on();
        }
    }

    private void presenceNoMoreDetectedHandler(){
        someoneIsInTheRoom = false;
        lightDeviceSimulator.off();
    }
}
