package com.df.milvus.controller;


import com.df.milvus.vector.BizVectorCollection;
import com.df.milvus.vector.VectorStoreFactory;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.SearchResults;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.response.QueryResultsWrapper;
import io.milvus.response.SearchResultsWrapper;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("api/")
public class EmbeddingController {


    @Autowired
    private EmbeddingModel embeddingModel;
    @Autowired
    MilvusServiceClient milvusServiceClient;
    @Autowired
    private VectorStoreFactory vectorStoreFactory;

    public EmbeddingController() {
    }

    EmbeddingController(EmbeddingModel embeddingModel, MilvusServiceClient milvusServiceClient,
                        VectorStoreFactory vectorStoreFactory) {
        this.embeddingModel = embeddingModel;
        this.milvusServiceClient = milvusServiceClient;
        this.vectorStoreFactory = vectorStoreFactory;
    }

    /**
     * 文字转向量编码集
     *
     * @param message
     * @return
     */
    @GetMapping("/ai/embedding")
    public Map embed(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        EmbeddingResponse embeddingResponse = this.embeddingModel.embedForResponse(List.of(message));
        Map map = new HashMap<>();
        map.put("message", message);
        map.put("embedding", embeddingResponse.getResult().getOutput());
        return map;
    }


    /**
     * 保存向量数据库
     */
    @GetMapping("/saveMilvus")
    public Map<String, Object> saveMilvus(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        Map<String, Object> map = new HashMap<>();
        //文本转向量
        float[] floats = embeddingModel.embed(message);
        List<Float> values = new ArrayList<>();
        for (float aFloat : floats) {
            values.add(aFloat);
        }
        //构建参数
        InsertParam.Field id = new InsertParam.Field("id", List.of(System.currentTimeMillis()));
        InsertParam.Field vector = new InsertParam.Field("vector", List.of(values));
        InsertParam.Field vectorText = new InsertParam.Field("vector_text", List.of(message));
        List<InsertParam.Field> fields = List.of(id, vector, vectorText);
        //插入参数
        InsertParam insertParam = InsertParam.newBuilder()
                .withDatabaseName("default")
                .withCollectionName("Test_Vector_Store")
                .withFields(fields)
                .build();
        //执行保存
        R<MutationResult> insert = milvusServiceClient.insert(insertParam);
        map.put("insert", insert.getData().getIDs().getIntId());
        return map;
    }


    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        //文本转向量
        float[] floats = embeddingModel.embed(message);
        List<Float> vectors=new ArrayList<>();
        for (float aFloat : floats) {
            vectors.add(aFloat);
        }
        SearchParam searchParam = SearchParam.newBuilder()
                .withDatabaseName("default")
                .withCollectionName("Test_Vector_Store")
                .withVectorFieldName("vector")
                .withMetricType(MetricType.COSINE)
                .withOutFields(List.of("id", "vector_text"))
                .withFloatVectors(List.of(vectors))
                .withTopK(2)
                .build();
        R<SearchResults> response = milvusServiceClient.search(searchParam);


        Map<String, Object> result = new HashMap<>();
        result.put("message", message);
        result.put("code", response.getStatus());
        result.put("success", response.getException() == null);

        if (response.getException() != null) {
            result.put("error", response.getException().getMessage());
            result.put("results", List.of());
            return result;
        }
        SearchResultsWrapper wrapper = new SearchResultsWrapper(response.getData().getResults());
        List<Map<String, Object>> rows = new ArrayList<>();
        for (QueryResultsWrapper.RowRecord rowRecord : wrapper.getRowRecords(0)) {
            rows.add(formatSearchRow(rowRecord));
        }
        result.put("results", rows);
        return result;
    }

    Map<String, Object> formatSearchRow(QueryResultsWrapper.RowRecord rowRecord) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", rowRecord.get("id"));
        result.put("text", rowRecord.get("vector_text"));
        if (rowRecord.contains("score")) {
            result.put("score", rowRecord.get("score"));
        }
        return result;
    }


    @PostMapping("/vector-store/{collection}")
    public Map<String, Object> saveToVectorStore(
            @PathVariable String collection,
            @RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {

        BizVectorCollection bizCollection = BizVectorCollection.fromPath(collection);
        String collectionName = vectorStoreFactory.collectionName(bizCollection);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("bizType", bizCollection.path());
        metadata.put("collectionName", collectionName);

        Document document = new Document(message, metadata);
        vectorStoreFactory.get(bizCollection).add(List.of(document));

        Map<String, Object> map = new HashMap<>();
        map.put("saved", true);
        map.put("collection", collectionName);
        map.put("message", message);
        return map;
    }


}
