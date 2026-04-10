package jp.co.createlink.core.soap;

import jp.co.createlink.core.exception.SoapClientException;
import jp.co.createlink.core.soap.generated.GetSampleDataRequest;
import jp.co.createlink.core.soap.generated.GetSampleDataResponse;
import jp.co.createlink.core.soap.generated.SampleData;
import jp.co.createlink.core.soap.generated.SampleServicePortType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * SampleSoapClient のユニットテスト。
 *
 * <p>このテストクラスは SOAP 接続クライアントのテスト実装パターンを示す。
 * 新規 SOAP システム接続時は、このクラスを参考にしてテストを実装すること。</p>
 *
 * <h2>テスト戦略</h2>
 * <ul>
 *   <li>実際の SOAP サーバーには接続しない（WireMock や Mockito でモック化する）</li>
 *   <li>{@link SampleServicePortType}（wsimport生成インターフェース）を Mockito でモック化する</li>
 *   <li>テスト用コンストラクタ {@link SampleSoapClient#SampleSoapClient(String, SampleServicePortType)}
 *       でモックを注入する</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class SampleSoapClientTest {

    /** モック SOAP ポート（wsimport生成インターフェース）。実際の SOAP 通信はしない。 */
    @Mock
    private SampleServicePortType mockPort;

    /** テスト対象のクライアント。テスト用コンストラクタでモックポートを注入する。 */
    private SampleSoapClient client;

    /** テスト用エンドポイント URL */
    private static final String TEST_ENDPOINT_URL = "http://test.example.com/soap/sample";

    /**
     * 各テストの前に実行するセットアップ。
     * テスト用コンストラクタでモックポートを注入してクライアントを初期化する。
     */
    @BeforeEach
    void setUp() {
        // テスト用コンストラクタでモックポートを注入する
        // これにより実際の SOAP サーバーに接続せずにテストできる
        client = new SampleSoapClient(TEST_ENDPOINT_URL, mockPort);
    }

    /**
     * 【テスト対象】getSampleData メソッド
     * 【テスト内容】正常なレスポンス（statusCode="0"）が返ってきた場合
     * 【期待結果】レスポンスオブジェクトがそのまま返されること
     *
     * <p>SOAP クライアントの基本的な正常系テストパターンを示す。</p>
     */
    @Test
    void should_return_response_when_status_code_is_zero() {
        // Arrange（準備）: モックレスポンスを構築する
        SampleData sampleData = new SampleData();
        sampleData.setId("TEST-001");
        sampleData.setName("テストデータ");
        sampleData.setValue("テスト値");

        GetSampleDataResponse mockResponse = new GetSampleDataResponse();
        mockResponse.setResult(sampleData);
        mockResponse.setStatusCode("0");  // 正常ステータス
        mockResponse.setMessage("正常");

        // モックポートが呼ばれた時に上記レスポンスを返すよう設定する
        when(mockPort.getSampleData(any(GetSampleDataRequest.class)))
                .thenReturn(mockResponse);

        // Act（実行）: テスト対象メソッドを呼び出す
        GetSampleDataResponse result = client.getSampleData("TEST-001");

        // Assert（検証）: レスポンスが正しく返されていることを確認する
        assertNotNull(result, "レスポンスが null であってはならない");
        assertEquals("0", result.getStatusCode(), "ステータスコードが 0 であること");
        assertNotNull(result.getResult(), "レスポンスのデータが null であってはならない");
        assertEquals("TEST-001", result.getResult().getId(), "ID が一致すること");
        assertEquals("テストデータ", result.getResult().getName(), "名前が一致すること");
    }

    /**
     * 【テスト対象】getSampleData メソッド
     * 【テスト内容】リクエストオブジェクトに正しくパラメータが設定されること
     * 【期待結果】モックポートに渡されたリクエストの id が引数と一致すること
     *
     * <p>ArgumentCaptor を使ってリクエスト内容を検証するパターンを示す。</p>
     */
    @Test
    void should_pass_correct_id_in_request() {
        // Arrange（準備）
        String targetId = "SAMPLE-XYZ-789";

        GetSampleDataResponse mockResponse = new GetSampleDataResponse();
        mockResponse.setStatusCode("0");
        SampleData data = new SampleData();
        data.setId(targetId);
        mockResponse.setResult(data);

        when(mockPort.getSampleData(any(GetSampleDataRequest.class)))
                .thenReturn(mockResponse);

        // Act（実行）
        client.getSampleData(targetId);

        // Assert（検証）: ArgumentCaptor でリクエストオブジェクトをキャプチャして検証する
        ArgumentCaptor<GetSampleDataRequest> requestCaptor =
                ArgumentCaptor.forClass(GetSampleDataRequest.class);
        verify(mockPort, times(1)).getSampleData(requestCaptor.capture());

        GetSampleDataRequest capturedRequest = requestCaptor.getValue();
        assertEquals(targetId, capturedRequest.getId(),
                "リクエストに正しい ID が設定されていること");
    }

    /**
     * 【テスト対象】getSampleData メソッド
     * 【テスト内容】エラーステータスコード（"0" 以外）が返ってきた場合
     * 【期待結果】SoapClientException がスローされること
     *
     * <p>SOAP 接続先からのエラーレスポンスを処理するパターンを示す。</p>
     */
    @Test
    void should_throw_soap_client_exception_when_status_code_is_error() {
        // Arrange（準備）: エラーステータスコードを含むレスポンスを設定する
        GetSampleDataResponse errorResponse = new GetSampleDataResponse();
        errorResponse.setStatusCode("E001");  // エラーステータスコード
        errorResponse.setMessage("対象データが見つかりません");
        SampleData emptyData = new SampleData();
        errorResponse.setResult(emptyData);

        when(mockPort.getSampleData(any(GetSampleDataRequest.class)))
                .thenReturn(errorResponse);

        // Act & Assert（実行・検証）: 例外がスローされることを確認する
        SoapClientException exception = assertThrows(
                SoapClientException.class,
                () -> client.getSampleData("NONEXISTENT-ID"),
                "エラーステータスコードの場合に SoapClientException がスローされること"
        );

        // 例外メッセージにステータスコードが含まれていることを確認する
        assertTrue(exception.getMessage().contains("E001"),
                "例外メッセージにエラーコードが含まれていること: " + exception.getMessage());
    }

    /**
     * 【テスト対象】getSampleData メソッド
     * 【テスト内容】null レスポンスが返ってきた場合
     * 【期待結果】SoapClientException がスローされること
     *
     * <p>予期しない null レスポンスのハンドリングパターンを示す。</p>
     */
    @Test
    void should_throw_soap_client_exception_when_response_is_null() {
        // Arrange（準備）: null レスポンスを返すよう設定する
        when(mockPort.getSampleData(any(GetSampleDataRequest.class)))
                .thenReturn(null);

        // Act & Assert（実行・検証）
        SoapClientException exception = assertThrows(
                SoapClientException.class,
                () -> client.getSampleData("ANY-ID"),
                "null レスポンスの場合に SoapClientException がスローされること"
        );

        assertNotNull(exception.getMessage(), "例外メッセージが設定されていること");
    }

    /**
     * 【テスト対象】getSampleData メソッド
     * 【テスト内容】SOAP 通信時に例外（接続エラー、タイムアウト等）が発生した場合
     * 【期待結果】SoapClientException にラップされてスローされること
     *
     * <p>JAX-WS の通信エラーを SoapClientException にラップするパターンを示す。</p>
     */
    @Test
    void should_wrap_jaxws_exception_in_soap_client_exception() {
        // Arrange（準備）: SOAP 通信で例外が発生するよう設定する
        RuntimeException networkError = new RuntimeException("接続タイムアウト");
        when(mockPort.getSampleData(any(GetSampleDataRequest.class)))
                .thenThrow(networkError);

        // Act & Assert（実行・検証）
        SoapClientException exception = assertThrows(
                SoapClientException.class,
                () -> client.getSampleData("ANY-ID"),
                "JAX-WS 例外が SoapClientException にラップされること"
        );

        // 原因例外が保持されていることを確認する
        assertNotNull(exception.getCause(), "原因例外が設定されていること");
        assertEquals(networkError, exception.getCause(), "原因例外が元の例外と一致すること");
    }

    /**
     * 【テスト対象】getEndpointUrl メソッド
     * 【テスト内容】コンストラクタで設定した URL が返ること
     * 【期待結果】設定した URL がそのまま返されること
     *
     * <p>エンドポイント URL の設定確認パターンを示す。</p>
     */
    @Test
    void should_return_configured_endpoint_url() {
        // Act（実行）
        String actualUrl = client.getEndpointUrl();

        // Assert（検証）
        assertEquals(TEST_ENDPOINT_URL, actualUrl,
                "設定したエンドポイント URL が返されること");
    }
}
