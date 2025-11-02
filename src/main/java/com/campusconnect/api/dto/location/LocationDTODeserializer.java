package com.campusconnect.api.dto.location;

import com.campusconnect.api.entity.embedded.Location;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * Flexible deserializer that accepts either:
 * - a plain string (interpreted as campus location)
 * - a full object { type, campusLocation, offCampusLocation }
 */
public class LocationDTODeserializer extends JsonDeserializer<LocationDTO> {
    @Override
    public LocationDTO deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken token = p.currentToken();

        if (token == JsonToken.VALUE_STRING) {
            // String like "College of Science" → default to CAMPUS with campusLocation
            String value = p.getValueAsString();
            LocationDTO dto = new LocationDTO();
            dto.setType(Location.LocationType.CAMPUS);
            dto.setCampusLocation(value);
            dto.setOffCampusLocation(null);
            return dto;
        }

        // Expect object form
        JsonNode node = p.getCodec().readTree(p);
        LocationDTO dto = new LocationDTO();

        if (node.hasNonNull("type")) {
            String typeStr = node.get("type").asText();
            dto.setType(Location.LocationType.valueOf(typeStr));
        } else {
            // If type omitted but campusLocation provided, assume CAMPUS; else OFF_CAMPUS if offCampusLocation present
            if (node.hasNonNull("campusLocation")) {
                dto.setType(Location.LocationType.CAMPUS);
            } else if (node.hasNonNull("offCampusLocation")) {
                dto.setType(Location.LocationType.OFF_CAMPUS);
            }
        }

        if (node.has("campusLocation") && !node.get("campusLocation").isNull()) {
            dto.setCampusLocation(node.get("campusLocation").asText());
        }
        if (node.has("offCampusLocation") && !node.get("offCampusLocation").isNull()) {
            dto.setOffCampusLocation(node.get("offCampusLocation").asText());
        }

        return dto;
    }
}


