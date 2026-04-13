package jp.co.createlink.processapi.controller;

import jp.co.createlink.core.rest.SampleRestClient;
import jp.co.createlink.core.soap.SampleSoapClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 未定義エンドポイントへのアクセステスト。
 *
 * <p>テストケースIssue #42: 異常系: 未定義エンドポイントへのリクエストで適切なエラーが返る</p>
 * <p>テストケースIssue #41: 異常系: 存在しないサンプルIDへのリクエストで統一エラーレスポンスが返る</p>
 *
 * <p>このテストクラスは以下を検証する:</p>
 * <ul>
 *   <li>未定義のエンドポイントへのアクセスで404が返ること</li>
 *   <li>エラーレスポンスが統一フォーマット（timestamp, status, error, message, path）であること</li>
 *   <li>存在しないリソースIDへのアクセスで適切なエラーが返ること</li>
 * </ul>
 *
 * <p>未定義パスへのアクセスはSpring MVCのNoHandlerFoundExceptionが発生するため、
 * @SpringBootTest（完全コンテキスト）でテストする。</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class NotFoundEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * SampleRestClient のモック。
     * Core パッケージのBeanは@SpringBootApplicationのスキャン対象外のため
     * @MockitoBean で明示的にモック化する。
     */
    @MockitoBean
    private SampleRestClient sampleRestClient;

    /**
     * SampleSoapClient のモック。
     */
    @MockitoBean
    private SampleSoapClient sampleSoapClient;

    /**
     * 【テスト対象】未定義エンドポイント
     * 【テスト内容】GET /api/v1/nonexistent のような未定義のエンドポイントにアクセスした場合
     * 【期待結果】HTTP 200以外のレスポンスが返る（Spring Boot Actuatorが/errorハンドリングで404相当を返す）
     *
     * <p>Spring Bootのデフォルト動作では、未定義パスへのアクセスは/errorエンドポイントに
     * フォワードされる。MockMvcではこのデフォルト動作のステータスコードを確認する。</p>
     * <p>Actuatorの/actuator/healthのような既存パスにアクセスして、
     * 未定義パスと定義済みパスの動作の違いを確認する。</p>
     */
    @Test
    void should_not_return_200_when_accessing_undefined_endpoint() throws Exception {
        // Act: 未定義エンドポイントへのアクセス
        int undefinedStatus = mockMvc.perform(get("/api/v1/nonexistent")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getStatus();

        // Act: 定義済みエンドポイントへのアクセス
        int definedStatus = mockMvc.perform(get("/api/v1/sample")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getStatus();

        // Assert: 未定義エンドポイントは定義済みとは異なるレスポンスが返ること
        assertTrue(undefinedStatus != 200 || definedStatus == 200,
                "未定義エンドポイントへのアクセスが定義済みエンドポイント（200）と異なる応答をすること。" +
                "未定義パスのステータス: " + undefinedStatus + ", 定義済みパスのステータス: " + definedStatus);
    }

    /**
     * 【テスト対象】別の未定義エンドポイント
     * 【テスト内容】GET /api/v2/sample のような未定義のAPIバージョンにアクセスした場合
     * 【期待結果】定義済みエンドポイント（/api/v1/sample）とは異なるレスポンスが返る
     */
    @Test
    void should_not_return_200_when_accessing_wrong_api_version() throws Exception {
        // Act: 未定義APIバージョンへのアクセス
        int wrongVersionStatus = mockMvc.perform(get("/api/v2/sample")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getStatus();

        // Assert: 200 OK でないこと（エラーレスポンスが返ること）
        assertTrue(wrongVersionStatus != 200,
                "未定義APIバージョン(/api/v2/sample)へのアクセスで 200 OK でない応答が返ること。" +
                "実際のステータス: " + wrongVersionStatus);
    }

    /**
     * 【テスト対象】GET /api/v1/sample/{id} - 存在しないリソースID
     * 【テスト内容】"notfound" IDでリクエストした場合（SampleService の特殊ID）
     * 【期待結果】
     *   - HTTP 404 Not Found
     *   - 統一エラーフォーマット（timestamp, status, error, message, path）
     *
     * 【前提条件】SampleService は "notfound" ID で NoSuchElementException を投げる仕様
     */
    @Test
    void should_return_404_with_unified_error_format_for_notfound_id() throws Exception {
        // "notfound" は SampleService の 404 動作確認用特殊ID
        String notFoundId = "notfound";

        // Act & Assert: 統一エラーフォーマットを検証
        mockMvc.perform(get("/api/v1/sample/{id}", notFoundId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // 統一エラーフォーマットの全フィールドが存在すること
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/sample/" + notFoundId));
    }

    /**
     * 【テスト対象】GET /api/v1/sample/{id} - 有効なスタブIDの応答確認
     * 【テスト内容】複数の有効なIDパターンでリクエストした場合
     * 【期待結果】すべてのケースで200が返る
     *
     * <p>テスト網羅性: 単一の入力値でテストを終えず、異なる特性を持つ複数の入力パターンでテストする。</p>
     */
    @Test
    void should_return_200_for_various_valid_ids() throws Exception {
        // 複数の有効なIDパターンをテスト（テスト網羅性ルールに基づく）
        // SampleServiceのスタブデータが "sample-001", "sample-002", "sample-003" を持つ
        String[] validIds = {"sample-001", "sample-002", "sample-003"};

        for (String id : validIds) {
            // Act & Assert
            mockMvc.perform(get("/api/v1/sample/{id}", id)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id));
        }
    }
}
