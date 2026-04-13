# 実装の振り返り（problems.md）

## 概要

| 項目 | 値 |
|------|-----|
| プロジェクト名 | Spring Boot APIテンプレートシステム |
| 記録項目数 | 5件 |
| 記録日 | 2026-04-13 |

## 一覧

| # | 項目名 | 苦労度 | 深刻度 | 原因カテゴリ | 影響範囲 |
|---|--------|--------|--------|-------------|---------|
| 1 | Gradle composite buildの設定と動作確認 | ★★★☆☆ | ★★★☆☆ | テンプレート不足 | ルール改善で予防可能 |
| 2 | マルチプロジェクト構成でのソースコード配置とビルドディレクトリの整理 | ★★★☆☆ | ★★☆☆☆ | ルール不足 | ルール改善で予防可能 |
| 3 | Spring Boot 3.4.xでのspringdoc-openapi設定の最新仕様対応 | ★★☆☆☆ | ★★☆☆☆ | 外部依存 | Output System固有 |
| 4 | テスト設計とE2Eテストの粒度調整 | ★★★☆☆ | ★★☆☆☆ | テンプレート不足 | ルール改善で予防可能 |
| 5 | OpenAPI 3.0仕様の統合と API定義の自動生成 | ★★☆☆☆ | ★★☆☆☆ | ルール不足 | ルール改善で予防可能 |

## 各項目の詳細

### 1. Gradle composite buildの設定と動作確認

- **苦労度**: ★★★☆☆（3/5）
- **深刻度**: ★★★☆☆（3/5）
- **原因カテゴリ**: テンプレート不足
- **影響範囲**: ルール改善で予防可能

#### 何が起きたか

マルチプロジェクト構成でCoreライブラリとProcess-APIテンプレートを分離する際、Gradle composite buildの設定が正しく動作していないことが判明しました。

- `settings.gradle.kts` で `includeBuild()` を使用してCoreを参照する設定を作成
- ローカルビルドではコンポジットビルドが機能するが、CI/CDパイプラインでの動作確認が不十分だった
- Process-API側がCoreのJARをMaven Centralから取得しようとするケースと、ローカルソースを参照するケースが混在

#### 原因

1. **composite buildの動作メカニズムの不明確さ**: メインプロジェクト（settings.gradle.kts）の置かれる階層と、includeBuildの相対パスの関係が複雑
2. **テンプレート不備**: Process-APIテンプレートのREADME.mdにcomposite build使用時の想定シナリオが記載されていない
3. **環境差の未考慮**: 開発環境（ローカル）とCI環境では、ビルド方式が異なる必要があるが、その設定が統一されていない

#### どう解決したか

1. **ルートディレクトリのsettings.gradle.ktsを整備**
   - `includeBuild("./output_system/core")` でCoreライブラリを明示的に参照
   - Gradle version catalogで依存バージョンを統一管理

2. **build.gradle.kts（各プロジェクト）でDependencySubstitutionを有効化**
   - ローカル開発時: CoreのJARの代わりにソースを直接参照
   - CI環境: Maven Centralから公開されたJARを使用

3. **IntegrationTest（CompositeBuildIntegrationTest）を追加**
   - ビルド後に実際にコンポジットビルドが機能しているかを検証
   - Process-APIがCoreの修正を即座に反映していることを確認

#### 改善提案

| 項目 | 内容 |
|------|------|
| 対象ファイル | `.claude/rules/gradle-rules.md`（新規作成） |
| 変更種別 | 新規追加 |

**具体的な変更内容:**

以下の内容で新規ファイル `.claude/rules/gradle-rules.md` を作成する:

