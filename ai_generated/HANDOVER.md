# HANDOVER

## 技術スタック
- 言語/フレームワーク: Java 21, Spring Framework 6.2.2
- ビルドツール: Gradle 8.12.1 (Kotlin DSL)
- SOAP: JAX-WS 4.0 (Jakarta EE 版: jakarta.xml.ws-api:4.0.2, jaxws-rt:4.0.3)
- REST: Spring WebFlux (spring-webflux:6.2.2) + Reactor Netty (reactor-netty-http:1.2.2)
- wsimport: com.sun.xml.ws:jaxws-tools:4.0.3（Java 21 では JDK 同梱なし）
- テスト: JUnit 5 (junit-jupiter:5.11.4) + Mockito (mockito-core:5.15.2)

## ディレクトリ構成

```
output_system/
├── core/
│   ├── build.gradle.kts               # java-library, Java 21, JAX-WS+REST依存, wsimportタスク
│   ├── settings.gradle.kts            # rootProject.name = "core"
│   ├── gradlew, gradlew.bat           # Gradle 8.12.1 Wrapper
│   ├── gradle/wrapper/
│   ├── specs/
│   │   ├── wsdl/sample-service.wsdl   # ダミーWSDL（getSampleDataオペレーション）
│   │   └── openapi/sample-api.yaml    # ダミーOpenAPI 3.0定義（GET/POST /samples）
│   └── src/
│       ├── main/
│       │   ├── java/jp/co/createlink/core/
│       │   │   ├── config/CoreConfig.java         # WebClient.Builder Bean（タイムアウト・SSL）
│       │   │   ├── exception/
│       │   │   │   ├── CoreException.java         # 共通例外（errorCode付き）
│       │   │   │   └── SoapClientException.java   # SOAP専用例外（既存・後方互換）
│       │   │   ├── rest/SampleRestClient.java     # REST接続クライアント雛形
│       │   │   └── soap/SampleSoapClient.java     # SOAP接続クライアント雛形
│       │   └── resources/
│       │       ├── application-core.yml           # SOAP/REST設定・SSL設定テンプレート
│       │       └── wsdl/sample-service.wsdl       # クラスパスリソース用WSDL
│       └── test/java/jp/co/createlink/core/
│           ├── exception/CoreExceptionTest.java   # 6件ユニットテスト
│           ├── rest/SampleRestClientTest.java     # 8件ユニットテスト（WebClientモック）
│           └── soap/SampleSoapClientTest.java     # 6件ユニットテスト
└── process-api-template/
    ├── build.gradle.kts               # war plugin, Spring Boot 3.4.4, Core依存（Composite Build）
    ├── settings.gradle.kts            # rootProject.name="process-api-template", includeBuild("../core")
    ├── gradlew, gradlew.bat           # Gradle 8.12.1 Wrapper
    ├── gradle/wrapper/
    ├── Dockerfile.build               # WSL環境でのビルド確認用（test bootWar）
    ├── README.md                      # プロジェクト概要・ビルド・起動・Swagger UIアクセス手順
    ├── CONTRIBUTING.md                # 新規ドメインAPI追加6ステップ手順・レイヤー構成説明
    ├── .github/
    │   └── copilot-instructions.md   # VibeCoding向けCopilot指示（技術スタック・テストパターン）
    ├── specs/
    │   └── openapi/
    │       └── process-api.yaml      # Copilot参照用OpenAPI 3.0.3定義
    └── src/
        ├── main/
        │   ├── java/jp/co/createlink/processapi/
        │   │   ├── SampleProcessApiApplication.java  # @SpringBootApplicationエントリーポイント
        │   │   ├── ServletInitializer.java            # WAR用（SpringBootServletInitializer継承）
        │   │   ├── config/SwaggerConfig.java          # springdoc-openapi OpenAPI Bean定義
        │   │   ├── controller/SampleController.java   # GET /api/v1/sample, /api/v1/sample/{id}
        │   │   ├── dto/SampleResponse.java            # レスポンスDTO
        │   │   ├── exception/ErrorResponse.java       # 統一エラーレスポンスDTO
        │   │   ├── exception/GlobalExceptionHandler.java # @RestControllerAdvice（CoreException/400/404/500）
        │   │   └── service/SampleService.java         # Core経由データ取得パターン（スタブ付き）
        │   └── resources/
        │       ├── application.yml                    # 共通設定（ポート8080 + springdoc + Actuator設定）
        │       ├── application-local.yml              # ローカル開発用（DEBUG ログレベル）
        │       ├── application-dev.yml                # 開発環境用（INFO ログレベル）
        │       ├── application-prod.yml               # 本番環境用（WARN ログレベル、ヘルス詳細非表示）
        │       └── logback-spring.xml                 # プロファイル別ログフォーマット（local:カラー/dev:テキスト/prod:JSON）
        └── test/java/jp/co/createlink/processapi/
            ├── actuator/ActuatorEndpointTest.java     # Actuator統合テスト（@SpringBootTest + MockMvc）
            ├── controller/SampleControllerTest.java   # MockMvcテストパターン（200/400/404/500）
            ├── exception/GlobalExceptionHandlerTest.java # エラーハンドリングテスト
            └── service/SampleServiceTest.java         # サービスユニットテスト
```

