package org.example.authentication.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.example.authentication.serializers.PointSerializer;
import org.locationtech.jts.geom.Point;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "user_location")
public class UserLocation {
    @Id
    @Column(name = "username", updatable = false)
    private String username;

    @Column(name = "location", columnDefinition = "POINT NOT NULL SRID 4326")
    @JsonSerialize(using = PointSerializer.class)
    private Point location;
}