```markdown
# Gradle マルチプロジェクト構成ガイド

## composite build（コンポジットビルド）の使い分け

マルチプロジェクト構成で、複数の独立したプロジェクトを1つのビルドユニットとして扱う場合、composite buildを使用する。

### パターン1: ローカル開発（ソース直接参照）

ルートの `settings.gradle.kts`:
\`\`\`kotlin
includeBuild("./subproject-a")
includeBuild("./subproject-b")
\`\`\`

効果:
- subproject-a、subproject-bのソースコードが直接参照される
- 修正時に即座にビルドに反映（JAR生成の待機不要）

### パターン2: CI/CD環境（Maven Central経由）

CI パイプラインでは composite build を使用せず、Maven Centralから公開済みJARを参照する。

`subproject-b/build.gradle.kts`:
\`\`\`kotlin
dependencies {
    implementation("com.example:subproject-a:1.0.0")  // Maven Central経由
}
\`\`\`

### Dependency Substitution（自動切り替え）

両環境を自動で切り替えるには、gradle.properties で環境変数を参照:

\`\`\`properties
# gradle.properties
useLocalBuild=true
\`\`\`

`build.gradle.kts`:
\`\`\`kotlin
if (gradle.parent != null && (properties["useLocalBuild"] as String?)?.toBoolean() != false) {
    // composite build: ローカルソース参照
    implementation(project(":subproject-a"))
} else {
    // 外部JAR: Maven Central経由
    implementation("com.example:subproject-a:1.0.0")
}
\`\`\`

### 検証テスト

以下のテストパターンでcomposite buildが正常に動作していることを確認:

1. **ローカルビルド検証**: `./gradlew build` でコンポジット構成が使用されているか
2. **JAR生成検証**: `./gradlew jar` で全プロジェクトのJARが生成されているか
3. **統合テスト**: 修正したsubprojectが、別のsubprojectで即座に反映されているか

テンプレート例: `CompositeBuildIntegrationTest.java`

## マルチプロジェクト構成のディレクトリ配置

以下を推奨:

\`\`\`
output_system/
├── settings.gradle.kts          # ルートプロジェクト設定
├── build.gradle.kts             # ルートビルド設定
├── core/                         # Coreライブラリ
│   └── build.gradle.kts
└── process-api-template/         # Process-APIテンプレート
    └── build.gradle.kts
\`\`\`

settings.gradle.kts:
\`\`\`kotlin
rootProject.name = "api-template"

includeBuild("./core")

include(":process-api-template")
\`\`\`
```

---

### 2. マルチプロジェクト構成でのソースコード配置とビルドディレクトリの整理

- **苦労度**: ★★★☆☆（3/5）
- **深刻度**: ★★☆☆☆（2/5）
- **原因カテゴリ**: ルール不足
- **影響範囲**: ルール改善で予防可能

#### 何が起きたか

複数のGradleプロジェクト（core と process-api-template）を output_system/ ディレクトリ配下に配置した際、以下の問題が発生:

1. ビルドディレクトリ（`build/`）が各プロジェクトの下に生成され、ディレクトリ構造が複雑化
2. `gradle clean` で全プロジェクトのビルド成果物を一括削除する設定が不明確だった
3. IDEがマルチプロジェクト構成を正しく認識できず、補完やナビゲーションが動作しないケースがあった

#### 原因

1. **ルール不備**: CLAUDE.mdに「マルチプロジェクト構成での推奨ディレクトリレイアウト」が記載されていない
2. **Gradle設定の不統一**: 各プロジェクト個別に build.gradle.kts を作成したため、設定が散在
3. **IDE設定**: IntelliJ IDEA等がマルチプロジェクトを正しく認識するための `.idea/modules.xml` や `*.iml` ファイルが不完全

#### どう解決したか

1. **ルートディレクトリに統一設定を配置**
   - `output_system/settings.gradle.kts`: includeで全プロジェクトを明示
   - `output_system/build.gradle.kts`: 共通設定（Java version、依存バージョン）をplugins/dependencies ブロックで統一

2. **各プロジェクトのbuild.gradle.ktsをシンプルに**
   - ルートから継承する設定を最小化
   - プロジェクト固有の依存のみを記載

3. **ビルド成果物の一括管理**
   - `allprojects { buildDir = ... }` でビルドディレクトリを統一
   - CI スクリプトで `./gradlew clean build` を一括実行

#### 改善提案

| 項目 | 内容 |
|------|------|
| 対象ファイル | `CLAUDE.md` の「ディレクトリ構成」セクション |
| 変更種別 | 既存修正 |

**具体的な変更内容:**

CLAUDE.md の「ディレクトリ構成」セクションに以下の注記を追加:

