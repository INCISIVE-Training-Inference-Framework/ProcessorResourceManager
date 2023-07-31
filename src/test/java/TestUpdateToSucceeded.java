import com.github.tomakehurst.wiremock.junit.WireMockRule;
import config.actions.Action;
import exceptions.BadInputParametersException;
import exceptions.InternalException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.*;
import platform.PlatformAdapter;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static config.environment.EnvironmentVariable.loadEnvironmentVariables;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class TestUpdateToSucceeded {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(8000), false);
    public static String testsRootDirectory = "src/test/resources/tmp_update_to_succeeded_tests/";

    @BeforeClass
    public static void beforeClass() throws Exception {
        if (Files.exists(Paths.get(testsRootDirectory))) {
            FileUtils.cleanDirectory(Paths.get(testsRootDirectory).toFile());
            FileUtils.deleteDirectory(Paths.get(testsRootDirectory).toFile());
        }
        Files.createDirectory(Paths.get(testsRootDirectory));

        // create dummy AI Model
        Files.createDirectory(Paths.get(testsRootDirectory + "dummy_ai_model"));
        Files.createFile(Paths.get(testsRootDirectory + "dummy_ai_model/model.pth"));

        // create dummy AI Engine Version user vars
        JSONObject userVarsJson = new JSONObject();
        userVarsJson.put("epochs", 2);
        userVarsJson.put("mode", "birads");
        try(PrintWriter userVarsFile = new PrintWriter(testsRootDirectory + "dummy_user_vars.json", StandardCharsets.UTF_8)) {
            userVarsFile.println(userVarsJson.toString(4));
        }

        // create dummy Evaluation Metrics
        JSONObject evaluationMetric1 = new JSONObject("{\"name\": \"accuracy\", \"value\": 0, \"description\": \"dummy description\"}");
        JSONObject evaluationMetric2 = new JSONObject("{\"name\": \"f1-score\", \"value\": 0}");
        JSONArray evaluationMetricsArray = new JSONArray();
        evaluationMetricsArray.put(evaluationMetric1);
        evaluationMetricsArray.put(evaluationMetric2);
        JSONObject evaluationMetricsJson = new JSONObject();
        evaluationMetricsJson.put("evaluation_metrics", evaluationMetricsArray);
        try(PrintWriter evaluationMetricsFile = new PrintWriter(testsRootDirectory + "dummy_evaluation_metrics.json", StandardCharsets.UTF_8)) {
            evaluationMetricsFile.println(evaluationMetricsJson.toString(4));
        }

        // create dummy Generic File
        Files.createDirectory(Paths.get(testsRootDirectory + "dummy_generic_file"));
        Files.createFile(Paths.get(testsRootDirectory + "dummy_generic_file/image1.png"));
        Files.createFile(Paths.get(testsRootDirectory + "dummy_generic_file/image2.png"));
    }

    @Before
    public void before() throws Exception {
        // create directory for specific test
        if (Files.exists(Paths.get(testsRootDirectory + "test"))) {
            FileUtils.cleanDirectory(Paths.get(testsRootDirectory + "test").toFile());
            FileUtils.deleteDirectory(Paths.get(testsRootDirectory + "test").toFile());
        }
        Files.createDirectory(Paths.get(testsRootDirectory + "test"));
    }

    @After
    public void after() throws Exception {
        // clean test environment
        FileUtils.cleanDirectory(Paths.get(testsRootDirectory + "test").toFile());
        FileUtils.deleteDirectory(Paths.get(testsRootDirectory + "test").toFile());
    }

    @AfterClass
    public static void afterClass() throws Exception {
        // clean test environment
        FileUtils.cleanDirectory(Paths.get(testsRootDirectory).toFile());
        FileUtils.deleteDirectory(Paths.get(testsRootDirectory).toFile());
    }

    @Test
    public void updateToSucceededUploadAIModelSuccess() throws Exception {
        // load default input json
        String updateToSucceededActionString = new String(Files.readAllBytes(Paths.get(
                "src/test/resources/input_configurations/update_to_succeeded_upload_ai_model.json"
        )));

        // create mock
        stubFor(post(urlEqualTo("/api/upload_ai_model/"))
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("data")
                                .withBody(equalToJson(
                                        """
                                        {
                                            "ai_engine_version": 1,
                                            "name": "test",
                                            "data_partners_patients": {},
                                            "description": "dummy description",
                                            "merge_type": "default"
                                        }
                                        """
                                ))
                )
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("ai_engine_version_user_vars")
                                .withHeader("Content-Type", containing("application/octet-stream"))
                )
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("contents")
                                .withHeader("Content-Type", containing("application/octet-stream"))
                )
                .willReturn(aResponse().withStatus(200).withBody("{\"id\": 1}")));
        stubFor(patch(urlEqualTo("/api/update_status/"))
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("data")
                                .withBody(equalToJson(
                                        """
                                        {
                                            "ai_model": {
                                                "ai_model": 1
                                            }
                                        }
                                        """
                                ))
                )
                .willReturn(aResponse().withStatus(200).withBody("{}")));

        // run domain
        String[] args = {updateToSucceededActionString};
        Application.main(args);
    }

    @Test
    public void updateToSucceededUploadEvaluationMetricsSuccess() throws Exception {
        // load default input json
        String updateToSucceededActionString = new String(Files.readAllBytes(Paths.get(
                "src/test/resources/input_configurations/update_to_succeeded_upload_evaluation_metrics.json"
        )));

        // create mock
        stubFor(post(urlEqualTo("/api/upload_evaluation_metric/"))
                .withRequestBody(equalToJson(
                        """
                        {
                            "ai_model": 2,
                            "name": "accuracy",
                            "data_partners_patients": {},
                            "value": 0,
                            "description": "dummy description"
                        }
                        """
                ))
                .willReturn(aResponse().withStatus(200).withBody("{\"id\": 3}")));
        stubFor(post(urlEqualTo("/api/upload_evaluation_metric/"))
                .withRequestBody(equalToJson(
                        """
                        {
                            "ai_model": 2,
                            "name": "f1-score",
                            "data_partners_patients": {},
                            "value": 0
                        }
                        """
                ))
                .willReturn(aResponse().withStatus(200).withBody("{\"id\": 4}")));
        stubFor(patch(urlEqualTo("/api/update_status/"))
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("data")
                                .withBody(equalToJson(
                                        """
                                        {
                                            "evaluation_metrics": [
                                                {"evaluation_metric": 3},
                                                {"evaluation_metric": 4}
                                            ]
                                        }
                                        """
                                ))
                )
                .willReturn(aResponse().withStatus(200).withBody("{}")));

        // run domain
        String[] args = {updateToSucceededActionString};
        Application.main(args);
    }

    @Test
    public void updateToSucceededUploadEvaluationMetricsMultipleSuccess() throws Exception {
        // load default input json
        String updateToSucceededActionString = new String(Files.readAllBytes(Paths.get(
                "src/test/resources/input_configurations/update_to_succeeded_upload_evaluation_metrics_multiple.json"
        )));

        // create dummy Evaluation Metrics
        Files.createDirectory(Paths.get(testsRootDirectory + "/evaluation_metrics/"));
        JSONObject evaluationMetric1 = new JSONObject("{\"name\": \"accuracy\", \"value\": 0, \"description\": \"dummy description\"}");
        JSONObject evaluationMetric2 = new JSONObject("{\"name\": \"f1-score\", \"value\": 0}");
        JSONObject evaluationMetric3 = new JSONObject("{\"name\": \"f2-score\", \"value\": 0}");
        JSONArray evaluationMetricsArray = new JSONArray();
        evaluationMetricsArray.put(evaluationMetric1);
        evaluationMetricsArray.put(evaluationMetric2);
        JSONObject evaluationMetricsJson = new JSONObject();
        evaluationMetricsJson.put("evaluation_metrics", evaluationMetricsArray);
        try(PrintWriter evaluationMetricsFile = new PrintWriter(testsRootDirectory + "/evaluation_metrics/data-partner-1.json", StandardCharsets.UTF_8)) {
            evaluationMetricsFile.println(evaluationMetricsJson.toString(4));
        }
        evaluationMetricsArray = new JSONArray();
        evaluationMetricsArray.put(evaluationMetric3);
        evaluationMetricsJson = new JSONObject();
        evaluationMetricsJson.put("evaluation_metrics", evaluationMetricsArray);
        try(PrintWriter evaluationMetricsFile = new PrintWriter(testsRootDirectory + "/evaluation_metrics/data-partner-2.json", StandardCharsets.UTF_8)) {
            evaluationMetricsFile.println(evaluationMetricsJson.toString(4));
        }

        // create mock
        stubFor(post(urlEqualTo("/api/upload_evaluation_metric/"))
                .withRequestBody(equalToJson(
                        """
                        {
                            "ai_model": 3,
                            "name": "accuracy",
                            "data_partners_patients": {"data-partner-1": ["null"]},
                            "value": 0,
                            "description": "dummy description"
                        }
                        """
                ))
                .willReturn(aResponse().withStatus(200).withBody("{\"id\": 3}")));
        stubFor(post(urlEqualTo("/api/upload_evaluation_metric/"))
                .withRequestBody(equalToJson(
                        """
                        {
                            "ai_model": 3,
                            "name": "f1-score",
                            "data_partners_patients": {"data-partner-1": ["null"]},
                            "value": 0
                        }
                        """
                ))
                .willReturn(aResponse().withStatus(200).withBody("{\"id\": 4}")));
        stubFor(post(urlEqualTo("/api/upload_evaluation_metric/"))
                .withRequestBody(equalToJson(
                        """
                        {
                            "ai_model": 3,
                            "name": "f2-score",
                            "data_partners_patients": {"data-partner-2": ["null"]},
                            "value": 0
                        }
                        """
                ))
                .willReturn(aResponse().withStatus(200).withBody("{\"id\": 5}")));
        stubFor(patch(urlEqualTo("/api/update_status/"))
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("data")
                                .withBody(equalToJson(
                                        """
                                        {
                                            "evaluation_metrics": [
                                                {"evaluation_metric": 3},
                                                {"evaluation_metric": 4},
                                                {"evaluation_metric": 5}
                                            ]
                                        }
                                        """
                                ))
                )
                .willReturn(aResponse().withStatus(200).withBody("{}")));

        // run domain
        String[] args = {updateToSucceededActionString};
        Application.main(args);
    }

    @Test
    public void updateToSucceededUploadAIModelAlongEvaluationMetricsSuccess() throws Exception {
        // load default input json
        String updateToSucceededActionString = new String(Files.readAllBytes(Paths.get(
                "src/test/resources/input_configurations/update_to_succeeded_upload_ai_model_along_evaluation_metrics.json"
        )));

        // create mock
        stubFor(post(urlEqualTo("/api/upload_ai_model/"))
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("data")
                                .withBody(equalToJson(
                                        """
                                        {
                                            "ai_engine_version": 1,
                                            "name": "test",
                                            "data_partners_patients": {},
                                            "description": "dummy description",
                                            "merge_type": "default"
                                        }
                                        """
                                ))
                )
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("ai_engine_version_user_vars")
                                .withHeader("Content-Type", containing("application/octet-stream"))
                )
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("contents")
                                .withHeader("Content-Type", containing("application/octet-stream"))
                )
                .willReturn(aResponse().withStatus(200).withBody("{\"id\": 1}")));
        stubFor(post(urlEqualTo("/api/upload_evaluation_metric/"))
                .withRequestBody(equalToJson(
                        """
                        {
                            "ai_model": 1,
                            "name": "accuracy",
                            "data_partners_patients": {},
                            "value": 0,
                            "description": "dummy description"
                        }
                        """
                ))
                .willReturn(aResponse().withStatus(200).withBody("{\"id\": 3}")));
        stubFor(post(urlEqualTo("/api/upload_evaluation_metric/"))
                .withRequestBody(equalToJson(
                        """
                        {
                            "ai_model": 1,
                            "name": "f1-score",
                            "data_partners_patients": {},
                            "value": 0
                        }
                        """
                ))
                .willReturn(aResponse().withStatus(200).withBody("{\"id\": 4}")));
        stubFor(patch(urlEqualTo("/api/update_status/"))
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("data")
                                .withBody(equalToJson(
                                        """
                                        {
                                            "ai_model": {
                                                "ai_model": 1
                                            },
                                            "evaluation_metrics": [
                                                {"evaluation_metric": 3},
                                                {"evaluation_metric": 4}
                                            ]
                                        }
                                        """
                                ))
                )
                .willReturn(aResponse().withStatus(200).withBody("{}")));

        // run domain
        String[] args = {updateToSucceededActionString};
        Application.main(args);
    }

    @Test
    public void updateToSucceededUploadAIModelAlongEvaluationMetricsMultipleSuccess() throws Exception {
        // load default input json
        String updateToSucceededActionString = new String(Files.readAllBytes(Paths.get(
                "src/test/resources/input_configurations/update_to_succeeded_upload_ai_model_along_evaluation_metrics_multiple.json"
        )));

        // create dummy Evaluation Metrics
        Files.createDirectories(Paths.get(testsRootDirectory + "/evaluation_metrics/"));
        JSONObject evaluationMetric1 = new JSONObject("{\"name\": \"accuracy\", \"value\": 0, \"description\": \"dummy description\"}");
        JSONObject evaluationMetric2 = new JSONObject("{\"name\": \"f1-score\", \"value\": 0}");
        JSONObject evaluationMetric3 = new JSONObject("{\"name\": \"f2-score\", \"value\": 0}");
        JSONArray evaluationMetricsArray = new JSONArray();
        evaluationMetricsArray.put(evaluationMetric1);
        evaluationMetricsArray.put(evaluationMetric2);
        JSONObject evaluationMetricsJson = new JSONObject();
        evaluationMetricsJson.put("evaluation_metrics", evaluationMetricsArray);
        try(PrintWriter evaluationMetricsFile = new PrintWriter(testsRootDirectory + "/evaluation_metrics/data-partner-1.json", StandardCharsets.UTF_8)) {
            evaluationMetricsFile.println(evaluationMetricsJson.toString(4));
        }
        evaluationMetricsArray = new JSONArray();
        evaluationMetricsArray.put(evaluationMetric3);
        evaluationMetricsJson = new JSONObject();
        evaluationMetricsJson.put("evaluation_metrics", evaluationMetricsArray);
        try(PrintWriter evaluationMetricsFile = new PrintWriter(testsRootDirectory + "/evaluation_metrics/data-partner-2.json", StandardCharsets.UTF_8)) {
            evaluationMetricsFile.println(evaluationMetricsJson.toString(4));
        }

        // create mock
        stubFor(post(urlEqualTo("/api/upload_ai_model/"))
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("data")
                                .withBody(equalToJson(
                                        """
                                        {
                                            "ai_engine_version": 1,
                                            "name": "test",
                                            "data_partners_patients": {},
                                            "description": "dummy description",
                                            "merge_type": "default"
                                        }
                                        """
                                ))
                )
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("ai_engine_version_user_vars")
                                .withHeader("Content-Type", containing("application/octet-stream"))
                )
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("contents")
                                .withHeader("Content-Type", containing("application/octet-stream"))
                )
                .willReturn(aResponse().withStatus(200).withBody("{\"id\": 1}")));
        stubFor(post(urlEqualTo("/api/upload_evaluation_metric/"))
                .withRequestBody(equalToJson(
                        """
                        {
                            "ai_model": 1,
                            "name": "accuracy",
                            "data_partners_patients": {"data-partner-1": ["null"]},
                            "value": 0,
                            "description": "dummy description"
                        }
                        """
                ))
                .willReturn(aResponse().withStatus(200).withBody("{\"id\": 3}")));
        stubFor(post(urlEqualTo("/api/upload_evaluation_metric/"))
                .withRequestBody(equalToJson(
                        """
                        {
                            "ai_model": 1,
                            "name": "f1-score",
                            "data_partners_patients": {"data-partner-1": ["null"]},
                            "value": 0
                        }
                        """
                ))
                .willReturn(aResponse().withStatus(200).withBody("{\"id\": 4}")));
        stubFor(post(urlEqualTo("/api/upload_evaluation_metric/"))
                .withRequestBody(equalToJson(
                        """
                        {
                            "ai_model": 1,
                            "name": "f2-score",
                            "data_partners_patients": {"data-partner-2": ["null"]},
                            "value": 0
                        }
                        """
                ))
                .willReturn(aResponse().withStatus(200).withBody("{\"id\": 5}")));
        stubFor(patch(urlEqualTo("/api/update_status/"))
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("data")
                                .withBody(equalToJson(
                                        """
                                        {
                                            "ai_model": {
                                                "ai_model": 1
                                            },
                                            "evaluation_metrics": [
                                                {"evaluation_metric": 3},
                                                {"evaluation_metric": 4},
                                                {"evaluation_metric": 5}
                                            ]
                                        }
                                        """
                                ))
                )
                .willReturn(aResponse().withStatus(200).withBody("{}")));

        // run domain
        String[] args = {updateToSucceededActionString};
        Application.main(args);
    }

    @Test
    public void updateToSucceededUploadGenericFileSuccess() throws Exception {
        // load default input json
        String updateToSucceededActionString = new String(Files.readAllBytes(Paths.get(
                "src/test/resources/input_configurations/update_to_succeeded_upload_generic_file.json"
        )));

        // create mock
        stubFor(post(urlEqualTo("/api/upload_generic_file/"))
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("data")
                                .withBody(equalToJson(
                                        """
                                        {
                                            "name": "execution 1"
                                        }
                                        """
                                ))
                )
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("contents")
                                .withHeader("Content-Type", containing("application/octet-stream"))
                )
                .willReturn(aResponse().withStatus(201).withBody("{\"id\": 5}")));
        stubFor(patch(urlEqualTo("/api/update_status/"))
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("data")
                                .withBody(equalToJson(
                                        """
                                        {
                                            "generic_file": {
                                                "generic_file": 5
                                            }
                                        }
                                        """
                                ))
                )
                .willReturn(aResponse().withStatus(200).withBody("{}")));

        // run domain
        String[] args = {updateToSucceededActionString};
        Application.main(args);
    }

    @Test
    public void updateToSucceededUploadAIModelFailed() throws Exception {
        // load default input json
        String updateToSucceededActionString = new String(Files.readAllBytes(Paths.get(
                "src/test/resources/input_configurations/update_to_succeeded_upload_ai_model.json"
        )));

        // create mock
        stubFor(post(urlEqualTo("/api/upload_ai_model/"))
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("data")
                                .withBody(equalToJson(
                                        """
                                        {
                                            "ai_engine_version": 1,
                                            "name": "test",
                                            "data_partners_patients": {},
                                            "description": "dummy description",
                                            "merge_type": "default"
                                        }
                                        """
                                ))
                )
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("ai_engine_version_user_vars")
                                .withHeader("Content-Type", containing("application/octet-stream"))
                )
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("contents")
                                .withHeader("Content-Type", containing("application/octet-stream"))
                )
                .willReturn(aResponse().withStatus(400).withBody("{\"id\": 1}")));

        // run domain
        Exception exception = assertThrows(InternalException.class, () -> {
            String[] args = {updateToSucceededActionString};
            Namespace parsedArgs = Application.parseInputArgs(args);
            List<Action> actions = Action.parseInputActions((JSONObject) parsedArgs.get("actions"));
            Map<String, Object> initialConfig = loadEnvironmentVariables(Application.getInitialEnvironmentVariables());
            PlatformAdapter platformAdapter = Factory.selectPlatformAdapter(initialConfig);
            Domain domain = new Domain(platformAdapter);
            domain.run(actions);
        });

        String expectedMessage = "Internal exception: Error while uploading AI Model. Wrong response status code. Expected: [200, 201]. Actual: 400. Bad Request";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void updateToSucceededUploadAIModelAlongEvaluationMetricsFailed() throws Exception {
        // load default input json
        String updateToSucceededActionString = new String(Files.readAllBytes(Paths.get(
                "src/test/resources/input_configurations/update_to_succeeded_upload_ai_model_along_evaluation_metrics.json"
        )));

        // create mock
        stubFor(post(urlEqualTo("/api/upload_ai_model/"))
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("data")
                                .withBody(equalToJson(
                                        """
                                        {
                                            "ai_engine_version": 1,
                                            "name": "test",
                                            "data_partners_patients": {},
                                            "description": "dummy description",
                                            "merge_type": "default"
                                        }
                                        """
                                ))
                )
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("ai_engine_version_user_vars")
                                .withHeader("Content-Type", containing("application/octet-stream"))
                )
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("contents")
                                .withHeader("Content-Type", containing("application/octet-stream"))
                )
                .willReturn(aResponse().withStatus(200).withBody("{\"id\": 1}")));
        stubFor(post(urlEqualTo("/api/upload_evaluation_metric/"))
                .withRequestBody(equalToJson(
                        """
                        {
                            "ai_model": 1,
                            "name": "accuracy",
                            "data_partners_patients": {},
                            "value": 0,
                            "description": "dummy description"
                        }
                        """
                ))
                .willReturn(aResponse().withStatus(400).withBody("{\"id\": 3}")));

        // run domain
        Exception exception = assertThrows(InternalException.class, () -> {
            String[] args = {updateToSucceededActionString};
            Namespace parsedArgs = Application.parseInputArgs(args);
            List<Action> actions = Action.parseInputActions((JSONObject) parsedArgs.get("actions"));
            Map<String, Object> initialConfig = loadEnvironmentVariables(Application.getInitialEnvironmentVariables());
            PlatformAdapter platformAdapter = Factory.selectPlatformAdapter(initialConfig);
            Domain domain = new Domain(platformAdapter);
            domain.run(actions);
        });

        String expectedMessage = "Internal exception: Error while uploading Evaluation Metric. Wrong response status code. Expected: [200, 201]. Actual: 400. Bad Request";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void updateToSucceededUploadGenericFileFailed() throws Exception {
        // load default input json
        String updateToSucceededActionString = new String(Files.readAllBytes(Paths.get(
                "src/test/resources/input_configurations/update_to_succeeded_upload_generic_file.json"
        )));

        // create mock
        stubFor(post(urlEqualTo("/api/upload_generic_file/"))
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("data")
                                .withBody(equalToJson(
                                        """
                                        {
                                            "name": "execution 1"
                                        }
                                        """
                                ))
                )
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("contents")
                                .withHeader("Content-Type", containing("application/octet-stream"))
                )
                .willReturn(aResponse().withStatus(400).withBody("{\"id\": 5}")));

        // run domain
        Exception exception = assertThrows(InternalException.class, () -> {
            String[] args = {updateToSucceededActionString};
            Namespace parsedArgs = Application.parseInputArgs(args);
            List<Action> actions = Action.parseInputActions((JSONObject) parsedArgs.get("actions"));
            Map<String, Object> initialConfig = loadEnvironmentVariables(Application.getInitialEnvironmentVariables());
            PlatformAdapter platformAdapter = Factory.selectPlatformAdapter(initialConfig);
            Domain domain = new Domain(platformAdapter);
            domain.run(actions);
        });

        String expectedMessage = "Internal exception: Error while uploading Generic File. Wrong response status code. Expected: [201]. Actual: 400. Bad Request";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void updateToSucceededFailed() throws Exception {
        // load default input json
        String updateToSucceededActionString = new String(Files.readAllBytes(Paths.get(
                "src/test/resources/input_configurations/update_to_succeeded_upload_generic_file.json"
        )));

        // create mock
        stubFor(post(urlEqualTo("/api/upload_generic_file/"))
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("data")
                                .withBody(equalToJson(
                                        """
                                        {
                                            "name": "execution 1"
                                        }
                                        """
                                ))
                )
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("contents")
                                .withHeader("Content-Type", containing("application/octet-stream"))
                )
                .willReturn(aResponse().withStatus(201).withBody("{\"id\": 5}")));
        stubFor(patch(urlEqualTo("/api/update_status/"))
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("data")
                                .withBody(equalToJson(
                                        """
                                        {
                                            "generic_file": {
                                                "generic_file": 5
                                            }
                                        }
                                        """
                                ))
                )
                .willReturn(aResponse().withStatus(400).withBody("{}")));

        // run domain
        Exception exception = assertThrows(InternalException.class, () -> {
            String[] args = {updateToSucceededActionString};
            Namespace parsedArgs = Application.parseInputArgs(args);
            List<Action> actions = Action.parseInputActions((JSONObject) parsedArgs.get("actions"));
            Map<String, Object> initialConfig = loadEnvironmentVariables(Application.getInitialEnvironmentVariables());
            PlatformAdapter platformAdapter = Factory.selectPlatformAdapter(initialConfig);
            Domain domain = new Domain(platformAdapter);
            domain.run(actions);
        });

        String expectedMessage = "Internal exception: Error while updating status to succeeded during the final step. Wrong response status code. Expected: [200]. Actual: 400. Bad Request";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void updateToSucceededAllSuccess() throws Exception {
        // load default input json
        String updateToSucceededActionString = new String(Files.readAllBytes(Paths.get(
                "src/test/resources/input_configurations/update_to_succeeded_all.json"
        )));

        // create mock
        stubFor(post(urlEqualTo("/api/upload_ai_model/"))
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("data")
                                .withBody(equalToJson(
                                        """
                                        {
                                            "ai_engine_version": 1,
                                            "name": "test",
                                            "data_partners_patients": {},
                                            "description": "dummy description",
                                            "merge_type": "default"
                                        }
                                        """
                                ))
                )
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("ai_engine_version_user_vars")
                                .withHeader("Content-Type", containing("application/octet-stream"))
                )
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("contents")
                                .withHeader("Content-Type", containing("application/octet-stream"))
                )
                .willReturn(aResponse().withStatus(200).withBody("{\"id\": 1}")));
        stubFor(post(urlEqualTo("/api/upload_evaluation_metric/"))
                .withRequestBody(equalToJson(
                        """
                        {
                            "ai_model": 1,
                            "name": "accuracy",
                            "data_partners_patients": {},
                            "value": 0,
                            "description": "dummy description"
                        }
                        """
                ))
                .willReturn(aResponse().withStatus(200).withBody("{\"id\": 3}")));
        stubFor(post(urlEqualTo("/api/upload_evaluation_metric/"))
                .withRequestBody(equalToJson(
                        """
                        {
                            "ai_model": 1,
                            "name": "f1-score",
                            "data_partners_patients": {},
                            "value": 0
                        }
                        """
                ))
                .willReturn(aResponse().withStatus(200).withBody("{\"id\": 4}")));
        stubFor(post(urlEqualTo("/api/upload_generic_file/"))
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("data")
                                .withBody(equalToJson(
                                        """
                                        {
                                            "name": "execution 1"
                                        }
                                        """
                                ))
                )
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("contents")
                                .withHeader("Content-Type", containing("application/octet-stream"))
                )
                .willReturn(aResponse().withStatus(201).withBody("{\"id\": 5}")));
        stubFor(patch(urlEqualTo("/api/update_status/"))
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("data")
                                .withBody(equalToJson(
                                        """
                                        {
                                            "ai_model": {
                                                "ai_model": 1
                                            },
                                            "evaluation_metrics": [
                                                {"evaluation_metric": 3},
                                                {"evaluation_metric": 4}
                                            ],
                                            "generic_file": {
                                                "generic_file": 5
                                            }
                                        }
                                        """
                                ))
                )
                .willReturn(aResponse().withStatus(200).withBody("{}")));

        // run domain
        String[] args = {updateToSucceededActionString};
        Application.main(args);

        verify(exactly(1), postRequestedFor(urlEqualTo("/api/upload_ai_model/")));
        verify(exactly(2), postRequestedFor(urlEqualTo("/api/upload_evaluation_metric/")));
        verify(exactly(1), postRequestedFor(urlEqualTo("/api/upload_generic_file/")));
        verify(exactly(1), patchRequestedFor(urlEqualTo("/api/update_status/")));
    }

    @Test
    public void updateToSucceededAllFailedWithGoodRollBack() throws Exception {
        // load default input json
        String updateToSucceededActionString = new String(Files.readAllBytes(Paths.get(
                "src/test/resources/input_configurations/update_to_succeeded_all.json"
        )));

        // create mock
        stubFor(post(urlEqualTo("/api/upload_ai_model/"))
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("data")
                                .withBody(equalToJson(
                                        """
                                        {
                                            "ai_engine_version": 1,
                                            "name": "test",
                                            "data_partners_patients": {},
                                            "description": "dummy description",
                                            "merge_type": "default"
                                        }
                                        """
                                ))
                )
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("ai_engine_version_user_vars")
                                .withHeader("Content-Type", containing("application/octet-stream"))
                )
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("contents")
                                .withHeader("Content-Type", containing("application/octet-stream"))
                )
                .willReturn(aResponse().withStatus(200).withBody("{\"id\": 1}")));
        stubFor(post(urlEqualTo("/api/upload_evaluation_metric/"))
                .withRequestBody(equalToJson(
                        """
                        {
                            "ai_model": 1,
                            "name": "accuracy",
                            "data_partners_patients": {},
                            "value": 0,
                            "description": "dummy description"
                        }
                        """
                ))
                .willReturn(aResponse().withStatus(200).withBody("{\"id\": 3}")));
        stubFor(post(urlEqualTo("/api/upload_evaluation_metric/"))
                .withRequestBody(equalToJson(
                        """
                        {
                            "ai_model": 1,
                            "name": "f1-score",
                            "data_partners_patients": {},
                            "value": 0
                        }
                        """
                ))
                .willReturn(aResponse().withStatus(200).withBody("{\"id\": 4}")));
        stubFor(post(urlEqualTo("/api/upload_generic_file/"))
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("data")
                                .withBody(equalToJson(
                                        """
                                        {
                                            "name": "execution 1"
                                        }
                                        """
                                ))
                )
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("contents")
                                .withHeader("Content-Type", containing("application/octet-stream"))
                )
                .willReturn(aResponse().withStatus(201).withBody("{\"id\": 5}")));
        stubFor(patch(urlEqualTo("/api/update_status/"))
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("data")
                                .withBody(equalToJson(
                                        """
                                        {
                                            "ai_model": {
                                                "ai_model": 1
                                            },
                                            "evaluation_metrics": [
                                                {"evaluation_metric": 3},
                                                {"evaluation_metric": 4}
                                            ],
                                            "generic_file": {
                                                "generic_file": 5
                                            }
                                        }
                                        """
                                ))
                )
                .willReturn(aResponse().withStatus(400).withBody("{}")));

        // run domain
        Exception exception = assertThrows(InternalException.class, () -> {
            String[] args = {updateToSucceededActionString};
            Namespace parsedArgs = Application.parseInputArgs(args);
            List<Action> actions = Action.parseInputActions((JSONObject) parsedArgs.get("actions"));
            Map<String, Object> initialConfig = loadEnvironmentVariables(Application.getInitialEnvironmentVariables());
            PlatformAdapter platformAdapter = Factory.selectPlatformAdapter(initialConfig);
            Domain domain = new Domain(platformAdapter);
            domain.run(actions);
        });

        String expectedMessage = "Internal exception: Error while updating status to succeeded during the final step. Wrong response status code. Expected: [200]. Actual: 400. Bad Request";
        assertTrue(exception.getMessage().contains(expectedMessage));

        verify(exactly(1), deleteRequestedFor(urlEqualTo("/api/delete_ai_model/1/")));
        verify(exactly(1), deleteRequestedFor(urlEqualTo("/api/delete_evaluation_metric/3/")));
        verify(exactly(1), deleteRequestedFor(urlEqualTo("/api/delete_evaluation_metric/4/")));
        verify(exactly(1), deleteRequestedFor(urlEqualTo("/api/delete_generic_file/5/")));
    }

    @Test
    public void updateToSucceededBadFormatted() throws Exception {
        // replace config with one that is bad formatted
        JSONObject auxiliaryAction = new JSONObject();
        auxiliaryAction.put("name", "update_to_succeeded");
        JSONArray auxiliaryActions = new JSONArray();
        auxiliaryActions.put(auxiliaryAction);
        JSONObject downloadExternalDataActionBadFormatted = new JSONObject();
        downloadExternalDataActionBadFormatted.put("actions", auxiliaryActions);

        Exception exception = assertThrows(BadInputParametersException.class, () -> {
            String[] args = {downloadExternalDataActionBadFormatted.toString()};
            Namespace parsedArgs = Application.parseInputArgs(args);
            List<Action> actions = Action.parseInputActions((JSONObject) parsedArgs.get("actions"));
            Map<String, Object> initialConfig = loadEnvironmentVariables(Application.getInitialEnvironmentVariables());
            PlatformAdapter platformAdapter = Factory.selectPlatformAdapter(initialConfig);
            Domain domain = new Domain(platformAdapter);
            domain.run(actions);
        });

        String expectedMessage = "Bad input parameters exception: Action update to succeeded is bad formatted: JSONObject[\"update_status_url\"] not found.";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

}
