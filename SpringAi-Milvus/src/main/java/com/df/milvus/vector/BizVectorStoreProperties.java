package com.df.milvus.vector;

import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "app.vector-store.milvus")
public class BizVectorStoreProperties {

    private String databaseName = "default";
    private int embeddingDimension = 1024;
    private IndexType indexType = IndexType.IVF_FLAT;
    private MetricType metricType = MetricType.COSINE;
    private boolean initializeSchema = true;
    private Map<String, String> collections = new HashMap<>();

    public BizVectorStoreProperties() {
        collections.put("job", "job_info");
        collections.put("resume", "resume_info");
        collections.put("company", "company_info");
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public int getEmbeddingDimension() {
        return embeddingDimension;
    }

    public void setEmbeddingDimension(int embeddingDimension) {
        this.embeddingDimension = embeddingDimension;
    }

    public IndexType getIndexType() {
        return indexType;
    }

    public void setIndexType(IndexType indexType) {
        this.indexType = indexType;
    }

    public MetricType getMetricType() {
        return metricType;
    }

    public void setMetricType(MetricType metricType) {
        this.metricType = metricType;
    }

    public boolean isInitializeSchema() {
        return initializeSchema;
    }

    public void setInitializeSchema(boolean initializeSchema) {
        this.initializeSchema = initializeSchema;
    }

    public Map<String, String> getCollections() {
        return collections;
    }

    public void setCollections(Map<String, String> collections) {
        this.collections = collections;
    }

    public String collectionName(BizVectorCollection collection) {
        return collections.getOrDefault(collection.path(), collection.defaultCollectionName());
    }
}
