package config.actions;

import exceptions.BadInputParametersException;
import org.json.JSONException;
import org.json.JSONObject;

public class ActionDownloadUserVars extends Action {

    private final String userVarsUrl;
    private final String outputPath;

    public ActionDownloadUserVars(String name, String userVarsUrl, String outputPath) {
        super(name);
        this.userVarsUrl = userVarsUrl;
        this.outputPath = outputPath;
    }

    public String getUserVarsUrl() {
        return userVarsUrl;
    }

    public String getOutputPath() {
        return outputPath;
    }

    @Override
    public String toString() {
        return "ActionDownloadUserVars{" +
                "userVarsUrl='" + userVarsUrl + '\'' +
                ", outputPath='" + outputPath + '\'' +
                '}';
    }

    public static ActionDownloadUserVars parseInputAction(JSONObject inputJson) throws BadInputParametersException {
        try {
            String name = inputJson.getString("name");
            String userVarsUrl = inputJson.getString("user_vars_url");
            String outputPath = inputJson.getString("output_path");
            return new ActionDownloadUserVars(name, userVarsUrl, outputPath);
        } catch (ClassCastException | JSONException e) {
            throw new BadInputParametersException("Action download user vars is bad formatted: " + e.getMessage());
        }
    }
}
