# Contributing Guide — Core ライブラリへの新規接続先追加

このドキュメントでは、Core ライブラリに新しい SOAP/REST 接続先クライアントを追加する手順を説明します。

---

## 目次

1. [新規 SOAP 接続先の追加手順](#新規soap接続先の追加手順)
2. [新規 REST 接続先の追加手順](#新規rest接続先の追加手順)
3. [パッケージ命名規則](#パッケージ命名規則)
4. [設定ファイルの更新方法](#設定ファイルの更新方法)
5. [テストの書き方ガイド](#テストの書き方ガイド)

---

## 新規 SOAP 接続先の追加手順

### ステップ 1: WSDL ファイルを配置する

接続先システムから WSDL を取得し、`specs/wsdl/` に配置します。

```bash
# 例: 接続先システムの WSDL を取得する
curl -o specs/wsdl/{system-name}-service.wsdl \
     "https://target-system.example.com/service?wsdl"
```

ファイル名は `{system-name}-service.wsdl` のように接続先システム名を含めます。

### ステップ 2: build.gradle.kts の wsimport 設定を更新する

`build.gradle.kts` の `generateSoapSources` タスクを新しい接続先に合わせて変更します。

**既存の `generateSoapSources` タスクを参考に、新しいタスクを追加します（複数接続先がある場合）:**

```kotlin
// build.gradle.kts に追加するタスク例
tasks.register<JavaExec>("generate{SystemName}SoapSources") {
    group = "build"
    description = "{SystemName} の WSDL からスタブコードを生成する"

    doFirst {
        wsimportOutputDir.mkdirs()
    }

    classpath = wsimportTools + configurations.runtimeClasspath.get()
    mainClass.set("com.sun.tools.ws.WsImport")

    args = listOf(
        "-keep",
        "-verbose",
        "-d", wsimportOutputDir.absolutePath,
        "-s", wsimportOutputDir.absolutePath,
        // ▼ パッケージ名を接続先システムに合わせて変更する
        "-p", "jp.co.createlink.core.soap.{systemname}",
        // ▼ ステップ 1 で配置した WSDL のパス
        "${projectDir}/specs/wsdl/{system-name}-service.wsdl"
    )
}
```

### ステップ 3: スタブコードを生成する

```bash
# スタブコードを生成する
./gradlew generate{SystemName}SoapSources

# 生成されたファイルを確認する
ls build/generated-sources/wsimport/jp/co/createlink/core/soap/{systemname}/
```

生成されたスタブクラス（`{SystemName}Service.java`、`{SystemName}PortType.java` 等）は手動で編集しないでください。
WSDL が変更された場合は、タスクを再実行して再生成します。

### ステップ 4: クライアントクラスを実装する

`src/main/java/jp/co/createlink/core/soap/` に新しいクライアントクラスを作成します。
`SampleSoapClient.java` をコピーして以下を変更してください。

```java
// 例: HogeSystemSoapClient.java
package jp.co.createlink.core.soap;

import jp.co.createlink.core.exception.SoapClientException;
// ▼ wsimport 生成スタブを import する
import jp.co.createlink.core.soap.hogesystem.HogeSystemService;
import jp.co.createlink.core.soap.hogesystem.HogeSystemPortType;
import jp.co.createlink.core.soap.hogesystem.GetHogeRequest;
import jp.co.createlink.core.soap.hogesystem.GetHogeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HogeSystemSoapClient {

    @Value("${core.soap.hogesystem.endpoint-url}")
    private String endpointUrl;

    // ... SampleSoapClient.java の実装パターンに倣って実装する
}
```

**実装時のポイント（`SampleSoapClient.java` コメントを参照）:**

1. `@Component` を付けて Spring DI コンテナに登録する
2. エンドポイント URL は `@Value` で `application-core.yml` から注入する（ハードコード禁止）
3. WSDL はクラスパスリソースから読み込む（`src/main/resources/wsdl/` にコピーする）
4. レスポンスの `statusCode` を確認してエラーハンドリングを実装する
5. 例外は `SoapClientException` にラップしてスローする

### ステップ 5: WSDL をクラスパスリソースにコピーする

ビルド成果物（JAR）に WSDL を含めるため、`src/main/resources/wsdl/` にコピーします。

```bash
cp specs/wsdl/{system-name}-service.wsdl \
   src/main/resources/wsdl/{system-name}-service.wsdl
```

### ステップ 6: application-core.yml にエンドポイント URL を追加する

`src/main/resources/application-core.yml` に接続先のエンドポイント URL を追加します。

```yaml
core:
  soap:
    # ... 既存の設定
    hogesystem:
      # ▼ 実際の接続先 URL に変更すること（環境別設定は application-{profile}.yml で上書き）
      endpoint-url: http://hogesystem.example.com/soap/service
```

---

## 新規 REST 接続先の追加手順

### ステップ 1: OpenAPI ファイルを配置する

接続先システムの OpenAPI 定義ファイルを `specs/openapi/` に配置します。

```bash
# 例: 接続先システムの OpenAPI 定義を取得する
curl -o specs/openapi/{system-name}-api.yaml \
     "https://target-system.example.com/v3/api-docs.yaml"
```

ファイル名は `{system-name}-api.yaml` のように接続先システム名を含めます。

> OpenAPI ファイルが入手できない場合は、接続先の API 仕様書をもとに `specs/openapi/sample-api.yaml` を参考にして手動で作成します。

### ステップ 2: クライアントクラスを実装する

`src/main/java/jp/co/createlink/core/rest/` に新しいクライアントクラスを作成します。
`SampleRestClient.java` をコピーして以下を変更してください。

```java
// 例: HogeSystemRestClient.java
package jp.co.createlink.core.rest;

import jp.co.createlink.core.exception.CoreException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class HogeSystemRestClient {

    private final WebClient webClient;

    public HogeSystemRestClient(
            WebClient.Builder webClientBuilder,
            // ▼ プロパティキーを application-core.yml に合わせて変更する
            @Value("${core.rest.hogesystem.base-url}") String baseUrl
    ) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * ホゲシステムのデータを取得する（GET）。
     *
     * @param id 取得対象の ID
     * @return レスポンス（実装時は接続先の DTO クラスに変更すること）
     */
    public HogeResponse getHogeData(String id) {
        try {
            return webClient.get()
                    // ▼ OpenAPI 定義のパスに合わせて変更する
                    .uri("/hoge/{id}", id)
                    .retrieve()
                    // ▼ 接続先のレスポンス DTO クラスに変更する
                    .bodyToMono(HogeResponse.class)
                    .block(Duration.ofSeconds(30));
        } catch (WebClientResponseException e) {
            throw new CoreException(
                    "REST_HOGE_" + e.getStatusCode().value(),
                    "ホゲシステム接続エラー: GET /hoge/" + id,
                    e
            );
        } catch (Exception e) {
            throw new CoreException("ホゲシステム接続エラー: GET /hoge/" + id, e);
        }
    }
}
```

**実装時のポイント（`SampleRestClient.java` コメントを参照）:**

1. `@Component` を付けて Spring DI コンテナに登録する
2. ベース URL は `@Value` で `application-core.yml` から注入する（ハードコード禁止）
3. `WebClient.Builder` は `CoreConfig.java` の Bean を使用する（SSL・タイムアウト設定済み）
4. `.block()` を使った同期呼び出しパターンを基本とする
5. 例外は `CoreException` にラップしてスローする（エラーコードは `"REST_{システム名}_{HTTPステータス}"` 形式を推奨）

### ステップ 3: application-core.yml にベース URL を追加する

`src/main/resources/application-core.yml` に接続先のベース URL を追加します。

```yaml
core:
  rest:
    # ... 既存の設定
    hogesystem:
      # ▼ 実際の接続先 URL に変更すること（環境別設定は application-{profile}.yml で上書き）
      base-url: http://hogesystem.example.com/api
```

---

## パッケージ命名規則

Core ライブラリのパッケージは `jp.co.createlink.core` 配下に配置します。

| パッケージ | 配置するクラス |
|-----------|--------------|
| `jp.co.createlink.core.soap` | SOAP 接続クライアント（`{SystemName}SoapClient.java`） |
| `jp.co.createlink.core.soap.{systemname}` | wsimport 生成スタブ（自動生成、手動編集禁止） |
| `jp.co.createlink.core.rest` | REST 接続クライアント（`{SystemName}RestClient.java`） |
| `jp.co.createlink.core.config` | 共通設定クラス（`CoreConfig.java` 等） |
| `jp.co.createlink.core.exception` | 共通例外クラス（`CoreException.java` 等） |

**命名規則:**

- クライアントクラス名: `{SystemName}SoapClient` / `{SystemName}RestClient`
- `{SystemName}` は接続先システムの英語名（PascalCase）
- `{systemname}` はパッケージ名（lowercase、ハイフン・アンダースコア禁止）

**命名例:**

| 接続先システム | クライアントクラス名 | パッケージ名 |
|--------------|-----------------|------------|
| 人事システム（HRSystem） | `HrSystemSoapClient` | `jp.co.createlink.core.soap.hrsystem` |
| 勤怠システム（KintaiAPI） | `KintaiApiRestClient` | `jp.co.createlink.core.rest` |
| 会計システム（AccountingWS） | `AccountingWsSoapClient` | `jp.co.createlink.core.soap.accountingws` |

---

## 設定ファイルの更新方法

### application-core.yml の構造

```yaml
core:
  soap:
    {systemname}:
      endpoint-url: http://...    # SOAP エンドポイント URL

  rest:
    connect-timeout-ms: 5000      # 接続タイムアウト（ミリ秒）
    read-timeout-sec: 30          # 読み取りタイムアウト（秒）
    write-timeout-sec: 30         # 書き込みタイムアウト（秒）
    {systemname}:
      base-url: http://...        # REST ベース URL
```

### 環境別の設定上書き

開発・本番環境で異なる URL を使用する場合は、Process-API 側の `application-{profile}.yml` で上書きします。

```yaml
# application-dev.yml（Process-API 側）
core:
  soap:
    hogesystem:
      endpoint-url: http://hogesystem.dev.example.com/soap/service
  rest:
    hogesystem:
      base-url: http://hogesystem.dev.example.com/api
```

---

## テストの書き方ガイド

### テストディレクトリ構成

```
src/test/java/jp/co/createlink/core/
├── soap/
│   └── {SystemName}SoapClientTest.java
├── rest/
│   └── {SystemName}RestClientTest.java
└── exception/
    └── CoreExceptionTest.java
```

### SOAP クライアントのテスト例

`SampleSoapClientTest.java` を参考に、Mockito でポートをモック化してテストします。

```java
// {SystemName}SoapClientTest.java
package jp.co.createlink.core.soap;

import jp.co.createlink.core.exception.SoapClientException;
import jp.co.createlink.core.soap.hogesystem.HogeSystemPortType;
import jp.co.createlink.core.soap.hogesystem.GetHogeRequest;
import jp.co.createlink.core.soap.hogesystem.GetHogeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * HogeSystemSoapClient のユニットテスト。
 * Mockito で SOAP ポートをモック化して通信なしでテストする。
 */
@ExtendWith(MockitoExtension.class)
class HogeSystemSoapClientTest {

    @Mock
    private HogeSystemPortType mockPort;

    private HogeSystemSoapClient client;

    @BeforeEach
    void setUp() {
        // テスト用コンストラクタでモックポートを注入する
        client = new HogeSystemSoapClient(
                "http://test.example.com/soap/hogesystem",
                mockPort
        );
    }

    /**
     * 【テスト対象】getHogeData メソッド
     * 【テスト内容】正常なレスポンスが返った場合
     * 【期待結果】レスポンスオブジェクトが返ること
     */
    @Test
    void should_return_response_when_soap_call_succeeds() {
        // Arrange
        GetHogeResponse mockResponse = new GetHogeResponse();
        mockResponse.setStatusCode("0");
        when(mockPort.getHoge(any(GetHogeRequest.class))).thenReturn(mockResponse);

        // Act
        GetHogeResponse result = client.getHogeData("TEST_ID");

        // Assert
        assertNotNull(result);
        assertEquals("0", result.getStatusCode());
    }

    /**
     * 【テスト対象】getHogeData メソッド
     * 【テスト内容】エラーレスポンス（statusCode != "0"）が返った場合
     * 【期待結果】SoapClientException がスローされること
     */
    @Test
    void should_throw_exception_when_soap_returns_error_status() {
        // Arrange
        GetHogeResponse errorResponse = new GetHogeResponse();
        errorResponse.setStatusCode("99");
        errorResponse.setMessage("システムエラー");
        when(mockPort.getHoge(any(GetHogeRequest.class))).thenReturn(errorResponse);

        // Act & Assert
        SoapClientException ex = assertThrows(
                SoapClientException.class,
                () -> client.getHogeData("ERROR_ID")
        );
        assertTrue(ex.getMessage().contains("99"));
    }
}
```

### REST クライアントのテスト例

`SampleRestClientTest.java` を参考に、`WebClient.Builder` をモック化してテストします。

```java
// {SystemName}RestClientTest.java
package jp.co.createlink.core.rest;

import jp.co.createlink.core.exception.CoreException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * HogeSystemRestClient のユニットテスト。
 * WebClient をモック化して HTTP 通信なしでテストする。
 */
@ExtendWith(MockitoExtension.class)
class HogeSystemRestClientTest {

    // ... WebClient モック設定（SampleRestClientTest.java を参照）

    /**
     * 【テスト対象】getHogeData メソッド
     * 【テスト内容】正常なレスポンスが返った場合
     * 【期待結果】レスポンスオブジェクトが返ること
     */
    @Test
    void should_return_response_when_rest_call_succeeds() {
        // Arrange / Act / Assert（SampleRestClientTest.java のパターンを参照）
    }
}
```

### テストの原則

1. **モックを使って外部通信を排除する** — 実際のシステムに接続するテストは書かない
2. **正常系・異常系・境界値を網羅する** — 最低でも「正常」「エラーレスポンス」「例外」の3パターン
3. **テストケース名は英語で記述する** — `should_{期待動作}_when_{条件}` の形式を推奨
4. **コメントは日本語で記述する** — テスト対象・テスト内容・期待結果を明記する
5. **AAA パターンに従う** — Arrange（準備）/ Act（実行）/ Assert（検証）で整理する
