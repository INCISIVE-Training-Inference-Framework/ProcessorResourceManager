package platform.types.dummy;

import config.actions.*;
import exceptions.InternalException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import platform.PlatformAdapter;

public class DummyPlatformAdapter implements PlatformAdapter {

    private static final Logger logger = LogManager.getLogger(DummyPlatformAdapter.class);

    @Override
    public void downloadPlatformData(ActionDownloadPlatformData action) throws InternalException {}

    @Override
    public void downloadExternalData(ActionDownloadExternalData action) throws InternalException {}

    @Override
    public void prepareInternalData(ActionPrepareInternalData action) throws InternalException {}

    @Override
    public void downloadUserVars(ActionDownloadUserVars action) throws InternalException {}

    @Override
    public void downloadAIModel(ActionDownloadAIModel action) throws InternalException {}

    @Override
    public void createDirectory(ActionCreateDirectory action) throws InternalException {}

    @Override
    public void runAIEngine(ActionRunAIEngine action) throws InternalException {}

    @Override
    public void endAIEngine(ActionEndAIEngine action) throws InternalException {}

    @Override
    public void updateToRunning(ActionUpdateToRunning action) throws InternalException {}

    @Override
    public void updateToFailed(ActionUpdateToFailed action) throws InternalException {}

    @Override
    public void updateToSucceeded(ActionUpdateToSucceeded action) throws InternalException {}

    @Override
    public void changeApiPortAndHost(ActionChangeApiHostAndPort action) throws InternalException {}

    @Override
    public void addDataProviderInfo(ActionAddDataProviderInfo action) throws InternalException {}

}
