package com.campusconnect.api.repository;

import com.campusconnect.api.entity.Trip;
import com.campusconnect.api.entity.User;
import com.campusconnect.api.entity.enums.TripStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, String> {
    List<Trip> findByTravelerIdOrderByCreatedAtDesc(String travelerId);

    List<Trip> findByStatusOrderByCreatedAtDesc(TripStatus status);

    @Query("SELECT t FROM Trip t WHERE t.status = 'ACTIVE' AND " +
            "(:fromLocation IS NULL OR t.fromLocation.campusLocation = :fromLocation OR t.fromLocation.offCampusAddress LIKE %:fromLocation%) AND " +
            "(:toLocation IS NULL OR t.toLocation.campusLocation = :toLocation OR t.toLocation.offCampusAddress LIKE %:toLocation%) AND " +
            "(:maxPrice IS NULL OR t.pricePerDelivery <= :maxPrice)")
    List<Trip> searchTrips(String fromLocation, String toLocation, BigDecimal maxPrice);


    @Query("SELECT t FROM Trip t WHERE " +
            "(:startLocation IS NULL OR LOWER(t.fromLocation) LIKE LOWER(CONCAT('%', :startLocation, '%'))) AND " +
            "(:endLocation IS NULL OR LOWER(t.toLocation) LIKE LOWER(CONCAT('%', :endLocation, '%'))) AND " +
            "(:departureDate IS NULL OR DATE(t.departureTime) = DATE(:departureDate)) AND " +
            "t.status = 'ACTIVE'")
    Page<Trip> findAvailableTripsWithFilters(@Param("startLocation") String startLocation,
                                             @Param("endLocation") String endLocation,
                                             @Param("departureDate") LocalDateTime departureDate,
                                             Pageable pageable);

    List<Trip> findByTravelerAndStatusOrderByCreatedAtDesc(User user, TripStatus tripStatus);

    List<Trip> findByTravelerOrderByCreatedAtDesc(User user);
}
