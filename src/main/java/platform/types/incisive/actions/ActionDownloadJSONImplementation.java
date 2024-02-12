package platform.types.incisive.actions;

import config.actions.ActionDownloadJSON;
import config.actions.ActionDownloadUserVars;
import exceptions.InternalException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static utils.HttpMethods.downloadFile;

public class ActionDownloadJSONImplementation {

    public static void run(ActionDownloadJSON action) throws InternalException {

        try {
            File file = new File(action.getOutputPath());
            String directory = file.getParentFile().getPath();
            Files.createDirectories(Paths.get(directory));
        } catch (IOException e) {
            throw new InternalException("Error while downloading JSON", e);
        }

        try (FileOutputStream outputStream = new FileOutputStream(action.getOutputPath())) {

            downloadFile(action.getJSONUrl(), action.getDownloadResumeRetries(), outputStream);
        } catch (IOException e) {
            throw new InternalException("Error while downloading JSON", e);
        }
    }
}
