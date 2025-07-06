package pl.villavibe.villavibe_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.villavibe.villavibe_backend.model.Device;
import pl.villavibe.villavibe_backend.model.enums.DeviceCategory;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findByBusinessId(Long businessId);
    List<Device> findByBusinessIdAndCategory(Long businessId, DeviceCategory category);
    Optional<Device> findByDeviceIdAndBusinessId(String deviceId, Long businessId);

    boolean existsByDeviceId(String deviceId);
}
