package platform.types.incisive.actions;

import config.actions.ActionUpdateToSucceeded;
import exceptions.InternalException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static utils.FileMethods.readJson;
import static utils.HttpMethods.*;
import static utils.ZipCompression.zipFile;

public class ActionUpdateToSucceededImplementation {

    private static final Logger logger = LogManager.getLogger(ActionUpdateToSucceededImplementation.class);

    public static void run(ActionUpdateToSucceeded action) throws InternalException {
        JSONObject entity = new JSONObject();
        List<Integer> createdAIModels = new ArrayList<>();
        List<Integer> createdEvaluationMetrics = new ArrayList<>();
        List<Integer> createdGenericFiles = new ArrayList<>();

        InternalException exceptionThrown = null;
        try {
            if (action.isUploadAIModel()) {
                logger.debug("Uploading AI Model");
                byte[] modelBytes;
                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    zipFile(action.getAiModelUploadPath(), outputStream);
                    modelBytes = outputStream.toByteArray();
                } catch (IOException e) {
                    throw new InternalException("Error while retrieving AI Model", e);
                }

                Path userVarsPath = Paths.get(action.getAiModelUserVarsPath());
                byte[] userVarsBytes = Files.readAllBytes(userVarsPath);

                List<String> fileNameList = new ArrayList<>();
                List<byte[]> fileEntityList = new ArrayList<>();
                fileNameList.add("contents");
                fileNameList.add("ai_engine_version_user_vars");
                fileEntityList.add(modelBytes);
                fileEntityList.add(userVarsBytes);

                Set<Integer> expectedStatusCode = new HashSet<>();
                expectedStatusCode.add(200);
                expectedStatusCode.add(201);
                JSONObject responseContent = postMultipartMethod(
                        action.getAiModelUploadUrl(),
                        action.getAiModelUploadMetadata(),
                        fileNameList,
                        fileEntityList,
                        expectedStatusCode,
                        "Error while uploading AI Model"
                );

                JSONObject aiModelEntity = new JSONObject();
                aiModelEntity.put("ai_model", responseContent.getInt("id"));
                entity.put("ai_model", aiModelEntity);
                createdAIModels.add(responseContent.getInt("id"));
            }

            if (action.isUploadEvaluationMetrics()) {
                logger.debug("Uploading Evaluation Metrics");

                JSONArray outputEvaluationMetricsArray = new JSONArray();
                JSONArray evaluationMetricsArray = new JSONArray();
                JSONObject evaluationMetricsGeneral;

                if (action.isEvaluationMetricMultiple()) {
                    List<Path> result;
                    try (Stream<Path> walk = Files.walk(Paths.get(action.getEvaluationMetricsUploadPath()))) {
                        result = walk
                                .filter(Files::isReadable)
                                .filter(Files::isRegularFile)
                                .filter(file -> file.getFileName().toString().endsWith(".json"))
                                .collect(Collectors.toList());
                        for (Path path : result) {
                            try (InputStream inputStream = new FileInputStream(path.toFile())) {
                                evaluationMetricsGeneral = readJson(inputStream);
                                JSONArray evaluationMetricsArrayTemp = evaluationMetricsGeneral.getJSONArray("evaluation_metrics");
                                JSONObject dataPartnersPatients = new JSONObject(String.format("{\"%s\": [\"null\"]}", path.getFileName().toString().replaceAll(".json", "")));
                                for (Object item: evaluationMetricsArrayTemp) {
                                    JSONObject itemJson = (JSONObject) item;
                                    itemJson.put("data_partners_patients", dataPartnersPatients);
                                    evaluationMetricsArray.put(itemJson);
                                }
                            }
                        }
                    }
                } else {
                    try (InputStream inputStream = new FileInputStream(action.getEvaluationMetricsUploadPath())) {
                        evaluationMetricsGeneral = readJson(inputStream);
                    }
                    evaluationMetricsArray = evaluationMetricsGeneral.getJSONArray("evaluation_metrics");
                }
                for (int i = 0; i < evaluationMetricsArray.length(); i++) {
                    JSONObject evaluationMetricEntity = new JSONObject(action.getEvaluationMetricUploadMetadata().toString());
                    if (action.isUploadAIModel()) evaluationMetricEntity.put("ai_model", entity.getJSONObject("ai_model").getInt("ai_model"));
                    else evaluationMetricEntity.put("ai_model", action.getEvaluationMetricAIModel());
                    JSONObject evaluationMetric = evaluationMetricsArray.getJSONObject(i);
                    evaluationMetricEntity.put("name", evaluationMetric.get("name"));
                    evaluationMetricEntity.put("value", evaluationMetric.get("value"));
                    if (evaluationMetric.has("description")) evaluationMetricEntity.put("description", evaluationMetric.getString("description"));
                    if (evaluationMetric.has("data_partners_patients")) evaluationMetricEntity.put("data_partners_patients", evaluationMetric.getJSONObject("data_partners_patients"));

                    Set<Integer> expectedStatusCode = new HashSet<>();
                    expectedStatusCode.add(200);
                    expectedStatusCode.add(201);
                    JSONObject responseContent = postJsonMethod(
                            action.getEvaluationMetricUploadUrl(),
                            evaluationMetricEntity,
                            expectedStatusCode,
                            "Error while uploading Evaluation Metric"
                    );
                    JSONObject outputEvaluationMetric = new JSONObject();
                    outputEvaluationMetric.put("evaluation_metric", responseContent.getInt("id"));
                    outputEvaluationMetricsArray.put(outputEvaluationMetric);
                    createdEvaluationMetrics.add(responseContent.getInt("id"));
                }
                entity.put("evaluation_metrics", outputEvaluationMetricsArray);
            }

            if (action.isUploadGenericFile()) {
                logger.debug("Uploading Generic File");
                byte[] genericFileBytes;
                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    zipFile(action.getGenericFileUploadPath(), outputStream);
                    genericFileBytes = outputStream.toByteArray();
                } catch (IOException e) {
                    throw new InternalException("Error while retrieving Generic File", e);
                }

                List<String> fileNameList = new ArrayList<>();
                List<byte[]> fileEntityList = new ArrayList<>();
                fileNameList.add("contents");
                fileEntityList.add(genericFileBytes);

                Set<Integer> expectedStatusCode = new HashSet<>();
                expectedStatusCode.add(201);

                JSONObject responseContent = postMultipartMethod(
                        action.getGenericFileUploadUrl(),
                        action.getGenericFileUploadMetadata(),
                        fileNameList,
                        fileEntityList,
                        expectedStatusCode,
                        "Error while uploading Generic File"
                );

                JSONObject aiModelEntity = new JSONObject();
                aiModelEntity.put("generic_file", responseContent.getInt("id"));
                entity.put("generic_file", aiModelEntity);
                createdGenericFiles.add(responseContent.getInt("id"));
            }

