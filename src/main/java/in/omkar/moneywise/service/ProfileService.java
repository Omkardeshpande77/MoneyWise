package in.omkar.moneywise.service;

import in.omkar.moneywise.dto.AuthenticatioDTO;
import in.omkar.moneywise.dto.ProfileDTO;
import in.omkar.moneywise.entity.ProfileEntity;
import in.omkar.moneywise.repository.ProfileRepository;
import in.omkar.moneywise.util.JwtUtil;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public ProfileDTO registerProfile(ProfileDTO profileDTO) {
        // Logic to register a new profile
        ProfileEntity newProfile =  toEntity(profileDTO);
        newProfile.setActivationCode(UUID.randomUUID().toString());
        newProfile = profileRepository.save(newProfile);
        // Send activation email logic can be added here
        String activationLink = "http://localhost:8080/api/v1/activate?code=" + newProfile.getActivationCode();
        String subject = "Activate your MoneyWise account";
        String body = "Dear " + newProfile.getName() + ",\n\nPlease activate your account by clicking the link below:\n" + activationLink + "\n\nThank you!";
        try {
            emailService.sendEmail(newProfile.getEmail(), subject, body);
        }
         catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return toDTO(newProfile);
    }
    public boolean activateProfile(String code) {
        return profileRepository.findByActivationCode(code).map(
                profile->{
                    profile.setIsActive(true);
                    profileRepository.save(profile);
                    return true;
                }
        ).orElse(false);
    }
    public ProfileEntity toEntity(ProfileDTO profileDTO) {
        return ProfileEntity.builder()
                .id(profileDTO.getId())
                .name(profileDTO.getName())
                .password(passwordEncoder.encode(profileDTO.getPassword()))
                .email(profileDTO.getEmail())
                .profileImageUrl(profileDTO.getProfileImageUrl())
                .createdAt(profileDTO.getCreatedAt())
                .updatedAt(profileDTO.getUpdatedAt())
                .build();
    }
    public ProfileDTO toDTO(ProfileEntity profileEntity) {
        return ProfileDTO.builder()
                .id(profileEntity.getId())
                .name(profileEntity.getName())
                .email(profileEntity.getEmail())
                .profileImageUrl(profileEntity.getProfileImageUrl())
                .createdAt(profileEntity.getCreatedAt())
                .updatedAt(profileEntity.getUpdatedAt())
                .build();
    }

    public boolean isActive(String email) {
        return profileRepository.findByEmail(email)
                .map(ProfileEntity::getIsActive)
                .orElse(false);
    }

    public ProfileEntity getCurrentUser() {
       Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
       return profileRepository.findByEmail(authentication.getName()).orElseThrow(()->new RuntimeException("User not found"));
    }

    public ProfileDTO getPublicProfile(String email){
        ProfileEntity profileEntity =null;
        if (email == null){
            profileEntity=getCurrentUser();
        }
        else {
            profileEntity=profileRepository.findByEmail(email).orElseThrow(()->new RuntimeException("User not found"));
        }
        return ProfileDTO.builder()
                .id(profileEntity.getId())
                .name(profileEntity.getName())
                .email(profileEntity.getEmail())
                .profileImageUrl(profileEntity.getProfileImageUrl())
                .createdAt(profileEntity.getCreatedAt())
                .updatedAt(profileEntity.getUpdatedAt())
                .build();
    }
    public Map<String, Object> authenticateAndGenerateToken(AuthenticatioDTO authenticatioDTO) {
        try {
            authenticationManager.authenticate(new
                    UsernamePasswordAuthenticationToken(authenticatioDTO.getEmail(),authenticatioDTO.getPassword()));
            String token=jwtUtil.generateToken(authenticatioDTO.getEmail());
            return Map.of(
                    "token",token,
                    "user",getPublicProfile(authenticatioDTO.getEmail())
            );
        } catch (Exception e) {
            throw new RuntimeException("Invalid Email or Password");
        }
    }
}
