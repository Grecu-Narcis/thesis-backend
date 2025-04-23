package org.example.postsservice.strategy.presignedurl;

import org.springframework.stereotype.Component;

@Component
public class PresignedUrlContext {
    private PresignedUrlStrategy strategy;

    public void setStrategy(String strategyType) {
        switch (strategyType) {
            case "get":
                strategy = new GetPresignedUrlStrategy();
                break;

            case "put":
                strategy = new PutPresignedUrlStrategy();
                break;
        }
    }

    public String generatePresignedUrl(String bucketName, String objectKey) {
        return strategy.createPresignedUrl(bucketName, objectKey);
    }
}
