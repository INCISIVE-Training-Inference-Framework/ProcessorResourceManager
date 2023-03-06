package config.actions;

import exceptions.BadInputParametersException;
import org.json.JSONException;
import org.json.JSONObject;

public class ActionCreateDirectory extends Action {

    private final String directoryPath;

    public ActionCreateDirectory(String name, String directoryPath) {
        super(name);
        this.directoryPath = directoryPath;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    @Override
    public String toString() {
        return "ActionCreateDirectory{" +
                "directoryPath='" + directoryPath + '\'' +
                '}';
    }

    public static ActionCreateDirectory parseInputAction(JSONObject inputJson) throws BadInputParametersException {
        try {
            String name = inputJson.getString("name");
            String directoryPath = inputJson.getString("directory_path");
            return new ActionCreateDirectory(name, directoryPath);
        } catch (ClassCastException | JSONException e) {
            throw new BadInputParametersException(String.format("Action create directory is bad formatted: %s", e.getMessage()));
        }
    }
}
