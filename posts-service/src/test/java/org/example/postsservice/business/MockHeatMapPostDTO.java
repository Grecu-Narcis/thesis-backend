package org.example.postsservice.business;

import org.example.postsservice.dto.HeatMapPostDTO;

public class MockHeatMapPostDTO implements HeatMapPostDTO {
    private final Long postId;
    private final Double latitude;
    private final Double longitude;

    public MockHeatMapPostDTO(Long postId, Double latitude, Double longitude) {
        this.postId = postId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public Long getPostId() {
        return postId;
    }

    @Override
    public Double getLatitude() {
        return latitude;
    }

    @Override
    public Double getLongitude() {
        return longitude;
    }
}
