package pl.villavibe.villavibe_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pl.villavibe.villavibe_backend.model.User;
import pl.villavibe.villavibe_backend.repository.UserRepository;
import pl.villavibe.villavibe_backend.model.Business;


import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    @GetMapping("/me/businesses")
    public ResponseEntity<List<Business>> getMyBusinesses(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .map(user -> ResponseEntity.ok(user.getBusinesses()))
                .orElse(ResponseEntity.notFound().build());
    }
    @PutMapping("/me/active-business")
    public ResponseEntity<?> setActiveBusiness(Authentication authentication, @RequestBody Long businessId) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .map(user -> {
                    user.setActiveBusinessId(businessId);
                    userRepository.save(user);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
    // 🔐 Endpoint zwracający aktualnego użytkownika
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
