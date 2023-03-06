package config.actions;

import exceptions.BadInputParametersException;
import org.json.JSONException;
import org.json.JSONObject;

public class ActionDownloadAIModel extends Action {

    private final String aiModelUrl;
    private final String outputPath;

    public ActionDownloadAIModel(String name, String aiModelUrl, String outputPath) {
        super(name);
        this.aiModelUrl = aiModelUrl;
        this.outputPath = outputPath;
    }

    public String getAiModelUrl() {
        return aiModelUrl;
    }

    public String getOutputPath() {
        return outputPath;
    }

    @Override
    public String toString() {
        return "ActionDownloadAIModel{" +
                "aiModelUrl='" + aiModelUrl + '\'' +
                ", outputPath='" + outputPath + '\'' +
                '}';
    }

    public static ActionDownloadAIModel parseInputAction(JSONObject inputJson) throws BadInputParametersException {
        try {
            String name = inputJson.getString("name");
            String aiModelUrl = inputJson.getString("ai_model_url");
            String outputPath = inputJson.getString("output_path");
            return new ActionDownloadAIModel(name, aiModelUrl, outputPath);
        } catch (ClassCastException | JSONException e) {
            throw new BadInputParametersException("Action download AI Model is bad formatted: " + e.getMessage());
        }
    }
}
