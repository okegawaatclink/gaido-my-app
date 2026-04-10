package jp.co.createlink.core.rest;

import jp.co.createlink.core.exception.CoreException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Map;

/**
 * SampleRestClient
 *
 * <p>Spring WebClient を使った REST 接続クライアントのテンプレートクラス。</p>
 *
 * <p>このクラスは「接続パターンの見本」として設計されている。新規 REST システムに
 * 接続する際は、このクラスをコピーして以下を変更すること:</p>
 * <ol>
 *   <li>クラス名を接続先システム名に合わせる（例: {@code HogeSystemRestClient}）</li>
 *   <li>{@code @Value} のプロパティキーを {@code application-core.yml} に合わせる</li>
 *   <li>メソッドを OpenAPI 定義に沿った実装に書き換える</li>
 *   <li>レスポンス型を接続先の DTO に合わせる</li>
 * </ol>
 *
 * <h2>WebClient 選定理由</h2>
 * <ul>
 *   <li>Spring 5 以降の推奨 REST クライアント（RestTemplate は非推奨方向）</li>
 *   <li>リアクティブ（非同期）・同期の両方をサポート</li>
 *   <li>タイムアウト・リトライ・SSL 設定が柔軟</li>
 * </ul>
 *
 * <h2>同期呼び出しパターン</h2>
 * <p>このテンプレートでは {@code .block()} を使った同期呼び出しパターンを採用する。
 * Process-API が同期処理前提の場合はこのパターンが最もシンプル。
 * リアクティブ（非同期）パターンに変更する場合は {@code .block()} を外し、
 * 戻り値を {@code Mono<T>} / {@code Flux<T>} に変更すること。</p>
 */
@Component
public class SampleRestClient {

    /**
     * デフォルトのタイムアウト時間（秒）。
     * 接続先システムの応答時間に合わせて調整すること。
     */
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    /**
     * WebClient インスタンス。
     * {@link #SampleRestClient(WebClient.Builder, String)} でベース URL を設定して生成する。
     */
    private final WebClient webClient;

    /**
     * コンストラクタ。
     *
     * <p>Spring の DI コンテナから {@link WebClient.Builder} を受け取り、
     * ベース URL を設定した {@link WebClient} インスタンスを生成する。</p>
     *
     * <p>{@link WebClient.Builder} は {@link jp.co.createlink.core.config.CoreConfig} で
     * Bean として定義されており、共通の SSL 設定・タイムアウト設定が適用されている。</p>
     *
     * @param webClientBuilder Spring が提供する {@link WebClient.Builder}（CoreConfig.java で定義）
     * @param baseUrl          接続先 REST API のベース URL（application-core.yml から注入）
     */
    public SampleRestClient(
            WebClient.Builder webClientBuilder,
            // ▼ プロパティキーは application-core.yml に合わせて変更すること
            @Value("${core.rest.sample.base-url:http://localhost:8080}") String baseUrl
    ) {
        // ベース URL を設定して WebClient インスタンスを生成する
        // ベース URL はクラス単位で固定し、各メソッドでパスを追加するパターン
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    // ============================================================
    // GET メソッドのサンプル
    // ============================================================

    /**
     * サンプルデータを1件取得する（GET）。
     *
     * <p>OpenAPI 定義の {@code GET /samples/{id}} に対応するサンプルメソッド。
     * 新規実装時はこのメソッドをコピーして:</p>
     * <ol>
     *   <li>パス・クエリパラメータを接続先の OpenAPI 定義に合わせる</li>
     *   <li>戻り値の型を接続先 DTO に合わせる</li>
     *   <li>エラーハンドリングを接続先のエラーレスポンスに合わせる</li>
     * </ol>
     *
     * @param id 取得対象のリソース ID
     * @return レスポンスボディを {@code Map} で返す（実装時は接続先の DTO に変更すること）
     * @throws CoreException 通信エラーまたは接続先からのエラーレスポンス時にスロー
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getSampleData(String id) {
        try {
            // .retrieve(): レスポンスを取得する（4xx/5xx は自動的に WebClientResponseException に変換）
            // .bodyToMono(): レスポンスボディを指定の型で受け取る
            // .block(timeout): 同期呼び出し（リアクティブパターンに変更する場合は .block() を外す）
            return webClient.get()
                    // ▼ パスは OpenAPI 定義に合わせて変更すること
                    .uri("/samples/{id}", id)
                    .retrieve()
                    // ▼ 戻り値の型は接続先の DTO クラスに変更すること（例: SampleResponse.class）
                    .bodyToMono(Map.class)
                    .block(DEFAULT_TIMEOUT);
        } catch (WebClientResponseException e) {
            // 接続先から 4xx / 5xx のエラーレスポンスが返った場合
            // ▼ エラーハンドリングは接続先のエラーレスポンス仕様に合わせて変更すること
            throw new CoreException(
                    "REST接続エラー: GET /samples/" + id
                            + " status=" + e.getStatusCode()
                            + " body=" + e.getResponseBodyAsString(),
                    e
            );
        } catch (Exception e) {
            // ネットワーク障害・タイムアウト等
            throw new CoreException("REST接続エラー: GET /samples/" + id, e);
        }
    }

    // ============================================================
    // POST メソッドのサンプル
    // ============================================================

    /**
     * サンプルデータを登録する（POST）。
     *
     * <p>OpenAPI 定義の {@code POST /samples} に対応するサンプルメソッド。
     * リクエストボディを JSON として送信し、登録結果を受け取る。</p>
     *
     * @param requestBody リクエストボディ（実装時は接続先のリクエスト DTO クラスに変更すること）
     * @return レスポンスボディを {@code Map} で返す（実装時は接続先のレスポンス DTO に変更すること）
     * @throws CoreException 通信エラーまたは接続先からのエラーレスポンス時にスロー
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> postSampleData(Map<String, Object> requestBody) {
        try {
            return webClient.post()
                    // ▼ パスは OpenAPI 定義に合わせて変更すること
                    .uri("/samples")
                    // リクエストボディを JSON として送信する
                    // ▼ bodyValue() の引数は接続先のリクエスト DTO オブジェクトに変更すること
                    .bodyValue(requestBody)
                    .retrieve()
                    // ▼ 戻り値の型は接続先のレスポンス DTO クラスに変更すること
                    .bodyToMono(Map.class)
                    .block(DEFAULT_TIMEOUT);
        } catch (WebClientResponseException e) {
            throw new CoreException(
                    "REST接続エラー: POST /samples"
                            + " status=" + e.getStatusCode()
                            + " body=" + e.getResponseBodyAsString(),
                    e
            );
        } catch (Exception e) {
            throw new CoreException("REST接続エラー: POST /samples", e);
        }
    }
}
