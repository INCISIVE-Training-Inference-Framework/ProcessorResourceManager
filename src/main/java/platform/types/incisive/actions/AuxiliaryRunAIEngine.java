package platform.types.incisive.actions;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import exceptions.InternalException;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import utils.FileMethods;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class AuxiliaryRunAIEngine {

    private static final int HTTP_CALLS_TIMEOUT = 3;  // seconds
    private static CountDownLatch countDownLatch = null;
    private static ServerHandlingOutput serverHandlingOutput = null;
    private static final Logger logger = LogManager.getLogger(AuxiliaryRunAIEngine.class);

    private final long maxIterationTime;
    private final String clientHost;
    private final String serverHost;
    private final String runUrl;
    private final String callbackUrl;

    private HttpServer server;

    public AuxiliaryRunAIEngine(
            long maxIterationTime,
            String clientHost,
            String serverHost,
            String runUrl,
            String callbackUrl
    ) {
        this.maxIterationTime =maxIterationTime;
        this.clientHost = clientHost;
        this.serverHost = serverHost;
        this.runUrl = runUrl;
        this.callbackUrl = callbackUrl;
    }

    private final class ServerHandlingOutput {

        private final boolean goodAck;
        private final String message;

        private ServerHandlingOutput(boolean goodAck, String message) {
            this.goodAck = goodAck;
            this.message = message;
        }

        private boolean isGoodAck() {
            return this.goodAck;
        }

        private String getMessage() {
            return this.message;
        }
    }

    public void initialize() throws InternalException {
        String serverIp = serverHost.split(":")[0];
        int serverPort = Integer.parseInt(serverHost.split(":")[1]);

        try {
            this.server = HttpServer.create(new InetSocketAddress(serverIp, serverPort), 0);
        } catch (IOException e) {
            throw new InternalException("Error while initializing server", e);
        }
        this.server.createContext(this.callbackUrl, new ServerHandler());
        this.server.setExecutor(null); // creates a default executor
        this.server.start();
    }

    public void run(String useCase) throws InternalException {
        logger.debug(String.format("Running the AI Engine. Use case: %s", useCase));

        RequestConfig config = RequestConfig.custom().setConnectTimeout(HTTP_CALLS_TIMEOUT * 1000).build();
        try(CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build()) {

            // create post
            HttpPost httpPost = new HttpPost(String.format(
                    "http://%s%s?use_case=%s&callback_url=%s",
                    this.clientHost,
                    this.runUrl,
                    useCase,
                    String.format("http://%s%s", this.serverHost, this.callbackUrl)
            ));
            StringEntity entity = new StringEntity("{}");
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // send post
            countDownLatch = new CountDownLatch(1);
            serverHandlingOutput = null;
            try(CloseableHttpResponse response = client.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK) throw new InternalException("Error while running use case. Status code equal to " + statusCode + ". " + response.getStatusLine().getReasonPhrase(), null);
            } catch (IOException e) {
                throw new InternalException("Error while running use case (during the query)", e);
            }

            // wait for ack
            boolean received;
            try {
                received = countDownLatch.await(this.maxIterationTime, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new InternalException("Error while running use case. While waiting the ack", e);
            }

            // check ack
            if (!received) throw new InternalException("Error while running use case. The end of the iteration was not notified on time", null);
            if (serverHandlingOutput == null) throw new InternalException("Error while running use case. The server did not save the response information", null);
            if (!serverHandlingOutput.isGoodAck()) {
                JSONObject errorMessageJson;
                String errorMessage;
                try {
                    errorMessageJson = new JSONObject(serverHandlingOutput.getMessage());
                    errorMessage = errorMessageJson.getString("message");
                } catch (JSONException e) {
                    throw new InternalException("Error while running use case. Error while parsing returning error message", e);
                }
                throw new InternalException(String.format("Error while running use case. AI Engine error -> %s", errorMessage), null);
            }

        } catch (IOException e) {
            throw new InternalException("Error while running use case (during the query creation)", e);
        }
    }

    public void clean() throws InternalException {
        // stop own server
        try {
            if (this.server != null) this.server.stop(0);
        } catch (Exception e) {
            throw new InternalException("Error while shutting down own server", e);
        }
    }

    private class ServerHandler implements HttpHandler {

        // ACK response handler
        @Override
        public void handle(HttpExchange httpExchange) {
            logger.debug("Received callback");
            boolean goodAck = true;
            int responseStatus = 200;
            String message = null;

            if (!"POST".equals(httpExchange.getRequestMethod())) {
                goodAck = false;
                responseStatus = 405;
                message = "Bad request. Only POST method allowed";
            } else {
                try (InputStream inputStream = httpExchange.getRequestBody()) {
                    try {
                        JSONObject jsonResponse = FileMethods.readJson(inputStream);
                        boolean success = (boolean) jsonResponse.get("SUCCESS");
                        if (!success) {
                            goodAck = false;
                            message = jsonResponse.toString();
                        }
                    } catch (IOException | JSONException e) {
                        goodAck = false;
                        responseStatus = 400;
                        message = String.format("Bad request: incorrect JSON format. %s", e.getMessage());
                    }
                } catch (IOException e) {
                    goodAck = false;
                    responseStatus = 500;
                    message = String.format("Server error: while reading response. %s", e.getMessage());
                }
            }

            try(OutputStream outputStream = httpExchange.getResponseBody()) {
                // encode response
                JSONObject response = new JSONObject();
                if (message != null) {
                    response.put("message", message);
                }

                // send response
                httpExchange.sendResponseHeaders(responseStatus, response.toString().length());
                outputStream.write(response.toString().getBytes());
                outputStream.flush();
            } catch (IOException e) {
                goodAck = false;
                message = String.format("Server error: while writing response. %s", e.getMessage());
            }

            serverHandlingOutput = new ServerHandlingOutput(goodAck, message);
            countDownLatch.countDown();
            logger.debug(goodAck + " " + responseStatus + " " + message);
        }

    }


}