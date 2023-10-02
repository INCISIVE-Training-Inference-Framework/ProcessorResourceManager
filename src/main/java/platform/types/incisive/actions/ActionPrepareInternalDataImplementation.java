package platform.types.incisive.actions;

import config.actions.ActionPrepareInternalData;
import exceptions.InternalException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static utils.FileMethods.readJson;
import static utils.HttpMethods.retrieveJsonMethod;

public class ActionPrepareInternalDataImplementation {

    private static final Logger logger = LogManager.getLogger(ActionPrepareInternalDataImplementation.class);

    private static final String INTERNAL_DATA_CANCER_PATHS = "./src/main/resources/auxiliary_elements/action_prepare_internal_data";
    private static final List<String> CANCER_TYPES = List.of("breast_cancer", "lung_cancer", "prostate_cancer", "colorectal_cancer");

    public static class CancerTypeLinksData {
        private final Map<String, Map<String, Object>> columnNamesLinks;
        private final Map<String, Object> columnPositionLinks;
        private final Map<String, Object> sheetPositionLinks;
        private final Map<String, Object> sheetStartPositionsLinks;
        private final Map<String, Object> sheetNamesLinks;

        public CancerTypeLinksData(String cancerType, JSONObject dataPartnerInformation) throws IOException {
            String internalPath = String.format("%s/%s", INTERNAL_DATA_CANCER_PATHS, cancerType);
            JSONObject columnNamesLinksJson = dataPartnerInformation.getJSONObject("fields_definition").getJSONObject(cancerType);
            JSONObject sheetNamesLinksJson = dataPartnerInformation.getJSONObject("sheets_definition");
            JSONObject columnPositionLinksJson = readJson(new FileInputStream(String.format("%s/link_column_positions.json", internalPath)));
            JSONObject sheetPositionLinksJson = readJson(new FileInputStream(String.format("%s/link_sheet_positions.json", internalPath)));
            JSONObject sheetStartPositionsLinksJson = readJson(new FileInputStream(String.format("%s/link_sheet_start_positions.json", internalPath)));
            this.columnNamesLinks = new HashMap<>();
            for (String sheetNameJsonKey : columnNamesLinksJson.keySet()) {
                columnNamesLinks.put(sheetNameJsonKey, columnNamesLinksJson.getJSONObject(sheetNameJsonKey).toMap());
            }
            this.columnPositionLinks = columnPositionLinksJson.toMap();
            this.sheetPositionLinks = sheetPositionLinksJson.toMap();
            this.sheetStartPositionsLinks = sheetStartPositionsLinksJson.toMap();
            this.sheetNamesLinks = sheetNamesLinksJson.toMap();
        }

        public Map<String, Map<String, Object>> getColumnNamesLinks() {
            return columnNamesLinks;
        }

        public Map<String, Object> getColumnPositionLinks() {
            return columnPositionLinks;
        }

        public Map<String, Object> getSheetPositionLinks() {
            return sheetPositionLinks;
        }

        public Map<String, Object> getSheetStartPositionsLinks() {
            return sheetStartPositionsLinks;
        }

        public Map<String, Object> getSheetNamesLinks() {
            return sheetNamesLinks;
        }
    }

    public static void run(ActionPrepareInternalData action) throws InternalException {
        InternalException exceptionThrown = null;
        Map<String, Path> cancerFinalLocations = new HashMap<>();
        List<FileInputStream> cancerInputStreams = new ArrayList<>();
        Map<String, XSSFWorkbook> cancerWorkbooks = new HashMap<>();
        try {
            // download data partner information
            Set<Integer> expectedStatusCode = new HashSet<>();
            expectedStatusCode.add(200);
            JSONObject dataPartnerInformation = retrieveJsonMethod(
                    action.getInformationUrl(),
                    expectedStatusCode,
                    "Error while retrieving information from data partner"
            );

            // load links information
            Map<String, CancerTypeLinksData> cancerLinksDatas = new HashMap<>();
            for (String cancerType: CANCER_TYPES) {
                cancerLinksDatas.put(cancerType, new CancerTypeLinksData(cancerType, dataPartnerInformation));
            }

            // copy templates files to final location and open them
            for (String cancerType: CANCER_TYPES) {
                Path finalLocation = Path.of(
                        action.getOutputPath(),
                        String.format("%s_Cancer.xlsx", cancerType.split("_")[0].substring(0, 1).toUpperCase() + cancerType.split("_")[0].substring(1))
                );
                cancerFinalLocations.put(cancerType, finalLocation);

                // copy templates files to final location
                Files.copy(Path.of(String.format("%s/%s/template.xlsx", INTERNAL_DATA_CANCER_PATHS, cancerType)), finalLocation);

                // open templates
                FileInputStream cancerInputStream = new FileInputStream(finalLocation.toFile());
                cancerInputStreams.add(cancerInputStream);
                cancerWorkbooks.put(cancerType, new XSSFWorkbook(cancerInputStream));
            }

            // fill templates
            for (String cancerType: CANCER_TYPES) {
                logger.debug(String.format("Filling template of %s", cancerType));
                XSSFWorkbook cancerWorkbook = cancerWorkbooks.get(cancerType);
                CancerTypeLinksData cancerLinksData = cancerLinksDatas.get(cancerType);

                JSONArray patientsInformation = dataPartnerInformation.getJSONArray("patients");
                for (int i = 0; i < patientsInformation.length(); i++) {
                    JSONObject patient = patientsInformation.getJSONObject(i);

                    if (patient.getJSONObject("clinical_data").has(cancerType) && !patient.getJSONObject("clinical_data").isNull(cancerType)) {
                        fillInternalPatientCancerData(
                                cancerWorkbook,
                                patient.getJSONObject("clinical_data").getJSONObject(cancerType),
                                cancerLinksData.getColumnNamesLinks(),
                                cancerLinksData.getColumnPositionLinks(),
                                cancerLinksData.getSheetPositionLinks(),
                                cancerLinksData.getSheetStartPositionsLinks(),
                                cancerLinksData.getSheetNamesLinks()
                        );
                    }
                }

                try (OutputStream cancerFileOutput = new FileOutputStream(cancerFinalLocations.get(cancerType).toFile())) {
                    cancerWorkbook.write(cancerFileOutput);
                }
            }

        } catch (IOException | JSONException | InternalException e) {
            exceptionThrown = new InternalException("Error while processing internal data", e);
            throw exceptionThrown;
        } finally {
            // close resources
            try {
                for (XSSFWorkbook cancerWorkbook : cancerWorkbooks.values()) {
                    if (cancerWorkbook != null) cancerWorkbook.close();
                }
                for (FileInputStream cancerInputStream : cancerInputStreams) {
                    if (cancerInputStream != null) cancerInputStream.close();
                }
            } catch (IOException e) {
                if (exceptionThrown != null) {
                    logger.error("Intermediate exception");
                    e.printStackTrace();
                    throw exceptionThrown;
                } else {
                    throw new InternalException("Error while closing resources", e);
                }
            }
        }
    }

