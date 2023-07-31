package platform.types.incisive.actions;

import exceptions.InternalException;
import utils.ZipCompression;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static utils.HttpMethods.downloadFile;

public class ActionDownloadExternalDataImplementation {

    public static void run(config.actions.ActionDownloadExternalData action) throws InternalException {
        String temporalZippedFilePath = String.format("%s/tmp_zip_file", action.getOutputPath());
        try (FileOutputStream outputStream = new FileOutputStream(temporalZippedFilePath)) {
            downloadFile(action.getExternalDataUrl(), action.getDownloadResumeRetries(), outputStream);
        } catch (IOException e) {
            throw new InternalException("Error while downloading external data", e);
        }

        try (FileInputStream inputStream = new FileInputStream(temporalZippedFilePath)) {
            ZipCompression.unZipFile(inputStream, Paths.get(action.getOutputPath()));
        } catch (IOException e) {
            throw new InternalException("Error while unzipping external data", e);
        }

        try {
            Files.delete(Paths.get(temporalZippedFilePath));
        } catch (IOException e) {
            throw new InternalException("Error while cleaning temporary external data", e);
        }
    }
}
