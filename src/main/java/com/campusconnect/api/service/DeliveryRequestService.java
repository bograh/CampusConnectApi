package com.campusconnect.api.service;

import com.campusconnect.api.config.JwtService;
import com.campusconnect.api.dto.delivery.CompleteDeliveryRequestDTO;
import com.campusconnect.api.dto.delivery.CreateDeliveryRequestDTO;
import com.campusconnect.api.dto.delivery.DeliveryRequestResponseDTO;
import com.campusconnect.api.dto.location.LocationDTO;
import com.campusconnect.api.entity.DeliveryRequest;
import com.campusconnect.api.entity.Trip;
import com.campusconnect.api.entity.User;
import com.campusconnect.api.entity.embedded.Location;
import com.campusconnect.api.entity.enums.RequestStatus;
import com.campusconnect.api.exception.BadRequestException;
import com.campusconnect.api.exception.ForbiddenException;
import com.campusconnect.api.exception.NotFoundException;
import com.campusconnect.api.repository.DeliveryRequestRepository;
import com.campusconnect.api.repository.TripRepository;
import com.campusconnect.api.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryRequestService {

    private final DeliveryRequestRepository deliveryRequestRepository;
    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final JwtService jwtService;

    @Transactional
    public DeliveryRequestResponseDTO createDeliveryRequest(CreateDeliveryRequestDTO request, HttpServletRequest httpRequest) {
        String email = jwtService.getEmailFromToken(httpRequest);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        DeliveryRequest deliveryRequest = DeliveryRequest.builder()
                .user(user)
                .pickupLocation(mapToLocation(request.getPickupLocation()))
                .dropoffLocation(mapToLocation(request.getDropoffLocation()))
                .itemDescription(request.getItemDescription())
                .itemSize(request.getItemSize())
                .priority(request.getPriority())
                .paymentAmount(request.getPaymentAmount())
                .pickupDate(LocalDate.parse(request.getPickupDate()))
                .pickupTime(LocalTime.parse(request.getPickupTime()))
                .contactInfo(request.getContactInfo())
                .specialInstructions(request.getSpecialInstructions())
                .status(RequestStatus.PENDING)
                .build();

        deliveryRequest = deliveryRequestRepository.save(deliveryRequest);

        return mapToResponseDTO(deliveryRequest);
    }

    public List<DeliveryRequestResponseDTO> getUserDeliveryRequests(String userId, String status) {
        List<DeliveryRequest> requests;
        
        if (status != null && !status.isEmpty()) {
            RequestStatus requestStatus = RequestStatus.valueOf(status.toUpperCase());
            requests = deliveryRequestRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, requestStatus);
        } else {
            requests = deliveryRequestRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }

        return requests.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<DeliveryRequestResponseDTO> getAvailableDeliveryRequests() {
        List<DeliveryRequest> requests = deliveryRequestRepository.findByStatusOrderByCreatedAtDesc(RequestStatus.PENDING);
        return requests.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public DeliveryRequestResponseDTO getDeliveryRequestById(String requestId) {
        DeliveryRequest request = deliveryRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Delivery request not found"));
        return mapToResponseDTO(request);
    }

    @Transactional
    public DeliveryRequestResponseDTO acceptDeliveryRequest(String requestId, String tripId, HttpServletRequest httpRequest) {
        String email = jwtService.getEmailFromToken(httpRequest);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        DeliveryRequest deliveryRequest = deliveryRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Delivery request not found"));

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NotFoundException("Trip not found"));

        if (!trip.getTraveler().getId().equals(user.getId())) {
            throw new ForbiddenException("You can only accept requests for your own trips");
        }

        if (deliveryRequest.getStatus() != RequestStatus.PENDING) {
            throw new BadRequestException("This delivery request is no longer available");
        }

        if (trip.getCurrentDeliveries() >= trip.getMaxDeliveries()) {
            throw new BadRequestException("This trip is already full");
        }

        deliveryRequest.setStatus(RequestStatus.MATCHED);
        deliveryRequest.setMatchedTrip(trip);

        trip.setCurrentDeliveries(trip.getCurrentDeliveries() + 1);

        deliveryRequestRepository.save(deliveryRequest);
        tripRepository.save(trip);

        return mapToResponseDTO(deliveryRequest);
    }

    @Transactional
    public DeliveryRequestResponseDTO markInTransit(String requestId, HttpServletRequest httpRequest) {
        String email = jwtService.getEmailFromToken(httpRequest);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        DeliveryRequest deliveryRequest = deliveryRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Delivery request not found"));

        if (deliveryRequest.getMatchedTrip() == null || 
            !deliveryRequest.getMatchedTrip().getTraveler().getId().equals(user.getId())) {
            throw new ForbiddenException("You can only update delivery requests for your trips");
        }

        if (deliveryRequest.getStatus() != RequestStatus.MATCHED) {
            throw new BadRequestException("Delivery request must be matched before marking in transit");
        }

        deliveryRequest.setStatus(RequestStatus.IN_TRANSIT);
        deliveryRequest = deliveryRequestRepository.save(deliveryRequest);

        return mapToResponseDTO(deliveryRequest);
    }

    @Transactional
    public DeliveryRequestResponseDTO completeDelivery(String requestId, CompleteDeliveryRequestDTO completeRequest, HttpServletRequest httpRequest) {
        String email = jwtService.getEmailFromToken(httpRequest);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        DeliveryRequest deliveryRequest = deliveryRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Delivery request not found"));

        if (deliveryRequest.getMatchedTrip() == null || 
            !deliveryRequest.getMatchedTrip().getTraveler().getId().equals(user.getId())) {
            throw new ForbiddenException("You can only complete deliveries for your trips");
        }

        if (deliveryRequest.getStatus() != RequestStatus.IN_TRANSIT) {
            throw new BadRequestException("Delivery request must be in transit before completing");
        }

        deliveryRequest.setStatus(RequestStatus.DELIVERED);
        deliveryRequest.setCompletedAt(LocalDateTime.now());
        deliveryRequest.setDeliveryProof(completeRequest.getDeliveryProof());
        deliveryRequest.setDeliveryNotes(completeRequest.getNotes());

        User traveler = deliveryRequest.getMatchedTrip().getTraveler();
        traveler.setTotalDeliveries(traveler.getTotalDeliveries() + 1);
        userRepository.save(traveler);

        deliveryRequest = deliveryRequestRepository.save(deliveryRequest);

        return mapToResponseDTO(deliveryRequest);
    }

    @Transactional
    public void cancelDeliveryRequest(String requestId, HttpServletRequest httpRequest) {
        String email = jwtService.getEmailFromToken(httpRequest);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        DeliveryRequest deliveryRequest = deliveryRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Delivery request not found"));

        if (!deliveryRequest.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You can only cancel your own delivery requests");
        }

        if (deliveryRequest.getStatus() == RequestStatus.DELIVERED) {
            throw new BadRequestException("Cannot cancel completed delivery requests");
        }

        if (deliveryRequest.getMatchedTrip() != null) {
            Trip trip = deliveryRequest.getMatchedTrip();
            trip.setCurrentDeliveries(trip.getCurrentDeliveries() - 1);
            tripRepository.save(trip);
        }

        deliveryRequest.setStatus(RequestStatus.CANCELLED);
        deliveryRequestRepository.save(deliveryRequest);
    }

    private Location mapToLocation(LocationDTO locationDTO) {
        return Location.builder()
                .type(locationDTO.getType())
                .campusLocation(locationDTO.getCampusLocation())
                .offCampusAddress(locationDTO.getOffCampusLocation())
                .build();
    }

    private LocationDTO mapToLocationDTO(Location location) {
        LocationDTO locationDTO = new LocationDTO();
        locationDTO.setType(location.getType());
        locationDTO.setCampusLocation(location.getCampusLocation());
        locationDTO.setOffCampusLocation(location.getOffCampusAddress());
        return locationDTO;
    }

    private DeliveryRequestResponseDTO mapToResponseDTO(DeliveryRequest deliveryRequest) {
        DeliveryRequestResponseDTO response = new DeliveryRequestResponseDTO();
        response.setId(deliveryRequest.getId());
        response.setUserId(deliveryRequest.getUser().getId());
        response.setUserName(deliveryRequest.getUser().getFirstName() + " " + deliveryRequest.getUser().getLastName());
        response.setUserPhone(deliveryRequest.getUser().getPhoneNumber());
        response.setUserEmail(deliveryRequest.getUser().getEmail());
        response.setUserRating(deliveryRequest.getUser().getRating());
        response.setPickupLocation(mapToLocationDTO(deliveryRequest.getPickupLocation()));
        response.setDropoffLocation(mapToLocationDTO(deliveryRequest.getDropoffLocation()));
        response.setItemDescription(deliveryRequest.getItemDescription());
        response.setItemSize(deliveryRequest.getItemSize());
        response.setPriority(deliveryRequest.getPriority());
        response.setPaymentAmount(deliveryRequest.getPaymentAmount());
        response.setPickupDate(deliveryRequest.getPickupDate());
        response.setPickupTime(deliveryRequest.getPickupTime());
        response.setContactInfo(deliveryRequest.getContactInfo());
        response.setSpecialInstructions(deliveryRequest.getSpecialInstructions());
        response.setStatus(deliveryRequest.getStatus());
        response.setMatchedTripId(deliveryRequest.getMatchedTrip() != null ? deliveryRequest.getMatchedTrip().getId() : null);
        response.setCompletedAt(deliveryRequest.getCompletedAt());
        response.setCreatedAt(deliveryRequest.getCreatedAt());
        return response;
    }
}
