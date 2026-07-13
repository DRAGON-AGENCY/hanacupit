package com.cupit.csv.importer;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.model.ImportBatch;
import com.cupit.model.NetstarSalesSummary;
import com.cupit.model.PaygateStoreMapping;
import com.cupit.repository.NetstarSalesSummaryRepository;
import com.cupit.repository.PaygateMappingRepository;

/**
 * ネットスターズ（店舗コード別集計）Excelを解析してm_netstar_sales_summaryに登録する。
 * ヘッダー行: 6-7行目（0-indexed: 5-6）、データ: 8行目以降（0-indexed: 7）。
 * 列構成（0-indexed）:
 *   0=空, 1=店舗コード, 2=店舗名, 3=合計件数, 4=合計金額,
 *   5=売上件数, 6=売上金額, 7=返金件数, 8=返金金額, 9=差引金額,
 *   10-14=Alipay, 15-19=d払い, 20-24=PayPay,
 *   25-29=楽天ペイ, 30-34=Smart Code, 35-39=WeChat Pay
 * 1ファイルに花キューピット全店舗分の店舗コードが行単位で混在するため、取引コードは
 * 行ごとに店舗コードでm_paygate_store_mapping を引き直して解決する。マッピングが
 * 見つからない行・データ変換エラーが発生した行はその行だけを登録せずスキップし、
 * ファイルの最後まで処理を継続する（データエラーによってファイル全体をロール
 * バックすることはしない）。
 */
@Component
public class NetstarFileImporter extends AbstractFileImporter {

    private static final int DATA_START_ROW = 7; // 0-indexed（Excel8行目）

    private final NetstarSalesSummaryRepository netstarSalesSummaryRepository;
    private final PaygateMappingRepository paygateMappingRepository;

    public NetstarFileImporter(
            NetstarSalesSummaryRepository netstarSalesSummaryRepository,
            PaygateMappingRepository paygateMappingRepository) {
        this.netstarSalesSummaryRepository = netstarSalesSummaryRepository;
        this.paygateMappingRepository = paygateMappingRepository;
    }

