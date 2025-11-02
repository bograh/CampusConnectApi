package com.campusconnect.api.dto.location;

import com.campusconnect.api.entity.embedded.Location;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonDeserialize(using = LocationDTODeserializer.class)
public class LocationDTO {
    @NotNull(message = "Location type is required")
    private Location.LocationType type;

    private String campusLocation;
    private String offCampusLocation;
}
