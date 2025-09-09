package com.campusconnect.api.repository;

import com.campusconnect.api.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {
    List<Review> findByRevieweeIdOrderByCreatedAtDesc(String revieweeId);

    List<Review> findByReviewerIdOrderByCreatedAtDesc(String reviewerId);

    List<Review> findByDeliveryIdOrderByCreatedAtDesc(String deliveryId);

    boolean existsByReviewerIdAndDeliveryId(String reviewerId, String deliveryId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewee.id = :userId")
    Double findAverageRatingByRevieweeId(String userId);
}

