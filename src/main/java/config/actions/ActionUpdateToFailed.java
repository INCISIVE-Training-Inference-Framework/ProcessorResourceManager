package config.actions;

import exceptions.BadInputParametersException;
import org.json.JSONException;
import org.json.JSONObject;

public class ActionUpdateToFailed extends Action {

    private final String updateStatusUrl;
    private final String message;

    public ActionUpdateToFailed(String name, String updateStatusUrl, String message) {
        super(name);
        this.updateStatusUrl = updateStatusUrl;
        this.message = message;
    }

    public String getUpdateStatusUrl() {
        return updateStatusUrl;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ActionUpdateToFailed{" +
                "updateStatusUrl='" + updateStatusUrl + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

    public static ActionUpdateToFailed parseInputAction(JSONObject inputJson) throws BadInputParametersException {
        try {
            String name = inputJson.getString("name");
            String updateStatusUrl = inputJson.getString("update_status_url");
            String message = inputJson.getString("message");
            return new ActionUpdateToFailed(name, updateStatusUrl, message);
        } catch (ClassCastException | JSONException e) {
            throw new BadInputParametersException(String.format("Action update to failed is bad formatted: %s", e.getMessage()));
        }
    }
}