> ## マルチプロジェクト構成の推奨レイアウト
>
> Gradle マルチプロジェクトを `output_system/` 配下に配置する場合、以下のディレクトリ構成を推奨する:
>
> \`\`\`
> output_system/
> ├── settings.gradle.kts          # ルートプロジェクト設定（include で全サブプロジェクトを明示）
> ├── build.gradle.kts             # 共通ビルド設定（Javaバージョン、プラグイン、dependencyVersions）
> ├── gradle.properties             # Gradle設定（useLocalBuild=true 等）
> ├── .gradle/
> ├── build/                        # ルートプロジェクトのビルド成果物
> │
> ├── core/                         # Coreライブラリ（サブプロジェクト1）
> │   ├── build.gradle.kts          # Core固有の設定のみ
> │   ├── build/                    # Coreのビルド成果物
> │   ├── src/
> │   └── ...
> │
> └── process-api-template/         # Process-APIテンプレート（サブプロジェクト2）
>     ├── build.gradle.kts          # Process-API固有の設定のみ
>     ├── build/                    # Process-APIのビルド成果物
>     ├── src/
>     └── ...
> \`\`\`
>
> ### ルート settings.gradle.kts の例
>
> \`\`\`kotlin
> rootProject.name = "api-template"
>
> includeBuild("./core")
> include(":process-api-template")
> \`\`\`
>
> ### ルート build.gradle.kts の例
>
> \`\`\`kotlin
> plugins {
>     java
>     id("io.spring.dependency-management") version "1.1.4" apply false
> }
>
> ext {
>     set("javaVersion", 21)
>     set("springBootVersion", "3.4.0")
> }
>
> subprojects {
>     apply plugin: "java"
>     apply plugin: "io.spring.dependency-management"
>
>     java {
>         sourceCompatibility = 21
>     }
>
>     dependencyManagement {
>         imports {
>             mavenBom "org.springframework.boot:spring-boot-dependencies:3.4.0"
>         }
>     }
> }
> \`\`\`
>
> ### CI での一括ビルド
>
> \`\`\`bash
> cd output_system
> ./gradlew clean build  # 全プロジェクトを一括ビルド
> \`\`\`

---

### 3. Spring Boot 3.4.xでのspringdoc-openapi設定の最新仕様対応

- **苦労度**: ★★☆☆☆（2/5）
- **深刻度**: ★★☆☆☆（2/5）
- **原因カテゴリ**: 外部依存
- **影響範囲**: Output System固有

#### 何が起きたか

springdoc-openapi を Spring Boot 3.4.x に統合する際、以下の互換性問題が発生:

1. `@RequestBody(required = true)` アノテーションの記法が springdoc 2.0以降で変更
2. Swagger UIのデフォルトパスが `/swagger-ui.html` から `/swagger-ui/index.html` に変更
3. OpenAPI定義の自動生成で、`@Operation`・`@Schema` の属性が部分的に無視される

#### 原因

SpringFox（Swagger 2.0時代の標準ツール）からspringdoc-openapiへの移行で、アノテーション体系が刷新されました。既存のコード例では古い記法を使用していたため、Spring Boot 3.4との組み合わせで互換性が失われました。

#### どう解決したか

1. **springdoc-openapi バージョン確定**
   - `implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.4.0")`

2. **SwaggerConfig.java の最新化**
   ```java
   @Configuration
   public class SwaggerConfig {
       @Bean
       public OpenAPI customOpenAPI() {
           return new OpenAPI()
               .info(new Info()
                   .title("Process-API Template")
                   .version("1.0.0"));
       }
   }
   ```

3. **Swagger UI アクセス先の統一**
   - 本体: `/swagger-ui/index.html` （新規）
   - 互換性: `/swagger-ui.html` にも対応（アクセス可能）

#### 改善提案

| 項目 | 内容 |
|------|------|
| 対象ファイル | `.claude/skills/develop-operations/references/LibrarySelectionGuide.md`（新規作成） |
| 変更種別 | 新規追加 |

**具体的な変更内容:**

新規ファイル `.claude/skills/develop-operations/references/LibrarySelectionGuide.md` を作成し、以下の内容を記載:

