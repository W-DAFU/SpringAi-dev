package com.df.milvus.vector;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class BizVectorStoreInitializer implements ApplicationRunner {

    private final VectorStoreFactory vectorStoreFactory;

    public BizVectorStoreInitializer(VectorStoreFactory vectorStoreFactory) {
        this.vectorStoreFactory = vectorStoreFactory;
    }

    @Override
    public void run(ApplicationArguments args) {
        vectorStoreFactory.initializeAll();
    }
}
