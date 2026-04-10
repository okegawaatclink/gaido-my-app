package jp.co.createlink.core.exception;

/**
 * SoapClientException
 *
 * <p>SOAP 接続クライアント（{@code jp.co.createlink.core.soap} パッケージ）が
 * 送出する実行時例外。</p>
 *
 * <p>以下のケースで使用する:</p>
 * <ul>
 *   <li>SOAP エンドポイントへの接続エラー（タイムアウト、接続拒否等）</li>
 *   <li>WSDL リソースの読み込みエラー</li>
 *   <li>接続先からのエラーレスポンス（statusCode != "0" 等）</li>
 * </ul>
 *
 * <p>Process-API の {@code @ControllerAdvice}（GlobalExceptionHandler）で
 * このクラスをキャッチして適切な HTTP レスポンスに変換すること。</p>
 */
public class SoapClientException extends RuntimeException {

    /**
     * メッセージのみ指定するコンストラクタ。
     *
     * @param message エラーメッセージ
     */
    public SoapClientException(String message) {
        super(message);
    }

    /**
     * メッセージと原因例外を指定するコンストラクタ。
     *
     * @param message エラーメッセージ
     * @param cause   原因例外（JAX-WS の通信例外等）
     */
    public SoapClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
