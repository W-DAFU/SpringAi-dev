package com.realtimevideo;

import com.realtimevideo.config.RealtimeVideoProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RealtimeVideoProperties.class)
public class RealtimeVideoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RealtimeVideoApplication.class, args);
    }
}
