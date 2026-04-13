package jp.co.createlink.core.document;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Coreライブラリのドキュメント・設定ファイル存在確認テスト。
 *
 * <p>テストケースIssue #36: PBI1.3 Coreドキュメント・Copilot設定で新規接続先追加手順を把握できる</p>
 *
 * <p>以下のファイルが存在し、必要な内容が含まれていることを検証する:</p>
 * <ul>
 *   <li>README.md: プロジェクト概要・ビルド手順・ディレクトリ構成</li>
 *   <li>CONTRIBUTING.md: 新規接続先追加手順</li>
 *   <li>.github/copilot-instructions.md: 技術スタック・命名規則・コーディング規約</li>
 * </ul>
 */
class CoreDocumentationTest {

    /** Coreプロジェクトのルートディレクトリ（プロジェクトルートからの相対パス）*/
    private static final Path CORE_ROOT = Paths.get("").toAbsolutePath();

    /**
     * 【テスト対象】README.md
     * 【テスト内容】README.mdが存在し、プロジェクト概要・ビルド手順・ディレクトリ構成が含まれること
     * 【期待結果】README.mdが存在し、必要なセクションのキーワードが含まれる
     */
    @Test
    void should_have_readme_with_required_sections() throws IOException {
        // Arrange
        Path readmePath = CORE_ROOT.resolve("README.md");

        // Assert: ファイルが存在すること
        assertTrue(Files.exists(readmePath),
                "README.md が存在すること: " + readmePath.toAbsolutePath());

        // Assert: 必要なコンテンツが含まれること
        String content = Files.readString(readmePath);
        assertTrue(content.contains("Core") || content.contains("core"),
                "README.md にプロジェクト名（Core）が含まれること");
        // ビルド手順のキーワード確認
        assertTrue(content.contains("gradlew") || content.contains("Gradle") || content.contains("ビルド"),
                "README.md にビルド手順が含まれること");
    }

    /**
     * 【テスト対象】CONTRIBUTING.md
     * 【テスト内容】CONTRIBUTING.mdが存在し、新規接続先追加手順が含まれること
     * 【期待結果】CONTRIBUTING.mdが存在し、SOAP/REST接続先追加に関する記述がある
     */
    @Test
    void should_have_contributing_with_new_connection_guide() throws IOException {
        // Arrange
        Path contributingPath = CORE_ROOT.resolve("CONTRIBUTING.md");

        // Assert: ファイルが存在すること
        assertTrue(Files.exists(contributingPath),
                "CONTRIBUTING.md が存在すること: " + contributingPath.toAbsolutePath());

        // Assert: 接続先追加手順が含まれること
        String content = Files.readString(contributingPath);
        assertTrue(content.contains("SOAP") || content.contains("REST") || content.contains("接続"),
                "CONTRIBUTING.md に接続先追加手順（SOAP/REST）が含まれること");
    }

    /**
     * 【テスト対象】.github/copilot-instructions.md
     * 【テスト内容】Copilotインストラクションファイルが存在し、技術スタック・命名規則が含まれること
     * 【期待結果】.github/copilot-instructions.mdが存在し、コーディング規約が含まれる
     */
    @Test
    void should_have_copilot_instructions_with_tech_stack_and_naming_rules() throws IOException {
        // Arrange
        Path copilotInstructionsPath = CORE_ROOT.resolve(".github/copilot-instructions.md");

        // Assert: ファイルが存在すること
        assertTrue(Files.exists(copilotInstructionsPath),
                ".github/copilot-instructions.md が存在すること: " + copilotInstructionsPath.toAbsolutePath());

        // Assert: 技術スタック情報が含まれること
        String content = Files.readString(copilotInstructionsPath);
        assertTrue(content.contains("Java") || content.contains("Spring") || content.contains("技術"),
                ".github/copilot-instructions.md に技術スタック情報が含まれること");
    }

    /**
     * 【テスト対象】specs/wsdl/ ディレクトリ
     * 【テスト内容】ダミーWSDLファイルが specs/wsdl/ に配置されていること
     * 【期待結果】specs/wsdl/ にWSDLファイルが存在する
     */
    @Test
    void should_have_wsdl_file_in_specs_directory() throws IOException {
        // Arrange
        Path wsdlDir = CORE_ROOT.resolve("specs/wsdl");

        // Assert: ディレクトリが存在すること
        assertTrue(Files.exists(wsdlDir) && Files.isDirectory(wsdlDir),
                "specs/wsdl/ ディレクトリが存在すること");

        // Assert: WSDLファイルが少なくとも1つ存在すること
        long wsdlCount = Files.list(wsdlDir)
                .filter(p -> p.toString().endsWith(".wsdl"))
                .count();
        assertTrue(wsdlCount > 0,
                "specs/wsdl/ にWSDLファイルが少なくとも1つ存在すること");
    }

    /**
     * 【テスト対象】specs/openapi/ ディレクトリ
     * 【テスト内容】ダミーOpenAPIファイルが specs/openapi/ に配置されていること
     * 【期待結果】specs/openapi/ にOpenAPIファイルが存在する
     */
    @Test
    void should_have_openapi_file_in_specs_directory() throws IOException {
        // Arrange
        Path openapiDir = CORE_ROOT.resolve("specs/openapi");

        // Assert: ディレクトリが存在すること
        assertTrue(Files.exists(openapiDir) && Files.isDirectory(openapiDir),
                "specs/openapi/ ディレクトリが存在すること");

        // Assert: OpenAPIファイルが少なくとも1つ存在すること
        long yamlCount = Files.list(openapiDir)
                .filter(p -> p.toString().endsWith(".yaml") || p.toString().endsWith(".yml"))
                .count();
        assertTrue(yamlCount > 0,
                "specs/openapi/ にOpenAPIファイル（.yaml/.yml）が少なくとも1つ存在すること");
    }
}
