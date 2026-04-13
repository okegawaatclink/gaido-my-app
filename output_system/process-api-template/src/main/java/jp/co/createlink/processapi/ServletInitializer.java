package jp.co.createlink.processapi;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * ServletInitializer
 *
 * <p>外部サーブレットコンテナ（Tomcat等）へのWARデプロイ時に必要な初期化クラス。
 * K8SでWARファイルをデプロイする際に使用される。
 *
 * <p>SpringBootServletInitializerを継承することで、外部Tomcatが
 * このクラスを見つけてSpring Bootアプリケーションを起動できるようになる。
 * （WAR内のMETA-INF/services/javax.servlet.ServletContainerInitializerに登録される）
 *
 * <p>組み込みTomcat（./gradlew bootRun）ではこのクラスは使用されず、
 * SampleProcessApiApplication.main()が使用される。
 *
 * <p>パッケージ:
 * Spring Boot 3.4.x では SpringBootServletInitializer は
 * {@code org.springframework.boot.web.servlet.support} パッケージにある。
 * （Spring Boot 2.x 以前の {@code org.springframework.boot.web.support} から移動済み）
 *
 * @see SampleProcessApiApplication アプリケーションエントリーポイント
 * @see org.springframework.boot.web.servlet.support.SpringBootServletInitializer
 */
public class ServletInitializer extends SpringBootServletInitializer {

    /**
     * 外部サーブレットコンテナからのアプリケーション起動設定。
     *
     * <p>SpringApplicationBuilderを使ってアプリケーションのソースを設定する。
     * ここで指定したクラスが@SpringBootApplicationを持つメインクラスになる。
     *
     * @param application SpringApplicationBuilderインスタンス
     * @return アプリケーションのソース（SampleProcessApiApplication）を設定したビルダー
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        // SampleProcessApiApplicationをSpring Bootのエントリーポイントとして設定する
        return application.sources(SampleProcessApiApplication.class);
    }
}
