package config.actions;

import exceptions.BadInputParametersException;
import org.json.JSONException;
import org.json.JSONObject;

public class ActionRunAIEngine extends Action {

    private final String useCase;
    private final long maxIterationTime;
    private final long maxInitializationTime;
    private final String clientHost;
    private final String serverHost;
    private final String pingUrl;
    private final String runUrl;
    private final String callbackUrl;


    public ActionRunAIEngine(
            String name,
            String useCase,
            long maxIterationTime,
            long maxInitializationTime,
            String clientHost,
            String serverHost,
            String pingUrl,
            String runUrl,
            String callbackUrl
    ) {
        super(name);
        this.useCase = useCase;
        this.maxIterationTime = maxIterationTime;
        this.maxInitializationTime = maxInitializationTime;
        this.clientHost = clientHost;
        this.serverHost = serverHost;
        this.pingUrl = pingUrl;
        this.runUrl = runUrl;
        this.callbackUrl = callbackUrl;
    }

    public String getUseCase() {
        return useCase;
    }

    public long getMaxIterationTime() {
        return maxIterationTime;
    }

    public long getMaxInitializationTime() {
        return maxInitializationTime;
    }

    public String getClientHost() {
        return clientHost;
    }

    public String getServerHost() {
        return serverHost;
    }

    public String getPingUrl() {
        return pingUrl;
    }

    public String getRunUrl() {
        return runUrl;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    @Override
    public String toString() {
        return "ActionRunAIEngine{" +
                "useCase='" + useCase + '\'' +
                ", maxIterationTime=" + maxIterationTime +
                ", maxInitializationTime=" + maxInitializationTime +
                ", clientHost='" + clientHost + '\'' +
                ", serverHost='" + serverHost + '\'' +
                ", pingUrl='" + pingUrl + '\'' +
                ", runUrl='" + runUrl + '\'' +
                ", callbackUrl='" + callbackUrl + '\'' +
                '}';
    }

    public static ActionRunAIEngine parseInputAction(JSONObject inputJson) throws BadInputParametersException {
        try {
            String name = inputJson.getString("name");
            String useCase = inputJson.getString("use_case");
            long maxIterationTime = inputJson.getLong("max_iteration_time");
            long maxInitializationTime = inputJson.getLong("max_initialization_time");
            String clientHost = inputJson.getString("client_host");
            String serverHost = inputJson.getString("server_host");
            String pingUrl = inputJson.getString("ping_url");
            String runUrl = inputJson.getString("run_url");
            String callbackUrl = inputJson.getString("callback_url");
            return new ActionRunAIEngine(
                    name,
                    useCase,
                    maxIterationTime,
                    maxInitializationTime,
                    clientHost,
                    serverHost,
                    pingUrl,
                    runUrl,
                    callbackUrl
            );
        } catch (ClassCastException | JSONException e) {
            throw new BadInputParametersException(String.format("Action run AI Engine is bad formatted: %s", e.getMessage()));
        }
    }
}
