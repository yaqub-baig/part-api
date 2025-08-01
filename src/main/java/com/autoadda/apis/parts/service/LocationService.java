package com.autoadda.apis.parts.service;

import com.autoadda.apis.parts.entity.Location;
import com.autoadda.apis.parts.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    public Location createLocation(Location location) {
        Location location1 = new Location ( );
        location1.setStreetAddress1(location.getStreetAddress1());
        location1.setStreetAddress2(location.getStreetAddress2());
        location1.setCity(location.getCity());
        location1.setPostalCode(location.getPostalCode());
        location1.setArea(location.getArea());
        location1.setState(location.getState());
        location1.setCountry(location.getCountry());
        location1.setGmap(location.getGmap());
        return locationRepository.save(location1);
    }


}
