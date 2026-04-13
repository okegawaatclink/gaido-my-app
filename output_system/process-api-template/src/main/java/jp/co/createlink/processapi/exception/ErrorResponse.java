package jp.co.createlink.processapi.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * ErrorResponse
 *
 * <p>統一エラーレスポンスDTO。{@link GlobalExceptionHandler} がエラー発生時に返す JSON 形式を定義する。</p>
 *
 * <h2>レスポンス形式</h2>
 * <pre>
 * {
 *   "timestamp": "2026-04-10T10:00:00.000",
 *   "status": 500,
 *   "error": "Internal Server Error",
 *   "message": "社内システムへの接続に失敗しました",
 *   "path": "/api/v1/sample"
 * }
 * </pre>
 *
 * <h2>フィールド説明</h2>
 * <ul>
 *   <li>{@code timestamp}: エラー発生時刻</li>
 *   <li>{@code status}: HTTP ステータスコード（数値）</li>
 *   <li>{@code error}: HTTP ステータス名（文字列）</li>
 *   <li>{@code message}: エラーメッセージ（人間が読める形式）</li>
 *   <li>{@code path}: エラーが発生したリクエストパス</li>
 * </ul>
 *
 * @see GlobalExceptionHandler エラーハンドラー（このDTOを生成する）
 */
@Schema(description = "統一エラーレスポンス")
public class ErrorResponse {

    /**
     * エラー発生時刻。
     * ISO 8601 形式（yyyy-MM-dd'T'HH:mm:ss.SSS）でシリアライズされる。
     */
    @Schema(description = "エラー発生時刻", example = "2026-04-10T10:00:00.000")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime timestamp;

    /**
     * HTTP ステータスコード（数値）。
     * 例: 400, 404, 500
     */
    @Schema(description = "HTTPステータスコード", example = "500")
    private int status;

    /**
     * HTTP ステータス名（文字列）。
     * 例: "Bad Request", "Not Found", "Internal Server Error"
     */
    @Schema(description = "HTTPステータス名", example = "Internal Server Error")
    private String error;

    /**
     * エラーメッセージ。
     * 人間が読める形式で記述する。
     */
    @Schema(description = "エラーメッセージ", example = "社内システムへの接続に失敗しました")
    private String message;

    /**
     * エラーが発生したリクエストパス。
     * {@link jakarta.servlet.http.HttpServletRequest#getRequestURI()} から取得する。
     */
    @Schema(description = "リクエストパス", example = "/api/v1/sample")
    private String path;

    // ============================================================
    // コンストラクタ
    // ============================================================

    /**
     * デフォルトコンストラクタ。
     * Jackson のデシリアライズに必要。
     */
    public ErrorResponse() {
    }

    /**
     * 全フィールドを設定するコンストラクタ。
     *
     * @param timestamp エラー発生時刻
     * @param status    HTTP ステータスコード
     * @param error     HTTP ステータス名
     * @param message   エラーメッセージ
     * @param path      リクエストパス
     */
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    // ============================================================
    // ゲッター・セッター
    // ============================================================

    /**
     * エラー発生時刻を返す。
     *
     * @return エラー発生時刻
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * エラー発生時刻を設定する。
     *
     * @param timestamp エラー発生時刻
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * HTTP ステータスコードを返す。
     *
     * @return HTTP ステータスコード
     */
    public int getStatus() {
        return status;
    }

    /**
     * HTTP ステータスコードを設定する。
     *
     * @param status HTTP ステータスコード
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * HTTP ステータス名を返す。
     *
     * @return HTTP ステータス名
     */
    public String getError() {
        return error;
    }

    /**
     * HTTP ステータス名を設定する。
     *
     * @param error HTTP ステータス名
     */
    public void setError(String error) {
        this.error = error;
    }

    /**
     * エラーメッセージを返す。
     *
     * @return エラーメッセージ
     */
    public String getMessage() {
        return message;
    }

    /**
     * エラーメッセージを設定する。
     *
     * @param message エラーメッセージ
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * リクエストパスを返す。
     *
     * @return リクエストパス
     */
    public String getPath() {
        return path;
    }

    /**
     * リクエストパスを設定する。
     *
     * @param path リクエストパス
     */
    public void setPath(String path) {
        this.path = path;
    }
}
