package jp.co.createlink.core.rest;

import jp.co.createlink.core.exception.CoreException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * SampleRestClient のユニットテスト。
 *
 * <p>このテストクラスは Spring WebClient を使った REST 接続クライアントの
 * テスト実装パターンを示す。新規 REST システム接続時は、このクラスを参考にして
 * テストを実装すること。</p>
 *
 * <h2>テスト戦略</h2>
 * <ul>
 *   <li>実際の HTTP サーバーには接続しない（Mockito で WebClient チェーンをモック化する）</li>
 *   <li>WebClient の各メソッドチェーン（get/post/uri/retrieve/bodyToMono/block）を
 *       モック化して、クライアントロジックのみを検証する</li>
 *   <li>正常系・エラー系（WebClientResponseException）・ネットワークエラー系を網羅する</li>
 * </ul>
 *
 * <h2>注意</h2>
 * <p>WebClient は流れるようなAPIチェーン（Fluent API）のため、
 * 各ステップの戻り値をそれぞれモック化する必要がある。</p>
 */
@ExtendWith(MockitoExtension.class)
class SampleRestClientTest {

    // WebClient の各チェーンステップをモック化する
    @Mock
    private WebClient mockWebClient;

    @Mock
    private WebClient.Builder mockWebClientBuilder;

    @Mock
    private WebClient.RequestHeadersUriSpec<?> mockGetUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> mockGetHeadersSpec;

    @Mock
    private WebClient.ResponseSpec mockResponseSpec;

    @Mock
    private WebClient.RequestBodyUriSpec mockPostBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> mockPostHeadersSpec;

    /** テスト対象のクライアント */
    private SampleRestClient client;

    /** テスト用ベース URL */
    private static final String TEST_BASE_URL = "http://test.example.com";

    /**
     * 各テストの前に実行するセットアップ。
     * WebClient.Builder をモック化して SampleRestClient を初期化する。
     */
    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        // WebClient.Builder.baseUrl().build() → mockWebClient を返すよう設定
        when(mockWebClientBuilder.baseUrl(anyString())).thenReturn(mockWebClientBuilder);
        when(mockWebClientBuilder.build()).thenReturn(mockWebClient);