```markdown
# ライブラリ選定ガイド

## Spring Boot + Swagger/OpenAPI統合

### springdoc-openapi（推奨）

Spring Boot 3.x以降では **springdoc-openapi** を使用してください。SpringFoxは保守終了です。

#### 依存関係

\`\`\`gradle
dependencies {
    // Spring Boot 3.4.x + Java 21 対応
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.4.0")
}
\`\`\`

#### Swagger UI へのアクセス

- 新規: `/swagger-ui/index.html` （推奨）
- 互換性: `/swagger-ui.html` （旧形式、動作するが非推奨）

#### 設定ファイル（application.yml）

\`\`\`yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    operations-sorter: method
    tags-sorter: alpha
  show-actuator: true
\`\`\`

#### アノテーション記法

SpringFox（SpringFox）ではなく、以下のアノテーションを使用：

\`\`\`java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Sample", description = "Sample API")
public class SampleController {
    
    @GetMapping("/sample/{id}")
    @Operation(summary = "データ取得", description = "IDでデータを取得")
    @ApiResponse(responseCode = "200", description = "成功",
        content = @Content(schema = @Schema(implementation = SampleResponse.class)))
    public ResponseEntity<SampleResponse> getSample(@PathVariable String id) {
        // ...
    }
}
\`\`\`

#### よくある間違い

| 誤り | 正しい形式 |
|-----|---------|
| `@RequestBody(required = true)` | `@RequestBody` のみ（必須） |
| `@ApiOperation(...)` | `@Operation(...)` |
| `@ApiResponse(...)` | `@ApiResponse(...)` （仕様は同じ） |

#### OpenAPI定義の自動生成確認

\`\`\`bash
curl http://localhost:8080/v3/api-docs.yaml
\`\`\`

出力ファイルに全エンドポイントが含まれているか確認してください。
```

---

### 4. テスト設計とE2Eテストの粒度調整

- **苦労度**: ★★★☆☆（3/5）
- **深刻度**: ★★☆☆☆（2/5）
- **原因カテゴリ**: テンプレート不足
- **影響範囲**: ルール改善で予防可能

#### 何が起きたか

テスト設計フェーズで、以下の粒度調整が必要になりました:

1. **単体テスト vs 統合テスト の境界が曖昧**
   - SampleServiceのテストは単体テストか、それともCoreライブラリへの呼び出しを含める統合テストか

2. **テスト対象範囲の過剰性**
   - Spring Actuator（/actuator/health）のテストは必須か、それともデフォルト動作として検証不要か

3. **E2Eテスト vs MockMvcテストの使い分け**
   - コントローラーレイヤーはMockMvcで十分か、それともPlaywrightで実際のHTTPリクエストを検証すべきか

#### 原因

1. **テスト標準化の不完全さ**: test-standards スキルで単体テストの粒度が統一されていない
2. **Javaプロジェクト向けE2Eテスト設計の缺落**: Playwrightはフロントエンド向けツールとして認識されており、API層のE2Eテストへの適用例が限定的

#### どう解決したか

1. **テスト粒度の明確化**
   - **単体テスト**: SampleService, GlobalExceptionHandler など、外部依存なしで検証可能な層
   - **統合テスト**: Process-API が Core を正しく呼び出しているか、composite build が機能しているか
   - **E2Eテスト**: 実際のHTTPリクエスト（curl, Playwright等）でAPI全体を検証

2. **ActuatorEndpointTest を追加**
   - Spring Actuator のデフォルト動作を検証（ヘルスチェック、メトリクス）

3. **プロセスフローテスト（ProcessApiDocumentationTest）**
   - 実装がOpenAPI定義と一致しているかを検証

#### 改善提案

| 項目 | 内容 |
|------|------|
| 対象ファイル | `.claude/skills/test-run-operations/references/JavaAPITestPatterns.md`（新規作成） |
| 変更種別 | 新規追加 |

**具体的な変更内容:**

新規ファイル `.claude/skills/test-run-operations/references/JavaAPITestPatterns.md` を作成し、以下の内容を記載:

