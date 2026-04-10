package jp.co.createlink.core.soap;

import jakarta.xml.ws.BindingProvider;
import jp.co.createlink.core.exception.SoapClientException;
import jp.co.createlink.core.soap.generated.GetSampleDataRequest;
import jp.co.createlink.core.soap.generated.GetSampleDataResponse;
import jp.co.createlink.core.soap.generated.SampleService;
import jp.co.createlink.core.soap.generated.SampleServicePortType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;

/**
 * SampleSoapClient
 *
 * <p>JAX-WS wsimport で生成されたスタブコードを利用した SOAP 接続クライアントのサンプル実装。
 * 新規 SOAP システム接続時は、このクラスを参考にしてクライアントクラスを実装すること。</p>
 *
 * <h2>新規SOAPシステム接続時の実装手順</h2>
 * <ol>
 *   <li>接続先の WSDL を {@code specs/wsdl/} に配置する</li>
 *   <li>{@code ./gradlew generateSoapSources} でスタブコードを生成する</li>
 *   <li>このクラスを参考にして {@code src/main/java/jp/co/createlink/core/soap/} に
 *       クライアントクラスを作成する</li>
 *   <li>{@code src/main/resources/application-core.yml} にエンドポイント URL を追加する</li>
 *   <li>Process-API の Service クラスから DI でクライアントを使用する</li>
 * </ol>
 *
 * <h2>パッケージ構成</h2>
 * <ul>
 *   <li>{@code soap/} - SOAP 接続クライアントを配置する（このクラス）</li>
 *   <li>{@code soap/generated/} - wsimport が自動生成したスタブクラス（手動編集禁止）</li>
 * </ul>
 *
 * @see SampleServicePortType 接続先サービスのインターフェース（wsimport生成）
 * @see SampleService JAX-WS サービスファクトリ（wsimport生成）
 */
@Component  // Spring DI コンテナに登録する。Process-API から @Autowired で注入可能。
public class SampleSoapClient {

    /**
     * SOAP エンドポイント URL。
     *
     * <p>ハードコードせず、application-core.yml から @Value で注入する。
     * 環境ごとに異なる URL を設定できるようにするため。</p>
     *
     * <p>▼ application-core.yml に以下を追加すること:</p>
     * <pre>
     * core:
     *   soap:
     *     sample:
     *       endpoint-url: http://target-system.example.com/soap/sample
     * </pre>
     *
     * <p>▼ 新規SOAPシステム接続時は、プロパティ名をシステムに合わせて変更すること。</p>
     */
    @Value("${core.soap.sample.endpoint-url:http://localhost:8080/soap/sample}")
    private String endpointUrl;

    /**
     * SOAP ポート（スタブインターフェース）。
     * {@link #createPort()} で初期化する。
     * @see #createPort()
     */
    private SampleServicePortType port;

    /**
     * デフォルトコンストラクタ。
     * Spring DI コンテナが @Component を検出してインスタンス化する。
     */
    public SampleSoapClient() {
        // Spring DI 用デフォルトコンストラクタ
        // @Value の注入はコンストラクタ呼び出し後に実行されるため、
        // ポートの初期化は遅延初期化（createPort）で行う
    }

    /**
     * テスト用コンストラクタ。
     *
     * <p>単体テストでモックポートを注入するために使用する。
     * 本番コードでは Spring DI によりデフォルトコンストラクタが使用される。</p>
     *
     * @param endpointUrl SOAP エンドポイント URL
     * @param port        モックポート（テスト用）
     */
    SampleSoapClient(String endpointUrl, SampleServicePortType port) {
        this.endpointUrl = endpointUrl;
        this.port = port;
    }

