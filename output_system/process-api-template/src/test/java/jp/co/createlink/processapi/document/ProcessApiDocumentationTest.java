package jp.co.createlink.processapi.document;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Process-APIテンプレートのドキュメント・設定ファイル存在確認テスト。
 *
 * <p>テストケースIssue #37: PBI2.1 Process-APIのGradleプロジェクトがWAR出力でき、CoreをComposite Buildで参照できる</p>
 * <p>テストケースIssue #40: PBI2.4 テストテンプレートとドキュメントで新規ドメインAPIの構築手順を把握できる</p>
 *
 * <p>以下のファイルが存在し、必要な内容が含まれていることを検証する:</p>
 * <ul>
 *   <li>README.md: プロジェクト概要・ビルド・起動・Swagger UIアクセス方法</li>
 *   <li>CONTRIBUTING.md: 新規ドメイン追加手順</li>
 *   <li>.github/copilot-instructions.md: 技術スタック・レイヤー構成・テストパターン</li>
 *   <li>specs/openapi/process-api.yaml: OpenAPI定義</li>
 *   <li>settings.gradle.kts: includeBuild("../core")の設定</li>
 * </ul>
 */
class ProcessApiDocumentationTest {

    /** Process-APIプロジェクトのルートディレクトリ */
    private static final Path PROJECT_ROOT = Paths.get("").toAbsolutePath();

    /**
     * 【テスト対象】README.md
     * 【テスト内容】README.mdが存在し、プロジェクト概要・ビルド・起動・Swagger UIアクセス方法が含まれること
     * 【期待結果】README.mdが存在し、必要なセクションのキーワードが含まれる
     */
    @Test
    void should_have_readme_with_project_overview_and_build_instructions() throws IOException {
        // Arrange
        Path readmePath = PROJECT_ROOT.resolve("README.md");

        // Assert: ファイルが存在すること
        assertTrue(Files.exists(readmePath),
                "README.md が存在すること: " + readmePath.toAbsolutePath());

        // Assert: 必要なコンテンツが含まれること
        String content = Files.readString(readmePath);
        assertTrue(content.length() > 100,
                "README.md が空でないこと（100文字以上）");
        // ビルド/起動に関するキーワード確認
        assertTrue(content.contains("gradlew") || content.contains("Gradle") || content.contains("ビルド") || content.contains("build"),
                "README.md にビルド手順が含まれること");
    }

    /**
     * 【テスト対象】CONTRIBUTING.md
     * 【テスト内容】CONTRIBUTING.mdが存在し、新規ドメイン追加手順が含まれること
     * 【期待結果】CONTRIBUTING.mdが存在し、ドメイン追加に関する記述がある
     */
    @Test
    void should_have_contributing_with_domain_addition_guide() throws IOException {
        // Arrange
        Path contributingPath = PROJECT_ROOT.resolve("CONTRIBUTING.md");

        // Assert: ファイルが存在すること
        assertTrue(Files.exists(contributingPath),
                "CONTRIBUTING.md が存在すること: " + contributingPath.toAbsolutePath());

        // Assert: ドメイン追加手順が含まれること
        String content = Files.readString(contributingPath);
        assertTrue(content.length() > 100,
                "CONTRIBUTING.md が空でないこと（100文字以上）");
        assertTrue(content.contains("Controller") || content.contains("Service") || content.contains("ドメイン") || content.contains("追加"),
                "CONTRIBUTING.md に新規ドメイン追加手順が含まれること");
    }

