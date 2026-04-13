package jp.co.createlink.processapi.service;

import jp.co.createlink.core.exception.CoreException;
import jp.co.createlink.core.rest.SampleRestClient;
import jp.co.createlink.core.soap.SampleSoapClient;
import jp.co.createlink.processapi.dto.SampleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * SampleService
 *
 * <p>サンプルAPIのサービスクラス。Controller から呼び出され、Core ライブラリ経由で
 * 社内システムからデータを取得する。</p>
 *
 * <h2>レイヤー責務</h2>
 * <ul>
 *   <li><b>Controller</b>: HTTPリクエスト/レスポンス処理、バリデーション</li>
 *   <li><b>Service（このクラス）</b>: ビジネスロジック、Coreクライアントの呼び出し</li>
 *   <li><b>Core</b>: 社内システムへの接続クライアント（SOAP/REST）</li>
 * </ul>
 *
 * <h2>新規ドメインAPI実装ガイド（VibeCoding用）</h2>
 * <ol>
 *   <li>このクラスをコピーして接続先システムに合わせたサービスクラスを作成する</li>
 *   <li>DI で Core ライブラリのクライアントを注入する（コンストラクタインジェクション推奨）</li>
 *   <li>ビジネスロジックに応じてメソッドを実装する</li>
 *   <li>Core クライアントが投げる {@link CoreException} は原則そのまま伝播させ、
 *       GlobalExceptionHandler で HTTP レスポンスに変換する</li>
 *   <li>データが存在しない場合は {@link NoSuchElementException} を投げ、
 *       GlobalExceptionHandler が 404 に変換する</li>
 * </ol>
 *
 * @see SampleRestClient REST接続クライアント（Coreライブラリ）
 * @see SampleSoapClient SOAP接続クライアント（Coreライブラリ）
 */
@Service  // Spring DI コンテナに登録する。Controller から @Autowired で注入可能。
public class SampleService {

    private static final Logger logger = LoggerFactory.getLogger(SampleService.class);

    /**
     * REST接続クライアント（Coreライブラリ）。
     * Core 経由でサンプルシステム（REST API）に接続する。
     *
     * <p>▼ 新規ドメイン実装時は、接続先システムに合わせた REST クライアントに変更すること。</p>
     */
    private final SampleRestClient sampleRestClient;

    /**
     * SOAP接続クライアント（Coreライブラリ）。
     * Core 経由でサンプルシステム（SOAP API）に接続する。
     *
     * <p>▼ 新規ドメイン実装時は、接続先システムに合わせた SOAP クライアントに変更すること。
     * SOAP 不要の場合はフィールドとコンストラクタ引数ごと削除する。</p>
     */
    private final SampleSoapClient sampleSoapClient;

    // ============================================================
    // コンストラクタ
    // ============================================================

    /**
     * コンストラクタ（コンストラクタインジェクション）。
     *
     * <p>Spring の DI コンテナが Core ライブラリの Bean を自動的に注入する。
     * {@code @Autowired} アノテーションは Spring 4.3 以降は単一コンストラクタの場合は省略可能。</p>
     *
     * <p>コンストラクタインジェクションを採用する理由:</p>
     * <ul>
     *   <li>フィールドが {@code final} にでき、不変性が保証される</li>
     *   <li>テスト時にモックを容易に注入できる</li>
     *   <li>必須依存関係が明確になる</li>
     * </ul>
     *
     * @param sampleRestClient REST接続クライアント（CoreライブラリのBean）
     * @param sampleSoapClient SOAP接続クライアント（CoreライブラリのBean）
     */
    public SampleService(SampleRestClient sampleRestClient, SampleSoapClient sampleSoapClient) {
        this.sampleRestClient = sampleRestClient;
        this.sampleSoapClient = sampleSoapClient;
    }

    // ============================================================
    // 公開メソッド
    // ============================================================

