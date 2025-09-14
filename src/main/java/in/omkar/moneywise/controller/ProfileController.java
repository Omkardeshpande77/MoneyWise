package in.omkar.moneywise.controller;

import in.omkar.moneywise.dto.AuthenticatioDTO;
import in.omkar.moneywise.dto.ProfileDTO;
import in.omkar.moneywise.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    @PostMapping("/register")
   public ResponseEntity<ProfileDTO> registerProfile( @RequestBody ProfileDTO profileDTO) {
        ProfileDTO registeredProfile = profileService.registerProfile(profileDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredProfile);
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activateProfile( @RequestParam String code) {
        boolean activated = profileService.activateProfile(code);
        if (activated) {
            return ResponseEntity.ok("Profile activated successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid activation code.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String,Object>>login(@RequestBody AuthenticatioDTO authenticatioDTO) {
        try {
            if (authenticatioDTO == null || authenticatioDTO.getEmail() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid request"));
            }
            if (!profileService.isActive(authenticatioDTO.getEmail())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Profile is not activated"));
            }
            Map<String, Object> response = profileService.authenticateAndGenerateToken(authenticatioDTO);
            return ResponseEntity.ok(response);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
}
