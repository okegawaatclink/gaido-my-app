# ディレクトリ構成

## マルチリポジトリ構成

GAiDoでは `output_system/` 配下にすべてのリポジトリを配置する。

```
output_system/
├── core/                              # Core ライブラリリポジトリ
│   ├── build.gradle.kts               # Gradle設定（JAR生成）
│   ├── settings.gradle.kts            # プロジェクト設定
│   ├── gradle/                        # Gradle Wrapper
│   │   └── wrapper/
│   ├── gradlew
│   ├── gradlew.bat
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/jp/co/createlink/core/
│   │   │   │   ├── soap/             # SOAP接続クライアント
│   │   │   │   │   └── SampleSoapClient.java
│   │   │   │   ├── rest/             # REST接続クライアント
│   │   │   │   │   └── SampleRestClient.java
│   │   │   │   ├── config/           # 共通設定
│   │   │   │   │   └── CoreConfig.java
│   │   │   │   └── exception/        # 共通例外
│   │   │   │       └── CoreException.java
│   │   │   └── resources/
│   │   │       └── application-core.yml
│   │   └── test/
│   │       └── java/jp/co/createlink/core/
│   │           └── soap/
│   │               └── SampleSoapClientTest.java
│   ├── specs/                         # 接続先SPEC配置ディレクトリ
│   │   ├── wsdl/                      # WSDLファイル
│   │   │   └── sample-service.wsdl    # ダミーWSDL
│   │   └── openapi/                   # OpenAPIファイル
│   │       └── sample-api.yaml        # ダミーOpenAPI
│   ├── .github/
│   │   └── copilot-instructions.md    # Copilotインストラクション
│   ├── README.md
│   └── CONTRIBUTING.md                # 新規接続先追加手順
│
├── process-api-template/              # Process-API テンプレートリポジトリ
│   ├── build.gradle.kts               # Gradle設定（WAR生成）
│   ├── settings.gradle.kts            # composite build設定（Coreを参照）
│   ├── gradle/
│   │   └── wrapper/
│   ├── gradlew
│   ├── gradlew.bat
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/jp/co/createlink/processapi/
│   │   │   │   ├── SampleProcessApiApplication.java
│   │   │   │   ├── ServletInitializer.java        # WAR用初期化
│   │   │   │   ├── controller/
│   │   │   │   │   └── SampleController.java      # Core経由サンプルAPI
│   │   │   │   ├── service/
│   │   │   │   │   └── SampleService.java
│   │   │   │   ├── config/
│   │   │   │   │   └── SwaggerConfig.java         # springdoc設定
│   │   │   │   └── exception/
│   │   │   │       ├── GlobalExceptionHandler.java # @ControllerAdvice
│   │   │   │       └── ErrorResponse.java
│   │   │   └── resources/
│   │   │       ├── application.yml                 # 共通設定
│   │   │       ├── application-local.yml           # ローカル環境
│   │   │       ├── application-dev.yml             # 開発環境
│   │   │       ├── application-prod.yml            # 本番環境
│   │   │       └── logback-spring.xml              # ログ設定
│   │   └── test/
│   │       └── java/jp/co/createlink/processapi/
│   │           └── controller/
│   │               └── SampleControllerTest.java   # MockMvcテスト
│   ├── specs/                         # Process-APIのSPEC配置ディレクトリ
│   │   └── openapi/
│   │       └── process-api.yaml       # このドメインのOpenAPI定義
│   ├── .github/
│   │   └── copilot-instructions.md    # Copilotインストラクション
│   ├── README.md
│   └── CONTRIBUTING.md                # 新規ドメイン追加手順
│
└── docker-compose.yml                 # ローカル開発用（任意）
```

## 重要なファイルの説明

| ファイル | 説明 |
|---------|------|
| `core/build.gradle.kts` | Java library plugin、JAR生成、JAX-WS wsimport設定 |
| `core/specs/` | 接続先のWSDL/OpenAPIを配置。Copilotが参照する |
| `process-api-template/build.gradle.kts` | War plugin、springdoc-openapi、Actuator依存 |
| `process-api-template/settings.gradle.kts` | `includeBuild("../core")` でcomposite build |
| `.github/copilot-instructions.md` | VibeCoding時のCopilotインストラクション |
| `CONTRIBUTING.md` | 新規ドメイン・接続先の追加手順書 |
