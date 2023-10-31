import config.actions.Action;
import exceptions.BadInputParametersException;
import exceptions.InternalException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.*;
import platform.PlatformAdapter;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static config.environment.EnvironmentVariable.loadEnvironmentVariables;
import static org.junit.Assert.*;

public class TestChangeApiHostAndPort {
    public static final String EXPERIMENTS_MAIN_NAME = "change_api_host_and_port";

    public static final Path jsonActionPath = Paths.get("src/test/resources/input_configurations", String.format("%s.json", EXPERIMENTS_MAIN_NAME));
    public static final Path testsRootDirectoryPath = Paths.get(String.format("src/test/resources/tmp_%s_tests/", EXPERIMENTS_MAIN_NAME));
    private static final Path testIndividualDirectoryPath = Paths.get(testsRootDirectoryPath.toString(), "test");
    public JSONObject jsonAction;

    public static Path configFilePath;
    public static JSONObject configContents;


    @BeforeClass
    public static void beforeClass() throws Exception {
        if (Files.exists(testsRootDirectoryPath)) {
            FileUtils.cleanDirectory(testsRootDirectoryPath.toFile());
            FileUtils.deleteDirectory(testsRootDirectoryPath.toFile());
        }
        Files.createDirectory(testsRootDirectoryPath);

        // create dummy config file
        configFilePath = Paths.get(testsRootDirectoryPath.toString(), "platform_vars.json");
        Files.createFile(configFilePath);
        List<String> lines = List.of("{\"something\": \"something\", \"api_host\": \"something\", \"api_port\": \"something\"}");
        Files.write(configFilePath, lines, StandardCharsets.UTF_8);
        lines = List.of("{\"something\": \"something\", \"api_host\": \"127.0.0.1\", \"api_port\": \"8080\"}");
        configContents = new JSONObject(lines.get(0));
    }

    @Before
    public void before() throws Exception {
        // create directory for specific test
        if (Files.exists(testIndividualDirectoryPath)) {
            FileUtils.cleanDirectory(testIndividualDirectoryPath.toFile());
            FileUtils.deleteDirectory(testIndividualDirectoryPath.toFile());
        }
        Files.createDirectory(testIndividualDirectoryPath);

        // load default input json
        String content = new String(Files.readAllBytes(jsonActionPath));
        jsonAction = new JSONObject(content);
    }

    @After
    public void after() throws Exception {
        // clean test environment
        FileUtils.cleanDirectory(testIndividualDirectoryPath.toFile());
        FileUtils.deleteDirectory(testIndividualDirectoryPath.toFile());
    }

    @AfterClass
    public static void afterClass() throws Exception {
        // clean test environment
        if (Files.exists(testsRootDirectoryPath)) {
            FileUtils.cleanDirectory(testsRootDirectoryPath.toFile());
            FileUtils.deleteDirectory(testsRootDirectoryPath.toFile());
        }
    }

    @Test
    public void changeApiHostAndPortSuccess() throws Exception {
        // run domain
        String[] args = {jsonAction.toString()};
        Application.main(args);

        // assure files are ok
        List<String> directoryFiles = Utils.listDirectoryFiles(testIndividualDirectoryPath.toString());
        assertEquals(List.of("platform_vars.json"), directoryFiles);

        // assure config contents are ok
        byte[] actualConfigBytes = Files.readAllBytes(Paths.get(testIndividualDirectoryPath.toString(), "platform_vars.json"));
        JSONObject actualConfig = new JSONObject(new String(actualConfigBytes, StandardCharsets.UTF_8));
        assertEquals(configContents.toString(), actualConfig.toString());
    }


    @Test
    public void changeApiHostAndPortFailed() throws Exception {
        JSONObject changeApiHostAndPortActionFailed = new JSONObject(jsonAction.toString());
        changeApiHostAndPortActionFailed.getJSONArray("actions").getJSONObject(0).put("read_file_path", "does_not_exist.json");
        Exception exception = assertThrows(InternalException.class, () -> {
            String[] args = {changeApiHostAndPortActionFailed.toString()};
            Namespace parsedArgs = Application.parseInputArgs(args);
            List<Action> actions = Action.parseInputActions((JSONObject) parsedArgs.get("actions"));
            Map<String, Object> initialConfig = loadEnvironmentVariables(Application.getInitialEnvironmentVariables());
            PlatformAdapter platformAdapter = Factory.selectPlatformAdapter(initialConfig);
            Domain domain = new Domain(platformAdapter);
            domain.run(actions);
        });

        String expectedMessage = "Error while loading configuration file";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void changeApiHostAndPortBadFormatted() throws Exception {
        // replace config with one that is bad formatted
        JSONObject changeApiHostAndPortActionBadFormatted = new JSONObject(jsonAction.toString());
        changeApiHostAndPortActionBadFormatted.getJSONArray("actions").getJSONObject(0).put("api_host_and_port", "127.0.0.18080");

        Exception exception = assertThrows(BadInputParametersException.class, () -> {
            String[] args = {changeApiHostAndPortActionBadFormatted.toString()};
            Namespace parsedArgs = Application.parseInputArgs(args);
            List<Action> actions = Action.parseInputActions((JSONObject) parsedArgs.get("actions"));
            Map<String, Object> initialConfig = loadEnvironmentVariables(Application.getInitialEnvironmentVariables());
            PlatformAdapter platformAdapter = Factory.selectPlatformAdapter(initialConfig);
            Domain domain = new Domain(platformAdapter);
            domain.run(actions);
        });

        String expectedMessage = "Bad input parameters exception: Action change api host and port is bad formatted: Index 1 out of bounds for length 1";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

}
