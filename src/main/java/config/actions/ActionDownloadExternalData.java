package config.actions;

import exceptions.BadInputParametersException;
import org.json.JSONException;
import org.json.JSONObject;

public class ActionDownloadExternalData extends Action {

    private final String externalDataUrl;
    private final String outputPath;

    public ActionDownloadExternalData(String name, String externalDataUrl, String outputPath) {
        super(name);
        this.externalDataUrl = externalDataUrl;
        this.outputPath = outputPath;
    }

    public String getExternalDataUrl() {
        return externalDataUrl;
    }

    public String getOutputPath() {
        return outputPath;
    }

    @Override
    public String toString() {
        return "ActionDownloadExternalData{" +
                "externalDataUrl='" + externalDataUrl + '\'' +
                ", outputPath='" + outputPath + '\'' +
                '}';
    }

    public static ActionDownloadExternalData parseInputAction(JSONObject inputJson) throws BadInputParametersException {
        try {
            String name = inputJson.getString("name");
            String externalDataUrl = inputJson.getString("external_data_url");
            String outputPath = inputJson.getString("output_path");
            return new ActionDownloadExternalData(name, externalDataUrl, outputPath);
        } catch (ClassCastException | JSONException e) {
            throw new BadInputParametersException("Action download external data is bad formatted: " + e.getMessage());
        }
    }
}
