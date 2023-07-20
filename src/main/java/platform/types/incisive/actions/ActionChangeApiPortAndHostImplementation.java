package platform.types.incisive.actions;

import config.actions.ActionChangeApiHostAndPort;
import exceptions.InternalException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import static utils.FileMethods.readJson;

public class ActionChangeApiPortAndHostImplementation {

    public static void run(ActionChangeApiHostAndPort action) throws InternalException {
        JSONObject configuration;
        try (InputStream inputStream = new FileInputStream(action.getReadFilePath())) {
            configuration = readJson(inputStream);
            configuration.put("api_host", action.getApiHost());
            configuration.put("api_port", action.getApiPort());
        } catch (IOException e) {
            throw new InternalException("Error while loading configuration file", e);
        }

        try (FileWriter file = new FileWriter(action.getWriteFilePath())) {
            file.write(configuration.toString());
        } catch (IOException e) {
            throw new InternalException("Error while updating configuration file", e);
        }
    }
}
