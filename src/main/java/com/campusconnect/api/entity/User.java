package com.campusconnect.api.entity;

import com.campusconnect.api.entity.embedded.ImageData;
import com.campusconnect.api.entity.enums.VerificationStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String studentId;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    @Builder.Default
    private Boolean phoneVerified = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean studentIdValidated = false;

    @Column
    private BigDecimal studentIdValidationScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING_VERIFICATION;

    private String profileImage;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "url", column = @Column(name = "student_id_image_url")),
            @AttributeOverride(name = "publicId", column = @Column(name = "student_id_image_public_id")),
            @AttributeOverride(name = "width", column = @Column(name = "student_id_image_width")),
            @AttributeOverride(name = "height", column = @Column(name = "student_id_image_height"))
    })
    private ImageData studentIdImage;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "url", column = @Column(name = "selfie_image_url")),
            @AttributeOverride(name = "publicId", column = @Column(name = "selfie_image_public_id")),
            @AttributeOverride(name = "width", column = @Column(name = "selfie_image_width")),
            @AttributeOverride(name = "height", column = @Column(name = "selfie_image_height"))
    })
    private ImageData selfieImage;

    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.valueOf(5.0);

    @Column(nullable = false)
    @Builder.Default
    private Integer totalDeliveries = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isOnline = false;

    private LocalDateTime lastSeen;

    @CreationTimestamp
    private LocalDateTime joinedDate;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "traveler", cascade = CascadeType.ALL)
    private List<Trip> trips;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<DeliveryRequest> deliveryRequests;

    @OneToMany(mappedBy = "reviewer", cascade = CascadeType.ALL)
    private List<Review> givenReviews;

    @OneToMany(mappedBy = "reviewee", cascade = CascadeType.ALL)
    private List<Review> receivedReviews;

}