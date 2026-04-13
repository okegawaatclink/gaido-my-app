# GitHub Copilot Instructions for Process-API Template

このファイルは GitHub Copilot がコードを生成する際に参照する指示書。
VibeCoding での新規ドメイン API 実装時の品質向上・一貫性確保を目的とする。

---

## 技術スタック

| 種別 | 技術 | バージョン |
|------|------|-----------|
| 言語 | Java | 21 (LTS) |
| フレームワーク | Spring Boot | 3.4.4 |
| ビルドツール | Gradle (Kotlin DSL) | 8.12.1 |
| 成果物形式 | WAR（K8S デプロイ用） |  |
| API ドキュメント | springdoc-openapi-starter-webmvc-ui | 2.8.6 |
| ヘルスチェック | Spring Boot Actuator |  |
| テスト | JUnit 5 + Mockito + MockMvc |  |
| Core ライブラリ参照 | Gradle Composite Build |  |

---

## パッケージ命名規則

```
jp.co.createlink.processapi.{レイヤー}
```

| レイヤー | パッケージ | 例 |
|---------|----------|-----|
| Controller | `jp.co.createlink.processapi.controller` | `SampleController` |
| Service | `jp.co.createlink.processapi.service` | `SampleService` |
| DTO | `jp.co.createlink.processapi.dto` | `SampleResponse` |
| 例外 | `jp.co.createlink.processapi.exception` | `ErrorResponse`, `GlobalExceptionHandler` |
| 設定 | `jp.co.createlink.processapi.config` | `SwaggerConfig` |

> 新規ドメイン API では `processapi` の部分をドメイン名に変更する（例: `orderapi`, `inventoryapi`）

---

## レイヤー分離ルール

### Controller 層（`@RestController`）

```java
@RestController
@RequestMapping("/api/v1/{resource}")
@Tag(name = "{Resource}", description = "...")
public class {Resource}Controller {

    private final {Resource}Service {resource}Service;

    // コンストラクタインジェクション（@Autowired 不要）
    public {Resource}Controller({Resource}Service {resource}Service) {
        this.{resource}Service = {resource}Service;
    }

    @GetMapping
    @Operation(summary = "...", description = "...")
    @ApiResponses(value = { ... })
    public ResponseEntity<List<{Resource}Response>> get{Resource}List() {
        // ログ出力 → Service 呼び出し → ResponseEntity.ok() で返す
        // ビジネスロジックは Service に委譲すること
    }
}
```

**Controller の責務**:
- HTTP リクエストの受け取り（`@PathVariable`, `@RequestParam`, `@RequestBody`）
- Service の呼び出し
- ResponseEntity で HTTP レスポンスを返す
- ログ出力（INFO レベル: リクエスト受信・正常完了）

**Controller でやってはいけないこと**:
- ビジネスロジックの実装
- Core クライアントの直接呼び出し
- データ変換・計算処理

---

### Service 層（`@Service`）

```java
@Service
public class {Resource}Service {

    private static final Logger logger = LoggerFactory.getLogger({Resource}Service.class);

    // Core クライアントをコンストラクタインジェクションで受け取る
    private final {System}RestClient {system}RestClient;
    private final {System}SoapClient {system}SoapClient;

    public {Resource}Service({System}RestClient {system}RestClient,
                              {System}SoapClient {system}SoapClient) {
        this.{system}RestClient = {system}RestClient;
        this.{system}SoapClient = {system}SoapClient;
    }

    public List<{Resource}Response> get{Resource}List() {
        // Core クライアント経由でデータ取得
        // 接続先未確定の場合は buildStub{Resource}List() でスタブデータを返す
    }
}
```

**Service の責務**:
- ビジネスロジック・データ変換
- Core クライアントの呼び出し（DI で受け取った Bean を使用）
- 接続先未確定時のスタブデータ返却（`buildStub*` メソッド）

---

### Core ライブラリ（`jp.co.createlink.core`）

Core ライブラリは `settings.gradle.kts` の `includeBuild("../core")` で参照する。
`build.gradle.kts` での依存宣言: `implementation("jp.co.createlink:core")`

**Core が提供するクラス**:

| クラス | 用途 |
|--------|------|
| `jp.co.createlink.core.rest.SampleRestClient` | REST 接続クライアント雛形 |
| `jp.co.createlink.core.soap.SampleSoapClient` | SOAP 接続クライアント雛形 |
| `jp.co.createlink.core.config.CoreConfig` | `WebClient.Builder` Bean（タイムアウト・SSL 設定済み） |
| `jp.co.createlink.core.exception.CoreException` | Core 接続エラー共通例外 |

**DI パターン**（Service での使用例）:

```java
// Core クライアントはコンストラクタインジェクションで受け取る
@Service
public class SampleService {
    private final SampleRestClient sampleRestClient;
    private final SampleSoapClient sampleSoapClient;

    public SampleService(SampleRestClient sampleRestClient,
                         SampleSoapClient sampleSoapClient) {
        this.sampleRestClient = sampleRestClient;
        this.sampleSoapClient = sampleSoapClient;
    }
}
```

