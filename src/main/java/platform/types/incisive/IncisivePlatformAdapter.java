package platform.types.incisive;

import config.actions.*;
import config.environment.EnvironmentVariable;
import exceptions.InternalException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import platform.PlatformAdapter;
import platform.types.incisive.actions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IncisivePlatformAdapter implements PlatformAdapter {

    public static List<EnvironmentVariable> getEnvironmentVariables() {
        return new ArrayList<>();
    }

    private static final Logger logger = LogManager.getLogger(IncisivePlatformAdapter.class);

    public IncisivePlatformAdapter(Map<String, Object> config) {}

    @Override
    public void downloadExternalData(ActionDownloadExternalData action) throws InternalException {
        ActionDownloadExternalDataImplementation.run(action);
    }

    @Override
    public void prepareInternalData(ActionPrepareInternalData action) throws InternalException {
        ActionPrepareInternalDataImplementation.run(action);
    }

    @Override
    public void downloadUserVars(ActionDownloadUserVars action) throws InternalException {
        ActionDownloadUserVarsImplementation.run(action);
    }

    @Override
    public void downloadAIModel(ActionDownloadAIModel action) throws InternalException {
        ActionDownloadAIModelImplementation.run(action);
    }

    @Override
    public void createDirectory(ActionCreateDirectory action) throws InternalException {
        ActionCreateDirectoryImplementation.run(action);
    }

    @Override
    public void pingAIEngine(ActionPingAIEngine action) throws InternalException {
        ActionPingAIEngineImplementation.run(action);
    }

    @Override
    public void runAIEngine(ActionRunAIEngine action) throws InternalException {
        ActionRunAIEngineImplementation.run(action);
    }

    @Override
    public void endAIEngine(ActionEndAIEngine action) throws InternalException {
        ActionEndAIEngineImplementation.run(action);
    }

    @Override
    public void updateToRunning(ActionUpdateToRunning action) throws InternalException {
        ActionUpdateToRunningImplementation.run(action);
    }

    @Override
    public void updateToFailed(ActionUpdateToFailed action) throws InternalException {
        ActionUpdateToFailedImplementation.run(action);
    }

    @Override
    public void updateToSucceeded(ActionUpdateToSucceeded action) throws InternalException {
        ActionUpdateToSucceededImplementation.run(action);
    }

    @Override
    public void changeApiPortAndHost(ActionChangeApiHostAndPort action) throws InternalException {
        ActionChangeApiPortAndHostImplementation.run(action);
    }

    @Override
    public void addDataProviderInfo(ActionAddDataProviderInfo action) throws InternalException {
        ActionAddDataProviderInfoImplementation.run(action);
    }

}
