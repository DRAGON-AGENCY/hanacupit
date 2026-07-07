package com.cupit.csv;

import java.util.EnumMap;
import java.util.Map;

import com.cupit.csv.validator.CsvFormatValidator;
import com.cupit.csv.validator.JcbCsvFormatValidator;
import com.cupit.csv.validator.JushinSbiCsvFormatValidator;
import com.cupit.csv.validator.NetstarsCsvFormatValidator;
import com.cupit.csv.validator.RakutenpayCsvFormatValidator;
import com.cupit.csv.validator.SumarejoCsvFormatValidator;

/**
 * 決済種類に対応する CsvFormatValidator のインスタンスを返すファクトリクラス。
 */
public final class CsvFormatValidatorFactory {

    private static final Map<PaymentType, CsvFormatValidator> VALIDATORS;

    static {
        VALIDATORS = new EnumMap<>(PaymentType.class);
        VALIDATORS.put(PaymentType.JCB, new JcbCsvFormatValidator());
        VALIDATORS.put(PaymentType.SUMAREJO, new SumarejoCsvFormatValidator());
        VALIDATORS.put(PaymentType.NETSTARS, new NetstarsCsvFormatValidator());
        VALIDATORS.put(PaymentType.RAKUTENPAY, new RakutenpayCsvFormatValidator());
        VALIDATORS.put(PaymentType.JUSHIN_SBI, new JushinSbiCsvFormatValidator());
    }

    private CsvFormatValidatorFactory() {
    }

    public static CsvFormatValidator getValidator(PaymentType paymentType) {
        CsvFormatValidator validator = VALIDATORS.get(paymentType);
        if (validator == null) {
            throw new IllegalArgumentException("対応するバリデーターが存在しません: " + paymentType);
        }
        return validator;
    }
}
