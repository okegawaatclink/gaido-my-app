package jp.co.createlink.processapi.controller;

import jp.co.createlink.processapi.dto.SampleResponse;
import jp.co.createlink.processapi.exception.GlobalExceptionHandler;
import jp.co.createlink.processapi.service.SampleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 【モジュール】SampleController
 * SampleController のユニットテスト。MockMvc を使用して HTTP リクエスト/レスポンスを検証する。
 * SampleService はモック化してコントローラーのみをテストする。
 */
@WebMvcTest(controllers = {SampleController.class, GlobalExceptionHandler.class})
class SampleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * SampleService のモック。
     * テストケースごとに振る舞いを設定する。
     */
    @MockitoBean
    private SampleService sampleService;

    /**
     * 【テスト対象】GET /api/v1/sample
     * 【テスト内容】正常なリクエストに対してサンプルデータ一覧が返ること
     * 【期待結果】HTTP 200 OK、JSON 配列で SampleResponse が返る
     */
    @Test
    void should_return_sample_list_when_get_sample_endpoint() throws Exception {
        // Arrange（準備）: サービスのモック設定
        List<SampleResponse> mockSamples = List.of(
                new SampleResponse("sample-001", "サンプル 1", Map.of("source", "stub")),
                new SampleResponse("sample-002", "サンプル 2", Map.of("source", "stub"))
        );
        when(sampleService.getSampleList()).thenReturn(mockSamples);

        // Act & Assert（実行 & 検証）
        mockMvc.perform(get("/api/v1/sample")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("sample-001"))
                .andExpect(jsonPath("$[0].name").value("サンプル 1"))
                .andExpect(jsonPath("$[1].id").value("sample-002"));
    }

    /**
     * 【テスト対象】GET /api/v1/sample
     * 【テスト内容】サービスが空リストを返す場合
     * 【期待結果】HTTP 200 OK、空の JSON 配列が返る
     */
    @Test
    void should_return_empty_array_when_no_samples_exist() throws Exception {
        // Arrange: 空リストを返すモック設定
        when(sampleService.getSampleList()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/v1/sample")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    /**
     * 【テスト対象】GET /api/v1/sample/{id}
     * 【テスト内容】存在するIDを指定した場合
     * 【期待結果】HTTP 200 OK、指定IDの SampleResponse が返る
     */
    @Test
    void should_return_sample_when_get_sample_by_valid_id() throws Exception {
        // Arrange
        String id = "sample-001";
        SampleResponse mockSample = new SampleResponse(id, "サンプル 1", Map.of("source", "stub"));
        when(sampleService.getSampleById(id)).thenReturn(mockSample);

        // Act & Assert
        mockMvc.perform(get("/api/v1/sample/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("サンプル 1"));
    }

    /**
     * 【テスト対象】GET /api/v1/sample/{id}
     * 【テスト内容】存在しないIDを指定した場合
     * 【期待結果】HTTP 404 Not Found、統一エラーフォーマットが返る
     *
     * 【前提条件】SampleService が NoSuchElementException を投げる
     */
    @Test
    void should_return_404_when_sample_not_found() throws Exception {
        // Arrange: 存在しないIDでは NoSuchElementException を投げるモック設定
        String notFoundId = "notfound";
        when(sampleService.getSampleById(notFoundId))
                .thenThrow(new NoSuchElementException("サンプルデータが見つかりません: id=" + notFoundId));

        // Act & Assert: 404 と統一エラーフォーマットを検証
        mockMvc.perform(get("/api/v1/sample/{id}", notFoundId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("サンプルデータが見つかりません: id=" + notFoundId))
                .andExpect(jsonPath("$.path").value("/api/v1/sample/" + notFoundId))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * 【テスト対象】GET /api/v1/sample/{id}
     * 【テスト内容】サービスが IllegalArgumentException を投げる場合
     * 【期待結果】HTTP 400 Bad Request、統一エラーフォーマットが返る
     */
    @Test
    void should_return_400_when_invalid_argument() throws Exception {
        // Arrange: IllegalArgumentException を投げるモック設定
        when(sampleService.getSampleById(anyString()))
                .thenThrow(new IllegalArgumentException("IDは空にできません"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/sample/{id}", "test-id")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("IDは空にできません"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
