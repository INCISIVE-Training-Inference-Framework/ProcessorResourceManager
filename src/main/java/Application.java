import config.actions.Action;
import config.actions.ActionUpdateToFailed;
import config.environment.EnvironmentVariable;
import config.environment.EnvironmentVariableType;
import exceptions.BadConfigurationException;
import exceptions.BadInputParametersException;
import exceptions.InternalException;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import platform.PlatformAdapter;

import java.util.List;
import java.util.Map;

import static config.environment.EnvironmentVariable.loadEnvironmentVariables;

public class Application {

    private static final Logger logger = LogManager.getLogger(Application.class);
    public static List<EnvironmentVariable> getInitialEnvironmentVariables() {
        return List.of(
                new EnvironmentVariable("PLATFORM_ADAPTER", EnvironmentVariableType.STRING, "INCISIVE")
        );
    }

    public static void main(String[] args) {
        Namespace parsedArgs = null;
        List<Action> actions = null;
        PlatformAdapter platformAdapter = null;
        try {
            // parse input parameters
            parsedArgs = parseInputArgs(args);
            actions = Action.parseInputActions((JSONObject) parsedArgs.get("actions"));

            // load main environmental variables
            Map<String, Object> initialConfig = loadEnvironmentVariables(Application.getInitialEnvironmentVariables());

            // load abstract classes implementations
            platformAdapter = Factory.selectPlatformAdapter(initialConfig);
        } catch (BadConfigurationException | BadInputParametersException e) {
            logger.error(e);
            System.exit(1);
        }

        try {
            // run main application
            Domain domain = new Domain(actions, platformAdapter);
            domain.run();
        } catch (InternalException e) {
            e.print(logger);

            // run actions when failed
            try {

                // contact external endpoint if required
                if (parsedArgs.get("failure_endpoint") != null) {
                    logger.error("Running failure actions. Updating to failed");

                    String errorMessage = e.getMessage();
                    if (e.getException() != null) {
                        errorMessage += ". " + e.getException().getMessage();
                    }

                    ActionUpdateToFailed actionUpdateToFailed = new ActionUpdateToFailed(
                            "update_to_failed",
                            (String) parsedArgs.get("failure_endpoint"),
                            errorMessage
                    );
                    platformAdapter.updateToFailed(actionUpdateToFailed);
                }

            } catch (InternalException e2) {
                e2.print(logger);
            }

            System.exit(1);
        }
    }

    public static Namespace parseInputArgs(String[] args) throws BadInputParametersException {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("processor_resource_manager");
        parser.addArgument("actions").type(JSONObject.class).help(
                "a JSONObject with a key \"actions\" with an array with the definitions of the actions to perform"
        );
        parser.addArgument("--failure-endpoint").type(String.class).help(
                "the endpoint to hit when an error occurs"
        );

        try {
            Namespace parsedArgs = parser.parseArgs(args);
            logger.info(parsedArgs);
            return parsedArgs;
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            throw new BadInputParametersException("Argument parser exception");
        }

    }
}
