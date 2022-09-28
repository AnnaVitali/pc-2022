package smart_room.centralized;

import smart_room.Controller;
import smart_room.Event;

import java.util.LinkedList;
import java.util.List;

public class AgentSuperLoop extends AbstractAgent {
    private final List<Event> inputs = new LinkedList<>();

    public AgentSuperLoop(){
        boardSimulator.register(this);
        boardSimulator.init();
        start();
    }

    public void start(){
        new Thread(() -> {
            while(true){
                while(!inputs.isEmpty()) {//sensing
                    Event inputToHandle = inputs.remove(0);
                    this.decideWhatToDoAndAct(inputToHandle);
                }
            }
        }).start();
    }

    private void decideWhatToDoAndAct(Event input){
        String className = input.getClass().getSimpleName();
        switch(className) {
            case "LightLevelChanged":
                this.lightLevelChangedHandler((LightLevelChanged) input);
                break;
            case "PresenceDetected":
                this.presenceDetectedHandler((PresenceDetected) input);
                break;
            case "PresenceNoMoreDetected":
                this.presenceNoMoreDetectedHandler((PresenceNoMoreDetected) input);

        }
    }

    @Override
    public void notifyEvent(Event ev) {
        inputs.add(ev);
    }
}
