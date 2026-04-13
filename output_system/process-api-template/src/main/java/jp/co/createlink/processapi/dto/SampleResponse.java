package jp.co.createlink.processapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * SampleResponse
 *
 * <p>サンプルAPIのレスポンスDTO。</p>
 *
 * <p>このクラスはテンプレートとして提供される。新規ドメインAPI実装時は、
 * このクラスを参考にして接続先システムのデータに合わせたDTOを作成すること。</p>
 *
 * <h2>実装ガイド（VibeCoding用）</h2>
 * <ol>
 *   <li>このクラスをコピーして接続先システムに合わせたフィールドを定義する</li>
 *   <li>{@code @Schema} アノテーションでフィールドの説明を記述する</li>
 *   <li>Jacksonのアノテーション（{@code @JsonProperty}等）が必要な場合は追加する</li>
 * </ol>
 */
@Schema(description = "サンプルデータレスポンス")
public class SampleResponse {

    /**
     * サンプルID。
     * Core経由で取得したデータの識別子。
     */
    @Schema(description = "サンプルID", example = "sample-001")
    private String id;

    /**
     * サンプル名。
     * Core経由で取得したデータの名称。
     */
    @Schema(description = "サンプル名", example = "サンプルデータ 1")
    private String name;

    /**
     * Core経由で取得した追加データ。
     *
     * <p>ここでは汎用的な Map 型としているが、新規実装時は
     * 接続先システムのデータ構造に合わせた専用DTOクラスに変更すること。</p>
     *
     * <p>▼ 変更例: SOAP の場合</p>
     * <pre>
     * private SoapResponseDto soapData;
     * </pre>
     *
     * <p>▼ 変更例: REST の場合</p>
     * <pre>
     * private RestResponseDto restData;
     * </pre>
     */
    @Schema(description = "Core経由で取得した追加データ（新規実装時は適切なDTOに変更すること）")
    private Map<String, Object> data;

    // ============================================================
    // コンストラクタ
    // ============================================================

    /**
     * デフォルトコンストラクタ。
     * Jackson のデシリアライズ（JSON → Java オブジェクト変換）に必要。
     */
    public SampleResponse() {
    }

    /**
     * 全フィールドを設定するコンストラクタ。
     *
     * @param id   サンプルID
     * @param name サンプル名
     * @param data Core経由で取得した追加データ
     */
    public SampleResponse(String id, String name, Map<String, Object> data) {
        this.id = id;
        this.name = name;
        this.data = data;
    }

    // ============================================================
    // ゲッター・セッター
    // ============================================================

    /**
     * サンプルIDを返す。
     *
     * @return サンプルID
     */
    public String getId() {
        return id;
    }

    /**
     * サンプルIDを設定する。
     *
     * @param id サンプルID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * サンプル名を返す。
     *
     * @return サンプル名
     */
    public String getName() {
        return name;
    }

    /**
     * サンプル名を設定する。
     *
     * @param name サンプル名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Core経由で取得した追加データを返す。
     *
     * @return 追加データ
     */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * Core経由で取得した追加データを設定する。
     *
     * @param data 追加データ
     */
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
