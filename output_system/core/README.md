# Core ライブラリ

社内システム連携のための SOAP/REST クライアントを提供するコアライブラリ。
Process-API テンプレートから Gradle Composite Build で参照して使用する。

## 概要

Core ライブラリは、複数の Process-API リポジトリが共通して使用する社内システム連携クライアントを集約します。
新規のSOAP/RESTシステムへの接続を追加する際は、このライブラリにクライアントクラスを実装します。

### 主な機能

| 機能 | 説明 |
|------|------|
| SOAP クライアント | JAX-WS wsimport で生成されたスタブを使った SOAP 接続 |
| REST クライアント | Spring WebClient を使った REST 接続 |
| 共通例外 | `CoreException` / `SoapClientException` による統一エラーハンドリング |
| 接続設定 | `application-core.yml` によるエンドポイント URL・タイムアウト管理 |

## 技術スタック

| 技術 | バージョン | 用途 |
|------|-----------|------|
| Java | 21 (LTS) | 言語 |
| Spring Context | 6.2.x | DI コンテナ・`@Component`/`@Value` |
| Spring WebFlux | 6.2.x | WebClient（REST クライアント） |
| Jakarta XML WS (JAX-WS) | 4.0.x | SOAP クライアント |
| Gradle (Kotlin DSL) | 8.x | ビルドツール |
| JUnit 5 | 5.11.x | ユニットテスト |
| Mockito | 5.x | モックフレームワーク |

> Java 21 では JAX-WS が JDK から削除されているため、Jakarta EE 版（`jakarta.xml.ws-api`）を使用します。

## ディレクトリ構成

```
core/
├── build.gradle.kts               # ビルド設定（JAX-WS wsimport タスク含む）
├── settings.gradle.kts            # プロジェクト名設定
├── gradlew / gradlew.bat          # Gradle Wrapper
├── specs/                         # 接続先 SPEC 配置ディレクトリ（Copilot が参照）
│   ├── wsdl/                      # WSDL ファイル（SOAP 接続先）
│   │   └── sample-service.wsdl    # ダミー WSDL（動作確認・参考用）
│   └── openapi/                   # OpenAPI ファイル（REST 接続先）
│       └── sample-api.yaml        # ダミー OpenAPI（参考用）
├── src/
│   ├── main/
│   │   ├── java/jp/co/createlink/core/
│   │   │   ├── soap/              # SOAP 接続クライアント
│   │   │   │   └── SampleSoapClient.java
│   │   │   ├── rest/              # REST 接続クライアント
│   │   │   │   └── SampleRestClient.java
│   │   │   ├── config/            # 共通設定（WebClient.Builder Bean 等）
│   │   │   │   └── CoreConfig.java
│   │   │   └── exception/         # 共通例外クラス
│   │   │       ├── CoreException.java
│   │   │       └── SoapClientException.java
│   │   └── resources/
│   │       ├── application-core.yml   # 接続先 URL・タイムアウト設定
│   │       └── wsdl/                  # クラスパスリソースとしてパッケージングする WSDL
│   └── test/
│       └── java/jp/co/createlink/core/
│           ├── soap/
│           │   └── SampleSoapClientTest.java
│           ├── rest/
│           │   └── SampleRestClientTest.java
│           └── exception/
│               └── CoreExceptionTest.java
├── .github/
│   └── copilot-instructions.md    # GitHub Copilot インストラクション（VibeCoding 用）
├── README.md                      # このファイル
└── CONTRIBUTING.md                # 新規接続先追加手順
```

### `specs/` ディレクトリについて

`specs/` ディレクトリは接続先システムの仕様書（WSDL/OpenAPI）を格納します。
GitHub Copilot がこのディレクトリのファイルを参照して、クライアントコードを自動生成します。

- `specs/wsdl/*.wsdl` — SOAP 接続先の WSDL ファイル
- `specs/openapi/*.yaml` — REST 接続先の OpenAPI 定義ファイル

## ビルド方法

```bash
# ビルド（コンパイル + テスト + JAR 生成）
./gradlew build

# JAR 生成のみ（テストスキップ）
./gradlew jar -x test

# クリーンビルド
./gradlew clean build
```

ビルド成果物は `build/libs/core-1.0.0-SNAPSHOT.jar` に生成されます。

### SOAP スタブコード生成（新規 SOAP 接続時のみ）

```bash
# WSDL から JAX-WS スタブコードを生成する
./gradlew generateSoapSources
```

生成されたコードは `build/generated-sources/wsimport/` に出力されます。
詳細な手順は [CONTRIBUTING.md](./CONTRIBUTING.md#新規soap接続先の追加手順) を参照してください。

## テスト実行方法

```bash
# 全テスト実行
./gradlew test

# テストレポートを確認する（ブラウザで開く）
open build/reports/tests/test/index.html
```

テストは JUnit 5 + Mockito で実装します。
テストの書き方については [CONTRIBUTING.md](./CONTRIBUTING.md#テストの書き方ガイド) を参照してください。

## Process-API からの参照方法

Process-API の `settings.gradle.kts` に以下を追加することで、Gradle Composite Build で Core を参照できます。

```kotlin
// settings.gradle.kts（Process-API 側）
includeBuild("../core")
```

Process-API の `build.gradle.kts` で依存関係を追加します。

```kotlin
// build.gradle.kts（Process-API 側）
dependencies {
    implementation("jp.co.createlink:core")
}
```

## 設定ファイル

Process-API 側の `application.yml` から以下を追加して Core の設定を読み込みます。

```yaml
# application.yml（Process-API 側）
spring:
  config:
    import: classpath:application-core.yml
```

`application-core.yml` には SOAP エンドポイント URL・REST ベース URL・タイムアウト等を定義します。
環境ごとの切り替えは `application-{profile}.yml` で上書きします。

## 関連リポジトリ

| リポジトリ | 説明 |
|-----------|------|
| `process-api-template` | Process-API テンプレート（Core を Composite Build で参照） |
