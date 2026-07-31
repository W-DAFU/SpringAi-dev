package com.df.milvus;

import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.milvus.autoconfigure.MilvusVectorStoreProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SpringAiMilvusApplicationTests {

    @Autowired
    private MilvusVectorStoreProperties milvusVectorStoreProperties;

    @Test
    void contextLoads() {
    }

    @Test
    void milvusVectorStorePropertiesBindFromConfiguration() {
        assertThat(milvusVectorStoreProperties.getDatabaseName()).isEqualTo("default");
        assertThat(milvusVectorStoreProperties.getCollectionName()).isEqualTo("Test_Vector_Store");
        assertThat(milvusVectorStoreProperties.getEmbeddingDimension()).isEqualTo(1024);
        assertThat(milvusVectorStoreProperties.isInitializeSchema()).isTrue();
    }

}
