package utils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileMethods {

    public static JSONObject readJson(InputStream inputStream) throws IOException {
        try (BufferedReader bufferReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;

            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bufferReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            return new JSONObject(stringBuilder.toString());
        }
    }

    public static void writeJson(Path outputPath, JSONObject jsonObject) throws IOException {
        if (Files.exists(outputPath)) Files.delete(outputPath);
        Files.createFile(outputPath);
        Files.writeString(outputPath, jsonObject.toString(4));
    }
}