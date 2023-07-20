package platform.types.incisive.actions;

import config.actions.ActionDownloadUserVars;
import exceptions.InternalException;

import java.io.FileOutputStream;
import java.io.IOException;

import static utils.HttpMethods.downloadFile;

public class ActionDownloadUserVarsImplementation {

    public static void run(ActionDownloadUserVars action) throws InternalException {
        try (FileOutputStream outputStream = new FileOutputStream(action.getOutputPath())) {
            downloadFile(action.getUserVarsUrl(), outputStream);
        } catch (IOException e) {
            throw new InternalException("Error while downloading user vars", e);
        }
    }
}
