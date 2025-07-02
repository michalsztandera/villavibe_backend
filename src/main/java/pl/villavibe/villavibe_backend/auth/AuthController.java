package pl.villavibe.villavibe_backend.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pl.villavibe.villavibe_backend.model.User;
import pl.villavibe.villavibe_backend.repository.UserRepository;
import pl.villavibe.villavibe_backend.model.enums.Role;


import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("User found: " + user.getEmail());
            System.out.println("Encoded password in DB: " + user.getPassword());
            System.out.println("Raw password: " + request.getPassword());

            boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
            System.out.println("Password match: " + matches);

            if (matches) {
                String token = jwtService.generateToken(request.getEmail());
                return new AuthResponse(token);
            }
        } else {
            System.out.println("User not found: " + request.getEmail());
        }

        throw new RuntimeException("Invalid credentials");
    }
    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        // Sprawdź, czy użytkownik o takim emailu już istnieje
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User with email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(Role.USER);
        userRepository.save(user);

        // Wygeneruj token JWT
        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(token);
    }


}
