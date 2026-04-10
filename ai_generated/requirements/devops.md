# デプロイ構造・コマンド

## デプロイターゲット

- **K8S (Kubernetes)**: WARファイルをDockerイメージに含めてデプロイ
- **サーブレットコンテナ**: Tomcat（Spring Boot組み込み）

## ビルドコマンド

### Core ライブラリ

```bash
cd output_system/core
./gradlew build
```

生成物: `build/libs/core-*.jar`

### Process-API（WARビルド）

```bash
cd output_system/process-api-template
./gradlew bootWar
```

生成物: `build/libs/process-api-template-*.war`

## ローカル開発

### 起動コマンド

```bash
# Core単体テスト
cd output_system/core
./gradlew test

# Process-API起動（組み込みTomcat）
cd output_system/process-api-template
./gradlew bootRun --args='--spring.profiles.active=local'
```

### アクセスURL（ローカル）

| URL | 説明 |
|-----|------|
| http://localhost:8080/swagger-ui.html | Swagger UI |
| http://localhost:8080/api/v1/sample | サンプルAPI |
| http://localhost:8080/actuator/health | ヘルスチェック |

## プロファイル構成

| プロファイル | 用途 | 設定ファイル |
|-------------|------|------------|
| local | ローカル開発 | application-local.yml |
| dev | 開発環境 | application-dev.yml |
| prod | 本番環境 | application-prod.yml |

## K8Sデプロイ（参考）

雛形にはK8Sマニフェストは含まないが、以下の設計を推奨:

### ヘルスチェック設定

```yaml
# K8S Deployment（参考）
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 10
  periodSeconds: 5
```

### セキュリティ推奨事項

- K8S NetworkPolicyでProcess-API間のアクセスを制限
- Actuatorエンドポイントは内部ポート（management.server.port）で分離推奨
- Secretsは環境変数またはK8S Secretで注入
