import config.actions.*;
import exceptions.InternalException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import platform.PlatformAdapter;

import java.util.List;

public class Domain {

    private static final Logger logger = LogManager.getLogger(Domain.class);

    private final List<Action> actions;
    private final PlatformAdapter platform;

    public Domain(List<Action> actions, PlatformAdapter platform) {
        this.actions = actions;
        this.platform = platform;
    }

    public void run() throws InternalException {
        for (Action action: this.actions) {
            logger.info(String.format("Running action \"%s\"", action.getClass().getSimpleName()));

            if (action instanceof ActionDownloadPlatformData) this.platform.downloadPlatformData((ActionDownloadPlatformData) action);
            else if (action instanceof ActionDownloadExternalData) this.platform.downloadExternalData((ActionDownloadExternalData) action);
            else if (action instanceof ActionDownloadUserVars) this.platform.downloadUserVars((ActionDownloadUserVars) action);
            else if (action instanceof ActionDownloadAIModel) this.platform.downloadAIModel((ActionDownloadAIModel) action);
            else if (action instanceof ActionCreateDirectory) this.platform.createDirectory((ActionCreateDirectory) action);
            else if (action instanceof ActionRunAIEngine) this.platform.runAIEngine((ActionRunAIEngine) action);
            else if (action instanceof ActionUpdateToRunning) this.platform.updateToRunning((ActionUpdateToRunning) action);
            else if (action instanceof ActionUpdateToSucceeded) this.platform.updateToSucceeded((ActionUpdateToSucceeded) action);
            else throw new InternalException(String.format("Action \"%s\" not implemented", action.getClass().getSimpleName()), null);

        }
    }

}
