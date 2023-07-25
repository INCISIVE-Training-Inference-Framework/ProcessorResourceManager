import com.github.tomakehurst.wiremock.junit.WireMockRule;
import config.actions.Action;
import config.actions.ActionRunAIEngine;
import exceptions.BadInputParametersException;
import exceptions.InternalException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.*;
import platform.PlatformAdapter;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static config.environment.EnvironmentVariable.loadEnvironmentVariables;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;


public class TestRunAIEngine {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(8001), false);
    public String runAIEngineActionString;
    public ActionRunAIEngine runAIEngineAction;
    public static String testsRootDirectory = "src/test/resources/tmp_run_ai_engine_tests/";

    @BeforeClass
    public static void beforeClass() throws Exception {
        if (Files.exists(Paths.get(testsRootDirectory))) {
            FileUtils.cleanDirectory(Paths.get(testsRootDirectory).toFile());
            FileUtils.deleteDirectory(Paths.get(testsRootDirectory).toFile());
        }
        Files.createDirectory(Paths.get(testsRootDirectory));
    }

    @Before
    public void before() throws Exception {
        // create directory for specific test
        if (Files.exists(Paths.get(testsRootDirectory + "test"))) {
            FileUtils.cleanDirectory(Paths.get(testsRootDirectory + "test").toFile());
            FileUtils.deleteDirectory(Paths.get(testsRootDirectory + "test").toFile());
        }
        Files.createDirectory(Paths.get(testsRootDirectory + "test"));


        // load default input json
        runAIEngineActionString = new String(Files.readAllBytes(Paths.get("src/test/resources/input_configurations/run_ai_engine.json")));
        List<Action> actions = Action.parseInputActions(new JSONObject(runAIEngineActionString));
        runAIEngineAction = (ActionRunAIEngine) actions.get(0);
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
    public void runAIEngineSuccess() throws Exception {
        // create mock
        stubFor(get(urlEqualTo(runAIEngineAction.getPingUrl()))
                .willReturn(aResponse().withStatus(200))
        );
        stubFor(post(urlEqualTo(String.format(
                "%s?use_case=%s&callback_url=%s",
                runAIEngineAction.getRunUrl(),
                runAIEngineAction.getUseCase(),
                String.format(
                        "http://%s%s",
                        runAIEngineAction.getServerHost(),
                        runAIEngineAction.getCallbackUrl()
                )
        ))).willReturn(aResponse().withStatus(200)));

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(4000);
                CloseableHttpClient client = HttpClients.createDefault();
                String callbackUrl = String.format(
                        "http://%s%s", runAIEngineAction.getServerHost(), runAIEngineAction.getCallbackUrl()
                );
                HttpPost httpPost = new HttpPost(callbackUrl);
                String json = "{\"SUCCESS\": true}";
                StringEntity entity = new StringEntity(json);
                httpPost.setEntity(entity);
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");
                client.execute(httpPost);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();

        // run domain
        String[] args = {runAIEngineActionString};
        Application.main(args);
    }


    @Test
    public void runAIEngineFailedPing() throws Exception {

        Exception exception = assertThrows(InternalException.class, () -> {
            String[] args = {runAIEngineActionString};
            Namespace parsedArgs = Application.parseInputArgs(args);
            List<Action> actions = Action.parseInputActions((JSONObject) parsedArgs.get("actions"));
            Map<String, Object> initialConfig = loadEnvironmentVariables(Application.getInitialEnvironmentVariables());
            PlatformAdapter platformAdapter = Factory.selectPlatformAdapter(initialConfig);
            Domain domain = new Domain(platformAdapter);
            domain.run(actions);
        });

        String expectedMessage = "Internal exception: Error while waiting for the AI Engine to be ready (during the query). Incorrect initialization with status code 404";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void runAIEngineFailedRun() throws Exception {
        // create mock
        stubFor(get(urlEqualTo(runAIEngineAction.getPingUrl()))
                .willReturn(aResponse().withStatus(200)));

        Exception exception = assertThrows(InternalException.class, () -> {
            String[] args = {runAIEngineActionString};
            Namespace parsedArgs = Application.parseInputArgs(args);
            List<Action> actions = Action.parseInputActions((JSONObject) parsedArgs.get("actions"));
            Map<String, Object> initialConfig = loadEnvironmentVariables(Application.getInitialEnvironmentVariables());
            PlatformAdapter platformAdapter = Factory.selectPlatformAdapter(initialConfig);
            Domain domain = new Domain(platformAdapter);
            domain.run(actions);
        });

        String expectedMessage = "Internal exception: Error while running use case. Status code equal to 404. Not Found";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void runAIEngineFailedRunNoCallback() throws Exception {
        // create mock
        stubFor(get(urlEqualTo(runAIEngineAction.getPingUrl()))
                .willReturn(aResponse().withStatus(200)));
        stubFor(post(urlEqualTo(String.format(
                "%s?use_case=%s&callback_url=%s",
                runAIEngineAction.getRunUrl(),
                runAIEngineAction.getUseCase(),
                String.format(
                        "http://%s%s",
                        runAIEngineAction.getServerHost(),
                        runAIEngineAction.getCallbackUrl()
                )
        ))).willReturn(aResponse().withStatus(200)));

        Exception exception = assertThrows(InternalException.class, () -> {
            String[] args = {runAIEngineActionString};
            Namespace parsedArgs = Application.parseInputArgs(args);
            List<Action> actions = Action.parseInputActions((JSONObject) parsedArgs.get("actions"));
            Map<String, Object> initialConfig = loadEnvironmentVariables(Application.getInitialEnvironmentVariables());
            PlatformAdapter platformAdapter = Factory.selectPlatformAdapter(initialConfig);
            Domain domain = new Domain(platformAdapter);
            domain.run(actions);
        });

        String expectedMessage = "Internal exception: Error while running use case. The end of the iteration was not notified on time";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void runAIEngineFailedAIEngineResponseBadParsed() throws Exception {
        // create mock
        stubFor(get(urlEqualTo(runAIEngineAction.getPingUrl()))
                .willReturn(aResponse().withStatus(200))
        );
        stubFor(post(urlEqualTo(String.format(
                "%s?use_case=%s&callback_url=%s",
                runAIEngineAction.getRunUrl(),
                runAIEngineAction.getUseCase(),
                String.format(
                        "http://%s%s",
                        runAIEngineAction.getServerHost(),
                        runAIEngineAction.getCallbackUrl()
                )
        ))).willReturn(aResponse().withStatus(200)));

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(4000);
                CloseableHttpClient client = HttpClients.createDefault();
                String callbackUrl = String.format(
                        "http://%s%s", runAIEngineAction.getServerHost(), runAIEngineAction.getCallbackUrl()
                );
                HttpPost httpPost = new HttpPost(callbackUrl);
                String json = "{\"SUCCESS\": false}";
                StringEntity entity = new StringEntity(json);
                httpPost.setEntity(entity);
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");
                client.execute(httpPost);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();

        // run domain
        Exception exception = assertThrows(InternalException.class, () -> {
            String[] args = {runAIEngineActionString};
            Namespace parsedArgs = Application.parseInputArgs(args);
            List<Action> actions = Action.parseInputActions((JSONObject) parsedArgs.get("actions"));
            Map<String, Object> initialConfig = loadEnvironmentVariables(Application.getInitialEnvironmentVariables());
            PlatformAdapter platformAdapter = Factory.selectPlatformAdapter(initialConfig);
            Domain domain = new Domain(platformAdapter);
            domain.run(actions);
        });

        String expectedMessage = "Internal exception: Error while running use case. Error while parsing returning error message";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void runAIEngineFailedAIEngineError() throws Exception {
        // create mock
        stubFor(get(urlEqualTo(runAIEngineAction.getPingUrl()))
                .willReturn(aResponse().withStatus(200))
        );
        stubFor(post(urlEqualTo(String.format(
                "%s?use_case=%s&callback_url=%s",
                runAIEngineAction.getRunUrl(),
                runAIEngineAction.getUseCase(),
                String.format(
                        "http://%s%s",
                        runAIEngineAction.getServerHost(),
                        runAIEngineAction.getCallbackUrl()
                )
        ))).willReturn(aResponse().withStatus(200)));

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(4000);
                CloseableHttpClient client = HttpClients.createDefault();
                String callbackUrl = String.format(
                        "http://%s%s", runAIEngineAction.getServerHost(), runAIEngineAction.getCallbackUrl()
                );
                HttpPost httpPost = new HttpPost(callbackUrl);
                String json = "{\"SUCCESS\": false, \"message\": \"user_vars.json file bad formatted\"}";
                StringEntity entity = new StringEntity(json);
                httpPost.setEntity(entity);
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");
                client.execute(httpPost);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();

        // run domain
        Exception exception = assertThrows(InternalException.class, () -> {
            String[] args = {runAIEngineActionString};
            Namespace parsedArgs = Application.parseInputArgs(args);
            List<Action> actions = Action.parseInputActions((JSONObject) parsedArgs.get("actions"));
            Map<String, Object> initialConfig = loadEnvironmentVariables(Application.getInitialEnvironmentVariables());
            PlatformAdapter platformAdapter = Factory.selectPlatformAdapter(initialConfig);
            Domain domain = new Domain(platformAdapter);
            domain.run(actions);
        });

        String expectedMessage = "Internal exception: Error while running use case. AI Engine error -> user_vars.json file bad formatted";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void runAIEngineBadFormatted() throws Exception {
        // replace config with one that is bad formatted
        JSONObject auxiliaryAction = new JSONObject();
        auxiliaryAction.put("name", "run_ai_engine");
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

        String expectedMessage = "Bad input parameters exception: Action run AI Engine is bad formatted: JSONObject[\"use_case\"] not found.";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

}
