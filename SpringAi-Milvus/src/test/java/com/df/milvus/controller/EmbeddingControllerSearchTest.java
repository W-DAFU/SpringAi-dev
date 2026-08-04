package com.df.milvus.controller;

import io.milvus.response.QueryResultsWrapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EmbeddingControllerSearchTest {

    @Test
    void formatSearchRowReturnsFrontendSafeFields() {
        EmbeddingController controller = new EmbeddingController(null, null, null);
        QueryResultsWrapper.RowRecord rowRecord = new QueryResultsWrapper.RowRecord();
        rowRecord.put("id", 1001L);
        rowRecord.put("vector_text", "我是岗位信息");
        rowRecord.put("score", 0.92F);

        Map<String, Object> result = controller.formatSearchRow(rowRecord);

        assertThat(result)
                .containsEntry("id", 1001L)
                .containsEntry("text", "我是岗位信息")
                .containsEntry("score", 0.92F);
    }
}