```markdown
# Java APIテストパターン集

## テスト粒度の使い分け

### 1. 単体テスト（Unit Test）

**対象**: ビジネスロジック層（Service）、ユーティリティ、例外処理

\`\`\`java
@ExtendWith(MockitoExtension.class)
class SampleServiceTest {
    
    @Mock
    private SampleSoapClient soapClient;
    
    @InjectMocks
    private SampleService service;
    
    @Test
    void testGetSampleData_Success() {
        // Mockのセットアップ
        when(soapClient.getById("123"))
            .thenReturn(new SampleData("123", "Test"));
        
        // テスト実行
        SampleResponse result = service.getSample("123");
        
        // 検証
        assertEquals("123", result.id());
        assertEquals("Test", result.name());
    }
}
\`\`\`

**特徴**:
- 外部依存を Mock で置き換え
- 実行時間: < 100ms
- テスト結果に再現性あり

### 2. コントローラーレイヤーテスト（MockMvc）

**対象**: REST Controller、リクエスト/レスポンス変換、HTTPステータス

\`\`\`java
@SpringBootTest
@AutoConfigureMockMvc
class SampleControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private SampleService sampleService;
    
    @Test
    void testGetSample_Returns200() throws Exception {
        // Service の Mock 応答を設定
        when(sampleService.getSample("123"))
            .thenReturn(new SampleResponse("123", "Test", new Object()));
        
        // HTTPリクエストシミュレーション
        mockMvc.perform(get("/api/v1/sample/123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("123"))
            .andExpect(jsonPath("$.name").value("Test"));
    }
}
\`\`\`

**特徴**:
- 実際のサーブレットコンテキストで実行（ただしネットワーク外）
- HTTPステータス、ヘッダー、JSON構造を検証
- Serviceは Mock で外部通信を遮断

### 3. 統合テスト（Integration Test）

**対象**: 複数モジュール間の連携、Gradle composite build の検証

\`\`\`java
@SpringBootTest
class CompositeBuildIntegrationTest {
    
    @Autowired
    private SampleService service;
    
    @Autowired
    private SampleSoapClient soapClient;
    
    @Test
    void testCoreLibraryIntegration() {
        // Core ライブラリが実際に注入されているか
        assertNotNull(soapClient);
        
        // Service が Core を通じてデータを取得できるか
        SampleResponse result = service.getSample("test-id");
        assertNotNull(result);
    }
}
\`\`\`

**特徴**:
- 複数のモジュール間連携を検証
- Coreライブラリが正しく注入されているか確認
- ローカルデータベース・ダミーAPI との連携テスト可能

### 4. E2Eテスト（API全体検証）

**対象**: 実際のHTTPリクエストによるAPI全体の動作検証

\`\`\`bash
# curl による簡易E2Eテスト
curl -X GET http://localhost:8080/api/v1/sample/test-id \
  -H "Content-Type: application/json" \
  | jq '.id'
\`\`\`

**Playwrightでの API テスト**:

\`\`\`java
@Test
void testAPIWithPlaywright() throws IOException {
    ProcessBuilder pb = new ProcessBuilder(
        "curl", "-s",
        "http://localhost:8080/api/v1/sample/test-id"
    );
    Process process = pb.start();
    BufferedReader reader = new BufferedReader(
        new InputStreamReader(process.getInputStream())
    );
    String response = reader.lines().collect(Collectors.joining());
    
    assertTrue(response.contains("\"id\""));
}
\`\`\`

## Actuator エンドポイント検証

Spring Actuator のエンドポイントは、特別な設定なしでデフォルト検証:

\`\`\`java
@SpringBootTest
@AutoConfigureMockMvc
class ActuatorEndpointTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testHealthCheckEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }
    
    @Test
    void testMetricsEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.names").isArray());
    }
}
\`\`\`

## テスト実行コマンド

\`\`\`bash
# 全テスト実行
./gradlew test

# 特定クラスのテスト
./gradlew test --tests SampleControllerTest

# 統合テストのみ（クラス名パターン）
./gradlew test --tests "*Integration*"

# テストレポート生成
./gradlew test --info
# レポート: build/reports/tests/test/index.html
\`\`\`
```

---

### 5. OpenAPI 3.0仕様の統合と API定義の自動生成

- **苦労度**: ★★☆☆☆（2/5）
- **深刻度**: ★★☆☆☆（2/5）
- **原因カテゴリ**: ルール不足
- **影響範囲**: ルール改善で予防可能

#### 何が起きたか

OpenAPI定義（openapi.yaml）を手動で作成する際、以下の課題が発生しました:

1. **springdoc-openapiの自動生成 vs 手動作成の責任分界が曖昧**
   - `/v3/api-docs.yaml` でspringdoc が自動生成するOpenAPI定義と、リポジトリに保存する `openapi.yaml` の関係が不明確

2. **API定義の更新漏れ**
   - コード修正後にOpenAPI定義を同期させ忘れるケースが発生

3. **OpenAPI定義をVibeCodingで参照する場合のパス**
   - GitHub Copilot が参照すべきOpenAPI定義が、どのファイル・ディレクトリに配置すべきか不明確

#### 原因

