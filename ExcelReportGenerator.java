package org.insight;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

public class ExcelReportGenerator {
private static final String SHEET_ID = "1AWdr6qS4FvriYe_H9hVfERe7TgZ7BgeWvVS6x5fS37c";
public static void downloadMonthlyReport(Sheets sheetsService) throws IOException {
    String lastMonth = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("MMMM yyyy"));
    String range = lastMonth + "!A:F"; // Adjust column range if needed
    ValueRange response = sheetsService.spreadsheets().values().get(SHEET_ID, range).execute();
    List<List<Object>> values = response.getValues();
    if (values != null && !values.isEmpty()) {
        String fileName = "Eero_Stability_Report_" + lastMonth.replace(" ", "_") + ".xlsx";
        saveToExcel(values, fileName);
        System.out.println(":white_check_mark: Monthly report saved: " + fileName);
    } else {
        System.out.println(":x: No data found for: " + lastMonth);
    }
}
private static void saveToExcel(List<List<Object>> data, String fileName) throws IOException {
    Workbook workbook = new XSSFWorkbook();
    Sheet excelSheet = workbook.createSheet("Report");
    for (int rowNum = 0; rowNum < data.size(); rowNum++) {
        Row row = excelSheet.createRow(rowNum);
        List<Object> rowData = data.get(rowNum);
        for (int colNum = 0; colNum < rowData.size(); colNum++) {
            Cell cell = row.createCell(colNum);
            cell.setCellValue(rowData.get(colNum).toString());
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setFontHeightInPoints((short) 10);
            if (rowNum == 0) { // Title row
                font.setBold(true);
                font.setFontHeightInPoints((short) 13);
            } else if (rowNum == 2) { // Header row
                font.setBold(true);
                font.setFontHeightInPoints((short) 12);
            }
            style.setFont(font);
            cell.setCellStyle(style);
        }
    }
    String downloadPath = "C:\\Users\\dhinah\\Downloads\\Eero Stability Monthly Report\\" + fileName;
    try (FileOutputStream fileOut = new FileOutputStream(new File(downloadPath))) {
        workbook.write(fileOut);
    }
    workbook.close();
    System.out.println(":open_file_folder: Excel file saved at: " + downloadPath);
}
}
