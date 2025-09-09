package com.campusconnect.api.repository;

import com.campusconnect.api.entity.DeliveryRequest;
import com.campusconnect.api.entity.Trip;
import com.campusconnect.api.entity.enums.ItemSize;
import com.campusconnect.api.entity.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DeliveryRequestRepository extends JpaRepository<DeliveryRequest, String> {
    List<DeliveryRequest> findByUserIdOrderByCreatedAtDesc(String userId);

    List<DeliveryRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status);

    List<DeliveryRequest> findByMatchedTripId(String tripId);

    @Query("SELECT dr FROM DeliveryRequest dr WHERE dr.status = 'PENDING' AND " +
            "(:minPrice IS NULL OR dr.paymentAmount >= :minPrice) AND " +
            "(:maxPrice IS NULL OR dr.paymentAmount <= :maxPrice) AND " +
            "(:itemSize IS NULL OR dr.itemSize = :itemSize)")
    List<DeliveryRequest> searchDeliveryRequests(BigDecimal minPrice, BigDecimal maxPrice,
                                                 ItemSize itemSize);

    List<DeliveryRequest> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, RequestStatus status);
    
    List<DeliveryRequest> findByMatchedTrip( Trip trip);
}
