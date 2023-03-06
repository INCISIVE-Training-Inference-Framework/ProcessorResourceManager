import exceptions.BadConfigurationException;
import platform.PlatformAdapter;
import platform.types.dummy.DummyPlatformAdapter;
import platform.types.incisive.IncisivePlatformAdapter;

import java.util.Map;

import static config.environment.EnvironmentVariable.loadEnvironmentVariables;

public class Factory {

    public static PlatformAdapter selectPlatformAdapter(Map<String, Object> initialConfig) throws BadConfigurationException {
        String communicationAdapterImplementation = (String) initialConfig.get("PLATFORM_ADAPTER");
        switch (communicationAdapterImplementation) {
            case "INCISIVE":
                Map<String, Object> config = loadEnvironmentVariables(IncisivePlatformAdapter.getEnvironmentVariables());
                return new IncisivePlatformAdapter(config);
            case "DUMMY":
                return new DummyPlatformAdapter();
            default:
                throw new BadConfigurationException("Communication adapter implementation unknown: " + communicationAdapterImplementation + ". Available: KAFKA");
        }
    }

}
