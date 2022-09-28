package smart_room.distributed.agents;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import smart_room.Controller;
import smart_room.Event;
import smart_room.LuminositySensorDevice;
import smart_room.distributed.LightLevelChanged;
import smart_room.distributed.LuminositySensorSimulator;

public class LightLevelChangerAgent implements Controller {

    private static final String LIGHT_LEVEL_CHANGED_TOPIC = "LightLevelChanged";
    private Vertx vertx;
    private MqttClient mqttClient;
    private LuminositySensorSimulator luminositySensorSimulator;

    public LightLevelChangerAgent(Vertx vertx){
        this.vertx = vertx;
        this.start();
    }

    @Override
    public void notifyEvent(Event ev) {
        double newLevel = ((LightLevelChanged) ev).getNewLevel();
        mqttClient.publish(LIGHT_LEVEL_CHANGED_TOPIC,
                Buffer.buffer(Double.toString(newLevel)),
                MqttQoS.AT_LEAST_ONCE,
                false,
                false);
    }

    private void start(){
        mqttClient = MqttClient.create(vertx);
        mqttClient.connect(8080, "localhost", s -> {
            System.out.println("Luminosity sensor connected");
            this.initializeFrame();
        });
    }

    private void initializeFrame(){
        luminositySensorSimulator = new LuminositySensorSimulator("MyLuminositySensor");
        luminositySensorSimulator.init();
        luminositySensorSimulator.register(this);
    }
}
