package com.df.milvus;

import com.df.milvus.vector.BizVectorCollection;
import com.df.milvus.vector.BizVectorStoreProperties;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.milvus.autoconfigure.MilvusVectorStoreProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SpringAiMilvusApplicationTests {

    @Autowired
    private MilvusVectorStoreProperties milvusVectorStoreProperties;
    @Autowired
    private BizVectorStoreProperties bizVectorStoreProperties;

    @Test
    void contextLoads() {
    }

    @Test
    void milvusVectorStorePropertiesUseDefaultsWhenDefaultCollectionConfigDisabled() {
        assertThat(milvusVectorStoreProperties.getDatabaseName()).isEqualTo("default");
        assertThat(milvusVectorStoreProperties.getCollectionName()).isEqualTo("vector_store");
        assertThat(milvusVectorStoreProperties.getEmbeddingDimension()).isEqualTo(1536);
        assertThat(milvusVectorStoreProperties.isInitializeSchema()).isFalse();
    }

    @Test
    void bizVectorStorePropertiesBindFromConfiguration() {
        assertThat(bizVectorStoreProperties.getDatabaseName()).isEqualTo("default");
        assertThat(bizVectorStoreProperties.getEmbeddingDimension()).isEqualTo(1024);
        assertThat(bizVectorStoreProperties.isInitializeSchema()).isTrue();
        assertThat(bizVectorStoreProperties.collectionName(BizVectorCollection.JOB)).isEqualTo("job_info");
        assertThat(bizVectorStoreProperties.collectionName(BizVectorCollection.RESUME)).isEqualTo("resume_info");
        assertThat(bizVectorStoreProperties.collectionName(BizVectorCollection.COMPANY)).isEqualTo("company_info");
    }

}
