package jp.co.createlink.core.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CoreException のユニットテスト。
 *
 * <p>このテストクラスは CoreException の各コンストラクタと
 * ゲッターメソッドが仕様通りに動作することを検証する。</p>
 */
class CoreExceptionTest {

    /**
     * 【テスト対象】CoreException(String message) コンストラクタ
     * 【テスト内容】メッセージのみ指定した場合
     * 【期待結果】メッセージが設定され、errorCode は null であること
     */
    @Test
    void should_have_message_and_null_errorCode_when_constructed_with_message_only() {
        // Arrange & Act（準備・実行）
        String message = "テストエラーメッセージ";
        CoreException exception = new CoreException(message);

        // Assert（検証）
        assertEquals(message, exception.getMessage(), "メッセージが設定されていること");
        assertNull(exception.getErrorCode(), "errorCode が null であること");
        assertFalse(exception.hasErrorCode(), "hasErrorCode() が false であること");
        assertNull(exception.getCause(), "原因例外が null であること");
    }

    /**
     * 【テスト対象】CoreException(String message, Throwable cause) コンストラクタ
     * 【テスト内容】メッセージと原因例外を指定した場合
     * 【期待結果】メッセージと原因例外が設定され、errorCode は null であること
     */
    @Test
    void should_have_message_and_cause_when_constructed_with_message_and_cause() {
        // Arrange（準備）
        String message = "REST接続エラー";
        RuntimeException cause = new RuntimeException("接続タイムアウト");

        // Act（実行）
        CoreException exception = new CoreException(message, cause);

        // Assert（検証）
        assertEquals(message, exception.getMessage(), "メッセージが設定されていること");
        assertNull(exception.getErrorCode(), "errorCode が null であること");
        assertFalse(exception.hasErrorCode(), "hasErrorCode() が false であること");
        assertEquals(cause, exception.getCause(), "原因例外が設定されていること");
    }

    /**
     * 【テスト対象】CoreException(String errorCode, String message) コンストラクタ
     * 【テスト内容】エラーコードとメッセージを指定した場合
     * 【期待結果】エラーコードとメッセージが設定されること
     *
     * <p>エラーコードを使って Process-API の GlobalExceptionHandler で
     * レスポンスを制御するパターンのテスト。</p>
     */
    @Test
    void should_have_errorCode_and_message_when_constructed_with_both() {
        // Arrange（準備）
        String errorCode = "REST_SAMPLE_404";
        String message = "指定されたリソースが見つかりません";

        // Act（実行）
        CoreException exception = new CoreException(errorCode, message);

        // Assert（検証）
        assertEquals(errorCode, exception.getErrorCode(), "エラーコードが設定されていること");
        assertEquals(message, exception.getMessage(), "メッセージが設定されていること");
        assertTrue(exception.hasErrorCode(), "hasErrorCode() が true であること");
        assertNull(exception.getCause(), "原因例外が null であること");
    }

    /**
     * 【テスト対象】CoreException(String errorCode, String message, Throwable cause) コンストラクタ
     * 【テスト内容】エラーコード・メッセージ・原因例外をすべて指定した場合
     * 【期待結果】すべてのフィールドが設定されること
     *
     * <p>最も情報量が多いコンストラクタが全フィールドを正しく保持することを確認する。</p>
     */
    @Test
    void should_have_all_fields_when_constructed_with_all_arguments() {
        // Arrange（準備）
        String errorCode = "REST_SAMPLE_500";
        String message = "サーバー内部エラーが発生しました";
        RuntimeException cause = new RuntimeException("HTTP 500 Internal Server Error");

        // Act（実行）
        CoreException exception = new CoreException(errorCode, message, cause);

        // Assert（検証）
        assertEquals(errorCode, exception.getErrorCode(), "エラーコードが設定されていること");
        assertEquals(message, exception.getMessage(), "メッセージが設定されていること");
        assertTrue(exception.hasErrorCode(), "hasErrorCode() が true であること");
        assertEquals(cause, exception.getCause(), "原因例外が設定されていること");
    }

    /**
     * 【テスト対象】CoreException が RuntimeException を継承していること
     * 【テスト内容】CoreException を RuntimeException としてキャッチできること
     * 【期待結果】RuntimeException として catch できること
     *
     * <p>Process-API で CoreException を RuntimeException として扱える実装パターンのテスト。</p>
     */
    @Test
    void should_be_catchable_as_runtime_exception() {
        // Act & Assert（実行・検証）
        assertDoesNotThrow(() -> {
            try {
                throw new CoreException("REST_001", "テストエラー");
            } catch (RuntimeException e) {
                // RuntimeException として catch できること
                assertInstanceOf(CoreException.class, e, "CoreException であること");
                CoreException coreEx = (CoreException) e;
                assertEquals("REST_001", coreEx.getErrorCode());
            }
        });
    }

    /**
     * 【テスト対象】hasErrorCode メソッド
     * 【テスト内容】エラーコードが空文字の場合
     * 【期待結果】hasErrorCode() は true を返すこと（null チェックのみ行うため）
     *
     * <p>空文字はエラーコードとして有効ではないが、null でないため hasErrorCode() は true を返す仕様。</p>
     */
    @Test
    void should_return_true_for_hasErrorCode_when_errorCode_is_empty_string() {
        // Arrange & Act（準備・実行）
        CoreException exception = new CoreException("", "メッセージ");

        // Assert（検証）: 空文字は null でないため true を返す
        assertTrue(exception.hasErrorCode(), "空文字の場合は null でないため true を返すこと");
    }
}
