package com.autoadda.apis.part.response;

import com.autoadda.apis.part.request.InventoryPartExcelRow;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data
public class InventoryPartExcelUploadResponse {

    private Integer totalRows;
    private Integer successfulRows;
    private Integer failedRows;
    private List<InventoryPartExcelRow> failedRowDetails;
    private String message;
    private String statusCode;

    public InventoryPartExcelUploadResponse(Integer totalRows, Integer successfulRows, Integer failedRows) {
        this.totalRows = totalRows;
        this.successfulRows = successfulRows;
        this.failedRows = failedRows;
        this.failedRowDetails = new ArrayList<>();
    }

    public void addFailedRow(InventoryPartExcelRow row) {
        if (this.failedRowDetails == null) {
            this.failedRowDetails = new ArrayList<>();
        }
        this.failedRowDetails.add(row);
    }
}
