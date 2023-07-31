package config.actions;

import exceptions.BadInputParametersException;
import org.json.JSONException;
import org.json.JSONObject;

public class ActionDownloadAIModel extends Action {

    private final String aiModelUrl;
    private final int downloadResumeRetries;
    private final String outputPath;

    public ActionDownloadAIModel(String name, String aiModelUrl, int downloadResumeRetries, String outputPath) {
        super(name);
        this.aiModelUrl = aiModelUrl;
        this.downloadResumeRetries = downloadResumeRetries;
        this.outputPath = outputPath;
    }

    public String getAiModelUrl() {
        return aiModelUrl;
    }

    public int getDownloadResumeRetries() {
        return downloadResumeRetries;
    }

    public String getOutputPath() {
        return outputPath;
    }

    @Override
    public String toString() {
        return "ActionDownloadAIModel{" +
                "aiModelUrl='" + aiModelUrl + '\'' +
                ", downloadResumeRetries=" + downloadResumeRetries +
                ", outputPath='" + outputPath + '\'' +
                '}';
    }

    public static ActionDownloadAIModel parseInputAction(JSONObject inputJson) throws BadInputParametersException {
        try {
            String name = inputJson.getString("name");
            String aiModelUrl = inputJson.getString("ai_model_url");
            int downloadResumeRetries = 3;
            if (inputJson.has("download_resume_retries")) downloadResumeRetries = inputJson.getInt("download_resume_retries");
            String outputPath = inputJson.getString("output_path");
            return new ActionDownloadAIModel(name, aiModelUrl, downloadResumeRetries, outputPath);
        } catch (ClassCastException | JSONException e) {
            throw new BadInputParametersException("Action download AI Model is bad formatted: " + e.getMessage());
        }
    }
}
