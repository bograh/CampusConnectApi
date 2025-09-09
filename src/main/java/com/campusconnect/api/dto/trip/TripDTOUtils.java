package com.campusconnect.api.dto.trip;

import com.campusconnect.api.dto.location.LocationDTO;
import com.campusconnect.api.entity.Trip;
import com.campusconnect.api.entity.User;
import com.campusconnect.api.entity.embedded.Location;
import org.springframework.stereotype.Component;

@Component
public class TripDTOUtils {

    public Trip updateTripFromRequestDTO(CreateTripRequestDTO createTripRequest, User user) {
        Trip trip = new Trip();
        trip.setTraveler(user);
        Location fromLocation = getLocation(createTripRequest.getFromLocation());
        Location toLocation = getLocation(createTripRequest.getToLocation());
        trip.setFromLocation(fromLocation);
        trip.setToLocation(toLocation);
        trip.setDepartureTime(createTripRequest.getDepartureTime());
        trip.setTransportMethod(createTripRequest.getVehicleType());
        trip.setMaxDeliveries(createTripRequest.getAvailableSeats());
        trip.setPricePerDelivery(createTripRequest.getPricePerDelivery());
        trip.setIsRecurring(createTripRequest.getRecurring());
        trip.setDescription(createTripRequest.getDescription());
        trip.setContactInfo(createTripRequest.getContactInfo());

        return trip;
    }

    public TripResponseDTO updateTripResponseDTO(Trip trip) {
        TripResponseDTO tripResponseDTO = new TripResponseDTO();
        tripResponseDTO.setId(trip.getId());
        tripResponseDTO.setTravelerId(trip.getTraveler().getId());
        String travelerName = trip.getTraveler().getFirstName() + " " + trip.getTraveler().getLastName();
        tripResponseDTO.setTravelerName(travelerName);
        tripResponseDTO.setTravelerPhone(trip.getTraveler().getPhoneNumber());
        tripResponseDTO.setTravelerRating(trip.getTraveler().getRating());
        tripResponseDTO.setFromLocation(getLocationDTO(trip.getFromLocation()));
        tripResponseDTO.setToLocation(getLocationDTO(trip.getToLocation()));
        tripResponseDTO.setDepartureTime(trip.getDepartureTime());
        tripResponseDTO.setTransportMethod(trip.getTransportMethod());
        tripResponseDTO.setMaxDeliveries(trip.getMaxDeliveries());
        tripResponseDTO.setCurrentDeliveries(trip.getCurrentDeliveries());
        tripResponseDTO.setPricePerDelivery(trip.getPricePerDelivery());
        tripResponseDTO.setIsRecurring(trip.getIsRecurring());
        tripResponseDTO.setStatus(trip.getStatus());
        tripResponseDTO.setDescription(trip.getDescription());
        tripResponseDTO.setCreatedAt(trip.getCreatedAt());
        return tripResponseDTO;
    }


    private Location getLocation(LocationDTO locationDTO) {
        Location location = new Location();
        Location.LocationType locationType = locationDTO.getType();
        location.setType(locationType);

        if (locationType.equals(Location.LocationType.CAMPUS)) {
            location.setCampusLocation(locationDTO.getCampusLocation());
            location.setOffCampusAddress(null);
        } else {
            location.setOffCampusAddress(locationDTO.getOffCampusLocation());
            location.setCampusLocation(null);
        }

        return location;
    }

    private LocationDTO getLocationDTO(Location location) {
        LocationDTO locationDTO = new LocationDTO();
        locationDTO.setType(location.getType());
        locationDTO.setCampusLocation(location.getCampusLocation());
        location.setOffCampusAddress(location.getOffCampusAddress());
        return locationDTO;
    }

}
