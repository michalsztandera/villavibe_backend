package pl.villavibe.villavibe_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.villavibe.villavibe_backend.model.Business;

public interface BusinessRepository extends JpaRepository<Business, Long> {
}
