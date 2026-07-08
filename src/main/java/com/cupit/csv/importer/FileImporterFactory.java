package com.cupit.csv.importer;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.cupit.csv.PaymentType;

/**
 * 決済種類に対応する FileImporter を返すファクトリ。
 */
@Component
public class FileImporterFactory {

    private final Map<PaymentType, FileImporter> importerMap;

    public FileImporterFactory(
            JcbFileImporter jcbFileImporter,
            SumarejoFileImporter sumarejoFileImporter,
            NetstarFileImporter netstarFileImporter,
            RakutenpayFileImporter rakutenpayFileImporter,
            JushinSbiFileImporter jushinSbiFileImporter,
            SteraJcbFileImporter steraJcbFileImporter,
            SteraCodeFileImporter steraCodeFileImporter,
            SteraCreditFileImporter steraCreditFileImporter) {
        importerMap = new EnumMap<>(PaymentType.class);
        importerMap.put(PaymentType.JCB, jcbFileImporter);
        importerMap.put(PaymentType.SUMAREJO, sumarejoFileImporter);
        importerMap.put(PaymentType.NETSTARS, netstarFileImporter);
        importerMap.put(PaymentType.RAKUTENPAY, rakutenpayFileImporter);
        importerMap.put(PaymentType.JUSHIN_SBI, jushinSbiFileImporter);
        importerMap.put(PaymentType.STERA_JCB, steraJcbFileImporter);
        importerMap.put(PaymentType.STERA_CODE, steraCodeFileImporter);
        importerMap.put(PaymentType.STERA_CREDIT, steraCreditFileImporter);
    }

    public FileImporter getImporter(PaymentType type) {
        FileImporter importer = importerMap.get(type);
        if (importer == null) {
            throw new IllegalArgumentException(
                    "対応するインポーターが存在しません: " + type);
        }
        return importer;
    }
}
