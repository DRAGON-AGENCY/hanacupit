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
import com.cupit.model.SteraJcbSalesDetail;
import com.cupit.model.SteraTerminal;
import com.cupit.repository.SteraJcbSalesDetailRepository;
import com.cupit.repository.SteraStoreRepository;
import com.cupit.repository.SteraTerminalRepository;

/**
 * stera terminal経由のJCB売上明細CSVを解析してm_stera_jcb_sales_detailに登録する。
 * ファイル形式は{@link JcbFileImporter}（PAYGATE Station側）と完全に同一（10列）だが、
 * 取引コードの解決先が異なる：PAYGATE店舗コードマッピングではなく
 * m_stera_terminal.jcb_merchant_noから解決する。ファイルの加盟店番号は全角数字＋
 * 全角ハイフン形式（例：２１８１－５００－５１－０００３４）だが、
 * m_stera_terminal.jcb_merchant_noは半角数字のみでハイフンを含まない形式
 * （例：21815005100026）で保持されているため、検索前に正規化する。
 */
@Component
public class SteraJcbFileImporter extends AbstractFileImporter {

    private final SteraJcbSalesDetailRepository steraJcbSalesDetailRepository;
    private final SteraTerminalRepository steraTerminalRepository;
    private final SteraStoreRepository steraStoreRepository;

    public SteraJcbFileImporter(
            SteraJcbSalesDetailRepository steraJcbSalesDetailRepository,
            SteraTerminalRepository steraTerminalRepository,
            SteraStoreRepository steraStoreRepository) {
        this.steraJcbSalesDetailRepository = steraJcbSalesDetailRepository;
        this.steraTerminalRepository = steraTerminalRepository;
        this.steraStoreRepository = steraStoreRepository;
    }

    @Override
    public ImportResult importFile(MultipartFile file, ImportBatch batch) throws IOException {
        List<SteraJcbSalesDetail> records = new ArrayList<>();
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
                    String merchantPart = fields.size() > 1
                            ? "加盟店番号「" + fields.get(1).trim() + "」: " : "";
                    errors.add(new CsvValidationError(rowNum, "",
                            merchantPart + "列数が不正です。期待: 10列、実際: "
                            + fields.size() + "列"));
                    continue;
                }

                String merchantNo = trim(fields.get(1));
                String normalizedMerchantNo = normalizeJcbMerchantNo(merchantNo);
                Optional<SteraTerminal> terminal = resolveActiveTerminal(
                        steraTerminalRepository.findByJcbMerchantNo(normalizedMerchantNo),
                        rowNum, "加盟店番号", "加盟店番号", merchantNo, errors);
                if (terminal.isEmpty()) {
                    continue;
                }
                String tradeCode = terminal.get().getTradeCode();
                if (!hasStoreAccount(tradeCode, rowNum, "加盟店番号", errors)) {
                    continue;
                }

                int errorCountBeforeRow = errors.size();
                SteraJcbSalesDetail detail = new SteraJcbSalesDetail();
                detail.setTradeCode(tradeCode);
                detail.setBatchId(batch.getBatchId());
                detail.setStoreName(trim(fields.get(0)));
                detail.setStoreNumber(merchantNo);
                detail.setCardCompany(trim(fields.get(2)));
                detail.setPaymentMethod(trim(fields.get(3)));
                detail.setCardName(trim(fields.get(4)));
                detail.setPaymentType(trim(fields.get(5)));
                detail.setSalesMethod(trim(fields.get(6)));
                detail.setSalesDate(trim(fields.get(7)));
                detail.setSalesCount(
                        parseIntChecked(fields.get(8), rowNum, "売上件数", errors));
                detail.setSalesAmount(
                        parseIntChecked(fields.get(9), rowNum, "売上金額（円）", errors));
                if (errors.size() > errorCountBeforeRow) {
                    continue; // この行にデータ変換エラーがあるため登録しない
                }
                detail.setUpdateEmployee(batch.getUpdateEmployee());
                detail.setCreateDate(today);
                records.add(detail);
            }
        }

        int totalDataRows = rowNum - 1; // ヘッダー行を除いたデータ行数
        records.forEach(steraJcbSalesDetailRepository::save);
        return new ImportResult(records.size(), totalDataRows, errors);
    }

    @Override
    public void deleteBatchData(int batchId) {
        steraJcbSalesDetailRepository.deleteByBatchId(batchId);
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
                throw new IllegalArgumentException("JCBファイルの列数が不足しています。");
            }
            String merchantNo = trim(fields.get(1));
            if (merchantNo.isEmpty()) {
                throw new IllegalArgumentException(
                        "2行目: 加盟店番号が空のため識別キーを取得できませんでした。");
            }
            return merchantNo; // 加盟店番号
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
                String merchantNo = trim(fields.get(1));
                if (!merchantNo.isEmpty()) {
                    keys.add(merchantNo);
                }
            }
        }
        return new ArrayList<>(keys);
    }

    /**
     * 検索結果から、有効な端末情報（terminal_end_dateが未設定＝現在も有効）を一意に絞り込む。
     * m_stera_terminalはjcb_merchant_no等に一意制約が無く、端末の再割当て等で同じキーの
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
     * ファイル上の加盟店番号（全角数字＋全角ハイフン、例：２１８１－５００－５１－０００３４）を、
     * m_stera_terminal.jcb_merchant_noの保存形式（半角数字のみ、例：21815005100026）に正規化する。
     */
    private String normalizeJcbMerchantNo(String rawMerchantNo) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rawMerchantNo.length(); i++) {
            char c = rawMerchantNo.charAt(i);
            if (c >= '０' && c <= '９') {
                sb.append((char) (c - '０' + '0'));
            } else if (c >= '0' && c <= '9') {
                sb.append(c);
            }
            // 全角・半角ハイフンおよびその他の文字は除去する
        }
        return sb.toString();
    }
}