            Set<Integer> expectedStatusCode = new HashSet<>();
            expectedStatusCode.add(200);
            patchMultipartMethod(
                    action.getUpdateStatusUrl(),
                    entity,
                    new ArrayList<>(),
                    new ArrayList<>(),
                    expectedStatusCode,
                    "Error while updating status to succeeded during the final step"
            );
        } catch (JSONException | IOException e) {
            exceptionThrown = new InternalException("Error while updating status to succeeded", e);
            throw exceptionThrown;
        } catch (InternalException e2) {
            exceptionThrown = e2;
            throw e2;
        } finally {
            if (exceptionThrown != null) {
                logger.error("Deleting already created elements");
                Set<Integer> expectedStatusCode = new HashSet<>();
                expectedStatusCode.add(204);
                for (int createdAIModelId : createdAIModels) {
                    logger.error(String.format("AI Model %d", createdAIModelId));
                    String url = String.format("%s/%d/", action.getAiModelDeleteUrl(), createdAIModelId);
                    try {
                        deleteMethod(url, expectedStatusCode, "Error while deleting AI Model");
                    } catch (InternalException e) {
                        e.print(logger);
                    }
                }
                for (int createdEvaluationMetricId : createdEvaluationMetrics) {
                    logger.error(String.format("Evaluation Metric %d", createdEvaluationMetricId));
                    String url = String.format("%s/%d/", action.getEvaluationMetricDeleteUrl(), createdEvaluationMetricId);
                    try {
                        deleteMethod(url, expectedStatusCode, "Error while deleting Evaluation Metric");
                    } catch (InternalException e) {
                        e.print(logger);
                    }
                }
                for (int createdGenericFileId : createdGenericFiles) {
                    logger.error(String.format("Generic File %d", createdGenericFileId));
                    String url = String.format("%s/%d/", action.getGenericFileDeleteUrl(), createdGenericFileId);
                    try {
                        deleteMethod(url, expectedStatusCode, "Error while deleting Generic File");
                    } catch (InternalException e) {
                        e.print(logger);
                    }
                }
            }
        }
    }
}