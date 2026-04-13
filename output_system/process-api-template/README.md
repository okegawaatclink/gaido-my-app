# Process-API Template

社内システム（SOAP/REST 経由）とのデータ連携を担う Process-API の開発テンプレート。
Spring Boot 3.4.x + WAR デプロイ（K8S 向け）を前提とした雛形プロジェクト。

## 概要

```
Core ライブラリ（SOAP/REST クライアント）
      ↑ Composite Build
Process-API（このテンプレート）
   Controller → Service → Core クライアント → 社内システム
```

### このテンプレートが提供するもの

| 提供物 | 内容 |
|--------|------|
| `SampleController` | GET /api/v1/sample, GET /api/v1/sample/{id} のエンドポイント例 |
| `SampleService` | Core 経由のデータ取得パターン（スタブ付き） |
| `GlobalExceptionHandler` | 統一エラーレスポンス（400/404/500） |
| `SampleControllerTest` | MockMvc を使ったテストパターン例 |
| `specs/openapi/process-api.yaml` | Copilot 参照用 OpenAPI SPEC |
| `.github/copilot-instructions.md` | VibeCoding 向けコード生成指示 |

---

## 技術スタック

| 種別 | 技術 | バージョン |
|------|------|-----------|
| 言語 | Java | 21 (LTS) |
| フレームワーク | Spring Boot | 3.4.4 |
| ビルドツール | Gradle (Kotlin DSL) | 8.12.1 |
| 成果物形式 | WAR | - |
| API ドキュメント | springdoc-openapi | 2.8.6 |
| ヘルスチェック | Spring Boot Actuator | (BOM 管理) |
| テスト | JUnit 5 + Mockito + MockMvc | (BOM 管理) |
| Core 参照 | Gradle Composite Build | - |

---

## ディレクトリ構成

```
process-api-template/
├── build.gradle.kts                    # WAR ビルド設定・依存関係
├── settings.gradle.kts                 # Composite Build 設定（../core 参照）
├── gradlew / gradlew.bat               # Gradle Wrapper
├── specs/
│   └── openapi/
│       └── process-api.yaml           # Copilot 参照用 OpenAPI SPEC
├── .github/
│   └── copilot-instructions.md        # VibeCoding 向け Copilot 指示
└── src/
    ├── main/
    │   ├── java/jp/co/createlink/processapi/
    │   │   ├── SampleProcessApiApplication.java  # エントリーポイント
    │   │   ├── ServletInitializer.java            # WAR 用（外部 Tomcat 対応）
    │   │   ├── config/SwaggerConfig.java          # OpenAPI Bean 定義
    │   │   ├── controller/SampleController.java   # REST エンドポイント
    │   │   ├── dto/SampleResponse.java            # レスポンス DTO
    │   │   ├── exception/ErrorResponse.java       # 統一エラーレスポンス DTO
    │   │   ├── exception/GlobalExceptionHandler.java # エラーハンドリング
    │   │   └── service/SampleService.java         # Core 経由データ取得パターン
    │   └── resources/
    │       ├── application.yml                    # 共通設定
    │       ├── application-local.yml              # ローカル開発用
    │       ├── application-dev.yml                # 開発環境用
    │       ├── application-prod.yml               # 本番環境用
    │       └── logback-spring.xml                 # プロファイル別ログ設定
    └── test/
        └── java/jp/co/createlink/processapi/
            ├── actuator/ActuatorEndpointTest.java     # Actuator 統合テスト
            ├── controller/SampleControllerTest.java   # MockMvc テストパターン
            ├── exception/GlobalExceptionHandlerTest.java
            └── service/SampleServiceTest.java
```

---

## ビルド方法

### 前提条件

- Java 21 がインストール済みであること（または Docker 環境）
- `output_system/core` と `output_system/process-api-template` が同じ親ディレクトリに存在すること（Composite Build の要件）

### WAR ビルド

```bash
# 1. Core の SOAP ソース生成（Composite Build の前提）
cd output_system/core
./gradlew generateSoapSources

# 2. Process-API の WAR ビルド（テスト込み）
cd output_system/process-api-template
./gradlew test bootWar
```

生成物: `build/libs/process-api-template-1.0.0-SNAPSHOT.war`

### Docker でビルドする場合（Java 未インストール環境）

```bash
# ビルドコンテキストは output_system/ を指定すること（Core も含む）
cd output_system
docker build -f process-api-template/Dockerfile.build .
```

---

## ローカル起動方法

```bash
cd output_system/process-api-template
./gradlew bootRun --args='--spring.profiles.active=local'
```

起動後、以下の URL でアクセスできる。

| URL | 説明 |
|-----|------|
| http://localhost:8080/api/v1/sample | サンプルデータ一覧取得 |
| http://localhost:8080/api/v1/sample/{id} | サンプルデータ取得 |
| http://localhost:8080/swagger-ui.html | **Swagger UI** |
| http://localhost:8080/v3/api-docs | OpenAPI 3.0 JSON |
| http://localhost:8080/v3/api-docs.yaml | OpenAPI 3.0 YAML |

---

## Swagger UI アクセス方法

1. アプリ起動後、ブラウザで http://localhost:8080/swagger-ui.html を開く
2. `Sample` タグを展開して各エンドポイントの詳細を確認できる
3. 「Try it out」ボタンでブラウザからリクエストを送信できる

---

## Actuator エンドポイント一覧

| エンドポイント | 説明 |
|---------------|------|
| `/actuator/health` | アプリ全体のヘルス状態 |
| `/actuator/health/liveness` | K8S Liveness Probe 用 |
| `/actuator/health/readiness` | K8S Readiness Probe 用 |
| `/actuator/info` | アプリケーション情報 |
| `/actuator/metrics` | Micrometer メトリクス |

---

## テスト実行方法

```bash
cd output_system/process-api-template

# テスト実行
./gradlew test

# テストレポート確認（HTML）
open build/reports/tests/test/index.html
```

### テストクラス一覧

| クラス | 内容 |
|--------|------|
| `SampleControllerTest` | MockMvc による Controller 層テスト（200/400/404/500） |
| `GlobalExceptionHandlerTest` | 統一エラーレスポンスのテスト |
| `SampleServiceTest` | Service 層ユニットテスト（Mockito） |
| `ActuatorEndpointTest` | Actuator エンドポイントの統合テスト |

---

## 新規ドメイン API の追加方法

詳細は [CONTRIBUTING.md](CONTRIBUTING.md) を参照。
