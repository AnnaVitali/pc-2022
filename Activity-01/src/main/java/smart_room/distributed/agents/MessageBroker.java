package smart_room.distributed.agents;

import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttServerOptions;
import io.vertx.mqtt.MqttTopicSubscription;
import io.vertx.mqtt.messages.codes.MqttSubAckReasonCode;

import java.nio.charset.Charset;
import java.util.*;

public class MessageBroker {

    private MqttServer mqttServer;
    private Vertx vertx;
    private Map<MqttEndpoint, List<String>> clientsAndTopics = new HashMap<>();

    public MessageBroker(Vertx vertx){
        this.vertx = vertx;
        this.listeningForNewConnection();
    }

    private void listeningForNewConnection(){
        mqttServer = MqttServer.create(vertx, new MqttServerOptions().setPort(8080).setHost("localhost"));
        mqttServer.endpointHandler(endpoint -> {
            System.out.println(endpoint.clientIdentifier() + "request to connect");
            clientsAndTopics.put(endpoint, new LinkedList<>());

            endpoint.disconnectMessageHandler(disconnectMessage -> {
                System.out.println("Received disconnect from client, reason code = " + disconnectMessage.code());
            });

            endpoint.subscribeHandler(subscribe -> {
                List<MqttSubAckReasonCode> reasonCodes = new ArrayList<>();
                for (MqttTopicSubscription s: subscribe.topicSubscriptions()) {
                    System.out.println("Subscription for " + s.topicName() + " with QoS " + s.qualityOfService());
                    clientsAndTopics.get(endpoint).add(s.topicName());
                    reasonCodes.add(MqttSubAckReasonCode.qosGranted(s.qualityOfService()));
                }
                endpoint.subscribeAcknowledge(subscribe.messageId(), reasonCodes, MqttProperties.NO_PROPERTIES);
            });

            endpoint.unsubscribeHandler(unsubscribe -> {
                for (String t: unsubscribe.topics()) {
                    System.out.println("Unsubscription for " + t);
                    clientsAndTopics.get(endpoint).remove(t);
                }
                endpoint.unsubscribeAcknowledge(unsubscribe.messageId());
            });

            endpoint.publishHandler(message -> {
                System.out.println("Just received message [" + message.payload().toString(Charset.defaultCharset()) + "] with QoS [" + message.qosLevel() + "] from the endpoint " + endpoint.clientIdentifier());
                if (message.qosLevel() == MqttQoS.AT_LEAST_ONCE) {
                    clientsAndTopics.forEach((k, v) -> {
                        if(v.contains(message.topicName())){
                            k.publish(message.topicName(), message.payload(), message.qosLevel(), false, false);
                        };
                    });
                    endpoint.publishAcknowledge(message.messageId());
                } else if (message.qosLevel() == MqttQoS.EXACTLY_ONCE) {
                    endpoint.publishReceived(message.messageId());
                }
            }).publishReleaseHandler(endpoint::publishComplete);

            endpoint.accept(false);
        }).listen(ar -> {
            if(ar.succeeded()){
                System.out.println("MQTT message broker is listening on port " + ar.result().actualPort());
            }else{
                System.out.println("Error on starting the message broker");
                ar.cause().printStackTrace();
            }
        });
    }

}
