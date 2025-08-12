package com.autoadda.apis.part.service;

import com.autoadda.apis.part.entity.Location;
import com.autoadda.apis.part.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

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
    public Location updateLocation(Integer id,Location location) {
        Location location1 = locationRepository.findById ( id ).orElseThrow ( () -> new NoSuchElementException ( "Location not found with id: " + id ) );
        if(location.getStreetAddress1()!=null) location1.setStreetAddress1(location.getStreetAddress1());
        if(location.getStreetAddress2()!=null) location1.setStreetAddress2(location.getStreetAddress2());
        if(location.getCity()!=null) location1.setCity(location.getCity());
        if(location.getPostalCode()!=null) location1.setPostalCode(location.getPostalCode());
        if(location.getArea()!=null) location1.setArea(location.getArea());
        if(location.getState()!=null) location1.setState(location.getState());
        if(location.getCountry()!=null) location1.setCountry(location.getCountry());
        if(location.getGmap()!=null) location1.setGmap(location.getGmap());
        return locationRepository.save ( location1 );
    }

}
