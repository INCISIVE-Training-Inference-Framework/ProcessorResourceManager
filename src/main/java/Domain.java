import config.actions.*;
import exceptions.InternalException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import platform.PlatformAdapter;

import java.util.List;

public class Domain {

    private static final Logger logger = LogManager.getLogger(Domain.class);

    private final PlatformAdapter platform;

    public Domain(PlatformAdapter platform) {
        this.platform = platform;
    }

    public void run(List<Action> actions) throws InternalException {
        for (Action action: actions) {
            this.run(action);
        }
    }

    public void run(Action action) throws InternalException {
        logger.info(String.format("Running action \"%s\"", action.getClass().getSimpleName()));

        if (action instanceof ActionDownloadExternalData) this.platform.downloadExternalData((ActionDownloadExternalData) action);
        else if (action instanceof ActionDownloadUserVars) this.platform.downloadUserVars((ActionDownloadUserVars) action);
        else if (action instanceof ActionDownloadAIModel) this.platform.downloadAIModel((ActionDownloadAIModel) action);
        else if (action instanceof ActionCreateDirectory) this.platform.createDirectory((ActionCreateDirectory) action);
        else if (action instanceof ActionPingAIEngine) this.platform.pingAIEngine((ActionPingAIEngine) action);
        else if (action instanceof ActionRunAIEngine) this.platform.runAIEngine((ActionRunAIEngine) action);
        else if (action instanceof ActionEndAIEngine) this.platform.endAIEngine((ActionEndAIEngine) action);
        else if (action instanceof ActionUpdateToRunning) this.platform.updateToRunning((ActionUpdateToRunning) action);
        else if (action instanceof ActionUpdateToSucceeded) this.platform.updateToSucceeded((ActionUpdateToSucceeded) action);
        else if (action instanceof ActionUpdateToFailed) this.platform.updateToFailed((ActionUpdateToFailed) action);
        else if (action instanceof ActionChangeApiHostAndPort) this.platform.changeApiPortAndHost((ActionChangeApiHostAndPort) action);
        else if (action instanceof ActionAddDataProviderInfo) this.platform.addDataProviderInfo((ActionAddDataProviderInfo) action);
        else if (action instanceof ActionPrepareInternalData) this.platform.prepareInternalData((ActionPrepareInternalData) action);
        else throw new InternalException(String.format("Action \"%s\" not implemented", action.getClass().getSimpleName()), null);
    }

}
