package utils;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ExcelUtils {
    private static final String FILE_PATH="./src/test/java/testdata/FlightData.xlsx";
    private Sheet sheet;

    public ExcelUtils(String sheetName) throws IOException {
        FileInputStream file = new FileInputStream(FILE_PATH);
        Workbook workbook = new XSSFWorkbook(file);
        sheet = workbook.getSheet(sheetName);
        file.close();
    }

    public Map<String, String> getRowData(int rowNum) {
        Map<String, String> data = new HashMap<>();
        Row headerRow = sheet.getRow(0);
        Row dataRow = sheet.getRow(rowNum);

        if(headerRow == null || dataRow == null) {
            throw new RuntimeException("Invalid row no: " + rowNum);
        }
        for(int i = 0; i < headerRow.getLastCellNum(); i++) {
            String key = headerRow.getCell(i).getStringCellValue().trim();
            String value = dataRow.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).toString().trim();
            data.put(key, value);
        }
        return data;
    }

    public int getRowCount() {
        return sheet.getLastRowNum();
    }
}
