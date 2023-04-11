package config.actions;

import exceptions.BadInputParametersException;
import org.json.JSONException;
import org.json.JSONObject;

public class ActionUpdateToSucceeded extends Action {

    private final String updateStatusUrl;

    private final boolean uploadAIModel;
    private final boolean uploadEvaluationMetrics;
    private final boolean uploadGenericFile;

    private final String aiModelUploadUrl;
    private final String aiModelDeleteUrl;
    private final String aiModelUploadPath;
    private final String aiModelUserVarsPath;
    private final JSONObject aiModelUploadMetadata;

    private final Integer evaluationMetricAIModel;
    private final String evaluationMetricUploadUrl;
    private final String evaluationMetricDeleteUrl;
    private final String evaluationMetricsUploadPath;
    private final JSONObject evaluationMetricUploadMetadata;

    private final String genericFileUploadUrl;
    private final String genericFileDeleteUrl;
    private final String genericFileUploadPath;
    private final JSONObject genericFileUploadMetadata;

    public ActionUpdateToSucceeded(
            String name,
            String updateStatusUrl,
            boolean uploadAIModel,
            boolean uploadEvaluationMetrics,
            boolean uploadGenericFile,
            String aiModelUploadUrl,
            String aiModelDeleteUrl,
            String aiModelUploadPath,
            String aiModelUserVarsPath,
            JSONObject aiModelUploadMetadata,
            Integer evaluationMetricAIModel,
            String evaluationMetricUploadUrl,
            String evaluationMetricDeleteUrl,
            String evaluationMetricsUploadPath,
            JSONObject evaluationMetricUploadMetadata,
            String genericFileUploadUrl,
            String genericFileDeleteUrl,
            String genericFileUploadPath,
            JSONObject genericFileUploadMetadata
    ) {
        super(name);
        this.updateStatusUrl = updateStatusUrl;
        this.uploadAIModel = uploadAIModel;
        this.uploadEvaluationMetrics = uploadEvaluationMetrics;
        this.uploadGenericFile = uploadGenericFile;
        this.aiModelUploadUrl = aiModelUploadUrl;
        this.aiModelDeleteUrl = aiModelDeleteUrl;
        this.aiModelUploadPath = aiModelUploadPath;
        this.aiModelUserVarsPath = aiModelUserVarsPath;
        this.aiModelUploadMetadata = aiModelUploadMetadata;
        this.evaluationMetricAIModel = evaluationMetricAIModel;
        this.evaluationMetricUploadUrl = evaluationMetricUploadUrl;
        this.evaluationMetricDeleteUrl = evaluationMetricDeleteUrl;
        this.evaluationMetricsUploadPath = evaluationMetricsUploadPath;
        this.evaluationMetricUploadMetadata = evaluationMetricUploadMetadata;
        this.genericFileUploadUrl = genericFileUploadUrl;
        this.genericFileDeleteUrl = genericFileDeleteUrl;
        this.genericFileUploadPath = genericFileUploadPath;
        this.genericFileUploadMetadata = genericFileUploadMetadata;
    }

    public String getUpdateStatusUrl() {
        return updateStatusUrl;
    }

    public boolean isUploadAIModel() {
        return uploadAIModel;
    }

    public boolean isUploadEvaluationMetrics() {
        return uploadEvaluationMetrics;
    }

    public boolean isUploadGenericFile() {
        return uploadGenericFile;
    }

    public String getAiModelUploadUrl() {
        return aiModelUploadUrl;
    }

    public String getAiModelDeleteUrl() {
        return aiModelDeleteUrl;
    }

    public String getAiModelUploadPath() {
        return aiModelUploadPath;
    }

    public String getAiModelUserVarsPath() {
        return aiModelUserVarsPath;
    }

    public JSONObject getAiModelUploadMetadata() {
        return aiModelUploadMetadata;
    }

    public Integer getEvaluationMetricAIModel() {
        return evaluationMetricAIModel;
    }

    public String getEvaluationMetricUploadUrl() {
        return evaluationMetricUploadUrl;
    }

    public String getEvaluationMetricDeleteUrl() {
        return evaluationMetricDeleteUrl;
    }

    public String getEvaluationMetricsUploadPath() {
        return evaluationMetricsUploadPath;
    }

    public JSONObject getEvaluationMetricUploadMetadata() {
        return evaluationMetricUploadMetadata;
    }

    public String getGenericFileUploadUrl() {
        return genericFileUploadUrl;
    }

    public String getGenericFileDeleteUrl() {
        return genericFileDeleteUrl;
    }

    public String getGenericFileUploadPath() {
        return genericFileUploadPath;
    }

    public JSONObject getGenericFileUploadMetadata() {
        return genericFileUploadMetadata;
    }

