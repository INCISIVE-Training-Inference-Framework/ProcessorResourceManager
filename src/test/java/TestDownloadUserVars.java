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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static config.environment.EnvironmentVariable.loadEnvironmentVariables;
import static org.junit.Assert.*;

public class TestDownloadUserVars {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8000);
    public JSONObject downloadUserVarsAction;
    public static Path configFilePath;
    public static JSONObject configContents;
    public static String testsRootDirectory = "src/test/resources/tmp_download_user_vars_tests/";

    @BeforeClass
    public static void beforeClass() throws Exception {
        if (Files.exists(Paths.get(testsRootDirectory))) {
            FileUtils.cleanDirectory(Paths.get(testsRootDirectory).toFile());
            FileUtils.deleteDirectory(Paths.get(testsRootDirectory).toFile());
        }
        Files.createDirectory(Paths.get(testsRootDirectory));

        // create dummy config file
        configFilePath = Paths.get(testsRootDirectory + "user_vars.json");
        Files.createFile(configFilePath);
        List<String> lines = List.of("{\"test\": \"dummy_json\"}");
        Files.write(configFilePath, lines, StandardCharsets.UTF_8);
        configContents = new JSONObject(lines.get(0));
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
        String content = new String(Files.readAllBytes(Paths.get("src/test/resources/input_configurations/download_user_vars.json")));
        downloadUserVarsAction = new JSONObject(content);
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
    public void downloadUserVarsSuccess() throws Exception {
        // create mock
        stubFor(head(urlEqualTo("/api/some_url/"))
                .willReturn(aResponse()
                        .withHeader("content-length", String.valueOf(Files.readAllBytes(configFilePath).length))
                ));
        stubFor(get(urlEqualTo("/api/some_url/"))
                .willReturn(aResponse().withBody(Files.readAllBytes(configFilePath))));

        // run domain
        String[] args = {downloadUserVarsAction.toString()};
        Application.main(args);

        // assure files are ok
        List<String> directoryFiles = Utils.listDirectoryFiles(testsRootDirectory + "test");
        assertEquals(List.of("user_vars.json"), directoryFiles);

        // assure config contents are ok
        byte[] actualConfigBytes = Files.readAllBytes(Paths.get(testsRootDirectory + "test/user_vars.json"));
        JSONObject actualConfig = new JSONObject(new String(actualConfigBytes, StandardCharsets.UTF_8));
        assertEquals(configContents.toString(), actualConfig.toString());
    }


    @Test
    public void downloadUserVarsFailed() throws Exception {
        // create mock
        stubFor(head(urlEqualTo("/api/some_url/"))
                .willReturn(aResponse()
                        .withHeader("content-length", String.valueOf(Files.readAllBytes(configFilePath).length))
                ));
        stubFor(get(urlEqualTo("/api/some_url/"))
                .willReturn(aResponse().withStatus(400).withBody("")));

        InternalException exception = assertThrows(InternalException.class, () -> {
            String[] args = {downloadUserVarsAction.toString()};
            Namespace parsedArgs = Application.parseInputArgs(args);
            List<Action> actions = Action.parseInputActions((JSONObject) parsedArgs.get("actions"));
            Map<String, Object> initialConfig = loadEnvironmentVariables(Application.getInitialEnvironmentVariables());
            PlatformAdapter platformAdapter = Factory.selectPlatformAdapter(initialConfig);
            Domain domain = new Domain(platformAdapter);
            domain.run(actions);
        });

        String expectedMessage = "Internal exception: Error while downloading user vars. Server returned HTTP response code: 400 for URL: http://localhost:8000/api/some_url/";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void downloadUserVarsBadFormatted() throws Exception {
        // replace config with one that is bad formatted
        JSONObject auxiliaryAction = new JSONObject();
        auxiliaryAction.put("name", "download_user_vars");
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

        String expectedMessage = "Bad input parameters exception: Action download user vars is bad formatted: JSONObject[\"user_vars_url\"] not found.";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

}
