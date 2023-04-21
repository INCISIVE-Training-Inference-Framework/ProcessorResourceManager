package config.actions;

import exceptions.BadInputParametersException;
import org.json.JSONObject;

public class ActionChangeApiHostAndPort extends Action {

    private final String readFilePath;
    private final String writeFilePath;
    private final String apiHost;
    private final String apiPort;

    public ActionChangeApiHostAndPort(String name, String readFilePath, String writeFilePath, String apiHost, String apiPort) {
        super(name);
        this.readFilePath = readFilePath;
        this.writeFilePath = writeFilePath;
        this.apiHost = apiHost;
        this.apiPort = apiPort;
    }

    public String getReadFilePath() {
        return readFilePath;
    }

    public String getWriteFilePath() {
        return writeFilePath;
    }

    public String getApiHost() {
        return apiHost;
    }

    public String getApiPort() {
        return apiPort;
    }

    @Override
    public String toString() {
        return "ActionChangeApiHostAndPort{" +
                "readFilePath='" + readFilePath + '\'' +
                ", writeFilePath='" + writeFilePath + '\'' +
                ", apiHost='" + apiHost + '\'' +
                ", apiPort='" + apiPort + '\'' +
                '}';
    }

    public static ActionChangeApiHostAndPort parseInputAction(JSONObject inputJson) throws BadInputParametersException {
        try {
            String name = inputJson.getString("name");
            String readFilePath = inputJson.getString("read_file_path");
            String writeFilePath = inputJson.getString("write_file_path");
            String apiHostAndPort = inputJson.getString("api_host_and_port");
            String apiHost = apiHostAndPort.split(":")[0];
            String apiPort = apiHostAndPort.split(":")[1];
            return new ActionChangeApiHostAndPort(name, readFilePath, writeFilePath, apiHost, apiPort);
        } catch (Exception e) {
            throw new BadInputParametersException(String.format("Action change api host and port is bad formatted: %s", e.getMessage()));
        }
    }
}
