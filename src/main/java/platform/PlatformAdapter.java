package platform;

import config.actions.*;
import exceptions.InternalException;

public interface PlatformAdapter {

    void downloadPlatformData(ActionDownloadPlatformData action) throws InternalException;

    void downloadExternalData(ActionDownloadExternalData action) throws InternalException;

    void downloadUserVars(ActionDownloadUserVars action) throws InternalException;

    void downloadAIModel(ActionDownloadAIModel action) throws InternalException;

    void createDirectory(ActionCreateDirectory action) throws InternalException;

    void runAIEngine(ActionRunAIEngine action) throws InternalException;

    void endAIEngine(ActionEndAIEngine action) throws InternalException;

    void updateToRunning(ActionUpdateToRunning action) throws InternalException;

    void updateToFailed(ActionUpdateToFailed action) throws InternalException;

    void updateToSucceeded(ActionUpdateToSucceeded action) throws InternalException;

    void changeApiPortAndHost(ActionChangeApiHostAndPort action) throws InternalException;

    void addDataProviderInfo(ActionAddDataProviderInfo action) throws InternalException;

}
