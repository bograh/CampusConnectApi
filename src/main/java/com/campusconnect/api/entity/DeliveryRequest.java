package com.campusconnect.api.entity;

import com.campusconnect.api.entity.embedded.Location;
import com.campusconnect.api.entity.enums.ItemSize;
import com.campusconnect.api.entity.enums.Priority;
import com.campusconnect.api.entity.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "delivery_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "pickup_location_type")),
            @AttributeOverride(name = "campusLocation", column = @Column(name = "pickup_campus_location")),
            @AttributeOverride(name = "offCampusAddress", column = @Column(name = "pickup_off_campus_address"))
    })
    private Location pickupLocation;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "dropoff_location_type")),
            @AttributeOverride(name = "campusLocation", column = @Column(name = "dropoff_campus_location")),
            @AttributeOverride(name = "offCampusAddress", column = @Column(name = "dropoff_off_campus_address"))
    })
    private Location dropoffLocation;

    @Column(nullable = false)
    private String itemDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemSize itemSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.NORMAL;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal paymentAmount;

    @Column(nullable = false)
    private LocalDate pickupDate;

    @Column(nullable = false)
    private LocalTime pickupTime;

    @Column(nullable = false)
    private String contactInfo;

    @Column(columnDefinition = "TEXT")
    private String specialInstructions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_trip_id")
    private Trip matchedTrip;

    private LocalDateTime completedAt;
    private String deliveryProof;
    private String deliveryNotes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}