    /**
     * サンプルデータ一覧取得。
     *
     * <p>Core 経由でサンプルシステムからデータを取得し、レスポンスDTOのリストで返す。</p>
     *
     * <h3>実装パターン（テンプレート）</h3>
     * <p>このメソッドは、Core クライアントを呼び出してデータを取得し、
     * Process-API のレスポンスDTOに変換するパターンを示す。</p>
     *
     * <p>現在はスタブデータを返しているが、新規ドメイン実装時には
     * Core クライアントの呼び出しに置き換えること。</p>
     *
     * <h3>Core クライアント呼び出しパターン（VibeCoding向けコメント）</h3>
     * <pre>
     * // REST クライアント経由でデータ取得（非同期→同期パターン）
     * Map&lt;String, Object&gt; restData = sampleRestClient.getSampleData("list");
     *
     * // SOAP クライアント経由でデータ取得
     * GetSampleDataResponse soapResponse = sampleSoapClient.getSampleData("all");
     * </pre>
     *
     * @return サンプルデータ一覧（{@link SampleResponse} のリスト）
     * @throws CoreException Core ライブラリが社内システムへの接続に失敗した場合
     */
    public List<SampleResponse> getSampleList() {
        logger.info("サンプルデータ一覧取得を開始します");

        try {
            // ▼▼▼ ここをドメイン固有のロジックに変更すること ▼▼▼
            //
            // パターン1: Core の REST クライアントでデータ取得
            // Map<String, Object> restData = sampleRestClient.getSampleData("list");
            //
            // パターン2: Core の SOAP クライアントでデータ取得
            // GetSampleDataResponse soapResponse = sampleSoapClient.getSampleData("all");
            //
            // 現在はスタブデータを返す（接続先未確定の場合のテンプレートパターン）
            // ▼▼▼ ここまで ▼▼▼

            List<SampleResponse> samples = buildStubSampleList();

            logger.info("サンプルデータ一覧取得が完了しました。件数={}", samples.size());
            return samples;

        } catch (CoreException e) {
            // CoreException はそのまま再スローする。
            // GlobalExceptionHandler が 500 Internal Server Error に変換する。
            logger.error("Core経由でのデータ取得に失敗しました: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 特定サンプルデータ取得。
     *
     * <p>指定された ID に対応するサンプルデータを Core 経由で取得して返す。</p>
     *
     * <p>現在はスタブデータを返しているが、新規ドメイン実装時には
     * Core クライアントの呼び出しに置き換えること。</p>
     *
     * <h3>Core クライアント呼び出しパターン（VibeCoding向けコメント）</h3>
     * <pre>
     * // REST クライアント経由でデータ取得
     * Map&lt;String, Object&gt; restData = sampleRestClient.getSampleData(id);
     * if (restData == null) {
     *     throw new NoSuchElementException("サンプルデータが見つかりません: id=" + id);
     * }
     *
     * // SOAP クライアント経由でデータ取得
     * GetSampleDataResponse soapResponse = sampleSoapClient.getSampleData(id);
     * </pre>
     *
     * @param id 取得対象のサンプルID
     * @return 指定IDのサンプルデータ
     * @throws NoSuchElementException 指定IDのデータが存在しない場合（HTTP 404 に変換される）
     * @throws CoreException          Core ライブラリが社内システムへの接続に失敗した場合（HTTP 500 に変換される）
     * @throws IllegalArgumentException IDが不正な場合（HTTP 400 に変換される）
     */
    public SampleResponse getSampleById(String id) {
        logger.info("サンプルデータ取得を開始します。id={}", id);

        // ▼ 入力値バリデーション（必要に応じてドメイン固有のルールに変更すること）
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("IDは空にできません");
        }

        try {
            // ▼▼▼ ここをドメイン固有のロジックに変更すること ▼▼▼
            //
            // パターン1: Core の REST クライアントでデータ取得
            // Map<String, Object> restData = sampleRestClient.getSampleData(id);
            // if (restData == null) {
            //     throw new NoSuchElementException("サンプルデータが見つかりません: id=" + id);
            // }
            // return convertToSampleResponse(id, restData);
            //
            // パターン2: Core の SOAP クライアントでデータ取得
            // GetSampleDataResponse soapResponse = sampleSoapClient.getSampleData(id);
            // return convertSoapResponseToDto(soapResponse);
            //
            // 現在はスタブデータを返す（接続先未確定の場合のテンプレートパターン）
            // ▼▼▼ ここまで ▼▼▼

            SampleResponse sample = buildStubSampleById(id);

            logger.info("サンプルデータ取得が完了しました。id={}", id);
            return sample;

        } catch (NoSuchElementException | IllegalArgumentException e) {
            // バリデーションエラー・存在しないデータはそのまま再スローする。
            // GlobalExceptionHandler が 404 / 400 に変換する。
            throw e;
        } catch (CoreException e) {
            // CoreException はそのまま再スローする。
            // GlobalExceptionHandler が 500 Internal Server Error に変換する。
            logger.error("Core経由でのデータ取得に失敗しました: id={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }

    // ============================================================
    // プライベートメソッド（スタブデータ生成）
    // ============================================================

    /**
     * スタブの一覧データを生成する（テンプレート用）。
     *
     * <p>接続先システムへの実際の接続実装が完了するまでの仮データ。
     * 新規ドメイン実装時はこのメソッドを削除し、Core クライアント呼び出しに置き換えること。</p>
     *
     * @return スタブのサンプルデータ一覧
     */
    private List<SampleResponse> buildStubSampleList() {
        List<SampleResponse> list = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            Map<String, Object> stubData = new HashMap<>();
            // ▼ スタブデータ: 実際の接続先システムのレスポンス形式に合わせて変更すること
            stubData.put("source", "stub");
            stubData.put("description", "Core経由で取得されるデータ（スタブ）");

            list.add(new SampleResponse(
                    "sample-00" + i,
                    "サンプルデータ " + i,
                    stubData
            ));
        }

        return list;
    }

    /**
     * スタブの特定IDデータを生成する（テンプレート用）。
     *
     * <p>接続先システムへの実際の接続実装が完了するまでの仮データ。
     * 新規ドメイン実装時はこのメソッドを削除し、Core クライアント呼び出しに置き換えること。</p>
     *
     * @param id 取得対象のID
     * @return スタブのサンプルデータ
     * @throws NoSuchElementException IDが "notfound" の場合（404 動作確認用）
     */
    private SampleResponse buildStubSampleById(String id) {
        // ▼ "notfound" を ID として渡すと 404 レスポンスを確認できる（デモ用）
        if ("notfound".equals(id)) {
            throw new NoSuchElementException("サンプルデータが見つかりません: id=" + id);
        }

        Map<String, Object> stubData = new HashMap<>();
        // ▼ スタブデータ: 実際の接続先システムのレスポンス形式に合わせて変更すること
        stubData.put("source", "stub");
        stubData.put("requestedId", id);
        stubData.put("description", "Core経由で取得されるデータ（スタブ）");

        return new SampleResponse(id, "サンプルデータ " + id, stubData);
    }
}