    @Override
    public ImportResult importFile(MultipartFile file, ImportBatch batch) throws IOException {
        List<NetstarSalesSummary> records = new ArrayList<>();
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
                String storeCode = getCellString(row, 1);
                if (storeCode == null || storeCode.isBlank()) {
                    continue; // 合計行等をスキップ
                }
                int rowNum = rowIdx + 1; // Excel表示行番号（1始まり）

                Optional<PaygateStoreMapping> mapping =
                        paygateMappingRepository.findFirstByNetstarStoreCode(storeCode);
                if (mapping.isEmpty()) {
                    errors.add(new CsvValidationError(rowNum, "店舗コード",
                            "店舗コード「" + storeCode
                                    + "」に対応する取引コードがm_paygate_store_mappingに存在しません。"));
                    continue;
                }

                int errorCountBeforeRow = errors.size();
                NetstarSalesSummary summary = new NetstarSalesSummary();
                summary.setTradeCode(mapping.get().getTradeCode());
                summary.setBatchId(batch.getBatchId());
                summary.setStoreCode(storeCode);
                summary.setStoreName(getCellString(row, 2));
                summary.setTotalCount(
                        getCellIntChecked(row, 3, rowNum, "合計件数", errors));
                summary.setTotalAmount(
                        getCellIntChecked(row, 4, rowNum, "合計金額", errors));
                summary.setSalesCount(
                        getCellIntChecked(row, 5, rowNum, "売上件数", errors));
                summary.setSalesAmount(
                        getCellIntChecked(row, 6, rowNum, "売上金額", errors));
                summary.setRefundCount(
                        getCellIntChecked(row, 7, rowNum, "返金件数", errors));
                summary.setRefundAmount(
                        getCellIntChecked(row, 8, rowNum, "返金金額", errors));
                summary.setNetAmount(
                        getCellIntChecked(row, 9, rowNum, "差引金額", errors));
                summary.setAlipaySalesCount(
                        getCellIntChecked(row, 10, rowNum, "Alipay件数", errors));
                summary.setAlipaySalesAmount(
                        getCellIntChecked(row, 11, rowNum, "Alipay売上金額", errors));
                summary.setAlipayRefundCount(
                        getCellIntChecked(row, 12, rowNum, "Alipay返金件数", errors));
                summary.setAlipayRefundAmount(
                        getCellIntChecked(row, 13, rowNum, "Alipay返金金額", errors));
                summary.setAlipayNetAmount(
                        getCellIntChecked(row, 14, rowNum, "Alipay差引金額", errors));
                summary.setDpaySalesCount(
                        getCellIntChecked(row, 15, rowNum, "d払い件数", errors));
                summary.setDpaySalesAmount(
                        getCellIntChecked(row, 16, rowNum, "d払い売上金額", errors));
                summary.setDpayRefundCount(
                        getCellIntChecked(row, 17, rowNum, "d払い返金件数", errors));
                summary.setDpayRefundAmount(
                        getCellIntChecked(row, 18, rowNum, "d払い返金金額", errors));
                summary.setDpayNetAmount(
                        getCellIntChecked(row, 19, rowNum, "d払い差引金額", errors));
                summary.setPaypaySalesCount(
                        getCellIntChecked(row, 20, rowNum, "PayPay件数", errors));
                summary.setPaypaySalesAmount(
                        getCellIntChecked(row, 21, rowNum, "PayPay売上金額", errors));
                summary.setPaypayRefundCount(
                        getCellIntChecked(row, 22, rowNum, "PayPay返金件数", errors));
                summary.setPaypayRefundAmount(
                        getCellIntChecked(row, 23, rowNum, "PayPay返金金額", errors));
                summary.setPaypayNetAmount(
                        getCellIntChecked(row, 24, rowNum, "PayPay差引金額", errors));
                summary.setRakutenSalesCount(
                        getCellIntChecked(row, 25, rowNum, "楽天ペイ件数", errors));
                summary.setRakutenSalesAmount(
                        getCellIntChecked(row, 26, rowNum, "楽天ペイ売上金額", errors));
                summary.setRakutenRefundCount(
                        getCellIntChecked(row, 27, rowNum, "楽天ペイ返金件数", errors));
                summary.setRakutenRefundAmount(
                        getCellIntChecked(row, 28, rowNum, "楽天ペイ返金金額", errors));
                summary.setRakutenNetAmount(
                        getCellIntChecked(row, 29, rowNum, "楽天ペイ差引金額", errors));
                summary.setSmartcodeSalesCount(
                        getCellIntChecked(row, 30, rowNum, "Smart Code件数", errors));
                summary.setSmartcodeSalesAmount(
                        getCellIntChecked(row, 31, rowNum, "Smart Code売上金額", errors));
                summary.setSmartcodeRefundCount(
                        getCellIntChecked(row, 32, rowNum, "Smart Code返金件数", errors));
                summary.setSmartcodeRefundAmount(
                        getCellIntChecked(row, 33, rowNum, "Smart Code返金金額", errors));
                summary.setSmartcodeNetAmount(
                        getCellIntChecked(row, 34, rowNum, "Smart Code差引金額", errors));
                summary.setWechatSalesCount(
                        getCellIntChecked(row, 35, rowNum, "WeChat Pay件数", errors));
                summary.setWechatSalesAmount(
                        getCellIntChecked(row, 36, rowNum, "WeChat Pay売上金額", errors));
                summary.setWechatRefundCount(
                        getCellIntChecked(row, 37, rowNum, "WeChat Pay返金件数", errors));
                summary.setWechatRefundAmount(
                        getCellIntChecked(row, 38, rowNum, "WeChat Pay返金金額", errors));
                summary.setWechatNetAmount(
                        getCellIntChecked(row, 39, rowNum, "WeChat Pay差引金額", errors));
                if (errors.size() > errorCountBeforeRow) {
                    continue; // この行にデータ変換エラーがあるため登録しない
                }
                summary.setUpdateEmployee(batch.getUpdateEmployee());
                summary.setCreateDate(today);
                records.add(summary);
            }
        }

        records.forEach(netstarSalesSummaryRepository::save);
        return new ImportResult(records.size(), records.size() + errors.size(), errors);
    }

    @Override
    public void deleteBatchData(int batchId) {
        netstarSalesSummaryRepository.deleteByBatchId(batchId);
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
                String storeCode = getCellString(row, 1);
                if (storeCode != null && !storeCode.isBlank()) {
                    return storeCode; // 店舗コード
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
                String storeCode = getCellString(row, 1);
                if (storeCode != null && !storeCode.isBlank()) {
                    keys.add(storeCode);
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
        return cell.getStringCellValue();
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
}