## ビルド・起動方法

```bash
# Core ライブラリビルド（WSL環境・ホストにJavaなし）
# WSL環境ではDockerボリュームマウントが使えないため Dockerfile.build を使用する
cd output_system/core
docker build -f Dockerfile.build .
# ※ Dockerfile.build は .gitignore 対象（ビルド確認専用）

# Coreビルドコマンド（通常）
# 事前に generateSoapSources が必要（SOAP生成コードが必要なため）
./gradlew generateSoapSources build

# Process-API テンプレートビルド（WSL環境・ホストにJavaなし）
# Composite BuildでCoreも含めるため、ビルドコンテキストはoutput_system/
cd output_system
docker build -f process-api-template/Dockerfile.build .

# Process-API WARビルド（通常・ホストにJavaあり）
# 事前にCore側でgenerateSoapSourcesが必要
cd output_system/core && ./gradlew generateSoapSources
cd output_system/process-api-template && ./gradlew bootWar

# Process-API ローカル起動
cd output_system/process-api-template
./gradlew bootRun --args='--spring.profiles.active=local'
# → http://localhost:8080 でアクセス可能
```

## 設計判断

- **wsimport ツール**: `jaxws-rt` ではなく `jaxws-tools` を使用。WsImport クラスは jaxws-tools に含まれており、jaxws-rt には含まれない
- **wsimport は通常ビルドに含めない**: `generateSoapSources` タスクは手動実行。WSDL変更時のみ実行
- **WebClient の Bean は CoreConfig で一元管理**: タイムアウト・SSL設定を DI で一元管理し、各 REST クライアントは `WebClient.Builder` を受け取って baseUrl だけ設定するパターンを採用
- **CoreException に errorCode を持たせる**: Process-API の GlobalExceptionHandler でレスポンスを制御できるよう errorCode フィールドを追加。エラーコード不要な場合は null
- **SoapClientException は削除せず残す**: 後方互換・SOAP専用の意味を明確にするため CoreException との共存を採用
- **SSL設定はオプション**: `core.ssl.trust-store-path` 未設定時は JVM デフォルト TrustStore を使用。企業プロキシ環境では設定可能
- **テストコンストラクタ**: SOAP は `package-private` のテスト用コンストラクタ、REST は WebClient.Builder をモック化するパターンを採用

## はまりポイント

- **WSL環境でDockerボリュームマウントが効かない**: ホストのファイルがコンテナ内から見えない問題。Dockerfile.build にソースをCOPYしてビルドする方法（`docker build` コンテキスト指定）で解決
- **gradle-wrapper.jar の取得**: ホストにGradleがないため、Dockerコンテナ内で生成してdocker cpで取り出した
- **jaxws-tools の mainClass**: `com.sun.tools.ws.WsImport`（jaxws-rt では ClassNotFoundException になる）
- **CoreConfig の SSLException**: `SslContextBuilder.build()` は `javax.net.ssl.SSLException`（チェック例外）をスローするため、ラムダ内で catch して `IllegalStateException` に変換する必要がある
- **SampleSoapClient のビルドは generateSoapSources が前提**: wsimport 生成コードを import しているため、`./gradlew build` 単体では失敗する。`./gradlew generateSoapSources build` で実行すること
- **Composite Buildの依存参照**: `project(":core")` はComposite Buildでは使えない。`"jp.co.createlink:core"` のような `group:artifact` 形式で指定する必要がある
- **Composite BuildでCoreをビルドする際もgenerateSoapSourcesが必要**: process-api-templateのbootWarがComposite Build経由でCoreをビルドするとき、SampleSoapClientがwsimport生成コードを参照するためエラーになる。事前にcore側でgenerateSoapSourcesを実行しておく必要がある（Dockerfile.buildの手順参照）
- **@SpringBootTest でCore BeanのNoSuchBeanDefinitionException**: @SpringBootApplication のデフォルトスキャンは `jp.co.createlink.processapi` のみ。Core の `jp.co.createlink.core` パッケージのBean（SampleRestClient等）はスキャン対象外のため、@SpringBootTest で完全コンテキスト起動すると Bean が見つからずエラーになる。@MockitoBean でモック化することで回避できる

## 実装済み機能

- PBI #3: SOAP接続クライアント雛形（JAX-WS wsimport + SampleSoapClient + テスト）
- PBI #4: REST接続クライアント雛形と共通設定・例外（WebClient + CoreConfig + CoreException + テスト）
- PBI #5: Coreライブラリのドキュメント整備（README.md, CONTRIBUTING.md, .github/copilot-instructions.md）
- PBI #6: Process-APIテンプレートのGradleプロジェクト基盤（bootWar/bootRun動作確認済み）
- PBI #7: サンプルAPIエンドポイント・Swagger UI・統一エラーハンドリング（springdoc-openapi 2.8.6 + @ControllerAdvice + ErrorResponse）
- PBI #8: Spring Actuatorヘルスチェック・プロファイル別設定（/actuator/health,liveness,readiness,info,metrics + application-{local,dev,prod}.yml + logback-spring.xml）
- PBI #9: テストテンプレートとドキュメント整備（SampleControllerTest MockMvcパターン + specs/openapi/process-api.yaml + README.md + CONTRIBUTING.md + .github/copilot-instructions.md）
