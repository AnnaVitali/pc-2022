package main;

import io.vertx.core.Vertx;
import smart_room.distributed.agents.LightDeviceAgent;
import smart_room.distributed.agents.LightLevelChangerAgent;
import smart_room.distributed.agents.MessageBroker;
import smart_room.distributed.agents.PresenceDetectionAgent;

public class MainMqttSolution {

    public static void main(String[] args){
        Vertx vertx = Vertx.vertx();
        MessageBroker messageBroker = new MessageBroker(vertx);
        PresenceDetectionAgent presenceDetectionAgent = new PresenceDetectionAgent(vertx);
        LightDeviceAgent lightDeviceAgent  = new LightDeviceAgent(vertx);
        LightLevelChangerAgent lightLevelChangerAgent = new LightLevelChangerAgent(vertx);

    }
}
