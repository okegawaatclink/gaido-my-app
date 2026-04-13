package jp.co.createlink.processapi.actuator;

import jp.co.createlink.core.rest.SampleRestClient;
import jp.co.createlink.core.soap.SampleSoapClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 【モジュール】Spring Boot Actuator エンドポイント
 * Actuatorエンドポイントの統合テスト。
 * 実際のアプリケーションコンテキストを起動して HTTP レスポンスを検証する。
 *
 * ■ テスト対象エンドポイント
 *   - /actuator/health       : ヘルスチェック（K8S probe対応）
 *   - /actuator/health/liveness  : K8S liveness probe
 *   - /actuator/health/readiness : K8S readiness probe
 *   - /actuator/info         : アプリ情報
 *   - /actuator/metrics      : メトリクス
 *
 * ■ Core Bean のモック化について
 *   SampleService は Core ライブラリの SampleRestClient / SampleSoapClient に依存している。
 *   Core のクライアントは jp.co.createlink.core パッケージに存在し、
 *   @SpringBootApplication のデフォルトスキャン対象（jp.co.createlink.processapi）外のため、
 *   テストコンテキストでは @MockitoBean でモック化して Spring コンテキストを起動できるようにする。
 */
@SpringBootTest
@AutoConfigureMockMvc
class ActuatorEndpointTest {

    /**
     * MockMvc: HTTP リクエストを Spring MVC コンテキスト内で擬似的に実行するためのユーティリティ。
     * Spring Boot Test が自動構成する。
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * SampleRestClient のモック。
     * このテストでは REST クライアントの実際の呼び出しは行わないため、
     * モックとして Spring コンテキストに登録することで起動エラーを回避する。
     *
     * Core ライブラリは jp.co.createlink.core パッケージにあり、
     * @SpringBootApplication のデフォルトスキャン対象外のため、
     * @MockitoBean で明示的に Bean を提供する。
     */
    @MockitoBean
    private SampleRestClient sampleRestClient;

    /**
     * SampleSoapClient のモック。
     * SampleRestClient と同様の理由でモック化する。
     */
    @MockitoBean
    private SampleSoapClient sampleSoapClient;

    /**
     * 【テスト対象】GET /actuator/health
     * 【テスト内容】Spring Actuator のヘルスチェックエンドポイントが応答すること
     * 【期待結果】
     *   - HTTP 200 OK が返る
     *   - JSON レスポンスの "status" フィールドが "UP" であること
     *
     * 【前提条件】
     *   application.yml に以下の設定がされていること:
     *   - management.endpoints.web.exposure.include: health,info,metrics
     *   - management.endpoint.health.show-details: always
     */
    @Test
    void should_return_200_and_status_UP_when_get_health_endpoint() throws Exception {
        // Act & Assert: /actuator/health が 200 OK かつ {"status":"UP"} を返すことを検証
        mockMvc.perform(get("/actuator/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    /**
     * 【テスト対象】GET /actuator/health/liveness
     * 【テスト内容】K8S liveness probe 用エンドポイントが応答すること
     * 【期待結果】
     *   - HTTP 200 OK が返る
     *   - JSON レスポンスの "status" フィールドが "UP" であること
     *
     * 【前提条件】
     *   application.yml に management.endpoint.health.probes.enabled=true が設定されていること
     *   このプロパティにより /actuator/health/liveness と /actuator/health/readiness が有効になる。
     */
    @Test
    void should_return_200_and_status_UP_when_get_health_liveness_endpoint() throws Exception {
        // Act & Assert: /actuator/health/liveness が 200 OK を返すことを検証
        mockMvc.perform(get("/actuator/health/liveness")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    /**
     * 【テスト対象】GET /actuator/health/readiness
     * 【テスト内容】K8S readiness probe 用エンドポイントが応答すること
     * 【期待結果】
     *   - HTTP 200 OK が返る
     *   - JSON レスポンスの "status" フィールドが "UP" であること
     *
     * 【前提条件】
     *   application.yml に management.endpoint.health.probes.enabled=true が設定されていること
     *   このプロパティにより /actuator/health/liveness と /actuator/health/readiness が有効になる。
     */
    @Test
    void should_return_200_and_status_UP_when_get_health_readiness_endpoint() throws Exception {
        // Act & Assert: /actuator/health/readiness が 200 OK を返すことを検証
        mockMvc.perform(get("/actuator/health/readiness")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    /**
     * 【テスト対象】GET /actuator/info
     * 【テスト内容】アプリ情報エンドポイントが応答すること
     * 【期待結果】
     *   - HTTP 200 OK が返る
     *   - JSON 形式のレスポンスが返る
     *
     * 【備考】
     *   /actuator/info の内容は application.yml の info セクションや
     *   build-info.properties で設定する。
     *   テストでは応答の有無のみ検証（内容はデプロイ環境によって変わるため）
     */
    @Test
    void should_return_200_when_get_info_endpoint() throws Exception {
        // Act & Assert: /actuator/info が 200 OK を返すことを検証
        mockMvc.perform(get("/actuator/info")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    /**
     * 【テスト対象】GET /actuator/metrics
     * 【テスト内容】メトリクスエンドポイントが応答すること
     * 【期待結果】
     *   - HTTP 200 OK が返る
     *   - JSON レスポンスに "names" フィールドが存在すること（メトリクス名の一覧）
     *
     * 【備考】
     *   /actuator/metrics は Micrometer が収集した全メトリクス名を返す。
     *   個別メトリクスは /actuator/metrics/{metricName} でアクセスできる。
     */
    @Test
    void should_return_200_and_metric_names_when_get_metrics_endpoint() throws Exception {
        // Act & Assert: /actuator/metrics が 200 OK かつ names フィールドを含むことを検証
        mockMvc.perform(get("/actuator/metrics")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.names").isArray());
    }
}
