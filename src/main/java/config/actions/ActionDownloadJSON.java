package config.actions;

import exceptions.BadInputParametersException;
import org.json.JSONException;
import org.json.JSONObject;

public class ActionDownloadJSON extends Action {

    private final String JSONUrl;
    private final int downloadResumeRetries;
    private final String outputPath;

    public ActionDownloadJSON(String name, String JSONUrl, int downloadResumeRetries, String outputPath) {
        super(name);
        this.JSONUrl = JSONUrl;
        this.downloadResumeRetries = downloadResumeRetries;
        this.outputPath = outputPath;
    }

    public String getJSONUrl() {
        return JSONUrl;
    }

    public int getDownloadResumeRetries() {
        return downloadResumeRetries;
    }

    public String getOutputPath() {
        return outputPath;
    }

    @Override
    public String toString() {
        return "ActionDownloadJSON{" +
                "JSONUrl='" + JSONUrl + '\'' +
                ", downloadResumeRetries=" + downloadResumeRetries +
                ", outputPath='" + outputPath + '\'' +
                '}';
    }

    public static ActionDownloadJSON parseInputAction(JSONObject inputJson) throws BadInputParametersException {
        try {
            String name = inputJson.getString("name");
            String JSONUrl = inputJson.getString("json_url");
            int downloadResumeRetries = 3;
            if (inputJson.has("download_resume_retries")) downloadResumeRetries = inputJson.getInt("download_resume_retries");
            String outputPath = inputJson.getString("output_path");
            return new ActionDownloadJSON(name, JSONUrl, downloadResumeRetries, outputPath);
        } catch (ClassCastException | JSONException e) {
            throw new BadInputParametersException("Action download user vars is bad formatted: " + e.getMessage());
        }
    }
}
