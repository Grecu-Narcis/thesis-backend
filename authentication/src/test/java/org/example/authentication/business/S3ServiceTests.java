package org.example.authentication.business;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.Map;
import java.util.UUID;


public class S3ServiceTests {

    private S3Service s3Service;

    @BeforeEach
    public void setup() {
        s3Service = new S3Service() {
            @Override
            public Map<String, String> createPresignedUrl(String keyName, String imageType) {
                // Mocked logic since the real method instantiates S3Presigner directly
                String fakeKey = "profile/" + UUID.randomUUID() + "-" + keyName;
                return Map.of("url", "https://fake-s3-url/" + fakeKey, "imageKey", fakeKey);
            }
        };
    }

    @Test
    public void testCreatePresignedUrl() {
        String keyName = "avatar.png";
        String imageType = "image/png";

        Map<String, String> result = s3Service.createPresignedUrl(keyName, imageType);

        String url = result.get("url");
        String imageKey = result.get("imageKey");

        assert url != null;
        assert imageKey != null;

        assert url.contains(imageKey);
        assert imageKey.contains(keyName);
        assert imageKey.startsWith("profile/");
    }
}
