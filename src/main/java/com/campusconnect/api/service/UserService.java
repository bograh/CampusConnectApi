package com.campusconnect.api.service;

import com.campusconnect.api.security.JwtService;
import com.campusconnect.api.dto.auth.AuthResponseDTO;
import com.campusconnect.api.dto.auth.MultipartSignupRequestDTO;
import com.campusconnect.api.dto.auth.PhoneVerificationRequestDTO;
import com.campusconnect.api.dto.auth.SignInRequestDTO;
import com.campusconnect.api.dto.auth.SignupRequestDTO;
import com.campusconnect.api.dto.user.UserProfileResponseDTO;
import com.campusconnect.api.entity.User;
import com.campusconnect.api.entity.embedded.ImageData;
import com.campusconnect.api.entity.enums.VerificationStatus;
import com.campusconnect.api.exception.ConflictException;
import com.campusconnect.api.exception.NotFoundException;
import com.campusconnect.api.exception.UnauthorizedException;
import com.campusconnect.api.exception.ValidationException;
import com.campusconnect.api.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ImageService imageService;
    private final PhoneVerificationService phoneVerificationService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponseDTO signup(SignupRequestDTO request) {
        validateUniqueUser(request.getEmail(), request.getStudentId(), request.getPhoneNumber());

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .studentId(request.getStudentId())
                .phoneNumber(request.getPhoneNumber())
                .verificationStatus(VerificationStatus.PENDING_VERIFICATION)
                .build();

        if (request.getStudentIdImage() != null) {
            ImageData imageData = imageService.uploadImage(request.getStudentIdImage(), "student-ids");
            user.setStudentIdImage(imageData);
        }

        if (request.getSelfieImage() != null) {
            ImageData imageData = imageService.uploadImage(request.getSelfieImage(), "selfies");
            user.setSelfieImage(imageData);
        }

        user = userRepository.save(user);

        phoneVerificationService.sendVerificationCode(user.getPhoneNumber());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword())
        );
        String token = jwtService.generateAccessToken(authentication);

        AuthResponseDTO response = new AuthResponseDTO();
        response.setMessage("User created successfully. Please verify your phone number.");
        response.setToken(token);
        response.setUser(mapToUserResponse(user));

        return response;
    }

    @Transactional
    public AuthResponseDTO signupMultipart(MultipartSignupRequestDTO request) {
        validateUniqueUser(request.getEmail(), request.getStudentId(), request.getPhoneNumber());

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .studentId(request.getStudentId())
                .phoneNumber(request.getPhoneNumber())
                .verificationStatus(VerificationStatus.PENDING_VERIFICATION)
                .build();

        // Handle multipart file uploads
        if (request.getStudentIdImage() != null && !request.getStudentIdImage().isEmpty()) {
            try {
                ImageData imageData = imageService.uploadImage(request.getStudentIdImage(), "student-ids");
                user.setStudentIdImage(imageData);
            } catch (Exception e) {
                log.error("Failed to upload student ID image: {}", e.getMessage());
                throw new RuntimeException("Failed to upload student ID image: " + e.getMessage());
            }
        }

        if (request.getSelfieImage() != null && !request.getSelfieImage().isEmpty()) {
            try {
                ImageData imageData = imageService.uploadImage(request.getSelfieImage(), "selfies");
                user.setSelfieImage(imageData);
            } catch (Exception e) {
                log.error("Failed to upload selfie image: {}", e.getMessage());
                throw new RuntimeException("Failed to upload selfie image: " + e.getMessage());
            }
        }

        user = userRepository.save(user);

        phoneVerificationService.sendVerificationCode(user.getPhoneNumber());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword())
        );
        String token = jwtService.generateAccessToken(authentication);

        AuthResponseDTO response = new AuthResponseDTO();
        response.setMessage("User created successfully. Please verify your phone number.");
        response.setToken(token);
        response.setUser(mapToUserResponse(user));

        return response;
    }

    public AuthResponseDTO signin(SignInRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new NotFoundException("User not found"));

            // Update last seen
            user.setLastSeen(LocalDateTime.now());
            user.setIsOnline(true);
            userRepository.save(user);

            String token = jwtService.generateAccessToken(authentication);

            AuthResponseDTO response = new AuthResponseDTO();
            response.setMessage("Login successful");
            response.setToken(token);
            response.setUser(mapToUserResponse(user));

            return response;
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid credentials");
        }
    }

    public UserProfileResponseDTO getCurrentUser(HttpServletRequest request) {
        String email = jwtService.getEmailFromToken(request);
        if (email == null) {
            throw new UnauthorizedException("Invalid token");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return mapToUserProfileResponse(user);
    }

    @Transactional
    public void verifyPhone(HttpServletRequest request, PhoneVerificationRequestDTO phoneVerificationRequest) {
        String email = jwtService.getEmailFromToken(request);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        boolean isValid = phoneVerificationService.verifyCode(
                user.getPhoneNumber(), 
                phoneVerificationRequest.getVerificationCode()
        );

        if (!isValid) {
            throw new ValidationException("Invalid verification code", Map.of("code", "Invalid or expired"));
        }

        user.setPhoneVerified(true);
        userRepository.save(user);
    }

    public void resendVerificationCode(HttpServletRequest request) {
        String email = jwtService.getEmailFromToken(request);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        phoneVerificationService.sendVerificationCode(user.getPhoneNumber());
    }

    @Transactional
    public void logout(HttpServletRequest request) {
        String email = jwtService.getEmailFromToken(request);
        if (email != null) {
            userRepository.findByEmail(email).ifPresent(user -> {
                user.setIsOnline(false);
                user.setLastSeen(LocalDateTime.now());
                userRepository.save(user);
            });
        }
    }

    private void validateUniqueUser(String email, String studentId, String phoneNumber) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("User with this email already exists");
        }

        if (userRepository.existsByStudentId(studentId)) {
            throw new ConflictException("User with this student ID already exists");
        }

        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new ConflictException("User with this phone number already exists");
        }
    }

    private AuthResponseDTO.UserData mapToUserResponse(User user) {
        AuthResponseDTO.UserData userData = new AuthResponseDTO.UserData();
        userData.setId(user.getId());
        userData.setFirstName(user.getFirstName());
        userData.setLastName(user.getLastName());
        userData.setEmail(user.getEmail());
        userData.setStudentId(user.getStudentId());
        userData.setPhonNumber(user.getPhoneNumber());
        userData.setPhoneVerified(user.getPhoneVerified());
        userData.setStudentIdValidated(user.getStudentIdValidated());
        userData.setVerificationStatus(user.getVerificationStatus().name());
        userData.setRating(user.getRating().doubleValue());
        userData.setTotalDeliveries(user.getTotalDeliveries().longValue());
        userData.setJoinedDate(user.getJoinedDate());

        if (user.getStudentIdImage() != null) {
            AuthResponseDTO.StudentIDImg studentIdImg = new AuthResponseDTO.StudentIDImg();
            studentIdImg.setUrl(user.getStudentIdImage().getUrl());
            studentIdImg.setPublicId(user.getStudentIdImage().getPublicId());
            userData.setStudentIdImage(studentIdImg);
        }

        if (user.getSelfieImage() != null) {
            AuthResponseDTO.SelfieImg selfieImg = new AuthResponseDTO.SelfieImg();
            selfieImg.setUrl(user.getSelfieImage().getUrl());
            selfieImg.setPublicId(user.getSelfieImage().getPublicId());
            userData.setSelfieImage(selfieImg);
        }

        return userData;
    }

    private UserProfileResponseDTO mapToUserProfileResponse(User user) {
        UserProfileResponseDTO profile = new UserProfileResponseDTO();
        profile.setId(user.getId());
        profile.setFirstName(user.getFirstName());
        profile.setLastName(user.getLastName());
        profile.setEmail(user.getEmail());
        profile.setStudentId(user.getStudentId());
        profile.setPhoneNumber(user.getPhoneNumber());
        profile.setPhoneVerified(user.getPhoneVerified());
        profile.setStudentIdValidated(user.getStudentIdValidated());
        profile.setVerificationStatus(user.getVerificationStatus());
        profile.setProfileImage(user.getProfileImage());
        profile.setRating(user.getRating());
        profile.setTotalDeliveries(user.getTotalDeliveries());
        profile.setJoinedDate(user.getJoinedDate());
        profile.setIsOnline(user.getIsOnline());
        profile.setLastSeen(user.getLastSeen());
        return profile;
    }
}
