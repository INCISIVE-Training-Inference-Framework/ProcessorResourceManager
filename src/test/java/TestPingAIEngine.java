import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import config.actions.Action;
import config.actions.ActionPingAIEngine;
import exceptions.BadInputParametersException;
import exceptions.InternalException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.io.FileUtils;
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


public class TestPingAIEngine {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(8001), false);
    public String pingAIEngineActionString;
    public ActionPingAIEngine pingAIEngineAction;
    public static String testsRootDirectory = "src/test/resources/tmp_ping_ai_engine_tests/";

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
        pingAIEngineActionString = new String(Files.readAllBytes(Paths.get("src/test/resources/input_configurations/ping_ai_engine.json")));
        List<Action> actions = Action.parseInputActions(new JSONObject(pingAIEngineActionString));
        pingAIEngineAction = (ActionPingAIEngine) actions.get(0);
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
    public void pingAIEngineSuccess() throws Exception {
        // create mock
        stubFor(get(urlEqualTo(pingAIEngineAction.getPingUrl()))
                .willReturn(aResponse().withStatus(200))
        );

        // run domain
        String[] args = {pingAIEngineActionString};
        Application.main(args);
    }

    @Test
    public void pingAIEngineFailedBadCode() throws Exception {

        Exception exception = assertThrows(InternalException.class, () -> {
            String[] args = {pingAIEngineActionString};
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
    public void pingAIEngineFailedTimeOut() throws Exception {
        // create mock
        stubFor(get(urlEqualTo(pingAIEngineAction.getPingUrl()))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER))
        );

        Exception exception = assertThrows(InternalException.class, () -> {
            String[] args = {pingAIEngineActionString};
            Namespace parsedArgs = Application.parseInputArgs(args);
            List<Action> actions = Action.parseInputActions((JSONObject) parsedArgs.get("actions"));
            Map<String, Object> initialConfig = loadEnvironmentVariables(Application.getInitialEnvironmentVariables());
            PlatformAdapter platformAdapter = Factory.selectPlatformAdapter(initialConfig);
            Domain domain = new Domain(platformAdapter);
            domain.run(actions);
        });

        String expectedMessage = "Internal exception: Error while waiting for the AI Engine to be ready (during the query)";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void pingAIEngineBadFormatted() throws Exception {
        // replace config with one that is bad formatted
        JSONObject auxiliaryAction = new JSONObject();
        auxiliaryAction.put("name", "ping_ai_engine");
        JSONArray auxiliaryActions = new JSONArray();
        auxiliaryActions.put(auxiliaryAction);
        JSONObject auxiliaryActionsJson = new JSONObject();
        auxiliaryActionsJson.put("actions", auxiliaryActions);

        Exception exception = assertThrows(BadInputParametersException.class, () -> {
            String[] args = {auxiliaryActionsJson.toString()};
            Namespace parsedArgs = Application.parseInputArgs(args);
            List<Action> actions = Action.parseInputActions((JSONObject) parsedArgs.get("actions"));
            Map<String, Object> initialConfig = loadEnvironmentVariables(Application.getInitialEnvironmentVariables());
            PlatformAdapter platformAdapter = Factory.selectPlatformAdapter(initialConfig);
            Domain domain = new Domain(platformAdapter);
            domain.run(actions);
        });

        String expectedMessage = "Bad input parameters exception: Action ping AI Engine is bad formatted: JSONObject[\"max_initialization_time\"] not found.";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

}
