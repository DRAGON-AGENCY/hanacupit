package com.cupit.util;

import java.util.Map;

/**
 * 全角カナを半角カナに変換するユーティリティ。その他統合振込CSV作成
 * （stera terminal）が全銀フォーマットで出力する受取人名（m_stera_store.
 * account_holder_kana）は全角カナで登録されている可能性があるため、CSV化時に
 * 半角カナへ変換する。濁音・半濁音は基底文字＋濁点/半濁点（ﾞ/ﾟ）の2文字に分解する。
 * 変換対象外の文字（英数字・記号・既に半角の文字等）はそのまま出力する。
 */
public final class HalfWidthKanaConverter {

    private static final Map<Character, String> FULL_TO_HALF = buildTable();

    private HalfWidthKanaConverter() {
    }

    public static String toHalfWidth(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder result = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            String replacement = FULL_TO_HALF.get(c);
            result.append(replacement != null ? replacement : c);
        }
        return result.toString();
    }

    private static Map<Character, String> buildTable() {
        Map<Character, String> table = new java.util.HashMap<>();
        table.put('ア', "ｱ");
        table.put('イ', "ｲ");
        table.put('ウ', "ｳ");
        table.put('エ', "ｴ");
        table.put('オ', "ｵ");
        table.put('カ', "ｶ");
        table.put('キ', "ｷ");
        table.put('ク', "ｸ");
        table.put('ケ', "ｹ");
        table.put('コ', "ｺ");
        table.put('サ', "ｻ");
        table.put('シ', "ｼ");
        table.put('ス', "ｽ");
        table.put('セ', "ｾ");
        table.put('ソ', "ｿ");
        table.put('タ', "ﾀ");
        table.put('チ', "ﾁ");
        table.put('ツ', "ﾂ");
        table.put('テ', "ﾃ");
        table.put('ト', "ﾄ");
        table.put('ナ', "ﾅ");
        table.put('ニ', "ﾆ");
        table.put('ヌ', "ﾇ");
        table.put('ネ', "ﾈ");
        table.put('ノ', "ﾉ");
        table.put('ハ', "ﾊ");
        table.put('ヒ', "ﾋ");
        table.put('フ', "ﾌ");
        table.put('ヘ', "ﾍ");
        table.put('ホ', "ﾎ");
        table.put('マ', "ﾏ");
        table.put('ミ', "ﾐ");
        table.put('ム', "ﾑ");
        table.put('メ', "ﾒ");
        table.put('モ', "ﾓ");
        table.put('ヤ', "ﾔ");
        table.put('ユ', "ﾕ");
        table.put('ヨ', "ﾖ");
        table.put('ラ', "ﾗ");
        table.put('リ', "ﾘ");
        table.put('ル', "ﾙ");
        table.put('レ', "ﾚ");
        table.put('ロ', "ﾛ");
        table.put('ワ', "ﾜ");
        table.put('ヲ', "ｦ");
        table.put('ン', "ﾝ");
        table.put('ッ', "ｯ");
        table.put('ャ', "ｬ");
        table.put('ュ', "ｭ");
        table.put('ョ', "ｮ");
        table.put('ァ', "ｧ");
        table.put('ィ', "ｨ");
        table.put('ゥ', "ｩ");
        table.put('ェ', "ｪ");
        table.put('ォ', "ｫ");
        table.put('ー', "ｰ");
        table.put('。', "｡");
        table.put('「', "｢");
        table.put('」', "｣");
        table.put('、', "､");
        table.put('・', "･");
        table.put('　', " ");

        // 濁音（基底文字＋濁点ﾞ）
        table.put('ガ', "ｶﾞ");
        table.put('ギ', "ｷﾞ");
        table.put('グ', "ｸﾞ");
        table.put('ゲ', "ｹﾞ");
        table.put('ゴ', "ｺﾞ");
        table.put('ザ', "ｻﾞ");
        table.put('ジ', "ｼﾞ");
        table.put('ズ', "ｽﾞ");
        table.put('ゼ', "ｾﾞ");
        table.put('ゾ', "ｿﾞ");
        table.put('ダ', "ﾀﾞ");
        table.put('ヂ', "ﾁﾞ");
        table.put('ヅ', "ﾂﾞ");
        table.put('デ', "ﾃﾞ");
        table.put('ド', "ﾄﾞ");
        table.put('バ', "ﾊﾞ");
        table.put('ビ', "ﾋﾞ");
        table.put('ブ', "ﾌﾞ");
        table.put('ベ', "ﾍﾞ");
        table.put('ボ', "ﾎﾞ");
        table.put('ヴ', "ｳﾞ");

        // 半濁音（基底文字＋半濁点ﾟ）
        table.put('パ', "ﾊﾟ");
        table.put('ピ', "ﾋﾟ");
        table.put('プ', "ﾌﾟ");
        table.put('ペ', "ﾍﾟ");
        table.put('ポ', "ﾎﾟ");

        return Map.copyOf(table);
    }

}
