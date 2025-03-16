package utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;

public class ExcelUtils {
    private static final String FILE_PATH="src/test/java/testdata/FlightData.xlsx";

    public static String readData(int rowNum, int colNum) {
        try {
            FileInputStream file = new FileInputStream(FILE_PATH);
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            Row row = sheet.getRow(rowNum);
            Cell cell = row.getCell(colNum);
            return cell.getStringCellValue();
        } catch (IOException e) {
            System.out.println("Error reading excel file: " + e.getMessage());
            return null;
        }
    }
}
