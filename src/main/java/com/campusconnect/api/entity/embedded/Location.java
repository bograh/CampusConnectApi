package com.campusconnect.api.entity.embedded;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {
    @Enumerated(EnumType.STRING)
    private LocationType type;

    private String campusLocation;

    private String offCampusAddress;

    public enum LocationType {
        CAMPUS, OFF_CAMPUS
    }
}
