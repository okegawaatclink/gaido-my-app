package jp.co.createlink.processapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * SwaggerConfig
 *
 * <p>springdoc-openapi の OpenAPI 情報を定義する設定クラス。
 * Swagger UI（{@code /swagger-ui.html}）に表示されるメタ情報を設定する。</p>
 *
 * <h2>アクセス URL</h2>
 * <ul>
 *   <li>Swagger UI: {@code /swagger-ui.html}</li>
 *   <li>OpenAPI JSON: {@code /v3/api-docs}</li>
 *   <li>OpenAPI YAML: {@code /v3/api-docs.yaml}</li>
 * </ul>
 *
 * <h2>新規ドメインAPI実装時の変更箇所（VibeCoding用）</h2>
 * <ul>
 *   <li>{@code title}: API名称に合わせて変更する</li>
 *   <li>{@code description}: API概要を記述する</li>
 *   <li>{@code version}: バージョン番号を管理する</li>
 *   <li>{@code servers}: 環境ごとのサーバー URL を追加する（例: 開発環境, 本番環境）</li>
 * </ul>
 *
 * <h2>依存関係</h2>
 * <p>{@code build.gradle.kts} に以下の依存を追加すること（Spring Boot 3.x 用）:</p>
 * <pre>
 * implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6")
 * </pre>
 */
@Configuration
public class SwaggerConfig {

    /**
     * OpenAPI Bean 定義。
     *
     * <p>springdoc-openapi がこの Bean を検出して Swagger UI のメタ情報として表示する。</p>
     *
     * <p>コントローラーの {@code @Operation}, {@code @ApiResponse} 等のアノテーションと
     * 組み合わせることで、対話的に確認できる API ドキュメントを生成できる。</p>
     *
     * @return OpenAPI 情報（タイトル、バージョン、説明、サーバー情報）
     */
    @Bean
    public OpenAPI processApiOpenAPI() {
        return new OpenAPI()
                // ▼ API のメタ情報（タイトル・バージョン・説明・連絡先）
                .info(new Info()
                        // ▼ 新規実装時はタイトルをドメインに合わせて変更すること
                        .title("Process-API Template")
                        // ▼ バージョン管理はプロジェクトのリリースポリシーに合わせて変更すること
                        .version("1.0.0")
                        // ▼ 新規実装時は API の概要を記述すること
                        .description("Process-API テンプレート。Controller → Service → Core のレイヤー構成パターンと、"
                                + "springdoc-openapi による Swagger UI 提供のサンプル実装。"
                                + "VibeCoding でドメイン固有の API を実装する際のスタートポイントとして使用すること。")
                        // ▼ 必要に応じて連絡先情報を設定する（任意）
                        .contact(new Contact()
                                .name("Process-API Team")
                                .email("process-api@createlink.co.jp")
                        )
                )
                // ▼ サーバー情報（複数環境を定義可能）
                // 新規実装時は環境ごとの URL を追加すること（例: 開発環境, 本番環境）
                .servers(List.of(
                        new Server()
                                // ▼ ローカル開発環境（bootRun 時のデフォルト URL）
                                .url("http://localhost:8080")
                                .description("ローカル開発環境")
                ));
    }
}
