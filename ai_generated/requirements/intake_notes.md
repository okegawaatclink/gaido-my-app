# 開発依頼内容

SpringでAPIを提供するシステム
・マルチプロジェクト
・Process-APIとして外部提供用のAPI群を提供するプロジェクト
　これはドメイン毎に複数でき上る
・coreとして社内の各システムと接続し、データを収集し、Process-APIに提供するプロジェクト
・github copilotで　Process-APIおよび接続先外部システムの情報（WSDLやopenapiなど）を渡すと、
  ソースコードを自動で作成してくれるVibeCoding前提のシステム
・デプロイ先はK8Sを想定してるのでwarだけ出来上がればOK
・swagger-uiでprocess-apiのSPECを提供したい
