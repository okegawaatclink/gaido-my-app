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
└── core/
    ├── build.gradle.kts               # java-library, Java 21, JAX-WS+REST依存, wsimportタスク
    ├── settings.gradle.kts            # rootProject.name = "core"
    ├── gradlew, gradlew.bat           # Gradle 8.12.1 Wrapper
    ├── gradle/wrapper/
    ├── specs/
    │   ├── wsdl/sample-service.wsdl   # ダミーWSDL（getSampleDataオペレーション）
    │   └── openapi/sample-api.yaml    # ダミーOpenAPI 3.0定義（GET/POST /samples）
    └── src/
        ├── main/
        │   ├── java/jp/co/createlink/core/
        │   │   ├── config/CoreConfig.java         # WebClient.Builder Bean（タイムアウト・SSL）
        │   │   ├── exception/
        │   │   │   ├── CoreException.java         # 共通例外（errorCode付き）
        │   │   │   └── SoapClientException.java   # SOAP専用例外（既存・後方互換）
        │   │   ├── rest/SampleRestClient.java     # REST接続クライアント雛形
        │   │   └── soap/SampleSoapClient.java     # SOAP接続クライアント雛形
        │   └── resources/
        │       ├── application-core.yml           # SOAP/REST設定・SSL設定テンプレート
        │       └── wsdl/sample-service.wsdl       # クラスパスリソース用WSDL
        └── test/java/jp/co/createlink/core/
            ├── exception/CoreExceptionTest.java   # 6件ユニットテスト
            ├── rest/SampleRestClientTest.java     # 8件ユニットテスト（WebClientモック）
            └── soap/SampleSoapClientTest.java     # 6件ユニットテスト
```

## ビルド・起動方法

```bash
# Core ライブラリビルド（WSL環境・ホストにJavaなし）
# WSL環境ではDockerボリュームマウントが使えないため Dockerfile.build を使用する
cd output_system/core
docker build -f Dockerfile.build .
# ※ Dockerfile.build は .gitignore 対象（ビルド確認専用）

# ビルドコマンド（通常）
# 事前に generateSoapSources が必要（SOAP生成コードが必要なため）
./gradlew generateSoapSources build
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

## 実装済み機能

- PBI #3: SOAP接続クライアント雛形（JAX-WS wsimport + SampleSoapClient + テスト）
- PBI #4: REST接続クライアント雛形と共通設定・例外（WebClient + CoreConfig + CoreException + テスト）
