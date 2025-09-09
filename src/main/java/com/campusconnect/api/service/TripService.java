package com.campusconnect.api.service;

import com.campusconnect.api.config.CachedPage;
import com.campusconnect.api.config.JwtService;
import com.campusconnect.api.dto.PaginatedResponseDTO;
import com.campusconnect.api.dto.PaginationDTO;
import com.campusconnect.api.dto.trip.CreateTripRequestDTO;
import com.campusconnect.api.dto.trip.TripDTOUtils;
import com.campusconnect.api.dto.trip.TripResponseDTO;
import com.campusconnect.api.dto.trip.UpdateTripRequestDTO;
import com.campusconnect.api.entity.DeliveryRequest;
import com.campusconnect.api.entity.Trip;
import com.campusconnect.api.entity.User;
import com.campusconnect.api.entity.enums.RequestStatus;
import com.campusconnect.api.entity.enums.TripStatus;
import com.campusconnect.api.exception.BadRequestException;
import com.campusconnect.api.exception.ForbiddenException;
import com.campusconnect.api.exception.NotFoundException;
import com.campusconnect.api.repository.DeliveryRequestRepository;
import com.campusconnect.api.repository.TripRepository;
import com.campusconnect.api.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TripService {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final TripDTOUtils tripDTOUtils;
    private final TripRepository tripRepository;
    private final DeliveryRequestRepository deliveryRequestRepository;

    @Transactional
    public TripResponseDTO createTrip(@Valid CreateTripRequestDTO createTripRequestDTO, HttpServletRequest request) {
        String email = jwtService.getEmailFromToken(request);
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException("User not found with email: [REDACTED]")
        );
        Trip trip = tripDTOUtils.updateTripFromRequestDTO(createTripRequestDTO, user);
        Trip newTrip = tripRepository.save(trip);
        return tripDTOUtils.updateTripResponseDTO(newTrip);
    }

    public PaginatedResponseDTO<TripResponseDTO> getAllTrips(
            String from, String to, String departureDate, int page, int limit) {

        Pageable pageable = PageRequest.of(page - 1, limit);

        Page<Trip> trips;
        LocalDateTime parsedDate = null;
        if (departureDate != null && !departureDate.isEmpty()) {
            parsedDate = LocalDateTime.parse(departureDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            trips = tripRepository.findAvailableTripsWithFilters(
                    from, to, parsedDate, pageable
            );
        } else {
            trips = tripRepository.findAll(pageable);
        }

        List<TripResponseDTO> tripResponseList = trips.getContent().stream()
                .map(tripDTOUtils::updateTripResponseDTO)
                .toList();

        return new PaginatedResponseDTO<>(tripResponseList, new PaginationDTO(page, pageable.getPageSize(), limit));
    }

    public TripResponseDTO getTripById(String tripId) {
        Trip trip = tripRepository.findById(tripId).orElseThrow(
                () -> new NotFoundException("Trip not found with id: [REDACTED]")
        );
        return tripDTOUtils.updateTripResponseDTO(trip);
    }

    @Transactional
    public TripResponseDTO updateTrip(String tripId, UpdateTripRequestDTO updateTripRequestDTO, HttpServletRequest request) {
        String email = jwtService.getEmailFromToken(request);
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException("User not found with email: [REDACTED]")
        );
        Trip trip = tripRepository.findById(tripId).orElseThrow(
                () -> new NotFoundException("Trip not found with id: [REDACTED]")
        );

        if (!trip.getTraveler().getId().equals(user.getId())) {
            throw new ForbiddenException("You can only update your own trips");
        }

        if (updateTripRequestDTO.getDepartureTime() != null) {
            trip.setDepartureTime(LocalDateTime.parse(updateTripRequestDTO.getDepartureTime()));
        }
        if (updateTripRequestDTO.getPricePerDelivery() != null) {
            trip.setPricePerDelivery(updateTripRequestDTO.getPricePerDelivery());
        }
        if (updateTripRequestDTO.getMaxDeliveries() != null) {
            if (updateTripRequestDTO.getMaxDeliveries() < trip.getCurrentDeliveries()) {
                throw new BadRequestException("Cannot reduce available seats below current deliveries");
            }
            trip.setMaxDeliveries(updateTripRequestDTO.getMaxDeliveries());
        }
        if (updateTripRequestDTO.getStatus() != null) {
            trip.setStatus(updateTripRequestDTO.getStatus());
        }

        trip.setUpdatedAt(LocalDateTime.now());
        trip = tripRepository.save(trip);

        return tripDTOUtils.updateTripResponseDTO(trip);

    }

    @Transactional
    public void cancelTrip(String tripId, HttpServletRequest request) {
        String email = jwtService.getEmailFromToken(request);
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException("User not found with email: [REDACTED]")
        );
        Trip trip = tripRepository.findById(tripId).orElseThrow(
                () -> new NotFoundException("Trip not found with id: [REDACTED]")
        );
        if (!trip.getTraveler().getId().equals(user.getId())) {
            throw new ForbiddenException("You can only cancel your own trips");
        }
        trip.setStatus(TripStatus.CANCELLED);
        trip.setUpdatedAt(LocalDateTime.now());
        tripRepository.save(trip);

        List<DeliveryRequest> matchedRequests = deliveryRequestRepository.findByMatchedTrip(trip);
        for (DeliveryRequest deliveryRequest : matchedRequests) {
            deliveryRequest.setStatus(RequestStatus.PENDING);
            deliveryRequest.setMatchedTrip(null);
            deliveryRequest.setUpdatedAt(LocalDateTime.now());
        }
        deliveryRequestRepository.saveAll(matchedRequests);
    }

    public List<TripResponseDTO> getUserTrips(String status, HttpServletRequest request) {
        String email = jwtService.getEmailFromToken(request);
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException("User not found with email: [REDACTED]")
        );
        List<Trip> trips;
        if (status != null) {
            TripStatus tripStatus = TripStatus.valueOf(status.toUpperCase());
            trips = tripRepository.findByTravelerAndStatusOrderByCreatedAtDesc(user, tripStatus);
        } else {
            trips = tripRepository.findByTravelerOrderByCreatedAtDesc(user);
        }

        return trips.stream()
                .map(tripDTOUtils::updateTripResponseDTO)
                .toList();

    }
}
