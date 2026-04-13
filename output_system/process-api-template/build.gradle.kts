/*
 * build.gradle.kts
 *
 * Process-APIテンプレートのビルド設定。
 * WAR出力（K8Sデプロイ用）とComposite BuildによるCore参照を設定する。
 *
 * ■ プラグイン選定理由
 *   - war: WAR形式での成果物生成に必要
 *   - org.springframework.boot: bootWar/bootRunタスク提供、依存バージョン管理
 *   - io.spring.dependency-management: Spring BOMによる依存バージョン一元管理
 *
 * ■ WAR出力の設定ポイント
 *   - war pluginが有効な場合、bootWarタスクが自動で利用可能になる
 *   - SpringBootServletInitializerを継承するServletInitializerが必要（外部Tomcatデプロイ用）
 *   - 組み込みTomcat（bootRun）はservletコンテナとして使用するため
 *     providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")は不要
 *     （外部Tomcatにデプロイする場合のみprovidedRuntimeに変更する）
 */

plugins {
    // war: WAR成果物を生成するためのプラグイン
    // このプラグインが有効になることでbootWarタスクが使えるようになる
    war

    // Spring Boot Plugin: bootWar/bootRunタスク、依存バージョン管理
    // バージョンはSpring Boot 3.4.x（Java 21対応の最新安定版）を使用
    id("org.springframework.boot") version "3.4.4"

    // Spring Dependency Management: Spring BOMの依存バージョン自動管理
    // 各ライブラリのバージョンをSpring Boot BOMで一元管理する
    id("io.spring.dependency-management") version "1.1.7"
}

// ============================================================
// グループ・バージョン設定
// ============================================================
group = "jp.co.createlink"
version = "1.0.0-SNAPSHOT"

// ============================================================
// Java バージョン設定（Java 21 LTS）
// ============================================================
java {
    // Java 21 LTS を指定（Core側と同一バージョン）
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

// ============================================================
// リポジトリ設定
// ============================================================
repositories {
    // Maven Central から依存関係を取得する
    mavenCentral()
}

// ============================================================
// 依存関係設定
// ============================================================
dependencies {
    // ----------------------------------------------------------------
    // Spring Boot Web Starter
    // Spring MVC（@RestController, @RequestMappingなど）とTomcatを含む。
    // RESTful APIの実装に必要な依存関係を一括で取得する。
    // ----------------------------------------------------------------
    implementation("org.springframework.boot:spring-boot-starter-web")

    // ----------------------------------------------------------------
    // Core ライブラリ（Composite Build経由で参照）
    // settings.gradle.ktsのincludeBuild("../core")によって
    // ソースレベルで参照される。MavenローカルへのPublishは不要。
    //
    // ■ Composite Buildでの参照方法
    //   project(":core") ではなく "group:artifact" 形式で指定する。
    //   Composite Buildではincluded buildのgroup/versionを使って参照する。
    //   core側のbuild.gradle.ktsのgroup="jp.co.createlink", version="1.0.0-SNAPSHOT"
    //   と一致させる必要がある。
    //
    // Coreが提供するもの:
    //   - SampleSoapClient: SOAP接続クライアント雛形
    //   - SampleRestClient: REST接続クライアント雛形
    //   - CoreConfig: WebClient.BuilderのBean設定
    //   - CoreException: 共通例外クラス
    // ----------------------------------------------------------------
    implementation("jp.co.createlink:core")

    // ----------------------------------------------------------------
    // springdoc-openapi（Swagger UI）
    // Spring Boot 3.4.x には springdoc-openapi-starter-webmvc-ui を使用する。
    // springdoc-openapi-ui（旧バージョン）は Spring Boot 3.x 非対応のため使用不可。
    //
    // バージョン 2.8.6:
    //   - Spring Boot 3.4.x / Spring Framework 6.2.x に対応
    //   - /swagger-ui.html で Swagger UI を提供
    //   - /v3/api-docs で OpenAPI 3.0 JSON を提供
    //   - @Operation, @ApiResponse 等のアノテーションをコントローラーに付与して
    //     API ドキュメントを充実させることができる
    //
    // サプライチェーン対策確認済み:
    //   2.8.6 は 2025-03-23 公開（385日以上経過）。7日間クールダウン条件を満たす。
    // ----------------------------------------------------------------
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6")

    // ----------------------------------------------------------------
    // Spring Boot Actuator
    // ヘルスチェック・メトリクス・情報エンドポイントを提供する。
    // K8SのlivenessProbe/readinessProbeに対応するために必要。
    //
    // 提供するエンドポイント（application.ymlで公開設定が必要）:
    //   - /actuator/health           : アプリ全体のヘルス状態
    //   - /actuator/health/liveness  : K8S Liveness probe（アプリが生きているか）
    //   - /actuator/health/readiness : K8S Readiness probe（トラフィック受付可能か）
    //   - /actuator/info             : アプリ情報（バージョン等）
    //   - /actuator/metrics          : Micrometer メトリクス
    //
    // バージョン管理: Spring Boot BOMで自動管理されるため、バージョン指定不要
    // ----------------------------------------------------------------
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // ----------------------------------------------------------------
    // テスト依存関係
    // spring-boot-starter-test にはJUnit 5、Mockito、MockMvcが含まれる
    // ----------------------------------------------------------------
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

// ============================================================
// テスト設定
// ============================================================
tasks.withType<Test> {
    // JUnit Platform (JUnit 5) を使用してテストを実行する
    useJUnitPlatform()

    // Java 21環境でMockitoが動的エージェントをロードする際の警告を抑制する
    jvmArgs("-XX:+EnableDynamicAgentLoading")
}
