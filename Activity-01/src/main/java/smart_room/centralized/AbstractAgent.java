package smart_room.centralized;

import smart_room.Controller;
import smart_room.centralized.SinglelBoardSimulator;

public abstract class AbstractAgent implements Controller {

    private static final double T_LEVEL = 0.5;

    private boolean someoneIsInTheRoom = false;
    private double currentLuminosityLevel = 0.0;
    protected final SinglelBoardSimulator boardSimulator = new SinglelBoardSimulator();

    abstract protected void start();

    protected void lightLevelChangedHandler(LightLevelChanged event){
        currentLuminosityLevel = event.getNewLevel();
        if(someoneIsInTheRoom && currentLuminosityLevel < T_LEVEL){
            boardSimulator.on();
        }else{
            boardSimulator.off();
        }
    }

    protected void presenceDetectedHandler(PresenceDetected event){
        someoneIsInTheRoom = true;
        if(currentLuminosityLevel < T_LEVEL){
            boardSimulator.on();
        }
    }

    protected void presenceNoMoreDetectedHandler(PresenceNoMoreDetected event){
        someoneIsInTheRoom = false;
        boardSimulator.off();
    }

}
