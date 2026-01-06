package com.autoadda.apis.part.service;

import com.autoadda.apis.part.request.InventoryPartExcelRow;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ExcelParserService {

    private static final List<String> VALID_CONDITIONS = Arrays.asList ( "USED", "GENUINE", "AFTER_MARKET" );

    public List<InventoryPartExcelRow> parseExcelFile(MultipartFile file) throws IOException {
        List<InventoryPartExcelRow> rows = new ArrayList<> ( );

        try (InputStream inputStream = file.getInputStream ( );
             Workbook workbook = new XSSFWorkbook ( inputStream )) {

            Sheet sheet = workbook.getSheetAt ( 0 );
            int lastRowNum = sheet.getLastRowNum ( );

            skipFirstRow(lastRowNum, sheet, rows);
        }

        return rows;
    }

    private void skipFirstRow(int lastRowNum, Sheet sheet, List<InventoryPartExcelRow> rows) {
        for (int rowIndex = 0; rowIndex <= lastRowNum; rowIndex++) {
            Row row = sheet.getRow ( rowIndex );

            // Skip header row and completely empty rows
            if ( rowIndex == 0 || row == null || isRowEmpty ( row ) ) {
                continue;
            }

            InventoryPartExcelRow excelRow = parseRow ( row, rowIndex );
            rows.add ( excelRow );
        }
    }

    /**
     * Check if a row is completely empty (all cells are null or blank)
     */
    private boolean isRowEmpty(Row row) {
        if ( row == null ) {
            return true;
        }

        for (int cellIndex = row.getFirstCellNum ( ); cellIndex < row.getLastCellNum ( ); cellIndex++) {
            Cell cell = row.getCell ( cellIndex );
            if ( cell != null && cell.getCellType ( ) != org.apache.poi.ss.usermodel.CellType.BLANK ) {
                String cellValue = getCellValueAsString ( cell );
                if ( cellValue != null && ! cellValue.trim ( ).isEmpty ( ) ) {
                    return false;
                }
            }
        }

        return true;
    }

    private InventoryPartExcelRow parseRow(Row row, int rowNumber) {
        InventoryPartExcelRow excelRow = InventoryPartExcelRow.builder ( )
                .rowNumber ( rowNumber )
                .isValid ( true )
                .build ( );

        try {
            // Column 0: Part Number
            String partNumber = getCellValueAsString ( row.getCell ( 0 ) );
            excelRow.setPartNumber ( partNumber );

            // Column 1: SKU
            String sku = getCellValueAsString ( row.getCell ( 1 ) );
            excelRow.setSku ( sku );

            // Column 2: Quantity
            Integer quantity = getCellValueAsInteger ( row.getCell ( 2 ) );
            excelRow.setQuantity ( quantity );

            // Column 3: Price/PC
            Double pricePerPiece = getCellValueAsDouble ( row.getCell ( 3 ) );
            excelRow.setPricePerPiece ( pricePerPiece );

            // Column 4: Condition
            String condition = getCellValueAsString ( row.getCell ( 4 ) );
            // Normalize condition to uppercase and replace spaces/dashes with underscores
            if ( condition != null ) {
                condition = normalizeCondition ( condition );
            }
            excelRow.setCondition ( condition );

            // Validate the row
            validateRow ( excelRow );

        }
        catch ( Exception e ) {
            excelRow.setIsValid ( false );
            excelRow.setErrorMessage ( "Error parsing row: " + e.getMessage ( ) );
        }

        return excelRow;
    }

    private void validateRow(InventoryPartExcelRow row) {
        StringBuilder errors = new StringBuilder ( );

        if ( row.getPartNumber ( ) == null || row.getPartNumber ( ).trim ( ).isEmpty ( ) ) {
            errors.append ( "Part Number is required. " );
        }

        if ( row.getSku ( ) == null || row.getSku ( ).trim ( ).isEmpty ( ) ) {
            errors.append ( "SKU is required. " );
        }

        if ( row.getQuantity ( ) == null || row.getQuantity ( ) <= 0 ) {
            errors.append ( "Quantity must be greater than 0. " );
        }

        if ( row.getPricePerPiece ( ) == null || row.getPricePerPiece ( ) <= 0 ) {
            errors.append ( "Price/PC must be greater than 0. " );
        }

        if ( row.getCondition ( ) == null || row.getCondition ( ).trim ( ).isEmpty ( ) ) {
            errors.append ( "Condition is required. " );
        } else if ( ! VALID_CONDITIONS.contains ( row.getCondition ( ) ) ) {
            errors.append ( "Condition must be one of: GENUINE, USED, AFTER_MARKET. " );
        }

        if ( !errors.isEmpty() ) {
            row.setIsValid ( false );
            row.setErrorMessage ( errors.toString ( ).trim ( ) );
        }
    }

    private String getCellValueAsString(Cell cell) {
        if ( cell == null ) {
            return null;
        }

        switch (cell.getCellType ( )) {
            case STRING:
                return cell.getStringCellValue ( ).trim ( );
            case NUMERIC:
                return String.valueOf ( (long) cell.getNumericCellValue ( ) );
            case BOOLEAN:
                return String.valueOf ( cell.getBooleanCellValue ( ) );
            case FORMULA:
                return cell.getCellFormula ( );
            default:
                return null;
        }
    }

    private Integer getCellValueAsInteger(Cell cell) {
        if ( cell == null ) {
            return null;
        }

        switch (cell.getCellType ( )) {
            case NUMERIC:
                return (int) cell.getNumericCellValue ( );
            case STRING:
                try {
                    return Integer.parseInt ( cell.getStringCellValue ( ).trim ( ) );
                }
                catch ( NumberFormatException e ) {
                    return null;
                }
            default:
                return null;
        }
    }

    private Double getCellValueAsDouble(Cell cell) {
        if ( cell == null ) {
            return null;
        }

        switch (cell.getCellType ( )) {
            case NUMERIC:
                return cell.getNumericCellValue ( );
            case STRING:
                try {
                    return Double.parseDouble ( cell.getStringCellValue ( ).trim ( ) );
                }
                catch ( NumberFormatException e ) {
                    return null;
                }
            default:
                return null;
        }
    }

    /**
     * Normalizes condition string to match ProductCondition enum values
     * Converts: "Genuine" -> "GENUINE", "Used" -> "USED", "AfterMarket" -> "AFTER_MARKET", etc.
     */
    private String normalizeCondition(String condition) {
        if ( condition == null ) {
            return null;
        }

        String trimmed = condition.trim ( ).toUpperCase ( );

        // Handle common variations
        if ( trimmed.equals ( "AFTERMARKET" ) || trimmed.equals ( "AFTER-MARKET" ) ) {
            return "AFTER_MARKET";
        }

        return trimmed;
    }
}