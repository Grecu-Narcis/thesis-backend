package org.example.postsservice.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.postsservice.serializers.PointSerializer;
import org.locationtech.jts.geom.Point;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "postId")
    private Long postId;

    @Column(name = "imageKey")
    private String imageKey;

    @Column(name = "createdBy")
    private String createdBy;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "likesCount")
    private int likesCount;

    @Column(name = "location", columnDefinition = "POINT NOT NULL SRID 4326")
    @JsonSerialize(using = PointSerializer.class)
    private Point location;

    @Column(name = "createdAt", nullable = false)
    private Date createdAt;

    @Column(name = "carBrand")
    private String carBrand;

    @Column(name = "carModel")
    private String carModel;

    @Column(name="productionYear")
    private int productionYear;

    public Post(String imageKey, String createdBy, String description,
                Point location, String carBrand, String carModel, int productionYear) {
        this.imageKey = imageKey;
        this.createdBy = createdBy;
        this.description = description;
        this.likesCount = 0;
        this.location = location;
        this.createdAt = new Date();
        this.carBrand = carBrand;
        this.carModel = carModel;
        this.productionYear = productionYear;
    }
}