1. **OpenAPI定義の運用ルール不備**: CLAUDE.md に「API定義の管理方針」が記載されていない
2. **自動生成vs手動作成の使い分け曖昧**: springdoc の自動生成機能で十分なのか、追加の手動編集が必要なのか不明確

#### どう解決したか

1. **运用方針を決定**
   - 開発環境（ローカル、テスト）: springdoc の自動生成（`/v3/api-docs.yaml`）を参照
   - リポジトリ保存: `openapi.yaml` に手動作成（VibeCodingでの参照用）

2. **README.mdから openapi.yaml へリンク**
   - WebAPI一覧セクションで `openapi.yaml` へのリンクを追加

3. **CI での自動検証（ProcessApiDocumentationTest）**
   - ビルド時に実装とOpenAPI定義の一致性を確認

#### 改善提案

| 項目 | 内容 |
|------|------|
| 対象ファイル | `.claude/rules/constraints.md` |
| 変更種別 | 既存修正（末尾に追記） |

**具体的な変更内容:**

`.claude/rules/constraints.md` の末尾に以下を追記:

> ## OpenAPI定義の管理方針
>
> ### 自動生成 vs 手動作成の使い分け
>
> #### springdoc-openapi（自動生成）
>
> Spring Boot アプリケーション起動時に以下のエンドポイントで自動生成:
>
> - `/v3/api-docs` （JSON形式）
> - `/v3/api-docs.yaml` （YAML形式）
>
> **用途**:
> - 開発中の動作確認
> - テスト環境でのSwagger UI表示
> - **本番環境ではこのエンドポイント自体を無効化すること** （セキュリティ上の理由）
>
> #### 手動作成（リポジトリ保存）
>
> リポジトリルートに `openapi.yaml` を配置し、VibeCodingでの参照用として維持:
>
> **用途**:
> - GitHub Copilot の参照ファイル（.github/copilot-instructions.md で指示）
> - README.md でのAPI一覧公開
> - API変更時のドキュメント
>
> ### 更新タイミング
>
> | 更新対象 | 更新タイミング | 実行者 |
> |---------|------------|--------|
> | `/v3/api-docs.yaml` | アプリケーション起動時（自動） | Spring Boot |
> | `openapi.yaml` | 大幅なAPI変更時（手動） | 開発者 |
>
> **注**: 小さなエンドポイント追加は springdoc 自動生成で十分。大幅な仕様変更時のみ `openapi.yaml` を更新。
>
> ### CI での検証
>
> ビルド時に以下を検証:
>
> \`\`\`bash
> # 1. springdoc が API ドキュメントを生成しているか
> ./gradlew test  # ProcessApiDocumentationTest が実行される
>
> # 2. 生成されたOpenAPI定義が有効なYAMLか
> npx swagger-parser validate build/swagger-ui/swagger.json
> \`\`\`
>
> ### .github/copilot-instructions.md での記載例
>
> \`\`\`markdown
> ## API仕様の確認
>
> 新規エンドポイント追加時は、以下を参照してください:
>
> - **OpenAPI定義**: [openapi.yaml](./openapi.yaml) （本リポジトリで管理）
> - **実装ドキュメント**: [WebAPIエンドポイント一覧](./README.md#WebAPIエンドポイント一覧)
> \`\`\`

---

## 改善優先度

| # | 項目名 | 優先度 | 対応期限 |
|---|--------|--------|---------|
| 1 | Gradle composite build ガイド作成 | **高** | 次プロジェクト開始時 |
| 2 | マルチプロジェクト構成ルール化 | **高** | 次プロジェクト開始時 |
| 3 | springdoc-openapi 統合ガイド | 中 | 3ヶ月以内 |
| 4 | Java API テストパターン集 | 中 | 3ヶ月以内 |
| 5 | OpenAPI定義運用ルール化 | 中 | 3ヶ月以内 |

## サマリ

このプロジェクトを通じて、以下の改善が gaido システム側で可能であることが判明しました:

1. **Gradle マルチプロジェクト構成の標準化**: composite build の使用例・CI での実装パターンを rules/constraints.md に追加
2. **Java/Spring Boot 向けテストテンプレート拡充**: Unit/Integration/E2E テストの粒度を明確化し、test-standards スキルを拡張
3. **OpenAPI定義の運用ガイド**: springdoc-openapi と手動作成の使い分けをドキュメント化

これらの改善により、同様のマルチプロジェクト・API開発案件での生産性向上が期待できます。
