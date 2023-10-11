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

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static config.environment.EnvironmentVariable.loadEnvironmentVariables;
import static org.junit.Assert.*;
import static utils.ZipCompression.zipFile;

public class TestDownloadExternalData {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8000);
    public JSONObject downloadExternalDataAction;
    public static ByteArrayOutputStream byteArrayOutputStream;
    public static String testsRootDirectory = "src/test/resources/tmp_download_external_data_tests/";

    @BeforeClass
    public static void beforeClass() throws Exception {
        if (Files.exists(Paths.get(testsRootDirectory))) {
            FileUtils.cleanDirectory(Paths.get(testsRootDirectory).toFile());
            FileUtils.deleteDirectory(Paths.get(testsRootDirectory).toFile());
        }

        // create dummy zip compressed file
        Files.createDirectory(Paths.get(testsRootDirectory));
        Files.createDirectory(Paths.get(testsRootDirectory + "tmp"));
        Files.createFile(Paths.get(testsRootDirectory + "tmp/image1.png"));
        Files.createFile(Paths.get(testsRootDirectory + "tmp/image2.png"));
        byteArrayOutputStream = new ByteArrayOutputStream();
        zipFile(testsRootDirectory + "tmp", byteArrayOutputStream);
        FileUtils.deleteDirectory(Paths.get(testsRootDirectory + "tmp").toFile());
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
        String content = new String(Files.readAllBytes(Paths.get("src/test/resources/input_configurations/download_external_data.json")));
        downloadExternalDataAction = new JSONObject(content);
    }

    @After
    public void after() throws Exception {
        // clean test environment
        FileUtils.cleanDirectory(Paths.get(testsRootDirectory + "test").toFile());
        FileUtils.deleteDirectory(Paths.get(testsRootDirectory + "test").toFile());
    }

    @AfterClass
    public static void afterClass() throws Exception {
        // close byte array
        if (byteArrayOutputStream != null) byteArrayOutputStream.close();

        // clean test environment
        FileUtils.cleanDirectory(Paths.get(testsRootDirectory).toFile());
        FileUtils.deleteDirectory(Paths.get(testsRootDirectory).toFile());
    }

    @Test
    public void downloadInputInferenceDataSuccess() throws Exception {
        // create mock
        stubFor(head(urlEqualTo("/api/some_url/"))
                .willReturn(aResponse()
                        .withHeader("content-length", String.valueOf(byteArrayOutputStream.toByteArray().length))
                ));
        stubFor(get(urlEqualTo("/api/some_url/"))
                .willReturn(aResponse().withBody(byteArrayOutputStream.toByteArray())));

        // run domain
        String[] args = {downloadExternalDataAction.toString()};
        Application.main(args);

        // assure files are ok
        List<String> directoryFiles = Utils.listDirectoryFiles(testsRootDirectory + "test");
        List<String> expectedDirectoryFiles = Arrays.asList("tmp", "image1.png", "image2.png");
        expectedDirectoryFiles.sort(null);
        assertEquals(expectedDirectoryFiles, directoryFiles);
    }


    @Test
    public void downloadInputInferenceDataFailed() throws Exception {
        // create mock
        stubFor(head(urlEqualTo("/api/some_url/"))
                .willReturn(aResponse()
                        .withHeader("content-length", String.valueOf(byteArrayOutputStream.toByteArray().length))
                ));
        stubFor(get(urlEqualTo("/api/some_url/"))
                .willReturn(aResponse().withStatus(400).withBody("")));

        InternalException exception = assertThrows(InternalException.class, () -> {
            String[] args = {downloadExternalDataAction.toString()};
            Namespace parsedArgs = Application.parseInputArgs(args);
            List<Action> actions = Action.parseInputActions((JSONObject) parsedArgs.get("actions"));
            Map<String, Object> initialConfig = loadEnvironmentVariables(Application.getInitialEnvironmentVariables());
            PlatformAdapter platformAdapter = Factory.selectPlatformAdapter(initialConfig);
            Domain domain = new Domain(platformAdapter);
            domain.run(actions);
        });

        String expectedMessage = "Internal exception: Error while downloading external data. Server returned HTTP response code: 400 for URL: http://localhost:8000/api/some_url/";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void downloadInputInferenceDataBadFormatted() throws Exception {
        // replace config with one that is bad formatted
        JSONObject auxiliaryAction = new JSONObject();
        auxiliaryAction.put("name", "download_external_data");
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

        String expectedMessage = "Bad input parameters exception: Action download external data is bad formatted: JSONObject[\"external_data_url\"] not found.";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

}
