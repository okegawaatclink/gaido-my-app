/*
 * build.gradle.kts
 *
 * Coreライブラリのビルド設定。
 * java-library plugin を使用してAPIとimplementationを分離する。
 * JAX-WS (wsimport) を使ったSOAPクライアントコード生成タスクを含む。
 *
 * ■ プラグイン選定理由
 *   - java-library: 依存性の公開範囲（api vs implementation）を明確に制御できる
 *   - java: 単純なJARを作る場合。ライブラリとして他プロジェクトに公開する場合は java-library を推奨
 */

plugins {
    // java-library: api/implementation の分離が可能。ライブラリ公開に適切。
    `java-library`
}

// ============================================================
// グループ・バージョン設定
// ============================================================
group = "jp.co.createlink"
version = "1.0.0-SNAPSHOT"

// ============================================================
// Java バージョン設定（Java 21 LTS）
// ============================================================
java {
    // Java 21 LTS を指定
    // Java 21 以降、JAX-WS は JDK から削除されているため Jakarta EE 版を使用する
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

// ============================================================
// リポジトリ設定
// ============================================================
repositories {
    // Maven Central から依存関係を取得する
    mavenCentral()
}

// ============================================================
// カスタム設定: wsimport ツール用クラスパス
// wsimport ツールは jaxws-tools に含まれており、通常の runtimeClasspath とは
// 別のクラスパスで実行する必要がある。
// ============================================================
val wsimportTools: Configuration by configurations.creating {
    // wsimport ツールの実行に必要なクラスパスを独立した設定として管理する
    isTransitive = true
}

// ============================================================
// 依存関係設定
// ============================================================
dependencies {
    // ----------------------------------------------------------------
    // Jakarta XML Web Services (JAX-WS) API
    // Java 21 では JAX-WS が JDK から削除されているため、
    // Jakarta EE 版（jakarta.xml.ws-api）を明示的に依存関係に追加する。
    // ----------------------------------------------------------------
    // API として公開する（Process-API 側でも参照可能にする）
    api("jakarta.xml.ws:jakarta.xml.ws-api:4.0.2")

    // JAX-WS リファレンス実装（実行時に必要）
    // wsimport で生成されたコードの実行に必要
    implementation("com.sun.xml.ws:jaxws-rt:4.0.3")

    // ----------------------------------------------------------------
    // Spring Context（@Component, @Value アノテーション利用のため）
    // SOAPクライアントをSpring DIコンテナで管理する場合に使用
    // ----------------------------------------------------------------
    implementation("org.springframework:spring-context:6.2.2")

    // ----------------------------------------------------------------
    // Spring WebFlux（Spring WebClient を使った REST 接続のため）
    // WebClient は Spring 5 以降に導入されたリアクティブ対応 REST クライアント。
    // spring-boot-starter-webflux に含まれる WebClient.Builder を使って
    // RESTful API との通信を行う。
    // spring-webflux 単体を使用（Spring Boot 環境でなくてもビルド可能にするため）。
    // Process-API 側では spring-boot-starter-webflux を使用してもよい。
    // ----------------------------------------------------------------
    // API として公開する（Process-API 側でも WebClient を参照できるようにする）
    api("org.springframework:spring-webflux:6.2.2")

    // Reactor Netty（WebClient のデフォルト HTTP クライアント実装）
    // WebClient の実行には HTTP クライアントの実装が必要。
    // Reactor Netty は Spring WebFlux 標準の非同期 HTTP クライアント。
    implementation("io.projectreactor.netty:reactor-netty-http:1.2.2")

    // ----------------------------------------------------------------
    // テスト依存関係
    // ----------------------------------------------------------------
    // JUnit 5 (JUnit Jupiter)
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    // Mockito（モックフレームワーク）
    testImplementation("org.mockito:mockito-core:5.15.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.15.2")

    // テスト実行エンジン（JUnit Platform Launcher）
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // ----------------------------------------------------------------
    // wsimport ツール
    // WsImport クラスは jaxws-tools に含まれている（jaxws-rt ではない）。
    // Java 21 の JDK には wsimport コマンドが含まれていないため、
    // Maven セントラルから Jakarta 版の wsimport ツールを使用する。
    // ----------------------------------------------------------------
    wsimportTools("com.sun.xml.ws:jaxws-tools:4.0.3")
}

// ============================================================
// テスト設定
// ============================================================
tasks.withType<Test> {
    // JUnit Platform (JUnit 5) を使用してテストを実行する
    useJUnitPlatform()

    // Mockito が JVM エージェントを動的ロードする際の警告を抑制する。
    // Java 21 以降、動的エージェントのロードはデフォルトで警告が出る。
    // -XX:+EnableDynamicAgentLoading を指定することで警告を抑制する。
    jvmArgs("-XX:+EnableDynamicAgentLoading")
}

// ============================================================
// JAX-WS wsimport タスク設定
// ============================================================
// wsimport は Java 21 の JDK に含まれていないため、
// Jakarta XML Web Services の JAR に含まれる wsimport を使用する。
// または Gradle カスタムタスクで JavaExec を使って実行する。
//
// ■ wsimport タスクの役割
//   specs/wsdl/*.wsdl を読み込み、
//   src/main/java/<package>/ 配下に SOAP クライアントの Java コードを生成する。
//
// ■ 使い方（新規SOAPシステム接続時）
//   1. 接続先システムの WSDL を specs/wsdl/ に配置する
//   2. generateSoapSources タスクの wsdlFile と destDir を接続先に合わせて変更する
//   3. ./gradlew generateSoapSources を実行してコードを生成する
//   4. 生成されたコードをもとに SampleSoapClient.java に倣ってクライアントを実装する
// ============================================================

// wsimport で生成するソースの出力ディレクトリ
val wsimportOutputDir = layout.buildDirectory.dir("generated-sources/wsimport").get().asFile

// wsimport 生成ソースをコンパイルパスに含める
sourceSets {
    main {
        java {
            // wsimport 生成コードをコンパイル対象に追加する
            srcDir(wsimportOutputDir)
        }
    }
}

/**
 * generateSoapSources タスク
 *
 * 指定した WSDL ファイルから JAX-WS の Java スタブコードを生成する。
 *
 * ■ 新規SOAPシステム接続時の手順
 *   - wsdlFile: 接続先の WSDL ファイルパスに変更する
 *   - packageName: 生成コードのパッケージ名を変更する（例: jp.co.createlink.core.soap.hogesystem）
 *   - destDir: 任意の出力先に変更することも可能（デフォルトは build/generated-sources/wsimport）
 */
tasks.register<JavaExec>("generateSoapSources") {
    group = "build"
    description = "WSDLファイルからJAX-WS Javaスタブコードを生成する"

    // 出力先ディレクトリを作成する
    doFirst {
        wsimportOutputDir.mkdirs()
    }

    // wsimport ツールのクラスパス（jaxws-tools + runtime の両方が必要）
    // WsImport クラスは jaxws-tools に含まれている
    classpath = wsimportTools + configurations.runtimeClasspath.get()
    mainClass.set("com.sun.tools.ws.WsImport")

    // wsimport 引数
    // -keep: 生成したソースファイルを残す
    // -verbose: 詳細ログを出力する
    // -d: クラスファイル出力先
    // -s: ソースファイル出力先
    // -p: 生成コードのパッケージ名（接続先に合わせて変更すること）
    args = listOf(
        "-keep",
        "-verbose",
        "-d", wsimportOutputDir.absolutePath,
        "-s", wsimportOutputDir.absolutePath,
        // ▼ パッケージ名: 新規SOAPシステム接続時はここを変更する
        "-p", "jp.co.createlink.core.soap.generated",
        // ▼ WSDLファイルパス: 新規SOAPシステム接続時はここを変更する
        "${projectDir}/specs/wsdl/sample-service.wsdl"
    )

    // コンパイルタスクより前に実行する
    mustRunAfter("processResources")
}

// compileJava タスクの前に generateSoapSources が実行されるよう依存関係を設定する
// （generateSoapSources は必要な場合のみ手動実行するため、通常ビルドには含めない）
// 通常ビルドに含める場合は以下のコメントを外す:
// tasks.named("compileJava") { dependsOn("generateSoapSources") }
