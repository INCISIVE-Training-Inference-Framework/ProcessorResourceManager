package config.actions;

import exceptions.BadInputParametersException;
import org.json.JSONException;
import org.json.JSONObject;

public class ActionPingAIEngine extends Action {

    private final long maxInitializationTime;
    private final String clientHost;
    private final String pingUrl;


    public ActionPingAIEngine(
            String name,
            long maxInitializationTime,
            String clientHost,
            String pingUrl
    ) {
        super(name);
        this.maxInitializationTime = maxInitializationTime;
        this.clientHost = clientHost;
        this.pingUrl = pingUrl;
    }

    public long getMaxInitializationTime() {
        return maxInitializationTime;
    }

    public String getClientHost() {
        return clientHost;
    }

    public String getPingUrl() {
        return pingUrl;
    }

    @Override
    public String toString() {
        return "ActionPingAIEngine{" +
                "maxInitializationTime=" + maxInitializationTime +
                ", clientHost='" + clientHost + '\'' +
                ", pingUrl='" + pingUrl + '\'' +
                '}';
    }

    public static ActionPingAIEngine parseInputAction(JSONObject inputJson) throws BadInputParametersException {
        try {
            String name = inputJson.getString("name");
            long maxInitializationTime = inputJson.getLong("max_initialization_time");
            String clientHost = inputJson.getString("client_host");
            String pingUrl = inputJson.getString("ping_url");
            return new ActionPingAIEngine(
                    name,
                    maxInitializationTime,
                    clientHost,
                    pingUrl
            );
        } catch (ClassCastException | JSONException e) {
            throw new BadInputParametersException(String.format("Action ping AI Engine is bad formatted: %s", e.getMessage()));
        }
    }
}
