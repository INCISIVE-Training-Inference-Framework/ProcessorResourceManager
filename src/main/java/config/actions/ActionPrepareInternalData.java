package config.actions;

import exceptions.BadInputParametersException;
import org.json.JSONException;
import org.json.JSONObject;

public class ActionPrepareInternalData extends Action {

    private final String outputPath;
    private final String informationUrl;

    public ActionPrepareInternalData(String name, String outputPath, String informationUrl) {
        super(name);
        this.outputPath = outputPath;
        this.informationUrl = informationUrl;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public String getInformationUrl() {
        return informationUrl;
    }

    @Override
    public String toString() {
        return "ActionPrepareInternalData{" +
                "outputPath='" + outputPath + '\'' +
                ", informationUrl=" + informationUrl +
                '}';
    }

    public static ActionPrepareInternalData parseInputAction(JSONObject inputJson) throws BadInputParametersException {
        try {
            String name = inputJson.getString("name");
            String outputPath = inputJson.getString("output_path");
            String informationUrl = inputJson.getString("information_url");
            return new ActionPrepareInternalData(name, outputPath, informationUrl);
        } catch (ClassCastException | JSONException e) {
            throw new BadInputParametersException("Action prepare internal data is bad formatted: " + e.getMessage());
        }
    }
}
