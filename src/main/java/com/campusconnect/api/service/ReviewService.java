package com.campusconnect.api.service;

import com.campusconnect.api.security.JwtService;
import com.campusconnect.api.dto.review.CreateReviewRequestDTO;
import com.campusconnect.api.dto.review.ReviewResponseDTO;
import com.campusconnect.api.entity.DeliveryRequest;
import com.campusconnect.api.entity.Review;
import com.campusconnect.api.entity.User;
import com.campusconnect.api.entity.enums.RequestStatus;
import com.campusconnect.api.exception.BadRequestException;
import com.campusconnect.api.exception.ConflictException;
import com.campusconnect.api.exception.ForbiddenException;
import com.campusconnect.api.exception.NotFoundException;
import com.campusconnect.api.repository.DeliveryRequestRepository;
import com.campusconnect.api.repository.ReviewRepository;
import com.campusconnect.api.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final DeliveryRequestRepository deliveryRequestRepository;
    private final JwtService jwtService;

    @Transactional
    public ReviewResponseDTO createReview(CreateReviewRequestDTO request, HttpServletRequest httpRequest) {
        String email = jwtService.getEmailFromToken(httpRequest);
        User reviewer = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        User reviewee = userRepository.findById(request.getRevieweeId())
                .orElseThrow(() -> new NotFoundException("Reviewee not found"));

        DeliveryRequest delivery = deliveryRequestRepository.findById(request.getDeliveryId())
                .orElseThrow(() -> new NotFoundException("Delivery request not found"));

        if (delivery.getStatus() != RequestStatus.DELIVERED) {
            throw new BadRequestException("Can only review completed deliveries");
        }

        boolean isRequester = delivery.getUser().getId().equals(reviewer.getId());
        boolean isTraveler = delivery.getMatchedTrip() != null && 
                            delivery.getMatchedTrip().getTraveler().getId().equals(reviewer.getId());
        
        if (!isRequester && !isTraveler) {
            throw new ForbiddenException("You can only review deliveries you were involved in");
        }

        if (reviewRepository.existsByReviewerIdAndDeliveryId(reviewer.getId(), delivery.getId())) {
            throw new ConflictException("You have already reviewed this delivery");
        }

        Review review = Review.builder()
                .reviewer(reviewer)
                .reviewee(reviewee)
                .delivery(delivery)
                .rating(request.getRating())
                .comment(request.getComment())
                .type(request.getType())
                .build();

        review = reviewRepository.save(review);

        updateUserRating(reviewee);

        return mapToResponseDTO(review);
    }

    public List<ReviewResponseDTO> getReviewsForUser(String userId) {
        List<Review> reviews = reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(userId);
        return reviews.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ReviewResponseDTO> getReviewsByUser(String userId) {
        List<Review> reviews = reviewRepository.findByReviewerIdOrderByCreatedAtDesc(userId);
        return reviews.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ReviewResponseDTO> getReviewsForDelivery(String deliveryId) {
        List<Review> reviews = reviewRepository.findByDeliveryIdOrderByCreatedAtDesc(deliveryId);
        return reviews.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public ReviewResponseDTO getReviewById(String reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found"));
        return mapToResponseDTO(review);
    }

    public double getUserAverageRating(String userId) {
        Double averageRating = reviewRepository.findAverageRatingByRevieweeId(userId);
        return averageRating != null ? averageRating : 5.0;
    }

    public long getUserReviewCount(String userId) {
        List<Review> reviews = reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(userId);
        return reviews.size();
    }

    @Transactional
    public void updateUserRating(User user) {
        Double averageRating = reviewRepository.findAverageRatingByRevieweeId(user.getId());
        if (averageRating != null) {
            BigDecimal rating = BigDecimal.valueOf(averageRating).setScale(2, RoundingMode.HALF_UP);
            user.setRating(rating);
            userRepository.save(user);
        }
    }

    public boolean canUserReviewDelivery(String userId, String deliveryId) {
        DeliveryRequest delivery = deliveryRequestRepository.findById(deliveryId)
                .orElseThrow(() -> new NotFoundException("Delivery request not found"));

        if (delivery.getStatus() != RequestStatus.DELIVERED) {
            return false;
        }

        boolean isRequester = delivery.getUser().getId().equals(userId);
        boolean isTraveler = delivery.getMatchedTrip() != null && 
                            delivery.getMatchedTrip().getTraveler().getId().equals(userId);

        if (!isRequester && !isTraveler) {
            return false;
        }

        return !reviewRepository.existsByReviewerIdAndDeliveryId(userId, deliveryId);
    }

    private ReviewResponseDTO mapToResponseDTO(Review review) {
        ReviewResponseDTO response = new ReviewResponseDTO();
        response.setId(review.getId());
        response.setReviewerId(review.getReviewer().getId());
        response.setReviewerName(review.getReviewer().getFirstName() + " " + review.getReviewer().getLastName());
        response.setRevieweeId(review.getReviewee().getId());
        response.setRevieweeName(review.getReviewee().getFirstName() + " " + review.getReviewee().getLastName());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setDeliveryId(review.getDelivery().getId());
        response.setType(review.getType());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }
}