    /**
     * サンプルデータ取得。
     *
     * <p>SOAP 接続のサンプルオペレーション。wsimport が生成した {@link SampleServicePortType}
     * インターフェースのメソッドを呼び出すパターンを示す。</p>
     *
     * <p>新規SOAPシステム接続時は、このメソッドを参考にして接続先のオペレーションに
     * 対応したメソッドを実装すること。</p>
     *
     * <h3>実装パターン</h3>
     * <ol>
     *   <li>リクエストオブジェクトを生成してパラメータをセットする</li>
     *   <li>ポート（スタブ）を取得する（初回はポートを生成、以降は再利用）</li>
     *   <li>ポートのメソッドを呼び出してレスポンスを取得する</li>
     *   <li>レスポンスのステータスコードを確認してエラーハンドリングを行う</li>
     * </ol>
     *
     * @param id 取得対象のID（接続先システムのキー項目）
     * @return SOAP レスポンス
     * @throws SoapClientException SOAP 通信エラーまたは接続先エラーレスポンスの場合
     */
    public GetSampleDataResponse getSampleData(String id) {
        // ▼ 1. リクエストオブジェクトを生成してパラメータをセットする
        //      接続先のオペレーションに合わせてリクエストクラスを変更すること
        GetSampleDataRequest request = new GetSampleDataRequest();
        request.setId(id);

        // ▼ 2. ポート（スタブ）を取得する
        //      getOrCreatePort() は遅延初期化で endpointUrl を反映したポートを返す
        SampleServicePortType soapPort = getOrCreatePort();

        try {
            // ▼ 3. ポートのメソッドを呼び出してレスポンスを取得する
            //      接続先のオペレーション名に合わせてメソッドを変更すること
            GetSampleDataResponse response = soapPort.getSampleData(request);

            // ▼ 4. レスポンスのステータスコードを確認する
            //      接続先のエラーコード定義に合わせて条件を変更すること
            if (response == null) {
                throw new SoapClientException("SOAP レスポンスが null でした。id=" + id);
            }
            if (!"0".equals(response.getStatusCode())) {
                throw new SoapClientException(
                        String.format("SOAP エラーレスポンス: statusCode=%s, message=%s",
                                response.getStatusCode(), response.getMessage()));
            }

            return response;

        } catch (SoapClientException e) {
            // アプリケーション例外はそのまま再スローする
            throw e;
        } catch (Exception e) {
            // JAX-WS の通信エラー（タイムアウト、接続拒否等）をアプリケーション例外にラップする
            throw new SoapClientException("SOAP 通信エラーが発生しました: " + e.getMessage(), e);
        }
    }

    /**
     * SOAP ポートを生成する。
     *
     * <p>JAX-WS の {@link SampleService}（wsimport生成）から
     * {@link SampleServicePortType} ポートを取得し、
     * エンドポイント URL を {@link BindingProvider} 経由で上書きする。</p>
     *
     * <p>これにより、WSDL に記述されたデフォルト URL の代わりに
     * application-core.yml で設定した URL に接続できる。</p>
     *
     * <p>▼ 新規SOAPシステム接続時のポイント:</p>
     * <ul>
     *   <li>{@link SampleService} を接続先の Service クラスに変更する</li>
     *   <li>WSDL URL は classpathリソースまたはファイルパスを指定する</li>
     * </ul>
     *
     * @return エンドポイント URL が設定済みの SOAP ポート
     */
    private SampleServicePortType createPort() {
        try {
            // WSDL をクラスパスリソースから読み込む。
            // ▼ 新規SOAPシステム接続時は WSDL のパスを変更すること
            URL wsdlUrl = SampleSoapClient.class
                    .getClassLoader()
                    .getResource("wsdl/sample-service.wsdl");

            if (wsdlUrl == null) {
                // WSDL が classpath に存在しない場合は specs/ ディレクトリを参照する（開発時）
                // 本番環境ではビルド時にリソースとしてパッケージングすること
                throw new SoapClientException("WSDL リソースが見つかりません: wsdl/sample-service.wsdl");
            }

            // JAX-WS Service からポートを取得する
            // ▼ 接続先の Service クラスに変更すること（wsimport生成）
            SampleService service = new SampleService(wsdlUrl);
            SampleServicePortType createdPort = service.getSampleServicePort();

            // エンドポイント URL を application-core.yml の設定値で上書きする
            // WSDL に記述された URL はデフォルト値として使用し、
            // 実際の接続先は設定ファイルから注入する
            BindingProvider bindingProvider = (BindingProvider) createdPort;
            bindingProvider.getRequestContext()
                    .put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointUrl);

            return createdPort;

        } catch (SoapClientException e) {
            throw e;
        } catch (Exception e) {
            throw new SoapClientException("SOAP ポートの初期化に失敗しました: " + e.getMessage(), e);
        }
    }

    /**
     * ポートの遅延初期化と取得。
     *
     * <p>初回呼び出し時に {@link #createPort()} でポートを生成する。
     * 2回目以降は同じポートインスタンスを再利用する（非スレッドセーフ）。</p>
     *
     * <p>スレッドセーフが必要な場合は synchronized を付与するか、
     * Spring の @Scope("prototype") を使用してリクエストごとに別インスタンスを生成すること。</p>
     *
     * @return SOAP ポート
     */
    private SampleServicePortType getOrCreatePort() {
        if (port == null) {
            port = createPort();
        }
        return port;
    }

    /**
     * エンドポイント URL を取得する（テスト・デバッグ用）。
     *
     * @return 設定されているエンドポイント URL
     */
    public String getEndpointUrl() {
        return endpointUrl;
    }
}
