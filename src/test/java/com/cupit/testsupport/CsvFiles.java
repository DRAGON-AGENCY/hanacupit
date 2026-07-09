package com.cupit.testsupport;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.springframework.mock.web.MockMultipartFile;

/**
 * stera terminal 系の単体テストで使用する {@code MultipartFile} を組み立てるヘルパー。
 * クラスパス上の実 INPUTファイル（src/test/resources/stera 配下）の読込みと、
 * 文字コードを指定したバイト列の生成の双方に対応する。
 */
public final class CsvFiles {

    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final String CRLF = "\r\n";
    private static final Charset MS932 = Charset.forName("MS932");

    private CsvFiles() {
    }

    /**
     * src/test/resources/stera 配下のファイルを読み込んで {@code MockMultipartFile} を返す。
     *
     * @param resourceName クラスパス上のファイル名（例: "stera_jcb_valid.csv"）
     * @return 読み込んだ内容を保持するアップロードファイル
     */
    public static MockMultipartFile fromClasspath(String resourceName) {
        String path = "/stera/" + resourceName;
        try (InputStream is = CsvFiles.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalStateException("テストリソースが見つかりません: " + path);
            }
            return new MockMultipartFile("file", resourceName, "text/csv", is.readAllBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * UTF-8（BOM付き）でエンコードしたアップロードファイルを生成する。
     *
     * @param filename ファイル名（拡張子検証に使用する）
     * @param lines    各行の内容（改行 CRLF で連結する）
     * @return 生成したアップロードファイル
     */
    public static MockMultipartFile utf8Bom(String filename, String... lines) {
        byte[] body = String.join(CRLF, lines).getBytes(StandardCharsets.UTF_8);
        byte[] content = new byte[UTF8_BOM.length + body.length];
        System.arraycopy(UTF8_BOM, 0, content, 0, UTF8_BOM.length);
        System.arraycopy(body, 0, content, UTF8_BOM.length, body.length);
        return new MockMultipartFile("file", filename, "text/csv", content);
    }

    /**
     * Shift-JIS（MS932）でエンコードしたアップロードファイルを生成する。
     *
     * @param filename ファイル名（拡張子検証に使用する）
     * @param lines    各行の内容（改行 CRLF で連結する）
     * @return 生成したアップロードファイル
     */
    public static MockMultipartFile ms932(String filename, String... lines) {
        byte[] content = String.join(CRLF, lines).getBytes(MS932);
        return new MockMultipartFile("file", filename, "text/csv", content);
    }

    /**
     * 任意のバイト列をそのまま保持するアップロードファイルを生成する。
     * UTF-16 など、文字コード検証の異常系テストで使用する。
     *
     * @param filename ファイル名
     * @param content  バイト列
     * @return 生成したアップロードファイル
     */
    public static MockMultipartFile ofBytes(String filename, byte[] content) {
        return new MockMultipartFile("file", filename, "text/csv", content);
    }
}
