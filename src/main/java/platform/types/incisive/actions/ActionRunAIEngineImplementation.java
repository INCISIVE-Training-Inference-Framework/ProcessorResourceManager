package platform.types.incisive.actions;

import config.actions.ActionPingAIEngine;
import config.actions.ActionRunAIEngine;
import exceptions.InternalException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ActionRunAIEngineImplementation {

    private static final Logger logger = LogManager.getLogger(ActionRunAIEngineImplementation.class);

    public static void run(ActionRunAIEngine action) throws InternalException {
        ActionPingAIEngine actionPingAIEngine = new ActionPingAIEngine(
                "ping_ai_engine",
                action.getMaxInitializationTime(),
                action.getClientHost(),
                action.getPingUrl()
        );
        AuxiliaryRunAIEngine runAIEngine = new AuxiliaryRunAIEngine(
                action.getMaxIterationTime(),
                action.getClientHost(),
                action.getServerHost(),
                action.getRunUrl(),
                action.getCallbackUrl()
        );
        boolean initialized = false;
        boolean exceptionThrown = false;
        try {
            runAIEngine.initialize();
            initialized = true;
            ActionPingAIEngineImplementation.run(actionPingAIEngine);
            runAIEngine.run(action.getUseCase());
        } catch (Exception e){
            exceptionThrown = true;
            throw e;
        } finally {
            try {
                if (initialized) runAIEngine.clean();
            } catch (Exception e) {
                if (exceptionThrown) {
                    logger.error("Intermediate exception");
                    e.printStackTrace();
                } else throw e;
            }
        }
    }
}
