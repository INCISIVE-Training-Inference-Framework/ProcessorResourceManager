import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.*;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;


// TODO do better tests
public class TestPrepareInternalData {
    public static final String EXPERIMENTS_MAIN_NAME = "prepare_internal_data";

    public static final Path jsonActionPath = Paths.get("src/test/resources/input_configurations", String.format("%s.json", EXPERIMENTS_MAIN_NAME));
    public static final Path testsRootDirectoryPath = Paths.get(String.format("src/test/resources/tmp_%s_tests/", EXPERIMENTS_MAIN_NAME));
    private static final Path testIndividualDirectoryPath = Paths.get(testsRootDirectoryPath.toString(), "test");
    public JSONObject jsonAction;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8000);
    public static ByteArrayOutputStream byteArrayOutputStream;

    @BeforeClass
    public static void beforeClass() throws Exception {
        if (Files.exists(testsRootDirectoryPath)) {
            FileUtils.cleanDirectory(testsRootDirectoryPath.toFile());
            FileUtils.deleteDirectory(testsRootDirectoryPath.toFile());
        }
        Files.createDirectory(testsRootDirectoryPath);
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
        // close byte array
        if (byteArrayOutputStream != null) byteArrayOutputStream.close();

        // clean test environment
        if (Files.exists(testsRootDirectoryPath)) {
            FileUtils.cleanDirectory(testsRootDirectoryPath.toFile());
            FileUtils.deleteDirectory(testsRootDirectoryPath.toFile());
        }
    }

    @Test
    public void prepareInternalDataSuccessBreast() throws Exception {
        // create mock
        JSONObject dataPartnerInformation = new JSONObject(new String(Files.readAllBytes(Paths.get("src/test/resources/input_configurations/prepare_internal_data_breast_data_partner_information.json"))));
        stubFor(get(urlEqualTo("/api/some_url/"))
                .willReturn(aResponse().withStatus(200).withBody(dataPartnerInformation.toString())));

        // run domain
        String[] args = {jsonAction.toString()};
        Application.main(args);

        // assure files are ok
        List<String> directoryFiles = Utils.listDirectoryFiles(testIndividualDirectoryPath.toString());
        List<String> expectedDirectoryFiles = Arrays.asList("Lung_Cancer.xlsx", "Prostate_Cancer.xlsx", "Breast_Cancer.xlsx", "Colorectal_Cancer.xlsx");
        expectedDirectoryFiles.sort(null);
        assertEquals(expectedDirectoryFiles, directoryFiles);
    }

    @Test
    public void prepareInternalDataSuccessLung() throws Exception {
        // create mock
        JSONObject dataPartnerInformation = new JSONObject(new String(Files.readAllBytes(Paths.get("src/test/resources/input_configurations/prepare_internal_data_lung_data_partner_information.json"))));
        stubFor(get(urlEqualTo("/api/some_url/"))
                .willReturn(aResponse().withStatus(200).withBody(dataPartnerInformation.toString())));

        // run domain
        String[] args = {jsonAction.toString()};
        Application.main(args);

        // assure files are ok
        List<String> directoryFiles = Utils.listDirectoryFiles(testIndividualDirectoryPath.toString());
        List<String> expectedDirectoryFiles = Arrays.asList("Lung_Cancer.xlsx", "Prostate_Cancer.xlsx", "Breast_Cancer.xlsx", "Colorectal_Cancer.xlsx");
        expectedDirectoryFiles.sort(null);
        assertEquals(expectedDirectoryFiles, directoryFiles);
    }

    @Test
    public void prepareInternalDataSuccessProstate() throws Exception {
        // create mock
        JSONObject dataPartnerInformation = new JSONObject(new String(Files.readAllBytes(Paths.get("src/test/resources/input_configurations/prepare_internal_data_prostate_data_partner_information.json"))));
        stubFor(get(urlEqualTo("/api/some_url/"))
                .willReturn(aResponse().withStatus(200).withBody(dataPartnerInformation.toString())));

        // run domain
        String[] args = {jsonAction.toString()};
        Application.main(args);

        // assure files are ok
        List<String> directoryFiles = Utils.listDirectoryFiles(testIndividualDirectoryPath.toString());
        List<String> expectedDirectoryFiles = Arrays.asList("Lung_Cancer.xlsx", "Prostate_Cancer.xlsx", "Breast_Cancer.xlsx", "Colorectal_Cancer.xlsx");
        expectedDirectoryFiles.sort(null);
        assertEquals(expectedDirectoryFiles, directoryFiles);
    }

    @Test
    public void prepareInternalDataSuccessColorectal() throws Exception {
        // create mock
        JSONObject dataPartnerInformation = new JSONObject(new String(Files.readAllBytes(Paths.get("src/test/resources/input_configurations/prepare_internal_data_colorectal_data_partner_information.json"))));
        stubFor(get(urlEqualTo("/api/some_url/"))
                .willReturn(aResponse().withStatus(200).withBody(dataPartnerInformation.toString())));

        // run domain
        String[] args = {jsonAction.toString()};
        Application.main(args);

        // assure files are ok
        List<String> directoryFiles = Utils.listDirectoryFiles(testIndividualDirectoryPath.toString());
        List<String> expectedDirectoryFiles = Arrays.asList("Lung_Cancer.xlsx", "Prostate_Cancer.xlsx", "Breast_Cancer.xlsx", "Colorectal_Cancer.xlsx");
        expectedDirectoryFiles.sort(null);
        assertEquals(expectedDirectoryFiles, directoryFiles);
    }

}
