import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.*;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;


// TODO do better tests
public class TestPrepareInternalData {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8000);
    public JSONObject prepareInternalDataAction;
    public static ByteArrayOutputStream byteArrayOutputStream;
    public static String testsRootDirectory = "src/test/resources/tmp_prepare_internal_data_tests/";

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
        String content = new String(Files.readAllBytes(Paths.get("src/test/resources/input_configurations/prepare_internal_data_breast.json")));
        prepareInternalDataAction = new JSONObject(content);
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
    public void prepareInternalDataSuccess() throws Exception {
        // run domain
        String[] args = {prepareInternalDataAction.toString()};
        Application.main(args);

        // assure files are ok
        List<String> directoryFiles = Utils.listDirectoryFiles(testsRootDirectory + "test");
        assertEquals(List.of("Breast_Cancer.xlsx"), directoryFiles);
    }

}
