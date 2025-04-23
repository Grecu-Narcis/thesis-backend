package org.example.postsservice.strategy.presignedurl;

public interface PresignedUrlStrategy {
    String createPresignedUrl(String bucket, String key);
}
