package com.campusconnect.api.dto.location;

import com.campusconnect.api.entity.embedded.Location;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocationDTO {
    @NotNull(message = "Location type is required")
    private Location.LocationType type;

    private String campusLocation;
    private String offCampusLocation;
}
