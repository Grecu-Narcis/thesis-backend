package org.example.postsservice.business;

import org.example.postsservice.strategy.presignedurl.PresignedUrlContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class S3Service {
    private final PresignedUrlContext presignedUrlContext;

    @Autowired
    public S3Service(PresignedUrlContext presignedUrlContext) {
        this.presignedUrlContext = presignedUrlContext;
    }

    public String createPresignedUrl(String bucketName, String keyName, String type) {
        this.presignedUrlContext.setStrategy(type.toLowerCase());

        return this.presignedUrlContext.generatePresignedUrl(bucketName, keyName);
    }
}
