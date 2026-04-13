# 新規ドメイン API の追加手順

このドキュメントでは、Process-API テンプレートをベースに新規ドメインの API を追加する手順を説明する。

---

## 全体フロー

```
Step 1: テンプレートをコピーして新規リポジトリ作成
    ↓
Step 2: パッケージ名・プロジェクト名をドメインに合わせてリネーム
    ↓
Step 3: Core リポジトリへの参照パスを調整
    ↓
Step 4: 接続先社内システムの SPEC を specs/ に配置
    ↓
Step 5: .github/copilot-instructions.md にドメイン情報を追記
    ↓
Step 6: VibeCoding で Controller/Service/テストを実装
```

---

## Step 1: テンプレートをコピーして新規リポジトリ作成

`output_system/process-api-template` を新規ドメイン用にコピーする。

```bash
# 例: 受注管理 API を新規作成する場合
cp -r output_system/process-api-template output_system/order-api
cd output_system/order-api
```

または GitHub でリポジトリを作成し、テンプレートの内容をコピーする。

---

## Step 2: パッケージ名をドメインに合わせてリネーム

### パッケージ命名規則

```
jp.co.createlink.{ドメイン名}
```

例:
- 受注管理: `jp.co.createlink.orderapi`
- 在庫管理: `jp.co.createlink.inventoryapi`
- 顧客管理: `jp.co.createlink.customerapi`

### リネーム対象

1. **`settings.gradle.kts`** の `rootProject.name`

   ```kotlin
   // 変更前
   rootProject.name = "process-api-template"

   // 変更後（例: 受注管理）
   rootProject.name = "order-api"
   ```

2. **`build.gradle.kts`** の `group`（変更不要な場合が多い）

3. **Java ソースファイルのパッケージ宣言**

   ```bash
   # パッケージ名を一括置換する例
   find src -name "*.java" -exec sed -i 's/jp.co.createlink.processapi/jp.co.createlink.orderapi/g' {} \;
   ```

4. **ディレクトリ構造**

   ```bash
   # ディレクトリを移動する
   mkdir -p src/main/java/jp/co/createlink/orderapi
   mv src/main/java/jp/co/createlink/processapi/* src/main/java/jp/co/createlink/orderapi/
   rm -rf src/main/java/jp/co/createlink/processapi
   ```

---

## Step 3: Core リポジトリへの参照パスを調整

`settings.gradle.kts` の `includeBuild` パスを、実際の Core リポジトリの配置場所に合わせて変更する。

```kotlin
// settings.gradle.kts

// デフォルト: ../core（同じ親ディレクトリに core リポジトリが存在する前提）
includeBuild("../core")

// Core が別の場所にある場合は絶対パスまたは相対パスを調整する
// includeBuild("/path/to/core")
```

---

## Step 4: 接続先社内システムの SPEC を specs/ に配置

接続先社内システムの WSDL または OpenAPI 定義ファイルを `specs/` に配置する。

```
{new-domain-api}/
├── specs/
│   ├── wsdl/
│   │   └── {system-name}-service.wsdl   # SOAP 接続先の WSDL
│   └── openapi/
│       ├── {domain}-api.yaml            # このドメイン API の OpenAPI 定義
│       └── {system-name}-api.yaml       # REST 接続先の OpenAPI 定義
```

### SOAP 接続先の場合

1. 接続先システムの担当者から WSDL を入手する
2. `specs/wsdl/{system-name}-service.wsdl` に配置する
3. `build.gradle.kts` の `wsimport` タスクの `wsdlDir` パスを更新する
4. `core/src/main/java/.../soap/SampleSoapClient.java` を参考に `{SystemName}SoapClient.java` を作成する

### REST 接続先の場合

1. 接続先システムの OpenAPI 定義を入手する
2. `specs/openapi/{system-name}-api.yaml` に配置する
3. `core/src/main/java/.../rest/SampleRestClient.java` を参考に `{SystemName}RestClient.java` を作成する

---

## Step 5: .github/copilot-instructions.md にドメイン情報を追記

