package config.actions;

import exceptions.BadInputParametersException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class Action {

    private final String name;

    public Action(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Action{" +
                "name='" + name + '\'' +
                '}';
    }

    public static List<Action> parseInputActions(JSONObject inputJson) throws BadInputParametersException {
        JSONArray inputActions;
        try {
            inputActions = (JSONArray) inputJson.get("actions");
        } catch (JSONException e) {
            throw new BadInputParametersException("The input JSON object does not contain a JSON array with key \"actions\" with the actions to perform");
        }

        List<Action> outputActions = new ArrayList<>();

        for (int i = 0; i < inputActions.length(); i++) {
            JSONObject inputAction = inputActions.getJSONObject(i);

            String actionName;
            try {
                actionName = inputAction.getString("name");
            } catch (ClassCastException | JSONException e) {
                throw new BadInputParametersException("Input action is bad formatted: " + e.getMessage());
            }

            Action action = switch (actionName) {
                case "download_platform_data" -> ActionDownloadPlatformData.parseInputAction(inputAction);
                case "download_external_data" -> ActionDownloadExternalData.parseInputAction(inputAction);
                case "download_user_vars" -> ActionDownloadUserVars.parseInputAction(inputAction);
                case "download_ai_model" -> ActionDownloadAIModel.parseInputAction(inputAction);
                case "create_directory" -> ActionCreateDirectory.parseInputAction(inputAction);
                case "run_ai_engine" -> ActionRunAIEngine.parseInputAction(inputAction);
                case "end_ai_engine" -> ActionEndAIEngine.parseInputAction(inputAction);
                case "update_to_running" -> ActionUpdateToRunning.parseInputAction(inputAction);
                case "update_to_failed" -> ActionUpdateToFailed.parseInputAction(inputAction);
                case "update_to_succeeded" -> ActionUpdateToSucceeded.parseInputAction(inputAction);
                case "change_api_host_and_port" -> ActionChangeApiHostAndPort.parseInputAction(inputAction);
                default -> throw new BadInputParametersException(String.format("The action with name \"%s\" does not exist", actionName));
            };
            outputActions.add(action);
        }

        return outputActions;
    }
}
