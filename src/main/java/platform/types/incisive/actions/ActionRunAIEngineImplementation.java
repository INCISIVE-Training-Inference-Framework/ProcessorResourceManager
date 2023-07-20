package platform.types.incisive.actions;

import config.actions.ActionRunAIEngine;
import exceptions.InternalException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ActionRunAIEngineImplementation {

    private static final Logger logger = LogManager.getLogger(ActionRunAIEngineImplementation.class);

    public static void run(ActionRunAIEngine action) throws InternalException {
        AuxiliaryRunAIEngine runAIEngine = new AuxiliaryRunAIEngine(
                action.getMaxIterationTime(),
                action.getMaxInitializationTime(),
                action.getClientHost(),
                action.getServerHost(),
                action.getPingUrl(),
                action.getRunUrl(),
                action.getCallbackUrl()
        );
        boolean initialized = false;
        boolean exceptionThrown = false;
        try {
            runAIEngine.initialize();
            initialized = true;
            runAIEngine.waitAIEngineToBeReady();
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