`.github/copilot-instructions.md` にドメイン固有の情報を追記する。

追記すべき内容:
- **接続先システム名**: 社内システムの名称と役割
- **ドメイン固有のパッケージ名**: `jp.co.createlink.{ドメイン名}`
- **SPEC ファイルの参照先**: `specs/` ディレクトリに配置したファイルのパス
- **ドメイン固有の例外**: 追加が必要な例外クラスがあれば記載

---

## Step 6: VibeCoding で API を実装

`.github/copilot-instructions.md` と `specs/` に配置した SPEC を参照しながら、
GitHub Copilot を使って以下のファイルを実装する。

### 実装対象ファイル（Sample* を {Domain}* にリネーム）

| ファイル | 内容 |
|---------|------|
| `controller/{Domain}Controller.java` | REST エンドポイント |
| `service/{Domain}Service.java` | Core 経由データ取得ロジック |
| `dto/{Domain}Response.java` | レスポンス DTO |
| `test/.../{Domain}ControllerTest.java` | MockMvc テスト |
| `test/.../{Domain}ServiceTest.java` | Service ユニットテスト |

### Copilot へのプロンプト例

```
# SampleController.java を参考に、{ドメイン名} ドメインの Controller を作成してください。
# - パッケージ: jp.co.createlink.{ドメイン名}.controller
# - エンドポイント: specs/openapi/{domain}-api.yaml を参照
# - Core クライアント: {SystemName}RestClient（または SoapClient）を使用
# - エラーハンドリング: GlobalExceptionHandler の既存パターンを維持
```

---

## レイヤー構成（Controller → Service → Core）

```
HTTP リクエスト
    ↓
@RestController（Controller 層）
  - リクエストの受け取り・バリデーション
  - Service 呼び出し
  - レスポンス返却
    ↓
@Service（Service 層）
  - ビジネスロジック
  - Core クライアント呼び出し
  - DTO への変換
    ↓
Core ライブラリ（jp.co.createlink.core）
  - SampleRestClient: REST 接続クライアント
  - SampleSoapClient: SOAP 接続クライアント
  - CoreException: 接続エラー共通例外
    ↓
社内システム（SOAP/REST）
```

### 各レイヤーのコーディングルール

| レイヤー | アノテーション | 責務 |
|---------|---------------|------|
| Controller | `@RestController`, `@RequestMapping` | HTTP I/O のみ。ビジネスロジックは Service に委譲 |
| Service | `@Service` | ビジネスロジック・Core 呼び出し・DTO 変換 |
| Core クライアント | - | 社内システムへの接続のみ（HTTP/SOAP）。Controller/Service 知識を持たない |

---

## よくある質問

### Q. テストで Core の Bean が起動エラーになる

`@SpringBootTest` は `jp.co.createlink.processapi` パッケージをスキャンするが、
Core の `jp.co.createlink.core` はスキャン対象外のため Bean 生成ができない。

解決策: `@MockitoBean` でモック化する

```java
@SpringBootTest
class YourTest {
    @MockitoBean
    private YourRestClient yourRestClient; // Core Bean をモック化

    @MockitoBean
    private YourSoapClient yourSoapClient; // Core Bean をモック化
}
```

Controller 単体テストの場合は `@WebMvcTest` + `@MockitoBean` を使う（`SampleControllerTest.java` 参照）。

### Q. `./gradlew build` で wsimport エラーになる

Core 側で `generateSoapSources` タスクを先に実行する必要がある。

```bash
cd output_system/core
./gradlew generateSoapSources

cd output_system/process-api-template
./gradlew build
```

### Q. 新規例外を追加したい

`GlobalExceptionHandler.java` に `@ExceptionHandler` メソッドを追加し、
`buildErrorResponse()` を使って統一フォーマットで返す。

```java
@ExceptionHandler(YourCustomException.class)
public ResponseEntity<ErrorResponse> handleYourCustomException(
        YourCustomException ex, HttpServletRequest request) {
    return buildErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
}
```
