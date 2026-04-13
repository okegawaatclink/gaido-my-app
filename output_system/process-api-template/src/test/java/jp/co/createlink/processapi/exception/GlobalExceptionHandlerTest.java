package jp.co.createlink.processapi.exception;

import jp.co.createlink.core.exception.CoreException;
import jp.co.createlink.processapi.controller.SampleController;
import jp.co.createlink.processapi.service.SampleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.NoSuchElementException;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 【モジュール】GlobalExceptionHandler
 * GlobalExceptionHandler のユニットテスト。
 * 各例外に対して統一エラーフォーマットが返ることを検証する。
 */
@WebMvcTest(controllers = {SampleController.class, GlobalExceptionHandler.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SampleService sampleService;

    /**
     * 【テスト対象】@ExceptionHandler(CoreException.class)
     * 【テスト内容】Core ライブラリが CoreException を投げた場合
     * 【期待結果】HTTP 500 Internal Server Error、統一エラーフォーマット（全フィールド存在）
     */
    @Test
    void should_return_500_with_error_response_when_CoreException_thrown() throws Exception {
        // Arrange: CoreException を投げるモック設定
        when(sampleService.getSampleList())
                .thenThrow(new CoreException("REST_001", "社内システムへの接続に失敗しました"));

        // Act & Assert: 500 と統一エラーフォーマットを検証
        mockMvc.perform(get("/api/v1/sample")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/sample"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * 【テスト対象】@ExceptionHandler(IllegalArgumentException.class)
     * 【テスト内容】Service が IllegalArgumentException を投げた場合
     * 【期待結果】HTTP 400 Bad Request、統一エラーフォーマット
     */
    @Test
    void should_return_400_with_error_response_when_IllegalArgumentException_thrown() throws Exception {
        // Arrange
        when(sampleService.getSampleById("bad-id"))
                .thenThrow(new IllegalArgumentException("IDの形式が不正です"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/sample/{id}", "bad-id")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("IDの形式が不正です"))
                .andExpect(jsonPath("$.path").value("/api/v1/sample/bad-id"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * 【テスト対象】@ExceptionHandler(NoSuchElementException.class)
     * 【テスト内容】Service が NoSuchElementException を投げた場合
     * 【期待結果】HTTP 404 Not Found、統一エラーフォーマット
     */
    @Test
    void should_return_404_with_error_response_when_NoSuchElementException_thrown() throws Exception {
        // Arrange
        when(sampleService.getSampleById("notfound"))
                .thenThrow(new NoSuchElementException("サンプルデータが見つかりません: id=notfound"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/sample/{id}", "notfound")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("サンプルデータが見つかりません: id=notfound"))
                .andExpect(jsonPath("$.path").value("/api/v1/sample/notfound"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * 【テスト対象】@ExceptionHandler(Exception.class)
     * 【テスト内容】予期しない例外が発生した場合
     * 【期待結果】HTTP 500 Internal Server Error、統一エラーフォーマット（一般的なメッセージ）
     */
    @Test
    void should_return_500_with_generic_message_when_unexpected_exception_thrown() throws Exception {
        // Arrange: RuntimeException（予期しない例外）を投げるモック設定
        when(sampleService.getSampleList())
                .thenThrow(new RuntimeException("予期しないエラー"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/sample")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                // 汎用例外ハンドラーは詳細メッセージを隠すことで情報漏洩を防ぐ
                .andExpect(jsonPath("$.message").value("内部サーバーエラーが発生しました"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
