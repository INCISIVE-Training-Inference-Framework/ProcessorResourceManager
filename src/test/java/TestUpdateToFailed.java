import com.github.tomakehurst.wiremock.junit.WireMockRule;
import config.actions.Action;
import config.actions.ActionUpdateToFailed;
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

public class TestUpdateToFailed {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(8000), false);
    public String updateToFailedActionString;
    public ActionUpdateToFailed updateToFailedAction;
    public static String testsRootDirectory = "src/test/resources/tmp_update_to_failed_tests/";

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
        updateToFailedActionString = new String(Files.readAllBytes(Paths.get("src/test/resources/input_configurations/update_to_failed.json")));
        List<Action> actions = Action.parseInputActions(new JSONObject(updateToFailedActionString));
        updateToFailedAction = (ActionUpdateToFailed) actions.get(0);
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
    public void updateToFailedSuccess() throws Exception {
        // create mock
        stubFor(patch(urlEqualTo("/api/some_url/"))
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("data")
                                .withBody(equalToJson("{\"message\": \"error message\"}"))
                )
                .willReturn(aResponse().withStatus(200).withBody("{}")));

        // run domain
        String[] args = {updateToFailedActionString};
        Namespace parsedArgs = Application.parseInputArgs(args);
        List<Action> actions = Action.parseInputActions((JSONObject) parsedArgs.get("actions"));
        Map<String, Object> initialConfig = loadEnvironmentVariables(Application.getInitialEnvironmentVariables());
        PlatformAdapter platformAdapter = Factory.selectPlatformAdapter(initialConfig);
        platformAdapter.updateToFailed((ActionUpdateToFailed) actions.get(0));
    }


    @Test
    public void updateToFailedFailed() throws Exception {
        Exception exception = assertThrows(InternalException.class, () -> {
            String[] args = {updateToFailedActionString};
            Namespace parsedArgs = Application.parseInputArgs(args);
            List<Action> actions = Action.parseInputActions((JSONObject) parsedArgs.get("actions"));
            Map<String, Object> initialConfig = loadEnvironmentVariables(Application.getInitialEnvironmentVariables());
            PlatformAdapter platformAdapter = Factory.selectPlatformAdapter(initialConfig);
            platformAdapter.updateToFailed((ActionUpdateToFailed) actions.get(0));
        });

        String expectedMessage = "Internal exception: Error while updating status to failed. Wrong response status code. Expected: [200]. Actual: 404. Not Found";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void updateToFailedBadFormatted() throws Exception {
        // replace config with one that is bad formatted
        JSONObject auxiliaryAction = new JSONObject();
        auxiliaryAction.put("name", "update_to_failed");
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
            platformAdapter.updateToFailed((ActionUpdateToFailed) actions.get(0));
        });

        String expectedMessage = "Bad input parameters exception: Action update to failed is bad formatted: JSONObject[\"update_status_url\"] not found.";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

}
