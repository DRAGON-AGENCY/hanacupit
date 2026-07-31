package com.cupit.csv.importer;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.model.ImportBatch;
import com.cupit.model.PaygateStoreMapping;
import com.cupit.model.RakutenPayTransaction;
import com.cupit.repository.PaygateMappingRepository;
import com.cupit.repository.RakutenPayTransactionRepository;

/**
 * 楽天ペイ取引明細Excelを解析してrakuten_pay_transactionに登録する。
 * ヘッダー行: 1行目（0-indexed: 0）、データ: 2行目以降（0-indexed: 1）。
 * 列構成（0-indexed）:
 *   0=ORDER_KEY, 1=PAYMENT_STATUS, 2=STORE_NO, 3=STORE_NAME,
 *   4=SHOP_CODE, 5=MERCHANT_CODE, 6=TOTAL_AMOUNT,
 *   7=CREATE_DATETIME, 8=CANCELED_DATETIME
 * 1ファイルに花キューピット全店舗分のSTORE_NOが行単位で混在するため、取引コードは
 * 行ごとにSTORE_NOでm_paygate_store_mapping を引き直して解決する。マッピングが
 * 見つからない行・データ変換エラーが発生した行はその行だけを登録せずスキップし、
 * ファイルの最後まで処理を継続する（データエラーによってファイル全体をロール
 * バックすることはしない）。
 */
@Component
public class RakutenpayFileImporter extends AbstractFileImporter {

    private static final int DATA_START_ROW = 1; // 0-indexed（Excel2行目）

    private final RakutenPayTransactionRepository rakutenPayTransactionRepository;
    private final PaygateMappingRepository paygateMappingRepository;

    public RakutenpayFileImporter(
            RakutenPayTransactionRepository rakutenPayTransactionRepository,
            PaygateMappingRepository paygateMappingRepository) {
        this.rakutenPayTransactionRepository = rakutenPayTransactionRepository;
        this.paygateMappingRepository = paygateMappingRepository;
    }

    @Override
    public ImportResult importFile(MultipartFile file, ImportBatch batch) throws IOException {
        List<RakutenPayTransaction> records = new ArrayList<>();
        List<CsvValidationError> errors = new ArrayList<>();
        LocalDate today = LocalDate.now();

        try (InputStream is = file.getInputStream();
             Workbook wb = new XSSFWorkbook(is)) {

            Sheet sheet = wb.getSheetAt(0);

            for (int rowIdx = DATA_START_ROW; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null) {
                    continue;
                }
                String orderKey = getCellString(row, 0);
                if (orderKey == null || orderKey.isBlank()) {
                    continue;
                }
                int rowNum = rowIdx + 1; // Excel表示行番号（1始まり）

                String storeNo = getCellString(row, 2);
                Optional<PaygateStoreMapping> mapping =
                        paygateMappingRepository.findFirstByRpayStoreCode(storeNo);
                if (mapping.isEmpty()) {
                    errors.add(new CsvValidationError(rowNum, "STORE_NO",
                            "STORE_NO「" + storeNo
                                    + "」に対応する取引コードが取引コード紐付データに存在しません。"));
                    continue;
                }

                int errorCountBeforeRow = errors.size();
                RakutenPayTransaction trn = new RakutenPayTransaction();
                trn.setTradeCode(mapping.get().getTradeCode());
                trn.setBatchId(batch.getBatchId());
                trn.setOrderKey(orderKey);
                trn.setPaymentStatus(getCellString(row, 1));
                trn.setStoreNo(storeNo);
                trn.setStoreName(getCellString(row, 3));
                trn.setShopCode(
                        getCellLongChecked(row, 4, rowNum, "SHOP_CODE", errors));
                trn.setMerchantCode(getCellString(row, 5));
                trn.setTotalAmount(
                        getCellIntChecked(row, 6, rowNum, "TOTAL_AMOUNT", errors));
                trn.setCreatedAt(getCellDateTime(row, 7));
                trn.setCanceledAt(getCellDateTime(row, 8));
                if (errors.size() > errorCountBeforeRow) {
                    continue; // この行にデータ変換エラーがあるため登録しない
                }
                trn.setUpdateEmployee(batch.getUpdateEmployee());
                trn.setCreateDate(today);
                records.add(trn);
            }
        }

        records.forEach(rakutenPayTransactionRepository::save);
        return new ImportResult(records.size(), records.size() + errors.size(), errors);
    }

    @Override
    public void deleteBatchData(int batchId) {
        rakutenPayTransactionRepository.deleteByBatchId(batchId);
    }

    @Override
    public String extractLookupKey(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream();
             Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            for (int rowIdx = DATA_START_ROW; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null) {
                    continue;
                }
                String storeNo = getCellString(row, 2);
                if (storeNo != null && !storeNo.isBlank()) {
                    return storeNo; // STORE_NO
                }
            }
            throw new IllegalArgumentException("データ行がありません。");
        }
    }

    @Override
    public List<String> extractAllLookupKeys(MultipartFile file) throws IOException {
        Set<String> keys = new LinkedHashSet<>();
        try (InputStream is = file.getInputStream();
             Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            for (int rowIdx = DATA_START_ROW; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null) {
                    continue;
                }
                String storeNo = getCellString(row, 2);
                if (storeNo != null && !storeNo.isBlank()) {
                    keys.add(storeNo);
                }
            }
        }
        return new ArrayList<>(keys);
    }

    private String getCellString(Row row, int colIdx) {
        Cell cell = row.getCell(colIdx);
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        }
        String s = cell.getStringCellValue();
        return s.isEmpty() ? null : s;
    }

    private int getCellIntChecked(
            Row row, int colIdx, int rowNum, String colName, List<CsvValidationError> errors) {
        Cell cell = row.getCell(colIdx);
        if (cell == null) {
            return 0;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        }
        String s = cell.getStringCellValue().trim();
        if (s.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(s.replace(",", ""));
        } catch (NumberFormatException e) {
            errors.add(new CsvValidationError(rowNum, colName,
                    "数値変換エラー。値: 「" + s + "」"));
            return 0;
        }
    }

    private long getCellLongChecked(
            Row row, int colIdx, int rowNum, String colName, List<CsvValidationError> errors) {
        Cell cell = row.getCell(colIdx);
        if (cell == null) {
            return 0L;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return (long) cell.getNumericCellValue();
        }
        String s = cell.getStringCellValue().trim();
        if (s.isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(s.replace(",", ""));
        } catch (NumberFormatException e) {
            errors.add(new CsvValidationError(rowNum, colName,
                    "数値変換エラー。値: 「" + s + "」"));
            return 0L;
        }
    }

    private LocalDateTime getCellDateTime(Row row, int colIdx) {
        Cell cell = row.getCell(colIdx);
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            Date date = DateUtil.getJavaDate(cell.getNumericCellValue());
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        return null;
    }
}
