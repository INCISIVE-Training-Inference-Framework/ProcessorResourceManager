package config.actions;

import exceptions.BadInputParametersException;
import org.json.JSONException;
import org.json.JSONObject;

public class ActionPrepareInternalData extends Action {

    private final String outputPath;
    private final JSONObject information;

    public ActionPrepareInternalData(String name, String outputPath, JSONObject information) {
        super(name);
        this.outputPath = outputPath;
        this.information = information;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public JSONObject getInformation() {
        return information;
    }

    @Override
    public String toString() {
        return "ActionPrepareInternalData{" +
                "outputPath='" + outputPath + '\'' +
                ", information=" + information +
                '}';
    }

    public static ActionPrepareInternalData parseInputAction(JSONObject inputJson) throws BadInputParametersException {
        try {
            String name = inputJson.getString("name");
            String outputPath = inputJson.getString("output_path");
            JSONObject information = inputJson.getJSONObject("information");
            return new ActionPrepareInternalData(name, outputPath, information);
        } catch (ClassCastException | JSONException e) {
            throw new BadInputParametersException("Action prepare internal data is bad formatted: " + e.getMessage());
        }
    }
}
