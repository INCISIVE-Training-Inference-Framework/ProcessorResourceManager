package platform.types.incisive.actions;

import config.actions.ActionPingAIEngine;
import exceptions.InternalException;
import org.apache.http.HttpStatus;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ConnectException;
import java.sql.Timestamp;
import java.time.Instant;

public class ActionPingAIEngineImplementation {

    private static final int HTTP_CALLS_TIMEOUT = 3;  // seconds
    private static final int ITERATION_SLEEP = 3;  // seconds

    private static final Logger logger = LogManager.getLogger(ActionPingAIEngineImplementation.class);

    public static void run(ActionPingAIEngine action) throws InternalException {
        Timestamp startTime = Timestamp.from(Instant.now());
        Timestamp currentTime = startTime;
        boolean AIEngineStarted = false;

        // set config
        RequestConfig config = RequestConfig.custom().setConnectTimeout(HTTP_CALLS_TIMEOUT * 1000).build();

        // create client
        try (CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build()) {

            // iterate until AIEngine is ready
            while (!AIEngineStarted && (currentTime.getTime() < (startTime.getTime() + action.getMaxInitializationTime() * 1000))) {

                // query AI Engine
                try(CloseableHttpResponse response = client.execute(new HttpGet(String.format("http://%s%s", action.getClientHost(), action.getPingUrl())))) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == HttpStatus.SC_OK) AIEngineStarted = true;
                    else throw new InternalException(String.format("Error while waiting for the AI Engine to be ready (during the query). Incorrect initialization with status code %d", statusCode), null);
                } catch (ConnectException | NoHttpResponseException | ConnectTimeoutException e) {
                    // do nothing
                } catch (IOException e) {
                    throw new InternalException("Error while waiting for the AI Engine to be ready (during the query)", e);
                }

                // sleep 3 seconds
                if (!AIEngineStarted) {
                    try {
                        Thread.sleep(ITERATION_SLEEP * 1000);
                    } catch (InterruptedException e) {
                        throw new InternalException("Error while waiting for the AI Engine to be ready (during the thread sleep)", e);
                    }
                }

                currentTime = Timestamp.from(Instant.now());
            }

        } catch (IOException e) {
            throw new InternalException("Error while waiting for the AI Engine to be ready (during the client instantiation)", e);
        }

        if (!AIEngineStarted) throw new InternalException("Error while waiting for the AI Engine to be ready. It did not start before the timeout", null);
    }
}
