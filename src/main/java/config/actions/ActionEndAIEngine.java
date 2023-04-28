package config.actions;

import exceptions.BadInputParametersException;
import org.json.JSONException;
import org.json.JSONObject;

public class ActionEndAIEngine extends Action {

    private final long maxFinalizationTime;
    private final int maxFinalizationRetries;
    private final String clientHost;
    private final String pingUrl;
    private final String endUrl;


    public ActionEndAIEngine(
            String name,
            long maxFinalizationTime,
            int maxFinalizationRetries,
            String clientHost,
            String pingUrl,
            String endUrl
    ) {
        super(name);
        this.maxFinalizationTime = maxFinalizationTime;
        this.maxFinalizationRetries = maxFinalizationRetries;
        this.clientHost = clientHost;
        this.pingUrl = pingUrl;
        this.endUrl = endUrl;
    }

    public long getMaxFinalizationTime() {
        return maxFinalizationTime;
    }

    public int getMaxFinalizationRetries() {
        return maxFinalizationRetries;
    }

    public String getClientHost() {
        return clientHost;
    }

    public String getPingUrl() {
        return pingUrl;
    }

    public String getEndUrl() {
        return endUrl;
    }

    @Override
    public String toString() {
        return "ActionEndAIEngine{" +
                "maxFinalizationTime=" + maxFinalizationTime +
                ", maxFinalizationRetries=" + maxFinalizationRetries +
                ", clientHost='" + clientHost + '\'' +
                ", pingUrl='" + pingUrl + '\'' +
                ", endUrl='" + endUrl + '\'' +
                '}';
    }

    public static ActionEndAIEngine parseInputAction(JSONObject inputJson) throws BadInputParametersException {
        try {
            String name = inputJson.getString("name");
            long maxFinalizationTime = inputJson.getLong("max_finalization_time");
            int maxFinalizationRetries = inputJson.getInt("max_finalization_retries");
            String clientHost = inputJson.getString("client_host");
            String pingUrl = inputJson.getString("ping_url");
            String endUrl = inputJson.getString("end_url");
            return new ActionEndAIEngine(
                    name,
                    maxFinalizationTime,
                    maxFinalizationRetries,
                    clientHost,
                    pingUrl,
                    endUrl
            );
        } catch (ClassCastException | JSONException e) {
            throw new BadInputParametersException(String.format("Action end AI Engine is bad formatted: %s", e.getMessage()));
        }
    }
}
