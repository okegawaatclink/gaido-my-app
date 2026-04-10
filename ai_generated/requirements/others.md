# その他

## VibeCoding設計方針

### GitHub Copilot活用フロー

1. 開発者がProcess-APIテンプレートをフォーク/コピーして新規ドメインリポジトリを作成
2. 接続先の社内システムのWSDL/OpenAPIをCore `specs/` ディレクトリに配置
3. `.github/copilot-instructions.md` にドメイン固有の指示を追記
4. GitHub CopilotがSPECを参照し、Controller/Service/Clientコードを自動生成
5. テストテンプレートを参考にテストコードを作成

### Copilotインストラクション方針

`.github/copilot-instructions.md` に以下を含める:

- プロジェクトの技術スタック情報（Java 21, Spring Boot 3.4.x, Gradle）
- パッケージ命名規則（jp.co.createlink.processapi.*）
- コーディング規約（@RestController, @Service等のレイヤー分離）
- Core ライブラリの使い方（依存性注入パターン）
- `specs/` ディレクトリのWSDL/OpenAPIファイルの参照方法
- エラーハンドリングパターン（@ControllerAdvice使用）
- テストの書き方（MockMvcパターン）

### 新規ドメイン追加手順（CONTRIBUTING.md）

1. process-api-templateをコピーして新規リポジトリ作成
2. パッケージ名をドメインに合わせてリネーム
3. `settings.gradle.kts` のCoreパスを調整
4. 接続先SPECを `specs/` に配置
5. Copilotインストラクションにドメイン情報を追記
6. VibeCodingでAPI実装

## テスト戦略

### テンプレートに含めるテスト

| テスト種別 | 内容 | フレームワーク |
|-----------|------|--------------|
| Controller単体テスト | MockMvcによるAPI呼び出しテスト | JUnit5 + MockMvc |
| Service単体テスト | Coreライブラリのモック化テスト | JUnit5 + Mockito |
| Core単体テスト | SOAP/REST接続のモックテスト | JUnit5 + WireMock |

### テストの目的

- VibeCoding時のテスト作成の参考パターンを提供
- 新規ドメイン追加時のテスト品質基準を示す

## マルチリポジトリ管理

### Gradle Composite Build設定

Process-API側の `settings.gradle.kts`:

```kotlin
// Coreライブラリをローカルソース参照
includeBuild("../core")
```

### 開発フロー

1. CoreとProcess-APIを同じ親ディレクトリにクローン
2. Process-APIからCoreをcomposite buildで参照
3. Coreの変更がProcess-APIに即座に反映される
