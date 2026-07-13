package com.cupit.csv.importer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.model.ImportBatch;
import com.cupit.model.SteraCodeSettlementDetail;
import com.cupit.model.SteraCodeSettlementSummary;
import com.cupit.model.SteraTerminal;
import com.cupit.repository.SteraCodeSettlementDetailRepository;
import com.cupit.repository.SteraCodeSettlementSummaryRepository;
import com.cupit.repository.SteraStoreRepository;
import com.cupit.repository.SteraTerminalRepository;

/**
 * stera code精算明細CSVを解析する。ブランド（楽天ペイ・PayPay等）ごとの明細ブロック末尾に
 * 小計行が1行入っており、伝票番号が固定値「99999」・決済時間が固定値「000000」であることで
 * 判別できる。小計行は個別取引として登録せず、m_stera_code_settlement_summaryへ
 * （突合検証用に）別途保存する。個別取引の取引コードはm_stera_terminal.terminal_idから解決する。
 */
@Component
public class SteraCodeFileImporter extends AbstractFileImporter {

    private static final String SUMMARY_ROW_SLIP_NUMBER = "99999";
    private static final String SUMMARY_ROW_SETTLEMENT_TIME = "000000";

    private final SteraCodeSettlementDetailRepository settlementDetailRepository;
    private final SteraCodeSettlementSummaryRepository settlementSummaryRepository;
    private final SteraTerminalRepository steraTerminalRepository;
    private final SteraStoreRepository steraStoreRepository;

    public SteraCodeFileImporter(
            SteraCodeSettlementDetailRepository settlementDetailRepository,
            SteraCodeSettlementSummaryRepository settlementSummaryRepository,
            SteraTerminalRepository steraTerminalRepository,
            SteraStoreRepository steraStoreRepository) {
        this.settlementDetailRepository = settlementDetailRepository;
        this.settlementSummaryRepository = settlementSummaryRepository;
        this.steraTerminalRepository = steraTerminalRepository;
        this.steraStoreRepository = steraStoreRepository;
    }

