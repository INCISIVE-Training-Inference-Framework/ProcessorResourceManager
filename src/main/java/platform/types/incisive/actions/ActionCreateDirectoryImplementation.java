package platform.types.incisive.actions;

import config.actions.ActionCreateDirectory;
import exceptions.InternalException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ActionCreateDirectoryImplementation {

    public static void run(ActionCreateDirectory action) throws InternalException {
        try {
            Files.createDirectories(Paths.get(action.getDirectoryPath()));
        } catch (IOException e) {
            throw new InternalException("Error while creating directory", e);
        }
    }
}
