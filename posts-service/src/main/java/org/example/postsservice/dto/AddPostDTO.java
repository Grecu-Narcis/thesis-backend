package org.example.postsservice.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AddPostDTO {
    private String imageKey;
    private String createdBy;
    private String description;
    private double latitude;
    private double longitude;
    private String carBrand;
    private String carModel;
    private int productionYear;
}
