package org.example.authentication.business;

import org.example.authentication.models.UserLocation;
import org.example.authentication.repositories.UserLocationRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserLocationService {
    private final UserLocationRepository userLocationRepository;
    private final GeometryFactory geometryFactory;
    private final int SRID = 4326;

    @Autowired
    public UserLocationService(UserLocationRepository userLocationRepository) {
        this.userLocationRepository = userLocationRepository;
        this.geometryFactory = new GeometryFactory();
    }

    public void save(String username, double latitude, double longitude) {
        Point userLocation = this.geometryFactory.createPoint(new Coordinate(longitude, latitude));
        userLocation.setSRID(this.SRID);

        userLocationRepository.save(new UserLocation(username, userLocation));
    }
}
