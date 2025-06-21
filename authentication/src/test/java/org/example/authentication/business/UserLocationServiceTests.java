package org.example.authentication.business;

import org.example.authentication.models.UserLocation;
import org.example.authentication.repositories.UserLocationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Point;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserLocationServiceTests {

    @Mock
    private UserLocationRepository userLocationRepository;

    @InjectMocks
    private UserLocationService userLocationService;

    @Test
    void testSave_storesUserLocationCorrectly() {
        double latitude = 45.76;
        double longitude = 21.23;
        String username = "geo_user";

        userLocationService.save(username, latitude, longitude);

        ArgumentCaptor<UserLocation> captor = ArgumentCaptor.forClass(UserLocation.class);
        verify(userLocationRepository).save(captor.capture());

        UserLocation saved = captor.getValue();
        assertEquals(username, saved.getUsername());

        Point point = saved.getLocation();
        assertEquals(longitude, point.getX());
        assertEquals(latitude, point.getY());
        assertEquals(4326, point.getSRID());
    }
}
