# CampusConnect Spring Boot Backend Migration Plan

## Overview

This guide provides a comprehensive plan for an AI agent to rebuild the CampusConnect backend from Next.js/Node.js to Spring Boot, maintaining all existing functionality while leveraging Spring Boot's enterprise-grade features.

## Current System Analysis

- **Campus delivery and rideshare platform** for KNUST students
- **User authentication** with student ID verification
- **Trip management** for travelers offering delivery services
- **Delivery request system** for package delivery needs
- **Smart matching algorithm** to connect trips with delivery requests
- **Real-time communication** and review system

## Phase 1: Project Setup & Dependencies

### 1.1 Spring Boot Starter Dependencies

```xml
<dependencies>
    <!-- Core Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Data & Database -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>

    <!-- Security & JWT -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.6</version>
    </dependency>

    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Email & SMS -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>

    <!-- File Upload -->
    <dependency>
        <groupId>com.cloudinary</groupId>
        <artifactId>cloudinary-http44</artifactId>
        <version>1.34.0</version>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 1.2 Application Properties

```yaml
# application.yml
spring:
  application:
    name: campusconnect-api

  datasource:
    url: jdbc:postgresql://localhost:5432/campusconnect
    username: ${DB_USER}
    password: ${DB_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

  security:
    jwt:
      secret: ${JWT_SECRET}
      expiration: 604800000 # 7 days

  mail:
    host: smtp.gmail.com
    port: 587
    username: ${EMAIL_USER}
    password: ${EMAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

cloudinary:
  cloud-name: ${CLOUDINARY_CLOUD_NAME}
  api-key: ${CLOUDINARY_API_KEY}
  api-secret: ${CLOUDINARY_API_SECRET}

app:
  cors:
    allowed-origins: ${ALLOWED_ORIGINS:http://localhost:3000}
```

## Phase 2: Database Design & JPA Entities

### 2.1 User Entity

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "student_id", unique = true, nullable = false)
    private String studentId;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status")
    private VerificationStatus verificationStatus;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "traveler", cascade = CascadeType.ALL)
    private List<Trip> trips = new ArrayList<>();

    @OneToMany(mappedBy = "requester", cascade = CascadeType.ALL)
    private List<DeliveryRequest> deliveryRequests = new ArrayList<>();
}

enum VerificationStatus {
    PENDING, VERIFIED, REJECTED
}
```

### 2.2 Trip Entity

```java
@Entity
@Table(name = "trips")
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "transport_method", nullable = false)
    private String transportMethod;

    @Column(name = "max_deliveries", nullable = false)
    private Integer maxDeliveries;

    @Column(name = "current_deliveries")
    private Integer currentDeliveries = 0;

    @Column(name = "price_per_delivery", nullable = false)
    private BigDecimal pricePerDelivery;

    @Column(name = "is_recurring")
    private Boolean isRecurring = false;

    @Enumerated(EnumType.STRING)
    private TripStatus status = TripStatus.ACTIVE;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL)
    private List<TripDeliveryMatch> matches = new ArrayList<>();
}

enum TripStatus {
    ACTIVE, COMPLETED, CANCELLED
}
```

### 2.3 DeliveryRequest Entity

```java
@Entity
@Table(name = "delivery_requests")
public class DeliveryRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @Embedded
    @AttributeOverrides({
        @AttributeOverrides(name = "type", column = @Column(name = "pickup_location_type")),
        @AttributeOverrides(name = "campusLocation", column = @Column(name = "pickup_campus_location")),
        @AttributeOverrides(name = "offCampusAddress", column = @Column(name = "pickup_off_campus_address"))
    })
    private Location pickupLocation;

    @Embedded
    @AttributeOverrides({
        @AttributeOverrides(name = "type", column = @Column(name = "dropoff_location_type")),
        @AttributeOverrides(name = "campusLocation", column = @Column(name = "dropoff_campus_location")),
        @AttributeOverrides(name = "offCampusAddress", column = @Column(name = "dropoff_off_campus_address"))
    })
    private Location dropoffLocation;

    @Column(name = "item_description", nullable = false)
    private String itemDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_size", nullable = false)
    private ItemSize itemSize;

    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.NORMAL;

    @Column(name = "payment_amount", nullable = false)
    private BigDecimal paymentAmount;

    @Column(name = "pickup_date", nullable = false)
    private LocalDate pickupDate;

    @Column(name = "pickup_time", nullable = false)
    private String pickupTime;

    @Column(name = "contact_info")
    private String contactInfo;

    @Column(name = "special_instructions")
    private String specialInstructions;

    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.PENDING;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

enum ItemSize {
    SMALL, MEDIUM, LARGE
}

enum Priority {
    NORMAL, HIGH, URGENT
}

enum RequestStatus {
    PENDING, MATCHED, IN_TRANSIT, DELIVERED, CANCELLED
}
```

### 2.4 Location Embeddable

```java
@Embeddable
public class Location {
    @Enumerated(EnumType.STRING)
    private LocationType type;

    @Enumerated(EnumType.STRING)
    private CampusLocation campusLocation;

    private String offCampusAddress;
}

enum LocationType {
    CAMPUS, OFF_CAMPUS
}

enum CampusLocation {
    UNIVERSITY_HALL, UNITY_HALL, AFRICA_HALL,
    QUEEN_ELIZABETH_II_HALL, INDEPENDENCE_HALL,
    REPUBLIC_HALL, OLD_BRUNEI, NEW_BRUNEI,
    COMPLEX, BABY_BRUNEI, HALL_7,
    COLLEGE_OF_SCIENCE, COLLEGE_OF_HEALTH_SCIENCES,
    COLLEGE_OF_HUMANITIES_AND_SOCIAL_SCIENCES,
    COLLEGE_OF_ENGINEERING,
    COLLEGE_OF_AGRICULTURE_AND_NATURAL_RESOURCES,
    COLLEGE_OF_ART_AND_BUILT_ENVIRONMENT,
    CASELY_HAYFORD_BUILDING
}
```

## Phase 3: Security & Authentication

### 3.1 JWT Security Configuration

```java
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
            .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/trips", "/api/delivery-requests").authenticated()
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

### 3.2 JWT Token Service

```java
@Service
public class JwtTokenService {

    @Value("${spring.security.jwt.secret}")
    private String secret;

    @Value("${spring.security.jwt.expiration}")
    private Long expiration;

    public String generateToken(UserDetails userDetails, User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("verificationStatus", user.getVerificationStatus().name());

        return createToken(claims, userDetails.getUsername());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // Additional JWT utility methods...
}
```

## Phase 4: API Controllers

### 4.1 Authentication Controller

```java
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@Valid @RequestBody SignupRequest request) {
        try {
            AuthResponse response = authService.signup(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Registration successful"));
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<AuthResponse>> signin(@Valid @RequestBody SigninRequest request) {
        try {
            AuthResponse response = authService.signin(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/verify-phone")
    public ResponseEntity<ApiResponse<String>> verifyPhone(@Valid @RequestBody PhoneVerificationRequest request) {
        try {
            authService.verifyPhone(request);
            return ResponseEntity.ok(ApiResponse.success("Phone verified successfully"));
        } catch (InvalidVerificationCodeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserProfile>> getCurrentUser(Authentication authentication) {
        UserProfile profile = authService.getCurrentUserProfile(authentication);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }
}
```

### 4.2 Trip Controller

```java
@RestController
@RequestMapping("/api/trips")
@PreAuthorize("isAuthenticated()")
public class TripController {

    @Autowired
    private TripService tripService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TripDTO>>> getTrips(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "all") String filter,
            Authentication authentication) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TripDTO> trips = tripService.getTrips(filter, authentication, pageable);

        return ResponseEntity.ok(ApiResponse.success(trips));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TripDTO>> createTrip(
            @Valid @RequestBody CreateTripRequest request,
            Authentication authentication) {

        TripDTO trip = tripService.createTrip(request, authentication);
        return ResponseEntity.ok(ApiResponse.success(trip, "Trip created successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteTrip(
            @PathVariable Long id,
            Authentication authentication) {

        tripService.deleteTrip(id, authentication);
        return ResponseEntity.ok(ApiResponse.success("Trip deleted successfully"));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<ApiResponse<JoinTripResponse>> joinTrip(
            @PathVariable Long id,
            Authentication authentication) {

        JoinTripResponse response = tripService.joinTrip(id, authentication);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
```

### 4.3 Delivery Request Controller

```java
@RestController
@RequestMapping("/api/delivery-requests")
@PreAuthorize("isAuthenticated()")
public class DeliveryRequestController {

    @Autowired
    private DeliveryRequestService deliveryRequestService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DeliveryRequestDTO>>> getDeliveryRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "all") String filter,
            Authentication authentication) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<DeliveryRequestDTO> requests = deliveryRequestService.getDeliveryRequests(filter, authentication, pageable);

        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DeliveryRequestDTO>> createDeliveryRequest(
            @Valid @RequestBody CreateDeliveryRequestRequest request,
            Authentication authentication) {

        DeliveryRequestDTO deliveryRequest = deliveryRequestService.createDeliveryRequest(request, authentication);
        return ResponseEntity.ok(ApiResponse.success(deliveryRequest, "Delivery request created successfully"));
    }

    @PostMapping("/{id}/offer")
    public ResponseEntity<ApiResponse<DeliveryOfferResponse>> offerToDeliver(
            @PathVariable Long id,
            Authentication authentication) {

        DeliveryOfferResponse response = deliveryRequestService.offerToDeliver(id, authentication);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
```

### 4.4 Matching Controller

```java
@RestController
@RequestMapping("/api/matches")
@PreAuthorize("isAuthenticated()")
public class MatchingController {

    @Autowired
    private MatchingService matchingService;

    @GetMapping
    public ResponseEntity<ApiResponse<MatchingResponse>> getMatches(
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(defaultValue = "50") int minScore,
            Authentication authentication) {

        MatchingResponse matches = matchingService.findMatches(type, minScore, authentication);
        return ResponseEntity.ok(ApiResponse.success(matches));
    }
}
```

## Phase 5: Service Layer Implementation

### 5.1 Authentication Service

```java
@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;

    public AuthResponse signup(SignupRequest request) {
        // Validate student ID format
        if (!StudentIdValidator.isValid(request.getStudentId())) {
            throw new InvalidStudentIdException("Invalid student ID format");
        }

        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setStudentId(request.getStudentId());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setVerificationStatus(VerificationStatus.PENDING);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // Send verification email
        emailService.sendVerificationEmail(savedUser);

        // Generate JWT token
        UserDetails userDetails = createUserDetails(savedUser);
        String token = jwtTokenService.generateToken(userDetails, savedUser);

        return AuthResponse.builder()
            .token(token)
            .user(UserMapper.toDTO(savedUser))
            .build();
    }

    public AuthResponse signin(SigninRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        if (!user.getIsActive()) {
            throw new AccountDisabledException("Account is disabled");
        }

        // Update last login
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Generate JWT token
        UserDetails userDetails = createUserDetails(user);
        String token = jwtTokenService.generateToken(userDetails, user);

        return AuthResponse.builder()
            .token(token)
            .user(UserMapper.toDTO(user))
            .build();
    }

    // Additional authentication methods...
}
```

### 5.2 Trip Service

```java
@Service
@Transactional
public class TripService {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    public Page<TripDTO> getTrips(String filter, Authentication authentication, Pageable pageable) {
        User currentUser = getCurrentUser(authentication);

        Page<Trip> trips;
        switch (filter.toLowerCase()) {
            case "my-trips":
                trips = tripRepository.findByTraveler(currentUser, pageable);
                break;
            case "available":
                trips = tripRepository.findAvailableTrips(currentUser.getId(), pageable);
                break;
            default:
                trips = tripRepository.findAllActiveTrips(pageable);
        }

        return trips.map(TripMapper::toDTO);
    }

    public TripDTO createTrip(CreateTripRequest request, Authentication authentication) {
        User traveler = getCurrentUser(authentication);

        // Validate locations
        LocationValidator.validateLocation(request.getFromLocation());
        LocationValidator.validateLocation(request.getToLocation());

        Trip trip = new Trip();
        trip.setTraveler(traveler);
        trip.setFromLocation(LocationMapper.fromDTO(request.getFromLocation()));
        trip.setToLocation(LocationMapper.fromDTO(request.getToLocation()));
        trip.setDepartureTime(request.getDepartureTime());
        trip.setTransportMethod(request.getTransportMethod());
        trip.setMaxDeliveries(request.getMaxDeliveries());
        trip.setPricePerDelivery(request.getPricePerDelivery());
        trip.setIsRecurring(request.getIsRecurring());
        trip.setStatus(TripStatus.ACTIVE);
        trip.setCreatedAt(LocalDateTime.now());
        trip.setUpdatedAt(LocalDateTime.now());

        Trip savedTrip = tripRepository.save(trip);
        return TripMapper.toDTO(savedTrip);
    }

    public void deleteTrip(Long tripId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new TripNotFoundException("Trip not found"));

        if (!trip.getTraveler().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only delete your own trips");
        }

        if (trip.getCurrentDeliveries() > 0) {
            throw new IllegalStateException("Cannot delete trip with active deliveries");
        }

        tripRepository.delete(trip);
    }

    public JoinTripResponse joinTrip(Long tripId, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new TripNotFoundException("Trip not found"));

        // Validation logic
        if (trip.getTraveler().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You cannot join your own trip");
        }

        if (trip.getStatus() != TripStatus.ACTIVE) {
            throw new IllegalStateException("This trip is no longer available");
        }

        if (trip.getCurrentDeliveries() >= trip.getMaxDeliveries()) {
            throw new IllegalStateException("This trip is already full");
        }

        // TODO: Create join request entity and notification system

        return JoinTripResponse.builder()
            .message(String.format("Interest registered! We'll notify %s that you want to join their trip.",
                trip.getTraveler().getFirstName()))
            .tripDetails(TripMapper.toDTO(trip))
            .build();
    }

    // Additional trip methods...
}
```

### 5.3 Matching Service

```java
@Service
@Transactional(readOnly = true)
public class MatchingService {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private DeliveryRequestRepository deliveryRequestRepository;

    public MatchingResponse findMatches(String type, int minScore, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        List<Match> matches = new ArrayList<>();

        switch (type.toLowerCase()) {
            case "my-trips":
                matches = findDeliveryRequestsForUserTrips(currentUser, minScore);
                break;
            case "my-requests":
                matches = findTripsForUserRequests(currentUser, minScore);
                break;
            default:
                // Could implement general matching logic
                matches = findAllMatches(currentUser, minScore);
        }

        return MatchingResponse.builder()
            .matches(matches)
            .totalMatches(matches.size())
            .build();
    }

    private List<Match> findDeliveryRequestsForUserTrips(User user, int minScore) {
        List<Trip> userTrips = tripRepository.findActiveByTraveler(user);
        List<DeliveryRequest> availableRequests = deliveryRequestRepository.findPendingExcludingUser(user.getId());

        return userTrips.stream()
            .flatMap(trip -> availableRequests.stream()
                .map(request -> createMatch(trip, request))
                .filter(match -> match.getScore() >= minScore))
            .sorted((m1, m2) -> Integer.compare(m2.getScore(), m1.getScore()))
            .collect(Collectors.toList());
    }

    private Match createMatch(Trip trip, DeliveryRequest request) {
        int score = calculateMatchScore(trip, request);

        return Match.builder()
            .type(MatchType.TRIP_REQUEST_MATCH)
            .score(score)
            .trip(TripMapper.toSummaryDTO(trip))
            .request(DeliveryRequestMapper.toSummaryDTO(request))
            .build();
    }

    private int calculateMatchScore(Trip trip, DeliveryRequest request) {
        int score = 0;

        // Location matching (highest priority)
        if (LocationMatcher.match(trip.getFromLocation(), request.getPickupLocation())) {
            score += 40;
        }
        if (LocationMatcher.match(trip.getToLocation(), request.getDropoffLocation())) {
            score += 40;
        }

        // Time compatibility
        long hoursDiff = ChronoUnit.HOURS.between(
            trip.getDepartureTime(),
            request.getPickupDate().atTime(LocalTime.parse(request.getPickupTime()))
        );

        if (Math.abs(hoursDiff) <= 2) score += 15;
        else if (Math.abs(hoursDiff) <= 6) score += 10;
        else if (Math.abs(hoursDiff) <= 24) score += 5;

        // Available capacity
        if (trip.getCurrentDeliveries() < trip.getMaxDeliveries()) {
            score += 5;
        }

        return score;
    }

    // Additional matching methods...
}
```

## Phase 6: Repository Layer

### 6.1 Custom Repository Queries

```java
@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    Page<Trip> findByTraveler(User traveler, Pageable pageable);

    @Query("SELECT t FROM Trip t WHERE t.traveler.id != :userId AND t.status = 'ACTIVE' " +
           "AND t.currentDeliveries < t.maxDeliveries")
    Page<Trip> findAvailableTrips(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t FROM Trip t WHERE t.status = 'ACTIVE'")
    Page<Trip> findAllActiveTrips(Pageable pageable);

    List<Trip> findActiveByTraveler(User traveler);

    @Query("SELECT t FROM Trip t WHERE t.traveler = :traveler AND t.status = 'ACTIVE'")
    List<Trip> findActiveByTraveler(@Param("traveler") User traveler);
}

@Repository
public interface DeliveryRequestRepository extends JpaRepository<DeliveryRequest, Long> {

    Page<DeliveryRequest> findByRequester(User requester, Pageable pageable);

    @Query("SELECT dr FROM DeliveryRequest dr WHERE dr.requester.id != :userId AND dr.status = 'PENDING'")
    Page<DeliveryRequest> findAvailableRequests(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT dr FROM DeliveryRequest dr WHERE dr.status = 'PENDING'")
    Page<DeliveryRequest> findAllPendingRequests(Pageable pageable);

    @Query("SELECT dr FROM DeliveryRequest dr WHERE dr.requester.id != :userId AND dr.status = 'PENDING'")
    List<DeliveryRequest> findPendingExcludingUser(@Param("userId") Long userId);
}

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    Boolean existsByStudentId(String studentId);

    @Query("SELECT u FROM User u WHERE u.verificationStatus = 'PENDING'")
    List<User> findPendingVerification();
}
```

## Phase 7: Testing Strategy

### 7.1 Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TripService tripService;

    @Test
    void createTrip_ValidRequest_ShouldCreateTrip() {
        // Given
        User traveler = createTestUser();
        CreateTripRequest request = createValidTripRequest();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(traveler));
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> {
            Trip trip = invocation.getArgument(0);
            trip.setId(1L);
            return trip;
        });

        Authentication auth = createMockAuthentication(traveler.getEmail());

        // When
        TripDTO result = tripService.createTrip(request, auth);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTransportMethod()).isEqualTo(request.getTransportMethod());
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void deleteTrip_NotTripOwner_ShouldThrowException() {
        // Given
        User owner = createTestUser();
        User otherUser = createTestUser();
        otherUser.setId(2L);
        otherUser.setEmail("other@example.com");

        Trip trip = createTestTrip();
        trip.setTraveler(owner);

        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherUser));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        Authentication auth = createMockAuthentication("other@example.com");

        // When & Then
        assertThatThrownBy(() -> tripService.deleteTrip(1L, auth))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("You can only delete your own trips");
    }
}
```

### 7.2 Integration Tests

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class TripControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    private String authToken;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = createAndSaveTestUser();
        UserDetails userDetails = createUserDetails(testUser);
        authToken = jwtTokenService.generateToken(userDetails, testUser);
    }

    @Test
    void createTrip_ValidRequest_ShouldReturnCreatedTrip() {
        // Given
        CreateTripRequest request = createValidTripRequest();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<CreateTripRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            "/api/trips", entity, ApiResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();

        // Verify in database
        List<Trip> trips = tripRepository.findAll();
        assertThat(trips).hasSize(1);
        assertThat(trips.get(0).getTraveler().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void getTrips_WithMyTripsFilter_ShouldReturnUserTrips() {
        // Given
        Trip trip1 = createAndSaveTrip(testUser);
        User otherUser = createAndSaveTestUser("other@example.com");
        Trip trip2 = createAndSaveTrip(otherUser);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // When
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
            "/api/trips?filter=my-trips", HttpMethod.GET, entity, ApiResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // Additional assertions for response content...
    }
}
```

## Phase 8: Configuration & Deployment

### 8.1 Docker Configuration

```dockerfile
# Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/campusconnect-api-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 8.2 Docker Compose

```yaml
# docker-compose.yml
version: "3.8"

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - DB_URL=jdbc:postgresql://db:5432/campusconnect
      - DB_USER=campusconnect
      - DB_PASSWORD=password
    depends_on:
      - db

  db:
    image: postgres:15
    environment:
      - POSTGRES_DB=campusconnect
      - POSTGRES_USER=campusconnect
      - POSTGRES_PASSWORD=password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

volumes:
  postgres_data:
```

## Phase 9: Implementation Checklist

### Phase 9.1: Core Setup ✓

- [ ] Set up Spring Boot project with required dependencies
- [ ] Configure application.yml with database and security settings
- [ ] Set up PostgreSQL database connection
- [ ] Implement basic project structure

### Phase 9.2: Data Layer ✓

- [ ] Create all JPA entities (User, Trip, DeliveryRequest, etc.)
- [ ] Implement repository interfaces with custom queries
- [ ] Set up database migration scripts (Flyway/Liquibase)
- [ ] Test database connectivity and entity mappings

### Phase 9.3: Security Implementation ✓

- [ ] Implement JWT authentication and authorization
- [ ] Create security configuration
- [ ] Implement password encoding and validation
- [ ] Add CORS configuration for frontend integration

### Phase 9.4: Business Logic ✓

- [ ] Implement all service layer classes
- [ ] Create DTOs and mapping utilities
- [ ] Implement validation logic
- [ ] Add business rule enforcement

### Phase 9.5: API Layer ✓

- [ ] Create all REST controllers
- [ ] Implement request/response DTOs
- [ ] Add validation annotations
- [ ] Implement error handling and exception mappers

### Phase 9.6: Advanced Features ✓

- [ ] Implement smart matching algorithm
- [ ] Add email notification service
- [ ] Implement file upload for user profiles
- [ ] Add SMS verification service

### Phase 9.7: Testing ✓

- [ ] Write comprehensive unit tests
- [ ] Implement integration tests
- [ ] Add API testing with TestRestTemplate
- [ ] Performance testing for matching algorithm

### Phase 9.8: Documentation & Deployment ✓

- [ ] Generate API documentation (OpenAPI/Swagger)
- [ ] Set up Docker containerization
- [ ] Prepare deployment configurations
- [ ] Create monitoring and logging setup

## Additional Considerations

### Performance Optimization

- Implement database indexing strategy
- Add caching layer (Redis) for frequent queries
- Optimize matching algorithm with spatial queries
- Implement pagination for large datasets

### Monitoring & Observability

- Add Actuator endpoints for health checks
- Implement logging with structured format
- Set up metrics collection (Micrometer/Prometheus)
- Add distributed tracing capabilities

### Security Enhancements

- Implement rate limiting
- Add request validation and sanitization
- Set up security headers
- Implement audit logging

This comprehensive plan provides a structured approach for an AI agent to successfully migrate the CampusConnect backend to Spring Boot while maintaining all existing functionality and adding enterprise-grade features.