    @Override
    public String toString() {
        return "ActionUpdateToSucceeded{" +
                "updateStatusUrl='" + updateStatusUrl + '\'' +
                ", uploadAIModel=" + uploadAIModel +
                ", uploadEvaluationMetrics=" + uploadEvaluationMetrics +
                ", uploadGenericFile=" + uploadGenericFile +
                ", aiModelUploadUrl='" + aiModelUploadUrl + '\'' +
                ", aiModelDeleteUrl='" + aiModelDeleteUrl + '\'' +
                ", aiModelUploadPath='" + aiModelUploadPath + '\'' +
                ", aiModelUserVarsPath='" + aiModelUserVarsPath + '\'' +
                ", aiModelUploadMetadata=" + aiModelUploadMetadata +
                ", evaluationMetricAIModel=" + evaluationMetricAIModel +
                ", evaluationMetricUploadUrl='" + evaluationMetricUploadUrl + '\'' +
                ", evaluationMetricDeleteUrl='" + evaluationMetricDeleteUrl + '\'' +
                ", evaluationMetricsUploadPath='" + evaluationMetricsUploadPath + '\'' +
                ", evaluationMetricUploadMetadata=" + evaluationMetricUploadMetadata +
                ", genericFileUploadUrl='" + genericFileUploadUrl + '\'' +
                ", genericFileDeleteUrl='" + genericFileDeleteUrl + '\'' +
                ", genericFileUploadPath='" + genericFileUploadPath + '\'' +
                ", genericFileUploadMetadata=" + genericFileUploadMetadata +
                '}';
    }

    public static ActionUpdateToSucceeded parseInputAction(JSONObject inputJson) throws BadInputParametersException {
        try {
            String name = inputJson.getString("name");
            String updateStatusUrl = inputJson.getString("update_status_url");
            boolean uploadAIModel = inputJson.getBoolean("upload_ai_model");
            boolean uploadEvaluationMetrics = inputJson.getBoolean("upload_evaluation_metrics");
            boolean uploadGenericFile = inputJson.getBoolean("upload_generic_file");
            String aiModelUploadUrl = null;
            String aiModelDeleteUrl = null;
            String aiModelUploadPath = null;
            String aiModelUserVarsPath = null;
            JSONObject aiModelUploadMetadata = null;
            Integer evaluationMetricAIModel = null;
            String evaluationMetricUploadUrl = null;
            String evaluationMetricDeleteUrl = null;
            String evaluationMetricsUploadPath = null;
            JSONObject evaluationMetricUploadMetadata = null;
            String genericFileUploadUrl = null;
            String genericFileDeleteUrl = null;
            String genericFileUploadPath = null;
            JSONObject genericFileUploadMetadata = null;
            if (uploadAIModel) {
                aiModelUploadUrl = inputJson.getString("ai_model_upload_url");
                aiModelDeleteUrl = inputJson.getString("ai_model_delete_url");
                aiModelUploadPath = inputJson.getString("ai_model_upload_path");
                aiModelUserVarsPath = inputJson.getString("ai_model_user_vars_path");
                aiModelUploadMetadata = inputJson.getJSONObject("ai_model_upload_metadata");
            }
            if (uploadEvaluationMetrics) {
                if (!uploadAIModel) {
                    evaluationMetricAIModel = inputJson.getInt("evaluation_metrics_ai_model");
                }
                evaluationMetricUploadUrl = inputJson.getString("evaluation_metrics_upload_url");
                evaluationMetricDeleteUrl = inputJson.getString("evaluation_metrics_delete_url");
                evaluationMetricsUploadPath = inputJson.getString("evaluation_metrics_upload_path");
                evaluationMetricUploadMetadata = inputJson.getJSONObject("evaluation_metrics_upload_metadata");
            }
            if (uploadGenericFile) {
                genericFileUploadUrl = inputJson.getString("generic_file_upload_url");
                genericFileDeleteUrl = inputJson.getString("generic_file_delete_url");
                genericFileUploadPath = inputJson.getString("generic_file_upload_path");
                genericFileUploadMetadata = inputJson.getJSONObject("generic_file_upload_metadata");
            }
            return new ActionUpdateToSucceeded(
                    name,
                    updateStatusUrl,
                    uploadAIModel,
                    uploadEvaluationMetrics,
                    uploadGenericFile,
                    aiModelUploadUrl,
                    aiModelDeleteUrl,
                    aiModelUploadPath,
                    aiModelUserVarsPath,
                    aiModelUploadMetadata,
                    evaluationMetricAIModel,
                    evaluationMetricUploadUrl,
                    evaluationMetricDeleteUrl,
                    evaluationMetricsUploadPath,
                    evaluationMetricUploadMetadata,
                    genericFileUploadUrl,
                    genericFileDeleteUrl,
                    genericFileUploadPath,
                    genericFileUploadMetadata
            );
        } catch (ClassCastException | JSONException e) {
            throw new BadInputParametersException(String.format("Action update to succeeded is bad formatted: %s", e.getMessage()));
        }
    }
}