    /**
     * 【テスト対象】.github/copilot-instructions.md
     * 【テスト内容】Copilotインストラクションファイルが存在し、技術スタック・レイヤー構成・テストパターンが含まれること
     * 【期待結果】.github/copilot-instructions.mdが存在し、コーディングガイドが含まれる
     */
    @Test
    void should_have_copilot_instructions_with_tech_stack_and_test_patterns() throws IOException {
        // Arrange
        Path copilotInstructionsPath = PROJECT_ROOT.resolve(".github/copilot-instructions.md");

        // Assert: ファイルが存在すること
        assertTrue(Files.exists(copilotInstructionsPath),
                ".github/copilot-instructions.md が存在すること: " + copilotInstructionsPath.toAbsolutePath());

        // Assert: 技術スタック情報が含まれること
        String content = Files.readString(copilotInstructionsPath);
        assertTrue(content.contains("Spring") || content.contains("Java") || content.contains("技術"),
                ".github/copilot-instructions.md に技術スタック情報が含まれること");
        // テストパターンに関するキーワード確認
        assertTrue(content.contains("Test") || content.contains("MockMvc") || content.contains("テスト"),
                ".github/copilot-instructions.md にテストパターン情報が含まれること");
    }

    /**
     * 【テスト対象】specs/openapi/process-api.yaml
     * 【テスト内容】OpenAPI定義ファイルが specs/openapi/ に配置されていること
     * 【期待結果】specs/openapi/process-api.yaml が存在する
     */
    @Test
    void should_have_openapi_definition_in_specs_directory() throws IOException {
        // Arrange
        Path openapiPath = PROJECT_ROOT.resolve("specs/openapi/process-api.yaml");

        // Assert: ファイルが存在すること
        assertTrue(Files.exists(openapiPath),
                "specs/openapi/process-api.yaml が存在すること: " + openapiPath.toAbsolutePath());

        // Assert: OpenAPI内容が含まれること
        String content = Files.readString(openapiPath);
        assertTrue(content.contains("openapi") || content.contains("paths"),
                "specs/openapi/process-api.yaml にOpenAPI定義が含まれること");
    }

    /**
     * 【テスト対象】settings.gradle.kts
     * 【テスト内容】settings.gradle.ktsにComposite Build設定（includeBuild("../core")）が含まれること
     * 【期待結果】settings.gradle.ktsが存在し、includeBuildでCoreを参照している
     */
    @Test
    void should_have_composite_build_configuration_in_settings() throws IOException {
        // Arrange
        Path settingsPath = PROJECT_ROOT.resolve("settings.gradle.kts");

        // Assert: ファイルが存在すること
        assertTrue(Files.exists(settingsPath),
                "settings.gradle.kts が存在すること");

        // Assert: Composite Build設定が含まれること
        String content = Files.readString(settingsPath);
        assertTrue(content.contains("includeBuild"),
                "settings.gradle.kts に includeBuild が含まれること（Composite Build設定）");
        assertTrue(content.contains("core"),
                "settings.gradle.kts に core プロジェクトへの参照が含まれること");
    }

    /**
     * 【テスト対象】src/main/java/.../SampleProcessApiApplication.java
     * 【テスト内容】Spring Bootアプリケーションのエントリーポイントが存在すること
     * 【期待結果】SampleProcessApiApplication.javaが存在する
     */
    @Test
    void should_have_spring_boot_application_entry_point() {
        // Arrange
        Path appPath = PROJECT_ROOT.resolve(
                "src/main/java/jp/co/createlink/processapi/SampleProcessApiApplication.java");

        // Assert: ファイルが存在すること
        assertTrue(Files.exists(appPath),
                "SampleProcessApiApplication.java が存在すること: " + appPath.toAbsolutePath());
    }

    /**
     * 【テスト対象】src/main/java/.../ServletInitializer.java
     * 【テスト内容】WAR用ServletInitializerが存在すること
     * 【期待結果】ServletInitializer.javaが存在する（外部TomcatデプロイのためのWAR設定）
     */
    @Test
    void should_have_servlet_initializer_for_war_deployment() {
        // Arrange
        Path servletInitPath = PROJECT_ROOT.resolve(
                "src/main/java/jp/co/createlink/processapi/ServletInitializer.java");

        // Assert: ファイルが存在すること
        assertTrue(Files.exists(servletInitPath),
                "ServletInitializer.java が存在すること（WAR用）: " + servletInitPath.toAbsolutePath());
    }
}
