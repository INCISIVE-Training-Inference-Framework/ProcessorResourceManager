package platform.types.incisive.actions;

import config.actions.ActionEndAIEngine;
import exceptions.InternalException;

public class ActionEndAIEngineImplementation {

    public static void run(ActionEndAIEngine action) throws InternalException {
        AuxiliaryEndAIEngine endAIEngine = new AuxiliaryEndAIEngine(
                action.getMaxFinalizationTime(),
                action.getMaxFinalizationRetries(),
                action.getClientHost(),
                action.getPingUrl(),
                action.getEndUrl()
        );
        endAIEngine.end();
    }
}
