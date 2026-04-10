/*
 * settings.gradle.kts
 *
 * Process-APIテンプレートのGradleプロジェクト設定。
 * Composite Buildを使ってCoreライブラリをソースレベルで参照する。
 *
 * ■ Composite Buildの選定理由
 *   - Coreライブラリをmavenローカルに公開せずにソースレベルで参照できる
 *   - Coreに変更を加えた際に即座にProcess-API側でも反映される
 *   - K8Sデプロイ時はbootWarタスクがCoreを含むFatWARを生成する
 *
 * ■ 新規ドメインのProcess-APIを作成する場合
 *   - このファイルをコピーしてrootProject.nameを変更する
 *   - includeBuildのパスはCoreリポジトリの相対パス（../core）を維持する
 */

// プロジェクト名（WARファイル名に使われる: process-api-template-*.war）
rootProject.name = "process-api-template"

// ============================================================
// Composite Build設定
// ============================================================
// Coreライブラリをソースレベルで参照する。
// パスは相対パスで指定するため、output_system/直下に両プロジェクトが
// 並ぶディレクトリ構成を前提としている。
//
// 参考: https://docs.gradle.org/current/userguide/composite_builds.html
// ============================================================
includeBuild("../core")
