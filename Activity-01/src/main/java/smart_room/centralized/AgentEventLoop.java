package smart_room.centralized;

import smart_room.Controller;
import smart_room.Event;

import java.util.LinkedList;
import java.util.List;
public class AgentEventLoop extends AbstractAgent {


    private final List<Event> eventQueue = new LinkedList<>();

    public AgentEventLoop(){
        boardSimulator.register(this);
        boardSimulator.init();
        start();
    }

    public void start(){
        new Thread(() -> {
            while (true){
                if(!eventQueue.isEmpty()) {
                    Event eventToHandle = eventQueue.remove(0);
                    String className = eventToHandle.getClass().getSimpleName();
                    switch(className){
                        case "LightLevelChanged":
                            this.lightLevelChangedHandler((LightLevelChanged) eventToHandle);
                            break;
                        case "PresenceDetected":
                            this.presenceDetectedHandler((PresenceDetected) eventToHandle);
                            break;
                        case "PresenceNoMoreDetected":
                            this.presenceNoMoreDetectedHandler((PresenceNoMoreDetected) eventToHandle);
                            break;
                    }
                }
            }
        }).start();
    }

    @Override
    public void notifyEvent(Event ev) {
        this.eventQueue.add(ev);
    }

}
