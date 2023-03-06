package config.actions;

import org.json.JSONObject;

public class ActionDownloadPlatformData extends Action {

    public ActionDownloadPlatformData(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public static ActionDownloadPlatformData parseInputAction(JSONObject inputJson) {
        return new ActionDownloadPlatformData(inputJson.getString("name"));
    }
}
