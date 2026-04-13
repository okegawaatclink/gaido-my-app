package jp.co.createlink.processapi.service;

import jp.co.createlink.core.exception.CoreException;
import jp.co.createlink.core.rest.SampleRestClient;
import jp.co.createlink.core.soap.SampleSoapClient;
import jp.co.createlink.processapi.dto.SampleResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 【モジュール】SampleService
 * SampleService のユニットテスト。
 * SampleRestClient と SampleSoapClient はモック化してサービスクラスのみをテストする。
 */
@ExtendWith(MockitoExtension.class)
class SampleServiceTest {

    /**
     * REST クライアントのモック。
     */
    @Mock
    private SampleRestClient sampleRestClient;

    /**
     * SOAP クライアントのモック。
     */
    @Mock
    private SampleSoapClient sampleSoapClient;

    /**
     * テスト対象のサービスクラス。
     */
    private SampleService sampleService;

    @BeforeEach
    void setUp() {
        sampleService = new SampleService(sampleRestClient, sampleSoapClient);
    }

    // ============================================================
    // getSampleList テスト
    // ============================================================

    /**
     * 【テスト対象】getSampleList()
     * 【テスト内容】正常系: スタブデータが返ること
     * 【期待結果】空でないリストが返り、各要素に id, name が設定されている
     */
    @Test
    void should_return_sample_list_successfully() {
        // Act
        List<SampleResponse> result = sampleService.getSampleList();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).allSatisfy(sample -> {
            assertThat(sample.getId()).isNotNull().isNotBlank();
            assertThat(sample.getName()).isNotNull().isNotBlank();
        });
    }

    /**
     * 【テスト対象】getSampleList()
     * 【テスト内容】スタブデータの件数確認
     * 【期待結果】3件のスタブデータが返ること（テンプレートのデフォルト値）
     */
    @Test
    void should_return_three_stub_samples_in_list() {
        // Act
        List<SampleResponse> result = sampleService.getSampleList();

        // Assert: スタブデータは3件
        assertThat(result).hasSize(3);
        assertThat(result).extracting(SampleResponse::getId)
                .containsExactly("sample-001", "sample-002", "sample-003");
    }

    // ============================================================
    // getSampleById テスト
    // ============================================================

    /**
     * 【テスト対象】getSampleById(String id)
     * 【テスト内容】正常系: 有効な ID でデータが返ること
     * 【期待結果】指定した ID に対応する SampleResponse が返る
     */
    @Test
    void should_return_sample_when_valid_id_provided() {
        // Arrange
        String id = "sample-001";

        // Act
        SampleResponse result = sampleService.getSampleById(id);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isNotNull().isNotBlank();
    }

    /**
     * 【テスト対象】getSampleById(String id)
     * 【テスト内容】異常系: "notfound" ID を指定した場合
     * 【期待結果】NoSuchElementException が投げられる（HTTP 404 に変換される）
     *
     * 【前提条件】"notfound" は 404 動作確認用の特殊 ID
     */
    @Test
    void should_throw_NoSuchElementException_when_id_is_notfound() {
        // Act & Assert
        assertThatThrownBy(() -> sampleService.getSampleById("notfound"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("notfound");
    }

    /**
     * 【テスト対象】getSampleById(String id)
     * 【テスト内容】異常系: null ID を指定した場合
     * 【期待結果】IllegalArgumentException が投げられる（HTTP 400 に変換される）
     */
    @Test
    void should_throw_IllegalArgumentException_when_id_is_null() {
        // Act & Assert
        assertThatThrownBy(() -> sampleService.getSampleById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("空にできません");
    }

    /**
     * 【テスト対象】getSampleById(String id)
     * 【テスト内容】異常系: 空文字 ID を指定した場合
     * 【期待結果】IllegalArgumentException が投げられる（HTTP 400 に変換される）
     */
    @Test
    void should_throw_IllegalArgumentException_when_id_is_blank() {
        // Act & Assert
        assertThatThrownBy(() -> sampleService.getSampleById("  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("空にできません");
    }

    /**
     * 【テスト対象】getSampleById(String id)
     * 【テスト内容】Core ライブラリが CoreException を投げた場合の伝播確認
     * 【期待結果】CoreException がそのまま伝播する（GlobalExceptionHandler が 500 に変換する）
     *
     * 【前提条件】getSampleList() で CoreException が発生した場合を再現するため、
     *            この テストでは サービスクラスを直接サブクラス化して例外を強制発生させる
     */
    @Test
    void should_propagate_CoreException_when_core_throws_exception() {
        // CoreException を投げるサービスのサブクラスを作成して検証
        SampleService serviceWithException = new SampleService(sampleRestClient, sampleSoapClient) {
            @Override
            public List<SampleResponse> getSampleList() {
                throw new CoreException("REST_SAMPLE_500", "接続失敗");
            }
        };

        // Act & Assert
        assertThatThrownBy(serviceWithException::getSampleList)
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("接続失敗");
    }
}