    @Override
    public ImportResult importFile(MultipartFile file, ImportBatch batch) throws IOException {
        List<SteraCodeSettlementDetail> detailRecords = new ArrayList<>();
        List<SteraCodeSettlementSummary> summaryRecords = new ArrayList<>();
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
                if (fields.size() < 10) {
                    String brandPart = !fields.isEmpty()
                            ? "ブランド「" + fields.get(0).trim() + "」: " : "";
                    errors.add(new CsvValidationError(rowNum, "",
                            brandPart + "列数が不正です。期待: 10列、実際: "
                            + fields.size() + "列"));
                    continue;
                }

                String brand = trim(fields.get(0));
                String terminalId = trim(fields.get(1));
                String slipNumber = trim(fields.get(2));
                String settlementTime = trim(fields.get(4));

                // 小計行は「伝票番号=99999」「決済時間=000000」に加え、端末識別番号列に
                // 代表加盟店番号（数字以外を含む形式）を保持する点で明細行と区別する。
                // 伝票番号99999・決済時間000000が偶然一致する実明細（端末識別番号は数字
                // のみ）を小計行と誤分類しないよう、端末識別番号が数字のみでないことも
                // 条件に含める。実サンプルでも小計行は非数字の端末識別番号のみ、明細行は
                // 数字のみの端末識別番号のみで両者は完全に分離できる。
                boolean isSummaryRow = SUMMARY_ROW_SLIP_NUMBER.equals(slipNumber)
                        && SUMMARY_ROW_SETTLEMENT_TIME.equals(settlementTime)
                        && !isNumeric(terminalId);

                if (isSummaryRow) {
                    addSummaryRow(summaryRecords, errors, rowNum, batch, today, brand, fields);
                } else {
                    addDetailRow(detailRecords, errors, rowNum, batch, today,
                            brand, terminalId, slipNumber, fields);
                }
            }
        }

        int totalDataRows = rowNum - 1; // ヘッダー行を除いたデータ行数
        detailRecords.forEach(settlementDetailRepository::save);
        summaryRecords.forEach(settlementSummaryRepository::save);
        return new ImportResult(detailRecords.size(), totalDataRows, errors);
    }

    private void addDetailRow(
            List<SteraCodeSettlementDetail> detailRecords, List<CsvValidationError> errors,
            int rowNum, ImportBatch batch, LocalDate today,
            String brand, String terminalId, String slipNumber, List<String> fields) {
        Optional<SteraTerminal> terminal = resolveActiveTerminal(
                steraTerminalRepository.findByTerminalId(terminalId),
                rowNum, "端末識別番号", "ブランド「" + brand + "」端末識別番号", terminalId, errors);
        if (terminal.isEmpty()) {
            return;
        }
        String tradeCode = terminal.get().getTradeCode();
        if (!hasStoreAccount(tradeCode, rowNum, "端末識別番号", errors)) {
            return;
        }

        int errorCountBeforeRow = errors.size();
        SteraCodeSettlementDetail detail = new SteraCodeSettlementDetail();
        detail.setTradeCode(tradeCode);
        detail.setBatchId(batch.getBatchId());
        detail.setBrand(brand);
        detail.setTerminalId(terminalId);
        detail.setSlipNumber(slipNumber);
        detail.setSettlementDate(trim(fields.get(3)));
        detail.setSettlementTime(trim(fields.get(4)));
        detail.setSalesReturnFlag(
                parseIntChecked(fields.get(5), rowNum, "1:売上2:返品", errors));
        detail.setSettlementAmount(
                parseIntChecked(fields.get(6), rowNum, "決済金額", errors));
        String subWalletName = trim(fields.get(9));
        detail.setSubWalletName(subWalletName.isEmpty() ? null : subWalletName);
        if (errors.size() > errorCountBeforeRow) {
            return; // この行にデータ変換エラーがあるため登録しない
        }
        detail.setUpdateEmployee(batch.getUpdateEmployee());
        detail.setCreateDate(today);
        detailRecords.add(detail);
    }

    private void addSummaryRow(
            List<SteraCodeSettlementSummary> summaryRecords, List<CsvValidationError> errors,
            int rowNum, ImportBatch batch, LocalDate today, String brand, List<String> fields) {
        int errorCountBeforeRow = errors.size();
        SteraCodeSettlementSummary summary = new SteraCodeSettlementSummary();
        summary.setBatchId(batch.getBatchId());
        summary.setBrand(brand);
        summary.setTransactionCount(
                parseIntChecked(fields.get(5), rowNum, "1:売上2:返品（小計行では件数合計）", errors));
        summary.setSettlementAmount(
                parseIntChecked(fields.get(6), rowNum, "決済金額（小計行では合計金額）", errors));
        summary.setFeeAmount(
                parseIntChecked(fields.get(7), rowNum, "手数料金額", errors));
        summary.setNetAmount(
                parseIntChecked(fields.get(8), rowNum, "収納金額", errors));
        if (errors.size() > errorCountBeforeRow) {
            return; // この行にデータ変換エラーがあるため登録しない
        }
        summary.setUpdateEmployee(batch.getUpdateEmployee());
        summary.setCreateDate(today);
        summaryRecords.add(summary);
    }

    /**
     * 検索結果から、有効な端末情報（terminal_end_dateが未設定＝現在も有効）を一意に絞り込む。
     * m_stera_terminalはterminal_id等に一意制約が無く、端末の再割当て等で同じキーの
     * 履歴行が複数存在しうるため、無条件にfindFirstで決め打ちすると誤った取引コードに
     * 紐付く恐れがある。0件・複数件（マスタデータの不整合）の場合はどちらもエラーとして
     * その行をスキップする。
     */
    private Optional<SteraTerminal> resolveActiveTerminal(
            List<SteraTerminal> candidates, int rowNum, String columnName,
            String keyLabel, String keyValue, List<CsvValidationError> errors) {
        List<SteraTerminal> active = candidates.stream()
                .filter(t -> t.getTerminalEndDate() == null)
                .collect(Collectors.toList());
        if (active.size() == 1) {
            return Optional.of(active.get(0));
        }
        if (active.isEmpty()) {
            errors.add(new CsvValidationError(rowNum, columnName,
                    keyLabel + "「" + keyValue
                            + "」に対応する有効な端末情報（終了日未設定）がm_stera_terminalに"
                            + "存在しません。"));
        } else {
            errors.add(new CsvValidationError(rowNum, columnName,
                    keyLabel + "「" + keyValue
                            + "」に対応する有効な端末情報がm_stera_terminalに複数件存在するため"
                            + "取引コードを一意に決定できません。マスタデータをご確認ください。"));
        }
        return Optional.empty();
    }

    /**
     * 解決済みの取引コードに対応する振込先口座情報がm_stera_storeに存在するか確認する。
     * その他統合振込CSV作成の確定処理はこの突合を行わない前提のため、口座情報の有無は
     * 必ずインポート時点で確認し、無ければ取引コード未解決の行と同様にスキップする。
     */
    private boolean hasStoreAccount(
            String tradeCode, int rowNum, String columnName, List<CsvValidationError> errors) {
        if (steraStoreRepository.findByTradeCode(tradeCode).isPresent()) {
            return true;
        }
        errors.add(new CsvValidationError(rowNum, columnName,
                "取引コード「" + tradeCode + "」に対応する振込先口座情報がm_stera_storeに"
                        + "存在しません。"));
        return false;
    }

    /**
     * 端末識別番号が数字のみで構成されるか判定する。stera codeの小計行は端末識別番号列に
     * 代表加盟店番号（数字以外を含む形式、実データでは「S」で始まる）を保持するため、
     * 数字のみの端末識別番号を持つ明細行と区別するために使用する。
     */
    private boolean isNumeric(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void deleteBatchData(int batchId) {
        settlementDetailRepository.deleteByBatchId(batchId);
        settlementSummaryRepository.deleteByBatchId(batchId);
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
            if (fields.size() < 2) {
                throw new IllegalArgumentException("stera codeファイルの列数が不足しています。");
            }
            String terminalId = trim(fields.get(1));
            if (terminalId.isEmpty()) {
                throw new IllegalArgumentException(
                        "2行目: 端末識別番号が空のため識別キーを取得できませんでした。");
            }
            return terminalId;
        }
    }

    @Override
    public List<String> extractAllLookupKeys(MultipartFile file) throws IOException {
        Set<String> keys = new LinkedHashSet<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), detectCharset(file)))) {
            reader.readLine(); // ヘッダー行スキップ
            String line;
            while ((line = reader.readLine()) != null) {
                line = stripCr(line);
                if (line.isBlank()) {
                    continue;
                }
                List<String> fields = parseLine(line);
                if (fields.size() < 2) {
                    continue;
                }
                String terminalId = trim(fields.get(1));
                if (!terminalId.isEmpty()) {
                    keys.add(terminalId);
                }
            }
        }
        return new ArrayList<>(keys);
    }
}
