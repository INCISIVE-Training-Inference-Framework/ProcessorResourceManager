package platform.types.incisive;

import config.actions.*;
import config.environment.EnvironmentVariable;
import exceptions.InternalException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import platform.PlatformAdapter;
import utils.ZipCompression;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static utils.FileMethods.readJson;
import static utils.HttpMethods.*;
import static utils.ZipCompression.zipFile;

public class IncisivePlatformAdapter implements PlatformAdapter {

    public static List<EnvironmentVariable> getEnvironmentVariables() {
        return new ArrayList<>();
    }

    private static final Logger logger = LogManager.getLogger(IncisivePlatformAdapter.class);

    public IncisivePlatformAdapter(Map<String, Object> config) {}

    @Override
    public void downloadPlatformData(ActionDownloadPlatformData action) throws InternalException {
        throw new InternalException("Download platform data method is not implemented", null);
    }

    @Override
    public void downloadExternalData(ActionDownloadExternalData action) throws InternalException {
        String temporalZippedFilePath = String.format("%s/tmp_zip_file", action.getOutputPath());
        try (FileOutputStream outputStream = new FileOutputStream(temporalZippedFilePath)) {
            downloadFile(action.getExternalDataUrl(), outputStream);
        } catch (IOException e) {
            throw new InternalException("Error while downloading external data", e);
        }

        try (FileInputStream inputStream = new FileInputStream(temporalZippedFilePath)) {
            ZipCompression.unZipFile(inputStream, Paths.get(action.getOutputPath()));
        } catch (IOException e) {
            throw new InternalException("Error while unzipping external data", e);
        }

        try {
            Files.delete(Paths.get(temporalZippedFilePath));
        } catch (IOException e) {
            throw new InternalException("Error while cleaning temporary external data", e);
        }
    }

    @Override
    public void downloadUserVars(ActionDownloadUserVars action) throws InternalException {
        try (FileOutputStream outputStream = new FileOutputStream(action.getOutputPath())) {
            downloadFile(action.getUserVarsUrl(), outputStream);
        } catch (IOException e) {
            throw new InternalException("Error while downloading user vars", e);
        }
    }

    @Override
    public void downloadAIModel(ActionDownloadAIModel action) throws InternalException {
        String temporalZippedFilePath = String.format("%s/tmp_zip_file", action.getOutputPath());

        try (FileOutputStream outputStream = new FileOutputStream(temporalZippedFilePath)) {
            downloadFile(action.getAiModelUrl(), outputStream);
        } catch (IOException e) {
            throw new InternalException("Error while downloading AI Model", e);
        }

        try (FileInputStream inputStream = new FileInputStream(temporalZippedFilePath)) {
            ZipCompression.unZipFile(inputStream, Paths.get(action.getOutputPath()));
        } catch (IOException e) {
            throw new InternalException("Error while unzipping AI Model", e);
        }

        try {
            Files.delete(Paths.get(temporalZippedFilePath));
        } catch (IOException e) {
            throw new InternalException("Error while cleaning temporary AI Model", e);
        }
    }

    @Override
    public void createDirectory(ActionCreateDirectory action) throws InternalException {
        try {
            Files.createDirectories(Paths.get(action.getDirectoryPath()));
        } catch (IOException e) {
            throw new InternalException("Error while creating directory", e);
        }
    }

    @Override
    public void runAIEngine(ActionRunAIEngine action) throws InternalException {
        RunAIEngine runAIEngine = new RunAIEngine(
                action.getMaxIterationTime(),
                action.getMaxInitializationTime(),
                action.getClientHost(),
                action.getServerHost(),
                action.getPingUrl(),
                action.getRunUrl(),
                action.getCallbackUrl()
        );
        boolean initialized = false;
        boolean exceptionThrown = false;
        try {
            runAIEngine.initialize();
            initialized = true;
            runAIEngine.waitAIEngineToBeReady();
            runAIEngine.run(action.getUseCase());
        } catch (Exception e){
           exceptionThrown = true;
           throw e;
        } finally {
            try {
                if (initialized) runAIEngine.clean();
            } catch (Exception e) {
                if (exceptionThrown) {
                    logger.error("Intermediate exception");
                    e.printStackTrace();
                } else throw e;
            }
        }
    }

    @Override
    public void endAIEngine(ActionEndAIEngine action) throws InternalException {
        EndAIEngine endAIEngine = new EndAIEngine(
                action.getMaxFinalizationTime(),
                action.getMaxFinalizationRetries(),
                action.getClientHost(),
                action.getPingUrl(),
                action.getEndUrl()
        );
        endAIEngine.end();
    }

    @Override
    public void updateToRunning(ActionUpdateToRunning action) throws InternalException {
        Set<Integer> expectedStatusCode = new HashSet<>();
        expectedStatusCode.add(200);
        patchMultipartMethod(
                action.getUpdateStatusUrl(),
                new JSONObject("{}"),
                new ArrayList<>(),
                new ArrayList<>(),
                expectedStatusCode,
                "Error while updating status to running"
        );
    }

    @Override
    public void updateToFailed(ActionUpdateToFailed action) throws InternalException {
        JSONObject entity = new JSONObject();
        entity.put("message", action.getMessage());
        Set<Integer> expectedStatusCode = new HashSet<>();
        expectedStatusCode.add(200);
        patchMultipartMethod(
                action.getUpdateStatusUrl(),
                entity,
                new ArrayList<>(),
                new ArrayList<>(),
                expectedStatusCode,
                "Error while updating status to failed"
        );
    }

    @Override
    public void updateToSucceeded(ActionUpdateToSucceeded action) throws InternalException {
        JSONObject entity = new JSONObject();
        List<Integer> createdAIModels = new ArrayList<>();
        List<Integer> createdEvaluationMetrics = new ArrayList<>();
        List<Integer> createdGenericFiles = new ArrayList<>();

        InternalException exceptionThrown = null;
        try {
            if (action.isUploadAIModel()) {
                logger.debug("Uploading AI Model");
                byte[] modelBytes;
                File modelDirectory = new File(action.getAiModelUploadPath());
                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    zipFile(modelDirectory, modelDirectory.getName(), outputStream);
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
                File modelDirectory = new File(action.getGenericFileUploadPath());
                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    zipFile(modelDirectory, modelDirectory.getName(), outputStream);
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

    @Override
    public void changeApiPortAndHost(ActionChangeApiHostAndPort action) throws InternalException {
        JSONObject configuration;
        try (InputStream inputStream = new FileInputStream(action.getReadFilePath())) {
            configuration = readJson(inputStream);
            configuration.put("api_host", action.getApiHost());
            configuration.put("api_port", action.getApiPort());
        } catch (IOException e) {
            throw new InternalException("Error while loading configuration file", e);
        }

        try (FileWriter file = new FileWriter(action.getWriteFilePath())) {
            file.write(configuration.toString());
        } catch (IOException e) {
        throw new InternalException("Error while updating configuration file", e);
        }
    }

}
