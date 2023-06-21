import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import config.actions.Action;
import config.actions.ActionEndAIEngine;
import exceptions.BadInputParametersException;
import exceptions.InternalException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.*;
import org.wiremock.webhooks.Webhooks;
import platform.PlatformAdapter;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.http.RequestMethod.DELETE;
import static config.environment.EnvironmentVariable.loadEnvironmentVariables;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.wiremock.webhooks.Webhooks.webhook;


public class TestEndAIEngine {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(8001).extensions(Webhooks.class), false);
    public String endAIEngineActionString;
    public ActionEndAIEngine endAIEngineAction;
    public static String testsRootDirectory = "src/test/resources/tmp_end_ai_engine_tests/";

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
        endAIEngineActionString = new String(Files.readAllBytes(Paths.get("src/test/resources/input_configurations/end_ai_engine.json")));
        List<Action> actions = Action.parseInputActions(new JSONObject(endAIEngineActionString));
        endAIEngineAction = (ActionEndAIEngine) actions.get(0);
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
    public void endAIEngineSuccess() throws Exception {
        // create mock
        stubFor(get(urlEqualTo(endAIEngineAction.getPingUrl()))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER))
        );
        UUID stubId1 = UUID.randomUUID();
        stubFor(get(urlEqualTo(endAIEngineAction.getPingUrl()))
                .withId(stubId1)
                .willReturn(aResponse().withStatus(200))
        );
        stubFor(post(urlEqualTo(endAIEngineAction.getEndUrl()))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER))
        );
        UUID stubId2 = UUID.randomUUID();
        stubFor(post(urlEqualTo(endAIEngineAction.getEndUrl()))
                .withId(stubId2)
                .willReturn(aResponse().withStatus(200))
                        .withPostServeAction("webhook", webhook()
                                .withMethod(DELETE)
                                .withUrl(wireMockRule.url("/__admin/mappings/" + stubId1))
                        )
                        .withPostServeAction("webhook", webhook()
                                .withMethod(DELETE)
                                .withUrl(wireMockRule.url("/__admin/mappings/" + stubId2))
                        )
        );

        // run domain
        String[] args = {endAIEngineActionString};
        Application.main(args);
    }

    @Test
    public void endAIEngineNotFinishFailure() throws Exception {
        // create mock
        stubFor(get(urlEqualTo(endAIEngineAction.getPingUrl()))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER))
        );
        UUID stubId1 = UUID.randomUUID();
        stubFor(get(urlEqualTo(endAIEngineAction.getPingUrl()))
                .withId(stubId1)
                .willReturn(aResponse().withStatus(200))
        );
        stubFor(post(urlEqualTo(endAIEngineAction.getEndUrl()))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER))
        );
        UUID stubId2 = UUID.randomUUID();
        stubFor(post(urlEqualTo(endAIEngineAction.getEndUrl()))
                .withId(stubId2)
                .willReturn(aResponse().withStatus(200))
        );

        // run domain
        Exception exception = assertThrows(InternalException.class, () -> {
            String[] args = {endAIEngineActionString};
            Namespace parsedArgs = Application.parseInputArgs(args);
            List<Action> actions = Action.parseInputActions((JSONObject) parsedArgs.get("actions"));
            Map<String, Object> initialConfig = loadEnvironmentVariables(Application.getInitialEnvironmentVariables());
            PlatformAdapter platformAdapter = Factory.selectPlatformAdapter(initialConfig);
            Domain domain = new Domain(platformAdapter);
            domain.run(actions);
        });

        String expectedMessage = "Internal exception: Error while waiting for the AI Engine to finish. It did not end before the timeout";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void endAIEngineBadFormatted() throws Exception {
        // replace config with one that is bad formatted
        JSONObject auxiliaryAction = new JSONObject();
        auxiliaryAction.put("name", "end_ai_engine");
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

        String expectedMessage = "Bad input parameters exception: Action end AI Engine is bad formatted: JSONObject[\"max_finalization_time\"] not found.";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

}
