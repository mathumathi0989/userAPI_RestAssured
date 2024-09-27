package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtil {
	// Method to get all data from the specified sheet
    public static List<Map<String, String>> getAllExcelData(String filePath, String sheetName) {
        List<Map<String, String>> allData = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);

            // Check if the sheet is null
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet " + sheetName + " does not exist.");
            }

            Row headerRow = sheet.getRow(0); // Assuming the first row contains headers
            DataFormatter formatter = new DataFormatter();

            // Loop through each row starting from row 1 (skipping headers)
            for (int rowIndex = 1; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++) {
                Row dataRow = sheet.getRow(rowIndex);
                Map<String, String> data = new HashMap<>();

                // Check if the data row is null
                if (dataRow != null) {
                    // Loop through each cell in the header row to read data
                    for (int cellIndex = 0; cellIndex < headerRow.getPhysicalNumberOfCells(); cellIndex++) {
                        Cell headerCell = headerRow.getCell(cellIndex);
                        if (headerCell != null) {
                            String key = formatter.formatCellValue(headerCell);
                            Cell valueCell = dataRow.getCell(cellIndex);
                            String value = (valueCell != null) ? formatter.formatCellValue(valueCell) : null; // Handle null cells
                            data.put(key, value);
                        }
                    }
                    allData.add(data); // Add the data map for the current row to the list
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allData; // Return the list of maps containing all rows' data
    }

    // Existing method to get data for a specific row
    public static Map<String, String> getExcelData(String filePath, String sheetName, int rowNum) {
        Map<String, String> data = new HashMap<>();
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
             
            Sheet sheet = workbook.getSheet(sheetName);

            // Check if the specified row number exists
            if (rowNum < 0 || rowNum >= sheet.getPhysicalNumberOfRows()) {
                throw new IllegalArgumentException("Row number " + rowNum + " does not exist in the sheet.");
            }

            Row headerRow = sheet.getRow(0); // Assuming the first row contains headers
            Row dataRow = sheet.getRow(rowNum);

            // Check if the data row is null
            if (dataRow == null) {
                throw new IllegalArgumentException("Data row " + rowNum + " is null in the sheet.");
            }

            DataFormatter formatter = new DataFormatter();

            // Loop through each cell in the header row to read data
            for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
                Cell headerCell = headerRow.getCell(i);
                if (headerCell != null) {
                    String key = formatter.formatCellValue(headerCell);
                    Cell valueCell = dataRow.getCell(i);
                    String value = (valueCell != null) ? formatter.formatCellValue(valueCell) : null; // Handle null cells
                    data.put(key, value);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
	
}
