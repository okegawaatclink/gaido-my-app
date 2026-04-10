# 要件ファイル

## ドキュメント一覧

| ドキュメント | 内容 |
|---|---|
| [architecture.md](architecture.md) | システム構成図 |
| [file_structure.md](file_structure.md) | ディレクトリ構成 |
| [api.md](api.md) | WebAPI一覧 |
| [devops.md](devops.md) | デプロイ構造・コマンド |
| [others.md](others.md) | その他（VibeCoding・Copilot設定） |

## 確定要件

### 機能要件

1. **マルチプロジェクト構成のSpring Boot APIテンプレートシステム**
   - Process-API: ドメイン毎の外部提供用API群テンプレート（初期対象: 回線管理、端末管理）
   - Core: 社内システム接続・データ収集ライブラリ（Process-APIにJAR依存で提供）
   - VibeCoding前提: GitHub Copilotでソースコード自動生成するための雛形

2. **Core ライブラリ**
   - 社内SOAP/RESTシステムへの接続テンプレート
   - JAX-WS (wsimport)によるSOAPクライアント生成サンプル
   - Spring WebClientによるREST接続サンプル（テンプレートのみ、サンプルには含まない）
   - ダミーWSDL/OpenAPIファイルの配置
   - 単一モジュール構成
   - Gradle composite buildによるProcess-APIからのソース参照

3. **Process-API テンプレート**
   - Core経由でデータ取得するサンプルAPIエンドポイント
   - springdoc-openapiによるSwagger UI提供
   - Spring Actuatorによるヘルスチェック・メトリクス
   - @ControllerAdviceによる共通エラーハンドリング
   - JUnit5 + MockMvcのテストテンプレート

4. **VibeCoding支援**
   - `.github/copilot-instructions.md` の配置
   - WSDL/OpenAPIファイルのリポジトリ内配置ディレクトリ
   - 新規ドメイン追加手順書（README.md / CONTRIBUTING.md）

### 非機能要件

1. **デプロイ**: K8S想定、WARファイル出力（Tomcat）
2. **ログ**: SLF4J + Logback
3. **構成管理**: application-{env}.yml によるプロファイル分離
4. **監視**: Spring Actuator（health, metrics, info）
5. **セキュリティ**: 内部ネットワーク利用のため認証なし
6. **配布形態**: 社内利用のみ（OSSライセンス制約は緩い）

### 技術選定

| カテゴリ | 選定技術 | 選定理由 | 却下した代替案 |
|---------|---------|---------|---------------|
| 言語 | Java 21 (LTS) | Virtual Threads、Record等の最新機能利用可能 | Java 17, Kotlin |
| フレームワーク | Spring Boot 3.4.x | 最新安定版、Java 21完全サポート | — |
| ビルドツール | Gradle (Kotlin DSL) | マルチプロジェクトに強い、柔軟な設定 | Maven |
| サーブレットコンテナ | Tomcat | Spring Bootデフォルト、実績豊富 | Jetty |
| SOAPクライアント | JAX-WS (wsimport) | 標準仕様、安定 | Spring WS, Apache CXF |
| RESTクライアント | Spring WebClient | リアクティブ対応、モダン | RestClient, OpenAPI Generator |
| API仕様 | springdoc-openapi | Spring Boot統合、アノテーションから自動生成 | 静的Swagger UI |
| ログ | SLF4J + Logback | Spring Bootデフォルト | Log4j2 |
| テスト | JUnit5 + MockMvc | Spring Boot標準 | — |

### スコープ

- **今回のGAiDoで作成するもの**: テンプレート・雛形のみ
- **VibeCodingで後から作成するもの**: 各ドメインの具体的なAPI実装、DB接続、認証、CI/CDパイプライン
- **作成しないもの（スコープ外）**: DB設計、認証・認可、CI/CDパイプライン、具体的なドメインロジック

## 開発プロセス設定

- コードレビュー: ユーザーレビュー
- 画面設計: UI無しのためスキップ

## 専門家分析

### PO分析
- MVP範囲: テンプレート・雛形のみ。具体的なドメイン実装はVibeCodingに委任
- 価値: 開発者がProcess-APIドメインを迅速に立ち上げるための標準化されたスタートポイント
- YAGNI適用: DB、認証、CI/CDは現時点では不要（VibeCoding時に追加）

### Architect分析
- マルチリポ構成: Core(1リポ) + Process-API(ドメイン毎にリポ)
- Gradle composite buildによりローカル開発時にCoreソースを直接参照
- WARパッケージング: K8Sデプロイ用にTomcatベースのWAR生成
- SPEC配置: リポジトリ内にWSDL/OpenAPIを配置し、VibeCoding時にCopilotが参照

### QA分析
- テストテンプレート: JUnit5 + MockMvcのサンプルテストを雛形に含む
- テスト戦略: 雛形としてController層のテストパターンを提示
- 品質基準: VibeCoding時の参考となるテストの書き方を示す

### Security分析
- 内部ネットワーク限定: 認証なしで問題なし
- K8S NetworkPolicyでのアクセス制限を推奨（devops.mdに記載）
- OWASP Top 10のうち、入力バリデーション（Injection）のテンプレートを含める
