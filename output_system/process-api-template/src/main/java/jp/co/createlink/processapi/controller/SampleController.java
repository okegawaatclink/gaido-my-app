package jp.co.createlink.processapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jp.co.createlink.processapi.dto.SampleResponse;
import jp.co.createlink.processapi.exception.ErrorResponse;
import jp.co.createlink.processapi.service.SampleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * SampleController
 *
 * <p>サンプルAPIエンドポイントのコントローラークラス。
 * Core ライブラリ経由でデータを取得するパターンを示すテンプレート。</p>
 *
 * <h2>レイヤー責務</h2>
 * <ul>
 *   <li><b>Controller（このクラス）</b>: HTTPリクエスト/レスポンス処理、バリデーション</li>
 *   <li><b>Service</b>: ビジネスロジック、Coreクライアントの呼び出し</li>
 *   <li><b>Core</b>: 社内システムへの接続クライアント（SOAP/REST）</li>
 * </ul>
 *
 * <h2>新規ドメインAPI実装ガイド（VibeCoding用）</h2>
 * <ol>
 *   <li>このクラスをコピーして接続先システムに合わせたコントローラーを作成する</li>
 *   <li>{@code @RequestMapping} のパスをドメインに合わせて変更する</li>
 *   <li>{@code @Operation} 等のアノテーションを更新してSwagger UIに表示される説明を充実させる</li>
 *   <li>サービスクラスを DI で注入して呼び出す</li>
 * </ol>
 *
 * @see SampleService サービスクラス（Core経由のデータ取得ロジック）
 */
@RestController
@RequestMapping("/api/v1/sample")
@Tag(name = "Sample", description = "サンプルAPIエンドポイント（Core経由データ取得パターンのテンプレート）")
public class SampleController {

    private static final Logger logger = LoggerFactory.getLogger(SampleController.class);

    /**
     * サービスクラス（コンストラクタインジェクション）。
     * Spring の DI コンテナが自動的に Bean を注入する。
     */
    private final SampleService sampleService;

    // ============================================================
    // コンストラクタ
    // ============================================================

    /**
     * コンストラクタ（コンストラクタインジェクション）。
     *
     * <p>Spring の DI コンテナが {@link SampleService} を自動的に注入する。
     * {@code @Autowired} は単一コンストラクタの場合は省略可能（Spring 4.3以降）。</p>
     *
     * @param sampleService サービスクラス
     */
    public SampleController(SampleService sampleService) {
        this.sampleService = sampleService;
    }

    // ============================================================
    // エンドポイント
    // ============================================================

    /**
     * サンプルデータ一覧取得。
     *
     * <p>GET {@code /api/v1/sample}</p>
     *
     * <p>Core 経由でサンプルシステムからデータを取得し、一覧として返す。
     * エラーが発生した場合は {@link jp.co.createlink.processapi.exception.GlobalExceptionHandler}
     * が統一フォーマットのエラーレスポンスに変換する。</p>
     *
     * @return サンプルデータ一覧（200 OK）
     */
    @GetMapping
    @Operation(
            summary = "サンプルデータ一覧取得",
            description = "Core経由で社内システムからサンプルデータを取得する。"
                    + "Controller → Service → Core のレイヤー構成パターンのテンプレート。"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "正常レスポンス",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SampleResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "サーバーエラー（Core経由の接続失敗等）",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<List<SampleResponse>> getSampleList() {
        logger.info("GET /api/v1/sample が呼び出されました");

        // ▼ Service を呼び出す。例外は GlobalExceptionHandler が処理する。
        List<SampleResponse> samples = sampleService.getSampleList();

        logger.info("GET /api/v1/sample が正常完了しました。件数={}", samples.size());
        return ResponseEntity.ok(samples);
    }

    /**
     * 特定サンプルデータ取得。
     *
     * <p>GET {@code /api/v1/sample/{id}}</p>
     *
     * <p>指定された ID のサンプルデータを Core 経由で取得して返す。
     * データが存在しない場合は 404 Not Found を返す。</p>
     *
     * <p>▼ 動作確認用: ID に "notfound" を指定すると 404 レスポンスを確認できる。</p>
     *
     * @param id 取得対象のサンプルID（パスパラメータ）
     * @return 指定IDのサンプルデータ（200 OK）
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "サンプルデータ取得",
            description = "指定された ID のサンプルデータを Core 経由で取得する。"
                    + "存在しない ID を指定すると 404 Not Found を返す（動作確認用: id='notfound'）。"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "正常レスポンス",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SampleResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "リクエストパラメータ不正",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "指定IDのデータが見つからない",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "サーバーエラー（Core経由の接続失敗等）",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<SampleResponse> getSampleById(
            @Parameter(description = "サンプルID（動作確認用: 'notfound' で 404 を確認できる）", required = true)
            @PathVariable String id
    ) {
        logger.info("GET /api/v1/sample/{} が呼び出されました", id);

        // ▼ Service を呼び出す。例外は GlobalExceptionHandler が処理する。
        SampleResponse sample = sampleService.getSampleById(id);

        logger.info("GET /api/v1/sample/{} が正常完了しました", id);
        return ResponseEntity.ok(sample);
    }
}
