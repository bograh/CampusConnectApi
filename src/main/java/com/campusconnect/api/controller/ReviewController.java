package com.campusconnect.api.controller;

import com.campusconnect.api.dto.review.CreateReviewRequestDTO;
import com.campusconnect.api.dto.review.ReviewResponseDTO;
import com.campusconnect.api.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponseDTO> createReview(
            @Valid @RequestBody CreateReviewRequestDTO request,
            HttpServletRequest httpRequest) {
        ReviewResponseDTO response = reviewService.createReview(request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDTO> getReviewById(@PathVariable String reviewId) {
        ReviewResponseDTO review = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsForUser(@PathVariable String userId) {
        List<ReviewResponseDTO> reviews = reviewService.getReviewsForUser(userId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/user/{userId}/given")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByUser(@PathVariable String userId) {
        List<ReviewResponseDTO> reviews = reviewService.getReviewsByUser(userId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/delivery/{deliveryId}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsForDelivery(@PathVariable String deliveryId) {
        List<ReviewResponseDTO> reviews = reviewService.getReviewsForDelivery(deliveryId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/user/{userId}/rating")
    public ResponseEntity<Map<String, Object>> getUserRatingStats(@PathVariable String userId) {
        double averageRating = reviewService.getUserAverageRating(userId);
        long reviewCount = reviewService.getUserReviewCount(userId);
        
        return ResponseEntity.ok(Map.of(
            "averageRating", averageRating,
            "reviewCount", reviewCount
        ));
    }

    @GetMapping("/can-review")
    public ResponseEntity<Map<String, Boolean>> canUserReviewDelivery(
            @RequestParam String userId,
            @RequestParam String deliveryId) {
        boolean canReview = reviewService.canUserReviewDelivery(userId, deliveryId);
        return ResponseEntity.ok(Map.of("canReview", canReview));
    }
}
