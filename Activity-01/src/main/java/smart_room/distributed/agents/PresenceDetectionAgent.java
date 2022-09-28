package smart_room.distributed.agents;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import smart_room.Controller;
import smart_room.Event;
import smart_room.distributed.PresDetectSensorSimulator;

public class PresenceDetectionAgent implements Controller {

    private static final String PRESENCE_DETECTED_TOPIC = "PresenceDetected";
    private static final String PRESENCE_NO_MORE_DETECTED_TOPIC = "PresenceNoMoreDetected";
    private MqttClient mqttClient;
    private PresDetectSensorSimulator presDetectSensorSimulator;
    private Vertx vertx;

    public PresenceDetectionAgent(Vertx vertx){
        this.vertx = vertx;
        this.start();
    }

    @Override
    public void notifyEvent(Event ev) {
        String className = ev.getClass().getSimpleName();
        switch(className){
            case "PresenceDetected":
                mqttClient.publish(PRESENCE_DETECTED_TOPIC,
                        Buffer.buffer("Presence detected"),
                        MqttQoS.AT_LEAST_ONCE,
                        false,
                        false);
                break;
            case "PresenceNoMoreDetected":
                mqttClient.publish(PRESENCE_NO_MORE_DETECTED_TOPIC,
                        Buffer.buffer("Presence no more detected"),
                        MqttQoS.AT_LEAST_ONCE,
                        false,
                        false);
                break;
        }
    }


    private void start(){
        mqttClient = MqttClient.create(vertx);
        mqttClient.connect(8080, "localhost", s -> {
            System.out.println("PIR connected");
            this.initializeFrame();
        });
    }

    private void initializeFrame(){
        presDetectSensorSimulator = new PresDetectSensorSimulator("MyPIR");
        presDetectSensorSimulator.init();
        presDetectSensorSimulator.register(this);
    }
}
