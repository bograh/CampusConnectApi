package com.campusconnect.api.entity;

import com.campusconnect.api.entity.embedded.Location;
import com.campusconnect.api.entity.enums.TransportMethod;
import com.campusconnect.api.entity.enums.TripStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "trips")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "traveler_id", nullable = false)
    private User traveler;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "from_location_type")),
            @AttributeOverride(name = "campusLocation", column = @Column(name = "from_campus_location")),
            @AttributeOverride(name = "offCampusAddress", column = @Column(name = "from_off_campus_address"))
    })
    private Location fromLocation;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "to_location_type")),
            @AttributeOverride(name = "campusLocation", column = @Column(name = "to_campus_location")),
            @AttributeOverride(name = "offCampusAddress", column = @Column(name = "to_off_campus_address"))
    })
    private Location toLocation;

    @Column(nullable = false)
    private LocalDateTime departureTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransportMethod transportMethod;

    @Column(nullable = false)
    private Integer maxDeliveries;

    @Column(nullable = false)
    private Integer currentDeliveries = 0;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerDelivery;

    @Column(nullable = false)
    private Boolean isRecurring = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripStatus status = TripStatus.ACTIVE;

    private String description;
    private String contactInfo;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "matchedTrip", cascade = CascadeType.ALL)
    private List<DeliveryRequest> matchedRequests;


}
