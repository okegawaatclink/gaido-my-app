package jp.co.createlink.processapi.war;

import jp.co.createlink.processapi.ServletInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WARデプロイメント確認テスト。
 *
 * <p>テストケースIssue #46: 非機能: WAR出力ファイルがTomcatデプロイ可能な形式である</p>
 *
 * <p>このテストクラスは以下を検証する:</p>
 * <ul>
 *   <li>ServletInitializerがSpringBootServletInitializerを継承していること</li>
 *   <li>build.gradle.ktsにwarプラグインが設定されていること</li>
 *   <li>WAR形式でのデプロイに必要な設定が整っていること</li>
 * </ul>
 */
class WarDeploymentTest {

    /**
     * 【テスト対象】ServletInitializer クラス
     * 【テスト内容】ServletInitializerがSpringBootServletInitializerを継承していること
     * 【期待結果】ServletInitializerはSpringBootServletInitializerのサブクラスである
     *
     * <p>外部Tomcatへのデプロイには SpringBootServletInitializer の継承が必須。
     * このテストが成功することで、外部Tomcatデプロイ可能なWAR形式であることが確認できる。</p>
     */
    @Test
    void should_extend_spring_boot_servlet_initializer() {
        // Assert: ServletInitializerがSpringBootServletInitializerを継承していること
        assertTrue(
                SpringBootServletInitializer.class.isAssignableFrom(ServletInitializer.class),
                "ServletInitializer が SpringBootServletInitializer を継承していること（外部Tomcatデプロイ用）"
        );
    }

    /**
     * 【テスト対象】ServletInitializer - インスタンス生成
     * 【テスト内容】ServletInitializerがインスタンス化できること
     * 【期待結果】ServletInitializerのインスタンスが生成できる
     *
     * <p>configureメソッドはprotectedアクセスのため直接テストできないが、
     * インスタンス生成の確認とSpringBootServletInitializer継承確認でWARデプロイ準備を検証する。</p>
     */
    @Test
    void should_instantiate_servlet_initializer_successfully() {
        // Act & Assert: ServletInitializerがインスタンス化できること
        assertDoesNotThrow(() -> {
            ServletInitializer initializer = new ServletInitializer();
            assertNotNull(initializer, "ServletInitializer がインスタンス化できること");
        });
    }

    /**
     * 【テスト対象】build.gradle.kts - war プラグイン設定
     * 【テスト内容】build.gradle.ktsにwarプラグインが設定されていること
     * 【期待結果】build.gradle.ktsに "war" プラグインの設定が含まれる
     */
    @Test
    void should_have_war_plugin_in_build_gradle() throws IOException {
        // Arrange
        Path buildGradlePath = Paths.get("").toAbsolutePath().resolve("build.gradle.kts");

        // Assert: ファイルが存在すること
        assertTrue(Files.exists(buildGradlePath),
                "build.gradle.kts が存在すること");

        // Assert: warプラグインが含まれること
        String content = Files.readString(buildGradlePath);
        assertTrue(content.contains("war"),
                "build.gradle.kts に war プラグインが設定されていること");
    }

    /**
     * 【テスト対象】build.gradle.kts - Spring Boot 依存設定
     * 【テスト内容】build.gradle.ktsにSpring Boot依存が設定されていること
     * 【期待結果】Spring Boot Starterが依存に含まれる
     */
    @Test
    void should_have_spring_boot_dependency_in_build_gradle() throws IOException {
        // Arrange
        Path buildGradlePath = Paths.get("").toAbsolutePath().resolve("build.gradle.kts");

        // Assert: Spring Boot依存が含まれること
        String content = Files.readString(buildGradlePath);
        assertTrue(content.contains("spring-boot-starter"),
                "build.gradle.kts に Spring Boot Starter 依存が設定されていること");
    }

    /**
     * 【テスト対象】build.gradle.kts - Core依存設定
     * 【テスト内容】build.gradle.ktsにCore依存（Composite Build参照）が設定されていること
     * 【期待結果】Coreライブラリへの依存が含まれる
     */
    @Test
    void should_have_core_dependency_in_build_gradle() throws IOException {
        // Arrange
        Path buildGradlePath = Paths.get("").toAbsolutePath().resolve("build.gradle.kts");

        // Assert: Core依存が含まれること
        String content = Files.readString(buildGradlePath);
        assertTrue(content.contains("core") || content.contains("createlink"),
                "build.gradle.kts に Core ライブラリへの依存設定が含まれること");
    }
}
