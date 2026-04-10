# WebAPI一覧

## Process-API サンプルエンドポイント

テンプレートに含めるサンプルAPIエンドポイント。Core経由で社内システムからデータを取得する例を示す。

| メソッド | パス | 説明 |
|---------|------|------|
| GET | `/api/v1/sample` | Core経由でサンプルデータを取得 |
| GET | `/api/v1/sample/{id}` | Core経由で特定のサンプルデータを取得 |

## 管理エンドポイント（Spring Actuator）

| メソッド | パス | 説明 |
|---------|------|------|
| GET | `/actuator/health` | ヘルスチェック（K8S liveness/readiness probe用） |
| GET | `/actuator/info` | アプリケーション情報 |
| GET | `/actuator/metrics` | メトリクス情報 |

## Swagger UI

| パス | 説明 |
|------|------|
| `/swagger-ui.html` | Swagger UI（springdoc-openapi自動生成） |
| `/v3/api-docs` | OpenAPI 3.0 JSON |
| `/v3/api-docs.yaml` | OpenAPI 3.0 YAML |

## OpenAPI定義（サンプル）

```yaml
openapi: "3.0.3"
info:
  title: "Process-API Template"
  description: "Process-API テンプレートのサンプルAPI定義"
  version: "1.0.0"
servers:
  - url: "http://localhost:8080"
    description: "ローカル開発環境"
paths:
  /api/v1/sample:
    get:
      summary: "サンプルデータ一覧取得"
      description: "Core経由で社内システムからサンプルデータを取得する"
      operationId: "getSampleList"
      tags:
        - "Sample"
      responses:
        "200":
          description: "正常レスポンス"
          content:
            application/json:
              schema:
                type: "array"
                items:
                  $ref: "#/components/schemas/SampleResponse"
        "500":
          description: "サーバーエラー"
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/ErrorResponse"
  /api/v1/sample/{id}:
    get:
      summary: "サンプルデータ取得"
      description: "Core経由で特定のサンプルデータを取得する"
      operationId: "getSampleById"
      tags:
        - "Sample"
      parameters:
        - name: "id"
          in: "path"
          required: true
          schema:
            type: "string"
      responses:
        "200":
          description: "正常レスポンス"
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/SampleResponse"
        "404":
          description: "データが見つからない"
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/ErrorResponse"
        "500":
          description: "サーバーエラー"
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/ErrorResponse"
components:
  schemas:
    SampleResponse:
      type: "object"
      properties:
        id:
          type: "string"
          description: "サンプルID"
        name:
          type: "string"
          description: "サンプル名"
        data:
          type: "object"
          description: "Core経由で取得したデータ"
    ErrorResponse:
      type: "object"
      properties:
        timestamp:
          type: "string"
          format: "date-time"
        status:
          type: "integer"
        error:
          type: "string"
        message:
          type: "string"
        path:
          type: "string"
```

## エラーレスポンス形式

@ControllerAdviceによる共通エラーハンドリング。レスポンス形式:

```json
{
  "timestamp": "2026-04-10T10:00:00.000+09:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "社内システムへの接続に失敗しました",
  "path": "/api/v1/sample"
}
```
