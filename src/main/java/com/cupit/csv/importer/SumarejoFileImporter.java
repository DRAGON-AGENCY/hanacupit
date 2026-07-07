package com.cupit.csv.importer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.model.ImportBatch;
import com.cupit.model.PaygateStoreMapping;
import com.cupit.model.TerminalMonthlyFee;
import com.cupit.repository.PaygateMappingRepository;
import com.cupit.repository.TerminalMonthlyFeeRepository;

/**
 * スマレジ（端末月額）CSVを解析してm_terminal_monthly_feeに登録する。
 * 文字コード: UTF-8 BOM付きは自動検出、なければMS932。ヘッダー行: 1行目、データ: 2行目以降、24列。
 * 列順序: 会社名,請求対象月,請求No,発行日,加盟店名,端末識別番号,単価,
 *         数量×9列, ﾄﾗﾝｻﾞｸｼｮﾝ数×4列, 決済金額×4列
 * 1ファイルに複数の端末識別番号が混在するため、取引コードは行ごとに端末識別番号で
 * m_paygate_store_mapping を引き直して解決する。マッピングが見つからない行・
 * データ変換エラーが発生した行はその行だけを登録せずスキップし、ファイルの最後まで
 * 処理を継続する（データエラーによってファイル全体をロールバックすることはしない）。
 */
@Component
public class SumarejoFileImporter extends AbstractFileImporter {


    private final TerminalMonthlyFeeRepository terminalMonthlyFeeRepository;
    private final PaygateMappingRepository paygateMappingRepository;

    public SumarejoFileImporter(
            TerminalMonthlyFeeRepository terminalMonthlyFeeRepository,
            PaygateMappingRepository paygateMappingRepository) {
        this.terminalMonthlyFeeRepository = terminalMonthlyFeeRepository;
        this.paygateMappingRepository = paygateMappingRepository;
    }

    @Override
    public ImportResult importFile(MultipartFile file, ImportBatch batch) throws IOException {
        List<TerminalMonthlyFee> records = new ArrayList<>();
        List<CsvValidationError> errors = new ArrayList<>();
        LocalDate today = LocalDate.now();
        int rowNum = 1; // 1行目＝ヘッダー

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), detectCharset(file)))) {

            reader.readLine(); // ヘッダー行スキップ

            String line;
            while ((line = reader.readLine()) != null) {
                rowNum++;
                line = stripCr(line);
                if (line.isBlank()) {
                    continue;
                }
                List<String> fields = parseLine(line);
                if (fields.size() < 24) {
                    String terminalPart = fields.size() > 5
                            ? "端末識別番号「" + fields.get(5).trim() + "」: " : "";
                    errors.add(new CsvValidationError(rowNum, "",
                            terminalPart + "列数が不正です。期待: 24列、実際: "
                            + fields.size() + "列"));
                    continue;
                }

                String terminalId = trim(fields.get(5));
                Optional<PaygateStoreMapping> mapping =
                        paygateMappingRepository.findFirstByTerminalId(terminalId);
                if (mapping.isEmpty()) {
                    errors.add(new CsvValidationError(rowNum, "端末識別番号",
                            "端末識別番号「" + terminalId
                                    + "」に対応する取引コードがm_paygate_store_mappingに存在しません。"));
                    continue;
                }

                int errorCountBeforeRow = errors.size();
                TerminalMonthlyFee fee = new TerminalMonthlyFee();
                fee.setTradeCode(mapping.get().getTradeCode());
                fee.setBatchId(batch.getBatchId());
                fee.setCompanyName(trim(fields.get(0)));
                fee.setBillingMonth(trim(fields.get(1)));
                fee.setBillingNo(trim(fields.get(2)));
                fee.setIssueDate(
                        parseDateHyphenChecked(fields.get(3), rowNum, "発行日", errors));
                fee.setStoreName(trim(fields.get(4)));
                fee.setTerminalId(terminalId);
                fee.setUnitPrice(
                        parseIntChecked(fields.get(6), rowNum, "単価", errors));
                fee.setQtyCredit(
                        parseIntChecked(fields.get(7), rowNum, "数量（クレジット）", errors));
                fee.setQtyQr(
                        parseIntChecked(fields.get(8), rowNum, "数量（QRコード決済）", errors));
                fee.setQtyIcTransportation(
                        parseIntChecked(fields.get(9), rowNum, "数量（電子マネー・交通系）", errors));
                fee.setQtyIcId(
                        parseIntChecked(fields.get(10), rowNum, "数量（電子マネー・ID）", errors));
                fee.setQtyIcWaon(
                        parseIntChecked(fields.get(11), rowNum, "数量（電子マネー・WAON）", errors));
                fee.setQtyIcNanaco(
                        parseIntChecked(fields.get(12), rowNum, "数量（電子マネー・nanaco）", errors));
                fee.setQtyIcEdyrakuten(
                        parseIntChecked(fields.get(13), rowNum, "数量（電子マネー・楽天Edy）", errors));
                fee.setQtyIcQuicpay(
                        parseIntChecked(fields.get(14), rowNum, "数量（電子マネー・QUICPay）", errors));
                fee.setQtySim(
                        parseIntChecked(fields.get(15), rowNum, "数量（SIM）", errors));
                fee.setTxCountCredit(
                        parseIntChecked(fields.get(16), rowNum, "ﾄﾗﾝｻﾞｸｼｮﾝ数（クレジット）", errors));
                fee.setTxCountQr(
                        parseIntChecked(fields.get(17), rowNum, "ﾄﾗﾝｻﾞｸｼｮﾝ数（QRコード決済）", errors));
                fee.setTxCountIc(
                        parseIntChecked(fields.get(18), rowNum, "ﾄﾗﾝｻﾞｸｼｮﾝ数（電子マネー）", errors));
                fee.setTxCountTotal(
                        parseIntChecked(fields.get(19), rowNum, "ﾄﾗﾝｻﾞｸｼｮﾝ数（合計）", errors));
                fee.setAmountCredit(
                        parseIntChecked(fields.get(20), rowNum, "決済金額（クレジット）", errors));
                fee.setAmountQr(
                        parseIntChecked(fields.get(21), rowNum, "決済金額（QRコード決済）", errors));
                fee.setAmountIc(
                        parseIntChecked(fields.get(22), rowNum, "決済金額（電子マネー）", errors));
                fee.setAmountTotal(
                        parseIntChecked(fields.get(23), rowNum, "決済金額（合計）", errors));
                if (errors.size() > errorCountBeforeRow) {
                    continue; // この行にデータ変換エラーがあるため登録しない
                }
                fee.setUpdateEmployee(batch.getUpdateEmployee());
                fee.setCreateDate(today);
                records.add(fee);
            }
        }

        int totalDataRows = rowNum - 1;
        records.forEach(terminalMonthlyFeeRepository::save);
        return new ImportResult(records.size(), totalDataRows, errors);
    }

    @Override
    public String extractLookupKey(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), detectCharset(file)))) {
            reader.readLine(); // ヘッダー行スキップ
            String line = reader.readLine();
            if (line == null || line.isBlank()) {
                throw new IllegalArgumentException("データ行がありません。");
            }
            List<String> fields = parseLine(stripCr(line));
            if (fields.size() < 6) {
                throw new IllegalArgumentException("スマレジファイルの列数が不足しています。");
            }
            String terminalId = trim(fields.get(5));
            if (terminalId.isEmpty()) {
                throw new IllegalArgumentException(
                        "2行目: 端末識別番号が空のため識別キーを取得できませんでした。");
            }
            return terminalId; // 端末識別番号
        }
    }
}
