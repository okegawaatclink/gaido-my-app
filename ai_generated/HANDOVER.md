# HANDOVER

## 技術スタック
- 言語/フレームワーク: Java 21, Spring Framework 6.2.2
- ビルドツール: Gradle 8.12.1 (Kotlin DSL)
- SOAP: JAX-WS 4.0 (Jakarta EE 版: jakarta.xml.ws-api:4.0.2, jaxws-rt:4.0.3)
- wsimport: com.sun.xml.ws:jaxws-tools:4.0.3（Java 21 では JDK 同梱なし）
- テスト: JUnit 5 (junit-jupiter:5.11.4) + Mockito (mockito-core:5.15.2)

## ディレクトリ構成

```
output_system/
└── core/
    ├── build.gradle.kts               # java-library, Java 21, JAX-WS, wsimportタスク
    ├── settings.gradle.kts            # rootProject.name = "core"
    ├── gradlew, gradlew.bat           # Gradle 8.12.1 Wrapper
    ├── gradle/wrapper/
    ├── specs/wsdl/sample-service.wsdl # ダミーWSDL（getSampleDataオペレーション）
    └── src/
        ├── main/
        │   ├── java/jp/co/createlink/core/
        │   │   ├── soap/SampleSoapClient.java     # SOAP接続クライアント雛形
        │   │   └── exception/SoapClientException.java
        │   └── resources/
        │       ├── application-core.yml           # エンドポイントURL設定
        │       └── wsdl/sample-service.wsdl       # クラスパスリソース用WSDL
        └── test/java/jp/co/createlink/core/
            └── soap/SampleSoapClientTest.java     # 6件ユニットテスト
```

## ビルド・起動方法

```bash
# Core ライブラリビルド（ソースあり時）
# 事前に generateSoapSources が必要
cd output_system/core
# Docker環境（ホスト環境にJavaなし）
docker run --rm -v $(pwd):/project -w /project gradle:8.12.1-jdk21 gradle generateSoapSources build --no-daemon

# テスト実行
docker run --rm -v $(pwd):/project -w /project gradle:8.12.1-jdk21 gradle generateSoapSources test --no-daemon
```

## 設計判断

- **wsimport ツール**: `jaxws-rt` ではなく `jaxws-tools` を使用。WsImport クラスは jaxws-tools に含まれており、jaxws-rt には含まれない
- **wsimport は通常ビルドに含めない**: `generateSoapSources` タスクは手動実行。通常の `./gradlew build` には含めない（WSDL変更時のみ実行）
- **WSDL のクラスパス配置**: `specs/wsdl/` は参照用、`src/main/resources/wsdl/` はクラスパスリソース用として両方に配置
- **テストコンストラクタ**: `package-private` のテスト用コンストラクタでモックポートを注入するパターンを採用

## はまりポイント

- **WSL環境でDockerボリュームマウントが効かない**: ホストのファイルがコンテナ内から見えない問題。Dockerfile.COPYを使う方法（docker buildコマンドのコンテキスト指定）で解決
- **gradle-wrapper.jar の取得**: ホストにGradleがないため、Dockerコンテナ内で生成してdocker cpで取り出した
- **jaxws-tools の mainClass**: `com.sun.tools.ws.WsImport`（jaxws-rt では ClassNotFoundException になる）

## 実装済み機能

- PBI #3: SOAP接続クライアント雛形（JAX-WS wsimport + SampleSoapClient + テスト）
