package org.insight;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AddSheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.CellFormat;
import com.google.api.services.sheets.v4.model.DimensionProperties;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.MergeCellsRequest;
import com.google.api.services.sheets.v4.model.RepeatCellRequest;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.TextFormat;
import com.google.api.services.sheets.v4.model.UpdateDimensionPropertiesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
public class GoogleSheetsUpdater { private static final String SHEET_ID = "1AWdr6qS4FvriYe_H9hVfERe7TgZ7BgeWvVS6x5fS37c";
public static void updateGoogleSheet(int onlineCount, int offlineCount, int totalCount, String downloadData, String uploadData) throws IOException, GeneralSecurityException {
    Sheets sheetsService = SheetsServiceUtil.getSheetsService();
    String sheetTitle = checkAndCreateNewSheet(sheetsService);
    ensureHeaders(sheetsService, sheetTitle);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String formattedDate = LocalDateTime.now().format(formatter);
    List<List<Object>> values = Arrays.asList(
        Arrays.asList(formattedDate, onlineCount, offlineCount, totalCount, downloadData, uploadData)
    );
    ValueRange body = new ValueRange().setValues(values);
    sheetsService.spreadsheets().values()
        .append(SHEET_ID, sheetTitle + "!A:F", body)
        .setValueInputOption("RAW")
        .setInsertDataOption("INSERT_ROWS")
        .execute();
    applyFormatting(sheetsService, sheetTitle);
    
}

private static String checkAndCreateNewSheet(Sheets sheetsService) throws IOException {
    String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
    // Check if the sheet already exists
    List<Sheet> sheets = sheetsService.spreadsheets().get(SHEET_ID).execute().getSheets();
    for (Sheet sheet : sheets) {
        if (sheet.getProperties().getTitle().equals(currentMonth)) {
            return currentMonth;
        }
    }
    // Download and save the previous month's data before creating a new sheet
    ExcelReportGenerator.downloadMonthlyReport(sheetsService);
    // Create a new sheet for the current month
    AddSheetRequest addSheetRequest = new AddSheetRequest()
        .setProperties(new SheetProperties().setTitle(currentMonth));
    BatchUpdateSpreadsheetRequest batchRequest = new BatchUpdateSpreadsheetRequest()
        .setRequests(Arrays.asList(new Request().setAddSheet(addSheetRequest)));
    sheetsService.spreadsheets().batchUpdate(SHEET_ID, batchRequest).execute();
    try {
        Thread.sleep(2000); // 1-second delay (Google Sheets needs time to register the new sheet)
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    // Apply formatting after the new sheet is created
    
    applyFormatting(sheetsService, currentMonth);
    System.out.println(":white_check_mark: New Google Sheet created and formatted: " + currentMonth);
    return currentMonth;
}


private static void ensureHeaders(Sheets sheetsService, String sheetTitle) throws IOException {
    String titleRange = sheetTitle + "!A1:F1";
    String emptyRowRange = sheetTitle + "!A2:F2";
    String headerRange = sheetTitle + "!A3:F3";
    String monthName = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM")).toUpperCase();
    List<List<Object>> titleRow = Arrays.asList(
        Arrays.asList(monthName + " MONTH STABILITY REPORT")
    );
    List<List<Object>> emptyRow = Arrays.asList(Arrays.asList("", "", "", "", "", ""));
    List<List<Object>> headerRow = Arrays.asList(
        Arrays.asList("DATE", "ONLINE DEVICES", "OFFLINE DEVICES", "TOTAL DEVICES", "DOWNLOAD DATA", "UPLOAD DATA")
    );
    sheetsService.spreadsheets().values().update(SHEET_ID, titleRange, new ValueRange().setValues(titleRow))
        .setValueInputOption("RAW").execute();
    sheetsService.spreadsheets().values().update(SHEET_ID, emptyRowRange, new ValueRange().setValues(emptyRow))
        .setValueInputOption("RAW").execute();
    sheetsService.spreadsheets().values().update(SHEET_ID, headerRange, new ValueRange().setValues(headerRow))
        .setValueInputOption("RAW").execute();
}
private static void applyFormatting(Sheets sheetsService, String sheetTitle) throws IOException {
    Spreadsheet spreadsheet = sheetsService.spreadsheets().get(SHEET_ID).execute();
    Integer sheetId = spreadsheet.getSheets().stream()
        .filter(s -> s.getProperties().getTitle().equals(sheetTitle))
        .findFirst().get().getProperties().getSheetId();
    List<Request> requests = new ArrayList<>();
    // **Merge Title Across 8 Columns for Full Visibility**
    requests.add(new Request().setMergeCells(new MergeCellsRequest()
        .setRange(new GridRange().setSheetId(sheetId).setStartRowIndex(0).setEndRowIndex(1).setStartColumnIndex(0).setEndColumnIndex(8))
        .setMergeType("MERGE_ALL")));
    // **Title Formatting (Centered, Bold, Larger Font)**
    requests.add(new Request().setRepeatCell(new RepeatCellRequest()
        .setRange(new GridRange().setSheetId(sheetId).setStartRowIndex(0).setEndRowIndex(1).setStartColumnIndex(0).setEndColumnIndex(8))
        .setCell(new CellData().setUserEnteredFormat(
            new CellFormat()
                .setHorizontalAlignment("CENTER")
                .setTextFormat(new TextFormat().setBold(true).setFontSize(14))
        )).setFields("userEnteredFormat")));
    // **Increase Row Height for Title**
    requests.add(new Request().setUpdateDimensionProperties(new UpdateDimensionPropertiesRequest()
        .setRange(new DimensionRange().setSheetId(sheetId).setDimension("ROWS").setStartIndex(0).setEndIndex(1))
        .setProperties(new DimensionProperties().setPixelSize(50))
        .setFields("pixelSize")));
    // **Headers Formatting**
    requests.add(new Request().setRepeatCell(new RepeatCellRequest()
        .setRange(new GridRange().setSheetId(sheetId).setStartRowIndex(2).setEndRowIndex(3).setStartColumnIndex(0).setEndColumnIndex(8))
        .setCell(new CellData().setUserEnteredFormat(
            new CellFormat()
                .setHorizontalAlignment("CENTER")
                .setTextFormat(new TextFormat().setBold(true).setFontSize(12))
        )).setFields("userEnteredFormat")));
    // **Increase Row Height for Headers**
    requests.add(new Request().setUpdateDimensionProperties(new UpdateDimensionPropertiesRequest()
        .setRange(new DimensionRange().setSheetId(sheetId).setDimension("ROWS").setStartIndex(2).setEndIndex(3))
        .setProperties(new DimensionProperties().setPixelSize(35))
        .setFields("pixelSize")));
    // **Set Column Widths for More Space**
    requests.add(new Request().setUpdateDimensionProperties(new UpdateDimensionPropertiesRequest()
        .setRange(new DimensionRange().setSheetId(sheetId).setDimension("COLUMNS").setStartIndex(0).setEndIndex(8))
        .setProperties(new DimensionProperties().setPixelSize(150))
        .setFields("pixelSize")));
    // **Reduce Row Height for Data Rows (From Row 4 Onwards)**
    requests.add(new Request().setUpdateDimensionProperties(new UpdateDimensionPropertiesRequest()
        .setRange(new DimensionRange().setSheetId(sheetId).setDimension("ROWS").setStartIndex(3))  // Row 4 onwards
        .setProperties(new DimensionProperties().setPixelSize(20))  // Reduced height
        .setFields("pixelSize")));
    // Apply all formatting requests
    BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest().setRequests(requests);
    sheetsService.spreadsheets().batchUpdate(SHEET_ID, batchUpdateRequest).execute();
}
}
