package config.actions;

import exceptions.BadInputParametersException;
import org.json.JSONException;
import org.json.JSONObject;

public class ActionUpdateToFailed extends Action {

    private final String updateStatusUrl;

    public ActionUpdateToFailed(String name, String updateStatusUrl) {
        super(name);
        this.updateStatusUrl = updateStatusUrl;
    }

    public String getUpdateStatusUrl() {
        return updateStatusUrl;
    }

    @Override
    public String toString() {
        return "ActionUpdateToFailed{" +
                "updateStatusUrl='" + updateStatusUrl + '\'' +
                '}';
    }

    public static ActionUpdateToFailed parseInputAction(JSONObject inputJson) throws BadInputParametersException {
        try {
            String name = inputJson.getString("name");
            String updateStatusUrl = inputJson.getString("update_status_url");
            return new ActionUpdateToFailed(name, updateStatusUrl);
        } catch (ClassCastException | JSONException e) {
            throw new BadInputParametersException(String.format("Action update to failed is bad formatted: %s", e.getMessage()));
        }
    }
}
