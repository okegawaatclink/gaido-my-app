package jp.co.createlink.core.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.SslProvider;

import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * CoreConfig
 *
 * <p>Coreライブラリの共通設定クラス。</p>
 *
 * <p>このクラスでは以下の共通設定を行う:</p>
 * <ul>
 *   <li>{@link WebClient.Builder} Bean の定義（SSL・タイムアウト設定込み）</li>
 *   <li>共通設定値（タイムアウト、SSL等）の集約管理</li>
 * </ul>
 *
 * <h2>Process-API での使い方</h2>
 * <p>Process-API の Spring Boot アプリケーションに Core JAR を依存関係として追加すると、
 * このクラスが自動的に Bean 登録される（コンポーネントスキャンまたは {@code @Import} を使用）。
 * {@link WebClient.Builder} はこのクラスで定義した Bean が注入されるため、
 * 各 REST クライアントクラスで共通設定を意識せず WebClient を使用できる。</p>
 *
 * <h2>設定値の注入元</h2>
 * <p>{@code application-core.yml} に定義した {@code core.*} プロパティが
 * {@link Value} アノテーションで注入される。</p>
 *
 * <h2>SSL設定</h2>
 * <p>デフォルトはシステム標準の TrustStore を使用する。
 * 企業プロキシ等で独自CA証明書が必要な場合は {@code core.ssl.trust-store-path} を設定すること。</p>
 */
@Configuration
public class CoreConfig {

    // ============================================================
    // 設定値（application-core.yml から注入）
    // ============================================================

    /**
     * HTTP 接続確立のタイムアウト（ミリ秒）。
     * デフォルト: 5000ms（5秒）
     */
    @Value("${core.rest.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    /**
     * HTTP レスポンス読み取りのタイムアウト（秒）。
     * デフォルト: 30秒
     */
    @Value("${core.rest.read-timeout-sec:30}")
    private int readTimeoutSec;

    /**
     * HTTP リクエスト書き込みのタイムアウト（秒）。
     * デフォルト: 30秒
     */
    @Value("${core.rest.write-timeout-sec:30}")
    private int writeTimeoutSec;

    /**
     * カスタム TrustStore ファイルパス（オプション）。
     * 企業プロキシ等の独自CA証明書が必要な場合に設定する。
     * 未設定の場合はシステム標準の TrustStore を使用する。
     * デフォルト: 空文字（未設定扱い）
     */
    @Value("${core.ssl.trust-store-path:}")
    private String trustStorePath;

    /**
     * カスタム TrustStore のパスワード。
     * {@code core.ssl.trust-store-path} を設定した場合に必要。
     * デフォルト: "changeit"（JDK デフォルトの cacerts パスワード）
     */
    @Value("${core.ssl.trust-store-password:changeit}")
    private String trustStorePassword;

    // ============================================================
    // Bean 定義
    // ============================================================

    /**
     * WebClient.Builder Bean を定義する。
     *
     * <p>以下の設定を適用した {@link WebClient.Builder} を返す:</p>
     * <ul>
     *   <li>接続タイムアウト（{@code core.rest.connect-timeout-ms}）</li>
     *   <li>読み取りタイムアウト（{@code core.rest.read-timeout-sec}）</li>
     *   <li>書き込みタイムアウト（{@code core.rest.write-timeout-sec}）</li>
     *   <li>SSL 設定（カスタム TrustStore または JVM デフォルト）</li>
     * </ul>
     *
     * <p>各 REST クライアントクラスはこの Bean を DI で受け取り、
     * ベース URL を設定して {@link WebClient} インスタンスを生成する。</p>
     *
     * @return タイムアウト・SSL 設定済みの {@link WebClient.Builder}
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        // Reactor Netty の HttpClient でタイムアウト・SSL を設定する
        HttpClient httpClient = buildHttpClient();

        return WebClient.builder()
                // Reactor Netty をベースの HTTP クライアントとして設定する
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }

    // ============================================================
    // 内部メソッド
    // ============================================================

    /**
     * タイムアウト・SSL 設定を適用した {@link HttpClient} を生成する。
     *
     * <p>{@code core.ssl.trust-store-path} が設定されている場合はカスタム TrustStore を使用し、
     * 未設定の場合はシステム標準の TrustStore を使用する。</p>
     *
     * @return 設定済みの {@link HttpClient}
     */
    private HttpClient buildHttpClient() {
        HttpClient httpClient = HttpClient.create()
                // 接続確立タイムアウト（TCP 接続が完了するまでの時間）
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                // レスポンス受信完了後の処理（読み取り・書き込みタイムアウト設定）
                .doOnConnected(conn -> conn
                        // 読み取りタイムアウト: サーバーからの応答を待つ最大時間
                        .addHandlerLast(new ReadTimeoutHandler(readTimeoutSec, TimeUnit.SECONDS))
                        // 書き込みタイムアウト: サーバーへのリクエスト送信完了までの最大時間
                        .addHandlerLast(new WriteTimeoutHandler(writeTimeoutSec, TimeUnit.SECONDS))
                );

        // ▼ カスタム TrustStore が設定されている場合は SSL 設定を適用する
        if (trustStorePath != null && !trustStorePath.isEmpty()) {
            httpClient = applyCustomSsl(httpClient);
        }
        // カスタム TrustStore が未設定の場合は JVM デフォルトの cacerts を使用する
        // （何もしなければ Reactor Netty は JVM のデフォルト TrustStore を使用する）

        return httpClient;
    }

    /**
     * カスタム TrustStore を使った SSL 設定を HttpClient に適用する。
     *
     * <p>企業プロキシ（例: Netskope, Zscaler）等の独自CA証明書が必要な環境で使用する。
     * {@code core.ssl.trust-store-path} に JKS または PKCS12 形式の TrustStore のパスを設定すること。</p>
     *
     * @param httpClient SSL 設定前の {@link HttpClient}
     * @return SSL 設定済みの {@link HttpClient}
     * @throws IllegalStateException TrustStore の読み込みに失敗した場合
     */
    private HttpClient applyCustomSsl(HttpClient httpClient) {
        try {
            // TrustStore を読み込む（JKS / PKCS12 形式に対応）
            // ▼ TrustStore 形式は接続先環境に合わせて変更すること
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            try (FileInputStream fis = new FileInputStream(trustStorePath)) {
                trustStore.load(fis, trustStorePassword.toCharArray());
            }

            // TrustManagerFactory を初期化する
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm()
            );
            trustManagerFactory.init(trustStore);

            // SSL コンテキストを生成する（SslContext#build() は SSLException をスローする）
            io.netty.handler.ssl.SslContext sslContext;
            try {
                sslContext = io.netty.handler.ssl.SslContextBuilder
                        .forClient()
                        .trustManager(trustManagerFactory)
                        .build();
            } catch (javax.net.ssl.SSLException e) {
                throw new IllegalStateException(
                        "SSL コンテキストの生成に失敗しました: " + trustStorePath, e
                );
            }

            // SSL コンテキストを設定した HttpClient を返す
            return httpClient.secure(spec -> spec.sslContext(sslContext));

        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            // TrustStore の読み込みに失敗した場合はアプリ起動を中止する
            throw new IllegalStateException(
                    "カスタム TrustStore の読み込みに失敗しました: " + trustStorePath, e
            );
        } catch (IllegalStateException e) {
            // 再スロー（SSL コンテキスト生成失敗）
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "SSL設定の適用に失敗しました: " + trustStorePath, e
            );
        }
    }
}
