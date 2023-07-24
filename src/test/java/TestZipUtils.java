import org.apache.commons.io.FileUtils;
import org.junit.*;
import utils.ZipCompression;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static utils.ZipCompression.zipFile;

public class TestZipUtils {

    public static String testsRootDirectoryGeneral = "src/test/resources/tmp_zip_utils_tests";
    public static String testsRootDirectorySpecific = String.format("%s/test/", testsRootDirectoryGeneral);

    @BeforeClass
    public static void beforeClass() throws Exception {
        if (Files.exists(Paths.get(testsRootDirectoryGeneral))) {
            FileUtils.cleanDirectory(Paths.get(testsRootDirectoryGeneral).toFile());
            FileUtils.deleteDirectory(Paths.get(testsRootDirectoryGeneral).toFile());
        }
        Files.createDirectory(Paths.get(testsRootDirectoryGeneral));
    }

    @Before
    public void before() throws Exception {
        // create directory for specific test
        if (Files.exists(Paths.get(testsRootDirectorySpecific))) {
            FileUtils.cleanDirectory(Paths.get(testsRootDirectorySpecific).toFile());
            FileUtils.deleteDirectory(Paths.get(testsRootDirectorySpecific).toFile());
        }
        Files.createDirectory(Paths.get(testsRootDirectorySpecific));
    }

    @After
    public void after() throws Exception {
        // clean test environment
        FileUtils.cleanDirectory(Paths.get(testsRootDirectorySpecific).toFile());
        FileUtils.deleteDirectory(Paths.get(testsRootDirectorySpecific).toFile());
    }

    @AfterClass
    public static void afterClass() throws Exception {
        // clean test environment
        FileUtils.cleanDirectory(Paths.get(testsRootDirectoryGeneral).toFile());
        FileUtils.deleteDirectory(Paths.get(testsRootDirectoryGeneral).toFile());
    }

    @Test
    public void zipFolderSuccess() throws Exception {
        // create zip compressed file
        Files.createDirectory(Paths.get(testsRootDirectorySpecific + "tmp"));
        Files.createFile(Paths.get(testsRootDirectorySpecific + "tmp/image1.png"));
        Files.createFile(Paths.get(testsRootDirectorySpecific + "tmp/image2.png"));
        Files.createDirectory(Paths.get(testsRootDirectorySpecific + "tmp/tmp"));
        Files.createFile(Paths.get(testsRootDirectorySpecific + "tmp/tmp/image3.png"));
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            zipFile(testsRootDirectorySpecific + "tmp", byteArrayOutputStream);
            File outputFile = new File(testsRootDirectorySpecific + "zipped_filed.zip");
            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                outputStream.write(byteArrayOutputStream.toByteArray());
            }
        }
        FileUtils.deleteDirectory(Paths.get(testsRootDirectorySpecific + "tmp").toFile());

        // uncompress zip compressed file
        Files.createDirectory(Paths.get(testsRootDirectorySpecific + "unzipped_file"));
        try (FileInputStream inputStream = new FileInputStream(testsRootDirectorySpecific + "zipped_filed.zip")) {
            ZipCompression.unZipFile(inputStream, Paths.get(testsRootDirectorySpecific + "unzipped_file"));
        }

        // assure files are ok
        List<String> directoryFiles = Utils.listDirectoryFiles(testsRootDirectorySpecific + "unzipped_file");
        assertEquals(Arrays.asList("tmp", "image1.png", "image2.png", "tmp", "image3.png"), directoryFiles);
    }

    @Test
    public void zipFolderContentsSuccess() throws Exception {
        // create zip compressed file
        Files.createDirectory(Paths.get(testsRootDirectorySpecific + "tmp"));
        Files.createFile(Paths.get(testsRootDirectorySpecific + "tmp/image1.png"));
        Files.createFile(Paths.get(testsRootDirectorySpecific + "tmp/image2.png"));
        Files.createDirectory(Paths.get(testsRootDirectorySpecific + "tmp/tmp"));
        Files.createFile(Paths.get(testsRootDirectorySpecific + "tmp/tmp/image3.png"));
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            zipFile(testsRootDirectorySpecific + "tmp/*", byteArrayOutputStream);
            File outputFile = new File(testsRootDirectorySpecific + "zipped_filed.zip");
            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                outputStream.write(byteArrayOutputStream.toByteArray());
            }
        }
        FileUtils.deleteDirectory(Paths.get(testsRootDirectorySpecific + "tmp").toFile());

        // uncompress zip compressed file
        Files.createDirectory(Paths.get(testsRootDirectorySpecific + "unzipped_file"));
        try (FileInputStream inputStream = new FileInputStream(testsRootDirectorySpecific + "zipped_filed.zip")) {
            ZipCompression.unZipFile(inputStream, Paths.get(testsRootDirectorySpecific + "unzipped_file"));
        }

        // assure files are ok
        List<String> directoryFiles = Utils.listDirectoryFiles(testsRootDirectorySpecific + "unzipped_file");
        assertEquals(Arrays.asList("image1.png", "image2.png", "tmp", "image3.png"), directoryFiles);
    }

    // TODO check only one file
    @Test
    public void zipOneFileSuccess() throws Exception {
        // create zip compressed file
        Files.createDirectory(Paths.get(testsRootDirectorySpecific + "tmp"));
        Files.createFile(Paths.get(testsRootDirectorySpecific + "tmp/image1.png"));
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            zipFile(testsRootDirectorySpecific + "tmp/image1.png", byteArrayOutputStream);
            File outputFile = new File(testsRootDirectorySpecific + "zipped_filed.zip");
            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                outputStream.write(byteArrayOutputStream.toByteArray());
            }
        }
        FileUtils.deleteDirectory(Paths.get(testsRootDirectorySpecific + "tmp").toFile());

        // uncompress zip compressed file
        Files.createDirectory(Paths.get(testsRootDirectorySpecific + "unzipped_file"));
        try (FileInputStream inputStream = new FileInputStream(testsRootDirectorySpecific + "zipped_filed.zip")) {
            ZipCompression.unZipFile(inputStream, Paths.get(testsRootDirectorySpecific + "unzipped_file"));
        }

        // assure files are ok
        List<String> directoryFiles = Utils.listDirectoryFiles(testsRootDirectorySpecific + "unzipped_file");
        assertEquals(List.of("image1.png"), directoryFiles);
    }

}
