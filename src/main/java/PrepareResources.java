import org.dhatim.fastexcel.reader.Cell;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.List;

import static utils.FileMethods.writeJson;

// For local run only -> prepare auxiliary resources for internal data processing
// no proper exception handling
public class PrepareResources {

    private static void prepareInternalDataResources() throws Exception {
        String template = "./src/main/resources/auxiliary_elements/action_prepare_internal_data/breast/template.xlsx";
        String outputDictionaryColumnPositions = "./src/main/resources/auxiliary_elements/action_prepare_internal_data/breast/link_column_positions.json";
        String outputDictionarySheetPositions = "./src/main/resources/auxiliary_elements/action_prepare_internal_data/breast/link_sheet_positions.json";
        String outputDictionarySheetStartPositions = "./src/main/resources/auxiliary_elements/action_prepare_internal_data/breast/link_sheet_start_positions.json";

        // create dictionary with links: column name -> excel column position
        JSONObject linksColumnPositions = new JSONObject();

        // create dictionary with links: sheet name -> sheet position
        JSONObject linksSheetPositions = new JSONObject();

        // create dictionary with links: sheet name -> sheet row start position
        JSONObject linksSheetStartPositions = new JSONObject();

        try (FileInputStream file = new FileInputStream(template);
             ReadableWorkbook wb = new ReadableWorkbook(file)) {

            List<Sheet> sheets = wb.getSheets().toList();
            for (Sheet sheet: sheets) {
                linksSheetPositions.put(sheet.getName(), sheet.getIndex());

                JSONObject sheetLinks = new JSONObject();

                // find row index with column names -> some sheets have an additional row on top
                Integer rowWithColumnNames = null;
                List<Row> rows = sheet.read();
                int rowIndex = 0;
                while (rowWithColumnNames == null && rowIndex < rows.size()) {
                    Row row = rows.get(rowIndex);
                    if (row.hasCell(0)) {
                        Cell firstCell = row.getCell(0);
                        if (firstCell != null) {
                            String firstCellValue = firstCell.getRawValue();
                            if (firstCellValue != null && firstCellValue.equals("Patient Number"))
                                rowWithColumnNames = rowIndex;
                        }
                    }
                    ++rowIndex;
                }
                if (rowWithColumnNames == null) throw new Exception("Row with column names not found");

                // collect column name and position links
                linksSheetStartPositions.put(sheet.getName(), rowWithColumnNames + 2);  // taking into account example row
                for (Cell cell : rows.get(rowWithColumnNames)) {
                    if (cell != null && cell.getRawValue() != null) {
                        sheetLinks.put(cell.getRawValue(), cell.getColumnIndex());
                    }
                }
                linksColumnPositions.put(sheet.getName(), sheetLinks);
            }
        }
        writeJson(Paths.get(outputDictionaryColumnPositions), linksColumnPositions);
        writeJson(Paths.get(outputDictionarySheetPositions), linksSheetPositions);
        writeJson(Paths.get(outputDictionarySheetStartPositions), linksSheetStartPositions);
    }

    public static void main(String[] args) {
        try {
            prepareInternalDataResources();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