        // テスト対象のクライアントを初期化する
        client = new SampleRestClient(mockWebClientBuilder, TEST_BASE_URL);
    }

    // ============================================================
    // GET メソッドのテスト
    // ============================================================

    /**
     * 【テスト対象】getSampleData メソッド
     * 【テスト内容】正常なレスポンスが返ってきた場合
     * 【期待結果】レスポンスの Map がそのまま返されること
     *
     * <p>WebClient を使った GET リクエストの正常系テストパターンを示す。</p>
     */
    @Test
    @SuppressWarnings("unchecked")
    void should_return_response_map_when_get_succeeds() {
        // Arrange（準備）: 正常なレスポンスを設定する
        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("id", "12345");
        expectedResponse.put("name", "テストデータ");
        expectedResponse.put("value", "sample-value");

        // WebClient GET チェーンをモック化する
        when(mockWebClient.get()).thenReturn((WebClient.RequestHeadersUriSpec) mockGetUriSpec);
        when(mockGetUriSpec.uri(anyString(), anyString())).thenReturn((WebClient.RequestHeadersSpec) mockGetHeadersSpec);
        when(mockGetHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(expectedResponse));

        // Act（実行）
        Map<String, Object> result = client.getSampleData("12345");

        // Assert（検証）
        assertNotNull(result, "レスポンスが null でないこと");
        assertEquals("12345", result.get("id"), "ID が一致すること");
        assertEquals("テストデータ", result.get("name"), "名前が一致すること");

        // GET メソッドが1回呼ばれたことを確認する
        verify(mockWebClient, times(1)).get();
    }

    /**
     * 【テスト対象】getSampleData メソッド
     * 【テスト内容】接続先から 404 Not Found が返ってきた場合
     * 【期待結果】CoreException がスローされ、ステータスコードとURLがメッセージに含まれること
     *
     * <p>4xx エラーレスポンスを CoreException にラップするパターンのテスト。</p>
     */
    @Test
    @SuppressWarnings("unchecked")
    void should_throw_core_exception_when_get_returns_404() {
        // Arrange（準備）: 404 エラーを返すよう設定する
        WebClientResponseException notFoundException = WebClientResponseException.create(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                null,
                null,
                null
        );

        when(mockWebClient.get()).thenReturn((WebClient.RequestHeadersUriSpec) mockGetUriSpec);
        when(mockGetUriSpec.uri(anyString(), anyString())).thenReturn((WebClient.RequestHeadersSpec) mockGetHeadersSpec);
        when(mockGetHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(Map.class)).thenReturn(Mono.error(notFoundException));

        // Act & Assert（実行・検証）
        CoreException exception = assertThrows(
                CoreException.class,
                () -> client.getSampleData("NONEXISTENT"),
                "404 エラーの場合に CoreException がスローされること"
        );

        // 例外メッセージにステータスコードが含まれていることを確認する
        assertTrue(exception.getMessage().contains("404"),
                "例外メッセージに 404 が含まれていること: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("GET"),
                "例外メッセージに GET が含まれていること: " + exception.getMessage());
        assertNotNull(exception.getCause(), "原因例外が設定されていること");
        assertEquals(notFoundException, exception.getCause(), "原因例外が WebClientResponseException であること");
    }

    /**
     * 【テスト対象】getSampleData メソッド
     * 【テスト内容】ネットワーク障害（接続拒否等）が発生した場合
     * 【期待結果】CoreException がスローされること
     *
     * <p>ネットワーク障害を CoreException にラップするパターンのテスト。</p>
     */
    @Test
    @SuppressWarnings("unchecked")
    void should_throw_core_exception_when_network_error_occurs_on_get() {
        // Arrange（準備）: ネットワーク障害をシミュレートする
        RuntimeException networkError = new RuntimeException("Connection refused: test.example.com/127.0.0.1:8080");

        when(mockWebClient.get()).thenReturn((WebClient.RequestHeadersUriSpec) mockGetUriSpec);
        when(mockGetUriSpec.uri(anyString(), anyString())).thenReturn((WebClient.RequestHeadersSpec) mockGetHeadersSpec);
        when(mockGetHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(Map.class)).thenReturn(Mono.error(networkError));

        // Act & Assert（実行・検証）
        CoreException exception = assertThrows(
                CoreException.class,
                () -> client.getSampleData("ANY-ID"),
                "ネットワークエラーの場合に CoreException がスローされること"
        );

        assertNotNull(exception.getCause(), "原因例外が設定されていること");
        assertEquals(networkError, exception.getCause(), "原因例外が NetworkError であること");
    }

    // ============================================================
    // POST メソッドのテスト
    // ============================================================

    /**
     * 【テスト対象】postSampleData メソッド
     * 【テスト内容】正常なレスポンスが返ってきた場合
     * 【期待結果】登録結果の Map がそのまま返されること
     *
     * <p>WebClient を使った POST リクエストの正常系テストパターンを示す。</p>
     */
    @Test
    @SuppressWarnings("unchecked")
    void should_return_response_map_when_post_succeeds() {
        // Arrange（準備）: 正常なレスポンスを設定する
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", "新規データ");
        requestBody.put("value", "new-value-001");

        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("id", "99999");
        expectedResponse.put("name", "新規データ");
        expectedResponse.put("value", "new-value-001");
        expectedResponse.put("createdAt", "2026-01-01T00:00:00Z");

        // WebClient POST チェーンをモック化する
        when(mockWebClient.post()).thenReturn(mockPostBodyUriSpec);
        when(mockPostBodyUriSpec.uri(anyString())).thenReturn(mockPostBodyUriSpec);
        when(mockPostBodyUriSpec.bodyValue(any())).thenReturn((WebClient.RequestHeadersSpec) mockPostHeadersSpec);
        when(mockPostHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(expectedResponse));

        // Act（実行）
        Map<String, Object> result = client.postSampleData(requestBody);

        // Assert（検証）
        assertNotNull(result, "レスポンスが null でないこと");
        assertEquals("99999", result.get("id"), "ID が一致すること");
        assertEquals("新規データ", result.get("name"), "名前が一致すること");

        // POST メソッドが1回呼ばれたことを確認する
        verify(mockWebClient, times(1)).post();
    }

    /**
     * 【テスト対象】postSampleData メソッド
     * 【テスト内容】接続先から 400 Bad Request が返ってきた場合
     * 【期待結果】CoreException がスローされ、ステータスコードがメッセージに含まれること
     */
    @Test
    @SuppressWarnings("unchecked")
    void should_throw_core_exception_when_post_returns_400() {
        // Arrange（準備）: 400 エラーを返すよう設定する
        WebClientResponseException badRequestException = WebClientResponseException.create(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                null,
                "{\"error\": \"invalid_request\"}".getBytes(),
                null
        );

        when(mockWebClient.post()).thenReturn(mockPostBodyUriSpec);
        when(mockPostBodyUriSpec.uri(anyString())).thenReturn(mockPostBodyUriSpec);
        when(mockPostBodyUriSpec.bodyValue(any())).thenReturn((WebClient.RequestHeadersSpec) mockPostHeadersSpec);
        when(mockPostHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(Map.class)).thenReturn(Mono.error(badRequestException));

        // Act & Assert（実行・検証）
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", "テスト");

        CoreException exception = assertThrows(
                CoreException.class,
                () -> client.postSampleData(requestBody),
                "400 エラーの場合に CoreException がスローされること"
        );

        assertTrue(exception.getMessage().contains("400"),
                "例外メッセージに 400 が含まれていること: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("POST"),
                "例外メッセージに POST が含まれていること: " + exception.getMessage());
    }

    /**
     * 【テスト対象】postSampleData メソッド
     * 【テスト内容】サーバーエラー（500）が発生した場合
     * 【期待結果】CoreException がスローされること
     */
    @Test
    @SuppressWarnings("unchecked")
    void should_throw_core_exception_when_post_returns_500() {
        // Arrange（準備）: 500 エラーを返すよう設定する
        WebClientResponseException serverErrorException = WebClientResponseException.create(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                null,
                null,
                null
        );

        when(mockWebClient.post()).thenReturn(mockPostBodyUriSpec);
        when(mockPostBodyUriSpec.uri(anyString())).thenReturn(mockPostBodyUriSpec);
        when(mockPostBodyUriSpec.bodyValue(any())).thenReturn((WebClient.RequestHeadersSpec) mockPostHeadersSpec);
        when(mockPostHeadersSpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(Map.class)).thenReturn(Mono.error(serverErrorException));

        // Act & Assert（実行・検証）
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", "テスト");

        CoreException exception = assertThrows(
                CoreException.class,
                () -> client.postSampleData(requestBody),
                "500 エラーの場合に CoreException がスローされること"
        );

        assertTrue(exception.getMessage().contains("500"),
                "例外メッセージに 500 が含まれていること: " + exception.getMessage());
        assertNotNull(exception.getCause(), "原因例外が設定されていること");
    }
}
