package utils;

import exceptions.InternalException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

public class HttpMethods {

    private static final Logger logger = LogManager.getLogger(HttpMethods.class);

    public static JSONObject postJsonMethod(String url, JSONObject entity, Set<Integer> expectedStatusCode, String errorMessage) throws InternalException {
        try(CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new StringEntity(entity.toString()));
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            return responseHandling(client, httpPost, expectedStatusCode, errorMessage);
        } catch (IOException e) {
            throw new InternalException(String.format("%s. Error during post creation", errorMessage), e);
        }
    }

    public static JSONObject postMultipartMethod(
            String url,
            JSONObject jsonEntity,
            List<String> fileNameList,
            List<File> fileEntityList,
            Set<Integer> expectedStatusCode,
            String errorMessage
    ) throws InternalException {
        return multipartMethod(new HttpPost(url), jsonEntity, fileNameList, fileEntityList, expectedStatusCode, errorMessage);
    }

    public static JSONObject patchMultipartMethod(
            String url,
            JSONObject jsonEntity,
            List<String> fileNameList,
            List<File> fileEntityList,
            Set<Integer> expectedStatusCode,
            String errorMessage
    ) throws InternalException {
        return multipartMethod(new HttpPatch(url), jsonEntity, fileNameList, fileEntityList, expectedStatusCode, errorMessage);
    }

    public static void downloadFile(String url, int totalAllowedResumeRetries, OutputStream outputStream) throws IOException {
        URL urlObject;
        try {
            urlObject = new URI(url).toURL();
        } catch (URISyntaxException e) {
            throw new IOException(e.getMessage());
        }

        long fileLength = getExternalFileLength(urlObject);
        long totalReadBytes = 0;

        int retries = 0;

        while(totalReadBytes < fileLength && retries <= totalAllowedResumeRetries) {
            logger.info(String.format("Downloading file; read bytes %d; bytes to read: %d; retries: %d", totalReadBytes, fileLength, retries));

            HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
            if (totalReadBytes > 0) {
                connection.setRequestProperty("Range", String.format("bytes=%d-%d", totalReadBytes, fileLength));
                if (connection.getResponseCode() != 206) throw new IOException("External server does not support partial downloads");
            }

            try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream())) {
                byte[] dataBuffer = new byte[8192];
                int readBytes;
                while ((readBytes = in.read(dataBuffer, 0, 8192)) != -1) {
                    outputStream.write(dataBuffer, 0, readBytes);
                    totalReadBytes += readBytes;
                }
            }
            retries += 1;
        }

        if (totalReadBytes != fileLength) {
            throw new IOException(String.format("Error download file; real size: %d; downloaded size: %d", fileLength, totalReadBytes));
        }
    }

    public static void deleteMethod(
            String url,
            Set<Integer> expectedStatusCode,
            String errorMessage
    ) throws InternalException {
        try(CloseableHttpClient client = HttpClients.createDefault()) {
            HttpDelete httpDelete = new HttpDelete(url);

            try(CloseableHttpResponse response = client.execute(httpDelete)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (!expectedStatusCode.contains(statusCode)) {
                    throw new InternalException(String.format(
                            "%s. Wrong response status code. Expected: %s. Actual: %d. %s",
                            errorMessage,
                            expectedStatusCode,
                            statusCode,
                            response.getStatusLine().getReasonPhrase()),
                            null
                    );
                }
            } catch (IOException | JSONException e) {
                throw new InternalException(String.format("%s. Error during post response", errorMessage), e);
            }
        } catch (IOException e) {
            throw new InternalException(String.format("%s. Error during delete creation", errorMessage), e);
        }
    }

    private static JSONObject multipartMethod(
            HttpEntityEnclosingRequestBase httpMethod,
            JSONObject jsonEntity,
            List<String> fileNameList,
            List<File> fileEntityList,
            Set<Integer> expectedStatusCode,
            String errorMessage
    ) throws InternalException {
        try(CloseableHttpClient client = HttpClients.createDefault()) {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addTextBody("data", jsonEntity.toString(), ContentType.APPLICATION_JSON);
            for (int i = 0; i < fileNameList.size(); ++i) {
                builder.addBinaryBody(
                        fileNameList.get(i),
                        fileEntityList.get(i),
                        ContentType.DEFAULT_BINARY,
                        fileNameList.get(i)
                );
            }
            HttpEntity entity = builder.build();
            httpMethod.setEntity(entity);
            return responseHandling(client, httpMethod, expectedStatusCode, errorMessage);
        } catch (IOException e) {
            throw new InternalException(String.format("%s. Error during post creation", errorMessage), e);
        }
    }

    private static JSONObject responseHandling(
            CloseableHttpClient client,
            HttpEntityEnclosingRequestBase httpMethod,
            Set<Integer> expectedStatusCode,
            String errorMessage
    ) throws InternalException {
        try(CloseableHttpResponse response = client.execute(httpMethod)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (!expectedStatusCode.contains(statusCode)) {
                throw new InternalException(String.format(
                        "%s. Wrong response status code. Expected: %s. Actual: %d. %s",
                        errorMessage,
                        expectedStatusCode,
                        statusCode,
                        response.getStatusLine().getReasonPhrase()),
                        null
                );
            }
            return new JSONObject(new String(
                    response.getEntity().getContent().readAllBytes(),
                    StandardCharsets.UTF_8
            ));
        } catch (IOException | JSONException e) {
            throw new InternalException(String.format("%s. Error during post response", errorMessage), e);
        }
    }

    private static long getExternalFileLength(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("HEAD");
        return connection.getContentLengthLong();
    }
}
