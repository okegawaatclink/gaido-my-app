package jp.co.createlink.processapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SampleProcessApiApplication
 *
 * <p>Process-APIテンプレートのSpring Bootアプリケーションエントリーポイント。
 * 組み込みTomcat（bootRun）でローカル起動する際のメインクラス。
 *
 * <p>このクラスは新規ドメインのProcess-API作成時にコピーして使用する。
 * クラス名とパッケージ名をドメインに合わせて変更すること。
 *
 * <p>起動方法:
 * <pre>
 *   ./gradlew bootRun --args='--spring.profiles.active=local'
 * </pre>
 *
 * <p>WAR生成（K8Sデプロイ用）:
 * <pre>
 *   ./gradlew bootWar
 * </pre>
 *
 * @see ServletInitializer WAR デプロイ時の初期化クラス
 */
@SpringBootApplication
public class SampleProcessApiApplication {

    /**
     * アプリケーションのエントリーポイント。
     *
     * <p>組み込みTomcatでSpring Bootアプリケーションを起動する。
     * 外部Tomcat（K8Sのサーブレットコンテナ）へのWARデプロイ時は
     * このmainメソッドではなくServletInitializerが使用される。
     *
     * @param args コマンドライン引数（例: --spring.profiles.active=local）
     */
    public static void main(String[] args) {
        SpringApplication.run(SampleProcessApiApplication.class, args);
    }
}
