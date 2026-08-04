package com.df.milvus.vector;

import java.util.Arrays;

public enum BizVectorCollection {

    JOB("job", "job_info"),
    RESUME("resume", "resume_info"),
    COMPANY("company", "company_info");

    private final String path;
    private final String defaultCollectionName;

    BizVectorCollection(String path, String defaultCollectionName) {
        this.path = path;
        this.defaultCollectionName = defaultCollectionName;
    }

    public String path() {
        return path;
    }

    public String collectionName() {
        return defaultCollectionName;
    }

    public String defaultCollectionName() {
        return defaultCollectionName;
    }

    public static BizVectorCollection fromPath(String path) {
        return Arrays.stream(values())
                .filter(collection -> collection.path.equalsIgnoreCase(path)
                        || collection.defaultCollectionName.equalsIgnoreCase(path))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported vector collection: " + path));
    }
}
