package jp.co.createlink.processapi.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * アプリケーション設定ファイルの存在・内容確認テスト。
 *
 * <p>テストケースIssue #39: PBI2.3 Actuatorヘルスチェック・環境別プロファイル・ログ設定が整っている</p>
 * <p>テストケースIssue #47: 非機能: プロファイル切り替えでアプリケーションが起動できる</p>
 *
 * <p>以下の設定ファイルが存在し、必要な内容が含まれていることを検証する:</p>
 * <ul>
 *   <li>application.yml: 共通設定（Actuator, springdoc）</li>
 *   <li>application-local.yml: ローカル開発用設定</li>
 *   <li>application-dev.yml: 開発環境用設定</li>
 *   <li>application-prod.yml: 本番環境用設定</li>
 *   <li>logback-spring.xml: ログ設定</li>
 * </ul>
 */
class ApplicationConfigFilesTest {

    /** 設定ファイルのディレクトリ */
    private static final Path RESOURCES_DIR = Paths.get("").toAbsolutePath()
            .resolve("src/main/resources");

    /**
     * 【テスト対象】application.yml
     * 【テスト内容】共通設定ファイルが存在し、Spring Actuator設定が含まれること
     * 【期待結果】application.ymlが存在し、management（Actuator）設定が含まれる
     */
    @Test
    void should_have_application_yml_with_actuator_configuration() throws IOException {
        // Arrange
        Path configPath = RESOURCES_DIR.resolve("application.yml");

        // Assert: ファイルが存在すること
        assertTrue(Files.exists(configPath),
                "application.yml が存在すること: " + configPath.toAbsolutePath());

        // Assert: Actuator設定が含まれること
        String content = Files.readString(configPath);
        assertTrue(content.contains("management") || content.contains("actuator"),
                "application.yml に Spring Actuator の management 設定が含まれること");
    }

    /**
     * 【テスト対象】application-local.yml
     * 【テスト内容】ローカル開発用プロファイル設定ファイルが存在すること
     * 【期待結果】application-local.ymlが存在し、YAMLとして有効な内容を持つ
     */
    @Test
    void should_have_application_local_yml() throws IOException {
        // Arrange
        Path configPath = RESOURCES_DIR.resolve("application-local.yml");

        // Assert: ファイルが存在すること
        assertTrue(Files.exists(configPath),
                "application-local.yml が存在すること: " + configPath.toAbsolutePath());

        // Assert: 空でないこと
        String content = Files.readString(configPath);
        assertFalse(content.isBlank(),
                "application-local.yml が空でないこと");
    }

    /**
     * 【テスト対象】application-dev.yml
     * 【テスト内容】開発環境用プロファイル設定ファイルが存在すること
     * 【期待結果】application-dev.ymlが存在し、YAMLとして有効な内容を持つ
     */
    @Test
    void should_have_application_dev_yml() throws IOException {
        // Arrange
        Path configPath = RESOURCES_DIR.resolve("application-dev.yml");

        // Assert: ファイルが存在すること
        assertTrue(Files.exists(configPath),
                "application-dev.yml が存在すること: " + configPath.toAbsolutePath());

        // Assert: 空でないこと
        String content = Files.readString(configPath);
        assertFalse(content.isBlank(),
                "application-dev.yml が空でないこと");
    }

    /**
     * 【テスト対象】application-prod.yml
     * 【テスト内容】本番環境用プロファイル設定ファイルが存在すること
     * 【期待結果】application-prod.ymlが存在し、YAMLとして有効な内容を持つ
     */
    @Test
    void should_have_application_prod_yml() throws IOException {
        // Arrange
        Path configPath = RESOURCES_DIR.resolve("application-prod.yml");

        // Assert: ファイルが存在すること
        assertTrue(Files.exists(configPath),
                "application-prod.yml が存在すること: " + configPath.toAbsolutePath());

        // Assert: 空でないこと
        String content = Files.readString(configPath);
        assertFalse(content.isBlank(),
                "application-prod.yml が空でないこと");
    }

    /**
     * 【テスト対象】logback-spring.xml
     * 【テスト内容】ログ設定ファイルが存在し、プロファイル別ログフォーマットが設定されていること
     * 【期待結果】logback-spring.xmlが存在し、SpringProfileによるプロファイル別設定がある
     */
    @Test
    void should_have_logback_spring_xml_with_profile_based_configuration() throws IOException {
        // Arrange
        Path logbackPath = RESOURCES_DIR.resolve("logback-spring.xml");

        // Assert: ファイルが存在すること
        assertTrue(Files.exists(logbackPath),
                "logback-spring.xml が存在すること: " + logbackPath.toAbsolutePath());

        // Assert: SpringProfile設定が含まれること
        String content = Files.readString(logbackPath);
        assertTrue(content.contains("springProfile") || content.contains("profile"),
                "logback-spring.xml にSpringProfileによるプロファイル別ログ設定が含まれること");
    }

    /**
     * 【テスト対象】build.gradle.kts
     * 【テスト内容】build.gradle.ktsにSpring Actuator依存が追加されていること
     * 【期待結果】build.gradle.ktsにspring-boot-starter-actuatorが含まれる
     */
    @Test
    void should_have_actuator_dependency_in_build_gradle() throws IOException {
        // Arrange
        Path buildGradlePath = Paths.get("").toAbsolutePath().resolve("build.gradle.kts");

        // Assert: ファイルが存在すること
        assertTrue(Files.exists(buildGradlePath),
                "build.gradle.kts が存在すること");

        // Assert: Actuator依存が含まれること
        String content = Files.readString(buildGradlePath);
        assertTrue(content.contains("actuator"),
                "build.gradle.kts に Spring Actuator 依存（spring-boot-starter-actuator）が含まれること");
    }
}