    private static void fillInternalPatientCancerData(
            Workbook cancerTypeExcel,
            JSONObject information,
            Map<String, Map<String, Object>> columnNamesLinks,
            Map<String, Object> columnPositionLinks,
            Map<String, Object> sheetPositionLinks,
            Map<String, Object> sheetStartPositionsLinks,
            Map<String, Object> sheetNamesLinks
    ) throws IOException, InternalException {
        for (String sheetNameJsonKey: information.keySet()) {
            logger.debug(String.format("Sheet name: %s", sheetNameJsonKey));
            Object sheetInfoTemp = information.get(sheetNameJsonKey);

            if (!sheetNamesLinks.containsKey(sheetNameJsonKey)) throw new InternalException(String.format("Not existent sheetJsonKeyNameLink: %s", sheetNameJsonKey), null);
            String sheetName = (String) sheetNamesLinks.get(sheetNameJsonKey);

            Sheet sheet = cancerTypeExcel.getSheet(sheetName);
            if (sheet == null) throw new InternalException(String.format("Not existent sheet with name %s", sheetName), null);

            if (!sheetStartPositionsLinks.containsKey(sheetName)) throw new InternalException(String.format("Not existent initial row position for sheet %s", sheetName), null);
            int initialRowPosition = (int) sheetStartPositionsLinks.get(sheetName);

            if (!(sheetInfoTemp instanceof JSONObject) && !(sheetInfoTemp instanceof JSONArray)) throw new InternalException(String.format("Unexpected token on sheetInfo: %s", sheetInfoTemp.getClass().getSimpleName()), null);

            JSONArray inputItems;
            if (sheetInfoTemp instanceof JSONObject sheetInfo) {
                inputItems = new JSONArray();
                inputItems.put(sheetInfo);
            } else {
                inputItems = (JSONArray) sheetInfoTemp;
            }

            for (int i = 0; i < inputItems.length(); i++) {
                JSONObject item = inputItems.getJSONObject(i);

                Row row = sheet.createRow(initialRowPosition);
                initialRowPosition += 1;

                item.keys().forEachRemaining(key -> {
                    try {
                        if (!item.isNull(key)) {
                            String value = item.getString(key);

                            if (!columnNamesLinks.containsKey(sheetNameJsonKey)) throw new InternalException(String.format("Not existent column name in columnNamesLinks: %s", sheetNameJsonKey), null);
                            Map columnNamesLinksSheetInfo = columnNamesLinks.get(sheetNameJsonKey);

                            if (!columnNamesLinksSheetInfo.containsKey(key)) throw new InternalException(String.format("Not existent column link name in columnNamesLinksSheetInfo: %s", key), null);
                            String columnName = (String) columnNamesLinksSheetInfo.get(key);

                            if (!columnPositionLinks.containsKey(sheetName)) throw new InternalException(String.format("Not existent sheet in columnPositionLinks: %s", sheetName), null);
                            Map columnPositionSheetInfo = (Map) columnPositionLinks.get(sheetName);

                            if (!columnPositionSheetInfo.containsKey(columnName)) {
                                throw new InternalException(String.format("Not existent columnName in columnPositionSheetInfo: %s", columnName), null);
                            }
                            int columnPosition = (int) columnPositionSheetInfo.get(columnName);

                            Cell cell = row.createCell(columnPosition);
                            boolean _continue = true;
                            try {
                                long aux = Long.parseLong(value);
                                cell.setCellValue(aux);
                                _continue = false;
                            } catch (NumberFormatException e) {
                                // empty
                            }
                            if (_continue) {
                                try {
                                    double aux = Double.parseDouble(value);
                                    cell.setCellValue(aux);
                                    _continue = false;
                                } catch (NumberFormatException e) {
                                    // empty
                                }
                                if (_continue) {
                                    cell.setCellValue(value);
                                }
                            }
                            logger.debug(String.format("Sheet: {%s}\tKey: {%s}\tValue: {%s}\t", sheetName, key, value));
                        } else {
                            logger.debug(String.format("Sheet: {%s}\tKey: {%s}\tValue: {null}\t", sheetName, key));
                        }
                    } catch (Exception e) {
                        logger.error(e);
                    }
                });
            }

            sheetStartPositionsLinks.put(sheetName, initialRowPosition);
        }
    }
}
