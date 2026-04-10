package jp.co.createlink.core.exception;

/**
 * CoreException
 *
 * <p>Coreライブラリ全体の共通例外クラス。</p>
 *
 * <p>以下のケースで使用する:</p>
 * <ul>
 *   <li>REST 接続エラー（タイムアウト、接続拒否、4xx/5xx レスポンス等）</li>
 *   <li>SOAP 接続エラー（{@link SoapClientException} のラップも可）</li>
 *   <li>設定値の不正（必須プロパティ未設定等）</li>
 *   <li>その他 Core ライブラリ内部の処理エラー</li>
 * </ul>
 *
 * <h2>Process-API での使い方</h2>
 * <p>Process-API の {@code @ControllerAdvice}（GlobalExceptionHandler）で
 * このクラスをキャッチして適切な HTTP レスポンス（例: 502 Bad Gateway）に変換すること。</p>
 *
 * <h2>エラーコードの設計方針</h2>
 * <p>エラーコードは接続先システム・エラー種別を識別するための文字列。
 * 例: {@code "REST_SAMPLE_001"} / {@code "SOAP_SAMPLE_002"} のように
 * {@code "{プロトコル}_{システム名}_{連番}"} の形式を推奨する。</p>
 *
 * <h2>SoapClientException との使い分け</h2>
 * <ul>
 *   <li>{@code SoapClientException}: SOAP クライアント固有のエラー（既存・後方互換のため残す）</li>
 *   <li>{@code CoreException}: REST + SOAP 両方をカバーする汎用例外（新規実装で使用を推奨）</li>
 * </ul>
 */
public class CoreException extends RuntimeException {

    /**
     * エラーコード。
     * 接続先システムやエラー種別を識別するための文字列。
     * エラーコードが不要な場合は {@code null} を指定する。
     *
     * 例: "REST_SAMPLE_404" / "SOAP_HOGESYSTEM_TIMEOUT" / "CONFIG_MISSING_URL"
     */
    private final String errorCode;

    // ============================================================
    // コンストラクタ
    // ============================================================

    /**
     * メッセージのみ指定するコンストラクタ。
     *
     * <p>エラーコードが不要な場合に使用する。</p>
     *
     * @param message エラーメッセージ（人間が読める形式で記述すること）
     */
    public CoreException(String message) {
        super(message);
        this.errorCode = null;
    }

    /**
     * メッセージと原因例外を指定するコンストラクタ。
     *
     * <p>外部システムとの通信エラーをラップする場合に使用する。
     * 原因例外のスタックトレースが保持されるため、デバッグに役立つ。</p>
     *
     * @param message エラーメッセージ
     * @param cause   原因例外（WebClientResponseException / JAX-WS 例外等）
     */
    public CoreException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    /**
     * エラーコード・メッセージを指定するコンストラクタ。
     *
     * <p>Process-API の GlobalExceptionHandler でエラーコードを使って
     * レスポンスを制御したい場合に使用する。</p>
     *
     * @param errorCode エラーコード（例: "REST_SAMPLE_404"）
     * @param message   エラーメッセージ
     */
    public CoreException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * エラーコード・メッセージ・原因例外を指定するコンストラクタ。
     *
     * <p>最も情報量が多いコンストラクタ。外部システムとの通信エラーをラップしつつ
     * エラーコードも付与する場合に使用する。</p>
     *
     * @param errorCode エラーコード（例: "REST_SAMPLE_500"）
     * @param message   エラーメッセージ
     * @param cause     原因例外
     */
    public CoreException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    // ============================================================
    // ゲッター
    // ============================================================

    /**
     * エラーコードを返す。
     *
     * @return エラーコード。コンストラクタで指定しなかった場合は {@code null}
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * エラーコードが設定されているかどうかを返す。
     *
     * @return エラーコードが {@code null} でない場合 {@code true}
     */
    public boolean hasErrorCode() {
        return errorCode != null;
    }
}
