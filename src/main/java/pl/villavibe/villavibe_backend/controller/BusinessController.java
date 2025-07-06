package pl.villavibe.villavibe_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.villavibe.villavibe_backend.model.Business;
import pl.villavibe.villavibe_backend.model.User;
import pl.villavibe.villavibe_backend.repository.BusinessRepository;
import pl.villavibe.villavibe_backend.repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/businesses")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // lub dostosuj
public class BusinessController {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;

    // 🔹 Lista wszystkich działalności
    @GetMapping
    public List<Business> getAll() {
        return businessRepository.findAll();
    }

    // 🔹 Pobierz jedną działalność po ID
    @GetMapping("/{id}")
    public Business getBusinessById(@PathVariable Long id) {
        return businessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Business not found with ID: " + id));
    }

    // 🔹 Dodaj nową działalność
    @PostMapping
    public Business create(@RequestBody Business business) {
        return businessRepository.save(business);
    }

    // 🔹 Edytuj działalność
    @PutMapping("/{id}")
    public Business update(@PathVariable Long id, @RequestBody Business updated) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Business not found with ID: " + id));

        business.setName(updated.getName());
        business.setAddress(updated.getAddress());
        business.setNip(updated.getNip());
        business.setEmail(updated.getEmail());
        business.setPhone(updated.getPhone());

        return businessRepository.save(business);
    }

    // 🔹 Usuń działalność
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Business not found with ID: " + id));
        businessRepository.delete(business);
    }

    // 🔹 Przypisz użytkownika do działalności
    @PostMapping("/{businessId}/add-user/{userId}")
    public Business addUserToBusiness(@PathVariable Long businessId, @PathVariable Long userId) {
        Business business = businessRepository.findById(businessId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();
        business.getUsers().add(user);
        user.getBusinesses().add(business);
        businessRepository.save(business);
        userRepository.save(user);
        return business;
    }

    // 🔹 Lista użytkowników danej działalności
    @GetMapping("/{businessId}/users")
    public List<User> getUsersForBusiness(@PathVariable Long businessId) {
        Business business = businessRepository.findById(businessId).orElseThrow();
        return business.getUsers();
    }
}
