package com.langia.backend.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.langia.backend.exception.EmailAlreadyExistsException;
import com.langia.backend.exception.InvalidEmailChangeCodeException;
import com.langia.backend.exception.RateLimitExceededException;
import com.langia.backend.exception.UserNotFoundException;
import com.langia.backend.model.EmailChangeRequest;
import com.langia.backend.model.User;
import com.langia.backend.repository.EmailChangeRequestRepository;
import com.langia.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for handling email change requests.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailChangeService {

    private final UserRepository userRepository;
    private final EmailChangeRequestRepository requestRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final EmailChangeRateLimitService rateLimitService;

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRATION_MINUTES = 15;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Request an email change. Generates a 6-digit verification code and sends it to the new email.
     *
     * @param userId   The user requesting the change
     * @param newEmail The new email address
     */
    @Transactional
    public void requestEmailChange(UUID userId, String newEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Check if new email is already in use
        if (userRepository.existsByEmail(newEmail)) {
            throw new EmailAlreadyExistsException("Email already in use");
        }

        // Invalidate any previous pending requests
        requestRepository.invalidateAllUserRequests(userId, LocalDateTime.now());

        // Generate 6-digit code
        String code = generateCode();
        String tokenHash = passwordEncoder.encode(code);

        EmailChangeRequest request = EmailChangeRequest.builder()
                .user(user)
                .newEmail(newEmail)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES))
                .build();

        requestRepository.save(request);

        // Send verification email
        emailService.sendEmailChangeVerification(newEmail, user.getName(), code);
        log.info("Email change requested for user {} to new email {}", userId, maskEmail(newEmail));
    }

    /**
     * Confirm an email change using the verification code.
     * Implements rate limiting to prevent brute-force attacks on the 6-digit code.
     *
     * @param userId The user confirming the change
     * @param code   The verification code
     * @throws RateLimitExceededException if too many failed attempts
     */
    @Transactional
    public void confirmEmailChange(UUID userId, String code) {
        // Check rate limit before processing
        if (rateLimitService.isAttemptLimitReached(userId)) {
            long lockoutTime = rateLimitService.getLockoutTimeRemaining(userId);
            log.warn("Email change verification blocked for user {} - rate limit exceeded", userId);
            throw new RateLimitExceededException(lockoutTime > 0 ? lockoutTime : 3600);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Find valid request using optimized query (only active, unexpired requests)
        EmailChangeRequest request = findValidRequest(userId, code);

        if (request == null) {
            // Record failed attempt and check if should lock
            boolean locked = rateLimitService.recordFailedAttempt(userId);
            if (locked) {
                long lockoutTime = rateLimitService.getLockoutTimeRemaining(userId);
                throw new RateLimitExceededException(lockoutTime > 0 ? lockoutTime : 3600);
            }
            throw new InvalidEmailChangeCodeException("Invalid or expired code");
        }

        // Revalidate that target email is still available (race condition protection)
        if (userRepository.existsByEmail(request.getNewEmail())) {
            request.markAsUsed(); // Invalidate this request
            requestRepository.save(request);
            throw new EmailAlreadyExistsException("Email is no longer available");
        }

        // Update email
        String oldEmail = user.getEmail();
        user.setEmail(request.getNewEmail());
        userRepository.save(user);

        // Mark request as used
        request.markAsUsed();
        requestRepository.save(request);

        // Clear rate limit attempts on successful verification
        rateLimitService.clearAttempts(userId);

        // Audit log - AC-AU-001
        auditService.logUpdate(
                "USER_EMAIL",
                userId,
                java.util.Map.of("email", oldEmail),
                java.util.Map.of("email", request.getNewEmail()),
                userId
        );

        // Notify old email
        emailService.sendEmailChangedNotification(oldEmail, user.getName(), request.getNewEmail());

        log.info("Email changed for user {} from {} to {}", userId, maskEmail(oldEmail), maskEmail(request.getNewEmail()));
    }

    /**
     * Find a valid (not expired, not used) request matching the code.
     * Uses optimized query to fetch only active requests for the user.
     */
    private EmailChangeRequest findValidRequest(UUID userId, String code) {
        // Get only active requests for user (optimized query)
        return requestRepository.findActiveRequestsByUserId(userId, LocalDateTime.now()).stream()
                .filter(r -> passwordEncoder.matches(code, r.getTokenHash()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Generate a 6-digit numeric code.
     */
    private String generateCode() {
        int code = RANDOM.nextInt(1000000);
        return String.format("%06d", code);
    }

    /**
     * Mask email for logging.
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        String maskedLocal = localPart.length() > 2
                ? localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1)
                : "***";

        return maskedLocal + "@" + domain;
    }
}
