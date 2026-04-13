package jp.co.createlink.processapi.exception;

import jakarta.servlet.http.HttpServletRequest;
import jp.co.createlink.core.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

/**
 * GlobalExceptionHandler
 *
 * <p>Process-API 全体の例外を一元的に処理するコントローラーアドバイス。
 * 例外ごとに適切な HTTP ステータスコードと統一フォーマットの {@link ErrorResponse} を返す。</p>
 *
 * <h2>ハンドリング対象と HTTP ステータスの対応</h2>
 * <table border="1">
 *   <tr><th>例外</th><th>HTTP ステータス</th><th>用途</th></tr>
 *   <tr><td>{@link IllegalArgumentException}</td><td>400 Bad Request</td><td>入力値不正</td></tr>
 *   <tr><td>{@link NoSuchElementException}</td><td>404 Not Found</td><td>リソースが存在しない</td></tr>
 *   <tr><td>{@link CoreException}</td><td>500 Internal Server Error</td><td>社内システム接続エラー</td></tr>
 *   <tr><td>{@link Exception}（その他）</td><td>500 Internal Server Error</td><td>予期しないエラー</td></tr>
 * </table>
 *
 * <h2>使い方（VibeCoding用）</h2>
 * <p>Controller / Service で以下の例外を投げるだけで、このクラスが自動的に変換する:</p>
 * <ul>
 *   <li>入力値が不正: {@code throw new IllegalArgumentException("メッセージ")}</li>
 *   <li>リソースなし: {@code throw new NoSuchElementException("メッセージ")}</li>
 *   <li>Core接続エラー: Core クライアントが自動的に {@link CoreException} を投げる</li>
 * </ul>
 *
 * <h2>新規例外の追加方法</h2>
 * <ol>
 *   <li>新規の {@code @ExceptionHandler} メソッドを追加する</li>
 *   <li>{@link #buildErrorResponse(HttpStatus, String, HttpServletRequest)} を使って
 *       {@link ErrorResponse} を生成する</li>
 *   <li>適切な {@link HttpStatus} を設定して {@link ResponseEntity} で返す</li>
 * </ol>
 *
 * @see ErrorResponse 統一エラーレスポンスDTO
 */
@RestControllerAdvice  // @ControllerAdvice + @ResponseBody の組み合わせ。JSON レスポンスを自動で返す。
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ============================================================
    // 例外ハンドラー
    // ============================================================

    /**
     * Core ライブラリ例外ハンドラー（500 Internal Server Error）。
     *
     * <p>社内システムへの接続失敗（SOAP/REST タイムアウト、エラーレスポンス等）を
     * 500 Internal Server Error として返す。</p>
     *
     * <p>Core ライブラリが提供する {@link CoreException} は、すべての接続エラーを
     * ラップしているため、このメソッドで一元処理できる。</p>
     *
     * @param ex      発生した Core ライブラリ例外
     * @param request HTTP リクエスト情報（パス取得に使用）
     * @return 500 エラーレスポンス（統一フォーマット）
     */
    @ExceptionHandler(CoreException.class)
    public ResponseEntity<ErrorResponse> handleCoreException(
            CoreException ex,
            HttpServletRequest request
    ) {
        logger.error("Core例外が発生しました: path={}, errorCode={}, message={}",
                request.getRequestURI(), ex.getErrorCode(), ex.getMessage(), ex);

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "社内システムへの接続に失敗しました: " + ex.getMessage(),
                request
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 入力値不正例外ハンドラー（400 Bad Request）。
     *
     * <p>Controller / Service で入力値検証に失敗した場合に 400 Bad Request を返す。</p>
     *
     * <p>使用例（Service クラスでのバリデーション）:</p>
     * <pre>
     * if (id == null || id.isBlank()) {
     *     throw new IllegalArgumentException("IDは空にできません");
     * }
     * </pre>
     *
     * @param ex      発生した入力値不正例外
     * @param request HTTP リクエスト情報（パス取得に使用）
     * @return 400 エラーレスポンス（統一フォーマット）
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        logger.warn("入力値不正例外が発生しました: path={}, message={}", request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * リソース未存在例外ハンドラー（404 Not Found）。
     *
     * <p>指定された ID のリソースが存在しない場合に 404 Not Found を返す。</p>
     *
     * <p>使用例（Service クラスでの存在チェック）:</p>
     * <pre>
     * if (data == null) {
     *     throw new NoSuchElementException("サンプルデータが見つかりません: id=" + id);
     * }
     * </pre>
     *
     * @param ex      発生したリソース未存在例外
     * @param request HTTP リクエスト情報（パス取得に使用）
     * @return 404 エラーレスポンス（統一フォーマット）
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(
            NoSuchElementException ex,
            HttpServletRequest request
    ) {
        logger.warn("リソース未存在例外が発生しました: path={}, message={}", request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * 汎用例外ハンドラー（500 Internal Server Error）。
     *
     * <p>上記のハンドラーで処理されなかった予期しない例外をキャッチして
     * 500 Internal Server Error を返す。</p>
     *
     * <p>スタックトレースをログに残すため、根本原因の調査に役立てること。</p>
     *
     * @param ex      発生した例外
     * @param request HTTP リクエスト情報（パス取得に使用）
     * @return 500 エラーレスポンス（統一フォーマット）
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        logger.error("予期しない例外が発生しました: path={}, exceptionType={}, message={}",
                request.getRequestURI(), ex.getClass().getName(), ex.getMessage(), ex);

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "内部サーバーエラーが発生しました",
                request
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    // ============================================================
    // プライベートメソッド
    // ============================================================

    /**
     * 統一フォーマットの {@link ErrorResponse} を生成する。
     *
     * <p>各 {@code @ExceptionHandler} メソッドから呼び出し、統一フォーマットで
     * エラーレスポンスを生成する。</p>
     *
     * @param status  HTTP ステータス
     * @param message エラーメッセージ
     * @param request HTTP リクエスト情報（パス取得に使用）
     * @return 統一フォーマットのエラーレスポンス
     */
    private ErrorResponse buildErrorResponse(HttpStatus status, String message, HttpServletRequest request) {
        return new ErrorResponse(
                LocalDateTime.now(),       // timestamp: エラー発生時刻
                status.value(),            // status: HTTP ステータスコード（数値）
                status.getReasonPhrase(),  // error: HTTP ステータス名（文字列）
                message,                   // message: エラーメッセージ
                request.getRequestURI()    // path: リクエストパス
        );
    }
}
