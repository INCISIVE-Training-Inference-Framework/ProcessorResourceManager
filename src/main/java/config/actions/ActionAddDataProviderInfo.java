package config.actions;

import exceptions.BadInputParametersException;
import org.json.JSONObject;

public class ActionAddDataProviderInfo extends Action {

    private final String readFilePath;
    private final String writeFilePath;
    private final String dataProvider;

    public ActionAddDataProviderInfo(String name, String readFilePath, String writeFilePath, String dataProvider) {
        super(name);
        this.readFilePath = readFilePath;
        this.writeFilePath = writeFilePath;
        this.dataProvider = dataProvider;
    }

    public String getReadFilePath() {
        return readFilePath;
    }

    public String getWriteFilePath() {
        return writeFilePath;
    }

    public String getDataProvider() {
        return dataProvider;
    }

    @Override
    public String toString() {
        return "ActionAddDataProviderInfo{" +
                "readFilePath='" + readFilePath + '\'' +
                ", writeFilePath='" + writeFilePath + '\'' +
                ", dataProvider='" + dataProvider + '\'' +
                '}';
    }

    public static ActionAddDataProviderInfo parseInputAction(JSONObject inputJson) throws BadInputParametersException {
        try {
            String name = inputJson.getString("name");
            String readFilePath = inputJson.getString("read_file_path");
            String writeFilePath = inputJson.getString("write_file_path");
            String dataProvider = inputJson.getString("data_provider");
            if (dataProvider.contains("-rm2")) {
                dataProvider = dataProvider.replaceAll("-rm2", "");  // path that fix issue with nodes
            }
            return new ActionAddDataProviderInfo(name, readFilePath, writeFilePath, dataProvider);
        } catch (Exception e) {
            throw new BadInputParametersException(String.format("Action add data provider info is bad formatted: %s", e.getMessage()));
        }
    }
}