---

## `specs/` ディレクトリの参照方法

`specs/` ディレクトリに配置されたファイルを API 実装の参考にすること。

```
specs/
├── openapi/
│   └── process-api.yaml   # このプロジェクトの OpenAPI SPEC
└── wsdl/                  # SOAP 接続先の WSDL（Core 側で wsimport に使用）
```

### OpenAPI SPEC の読み方（`specs/openapi/process-api.yaml`）

- `paths:` セクション: 実装すべきエンドポイントの一覧
- `components/schemas:` セクション: リクエスト/レスポンスの DTO 定義
- `responses:` セクション: 各 HTTP ステータスコードに対するレスポンス形式

---

## エラーハンドリングパターン

全例外は `GlobalExceptionHandler`（`@RestControllerAdvice`）で統一 `ErrorResponse` に変換する。

### 例外と HTTP ステータスコードの対応

| 例外クラス | HTTP ステータス | 用途 |
|-----------|---------------|------|
| `CoreException` | 500 Internal Server Error | Core 接続失敗・社内システムエラー |
| `IllegalArgumentException` | 400 Bad Request | リクエストパラメータ不正 |
| `NoSuchElementException` | 404 Not Found | 指定 ID のデータが存在しない |
| `Exception` (catch-all) | 500 Internal Server Error | 予期しないエラー |

### 新規例外ハンドラーの追加方法

`GlobalExceptionHandler.java` に `@ExceptionHandler` メソッドを追加する:

```java
@ExceptionHandler(YourCustomException.class)
public ResponseEntity<ErrorResponse> handleYourCustomException(
        YourCustomException ex, HttpServletRequest request) {
    logger.error("カスタムエラーが発生しました: {}", ex.getMessage(), ex);
    return buildErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
}
```

---

## テストの書き方（MockMvc パターン）

### Controller 単体テスト（`@WebMvcTest`）

```java
@WebMvcTest(controllers = {YourController.class, GlobalExceptionHandler.class})
class YourControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private YourService yourService; // Service をモック化

    @Test
    void should_return_200_when_get_list() throws Exception {
        // Arrange: Service のモック設定
        when(yourService.getList()).thenReturn(List.of(/* モックデータ */));

        // Act & Assert
        mockMvc.perform(get("/api/v1/{resource}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("expected-id"));
    }

    @Test
    void should_return_404_when_not_found() throws Exception {
        // Arrange: NoSuchElementException を throw するモック設定
        when(yourService.getById(anyString()))
                .thenThrow(new NoSuchElementException("データが見つかりません: id=notfound"));

        // Act & Assert: 404 と統一エラーフォーマットを検証
        mockMvc.perform(get("/api/v1/{resource}/notfound")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
```

### Service 単体テスト（`@ExtendWith(MockitoExtension.class)`）

```java
@ExtendWith(MockitoExtension.class)
class YourServiceTest {

    @Mock
    private YourRestClient yourRestClient; // Core クライアントをモック化

    @InjectMocks
    private YourService yourService;

    @Test
    void should_return_data_from_core() {
        // Arrange: Core クライアントのモック設定
        // Act: Service メソッドの呼び出し
        // Assert: 戻り値の検証
    }
}
```

### 統合テスト（`@SpringBootTest`）

`@SpringBootTest` 使用時は Core の Bean（`SampleRestClient`, `SampleSoapClient`）を
`@MockitoBean` でモック化すること（Core は `jp.co.createlink.core` パッケージのため
Spring Boot のデフォルトスキャン対象外）。

```java
@SpringBootTest
@AutoConfigureMockMvc
class YourIntegrationTest {

    @MockitoBean
    private SampleRestClient sampleRestClient; // 必須: Core Bean のモック化

    @MockitoBean
    private SampleSoapClient sampleSoapClient; // 必須: Core Bean のモック化
}
```

---

## ログ出力規則

```java
private static final Logger logger = LoggerFactory.getLogger(YourClass.class);

// INFO: リクエスト受信・正常完了
logger.info("GET /api/v1/{} が呼び出されました", id);
logger.info("GET /api/v1/{} が正常完了しました", id);

// WARN: 業務エラー（404 等）
logger.warn("リソースが見つかりません: id={}", id);

// ERROR: システムエラー（500 等）
logger.error("Core 接続に失敗しました: {}", ex.getMessage(), ex);
```

---

## Swagger UI アノテーション

```java
@Tag(name = "{Resource}", description = "...")  // クラスに付与
@Operation(summary = "...", description = "...") // メソッドに付与
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "正常レスポンス",
                 content = @Content(schema = @Schema(implementation = YourResponse.class))),
    @ApiResponse(responseCode = "404", description = "データが見つからない",
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
@Parameter(description = "リソース ID", required = true) // パスパラメータに付与
```
