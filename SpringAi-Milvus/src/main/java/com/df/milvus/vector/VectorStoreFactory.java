package com.df.milvus.vector;

import io.milvus.client.MilvusServiceClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VectorStoreFactory {

    private final MilvusServiceClient milvusServiceClient;
    private final EmbeddingModel embeddingModel;
    private final BizVectorStoreProperties properties;
    private final Map<BizVectorCollection, VectorStore> cache = new ConcurrentHashMap<>();

    public VectorStoreFactory(MilvusServiceClient milvusServiceClient, EmbeddingModel embeddingModel,
            BizVectorStoreProperties properties) {
        this.milvusServiceClient = milvusServiceClient;
        this.embeddingModel = embeddingModel;
        this.properties = properties;
    }

    public VectorStore get(BizVectorCollection collection) {
        return cache.computeIfAbsent(collection, this::createVectorStore);
    }

    public void initializeAll() {
        for (BizVectorCollection collection : BizVectorCollection.values()) {
            get(collection);
        }
    }

    public String collectionName(BizVectorCollection collection) {
        return properties.collectionName(collection);
    }

    private VectorStore createVectorStore(BizVectorCollection collection) {
        String collectionName = properties.collectionName(collection);
        MilvusVectorStore vectorStore = MilvusVectorStore.builder(milvusServiceClient, embeddingModel)
                .databaseName(properties.getDatabaseName())
                .collectionName(collectionName)
                .embeddingDimension(properties.getEmbeddingDimension())
                .indexType(properties.getIndexType())
                .metricType(properties.getMetricType())
                .initializeSchema(properties.isInitializeSchema())
                .build();

        try {
            vectorStore.afterPropertiesSet();
        }
        catch (Exception e) {
            throw new IllegalStateException("Failed to initialize Milvus collection: " + collectionName, e);
        }

        return vectorStore;
    }
}
