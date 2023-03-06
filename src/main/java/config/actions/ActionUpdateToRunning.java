package config.actions;

import exceptions.BadInputParametersException;
import org.json.JSONException;
import org.json.JSONObject;

public class ActionUpdateToRunning extends Action {

    private final String updateStatusUrl;

    public ActionUpdateToRunning(String name, String updateStatusUrl) {
        super(name);
        this.updateStatusUrl = updateStatusUrl;
    }

    public String getUpdateStatusUrl() {
        return updateStatusUrl;
    }

    @Override
    public String toString() {
        return "ActionUpdateToRunning{" +
                "updateStatusUrl='" + updateStatusUrl + '\'' +
                '}';
    }

    public static ActionUpdateToRunning parseInputAction(JSONObject inputJson) throws BadInputParametersException {
        try {
            String name = inputJson.getString("name");
            String updateStatusUrl = inputJson.getString("update_status_url");
            return new ActionUpdateToRunning(name, updateStatusUrl);
        } catch (ClassCastException | JSONException e) {
            throw new BadInputParametersException(String.format("Action update to running is bad formatted: %s", e.getMessage()));
        }
    }
}
