package pl.villavibe.villavibe_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.villavibe.villavibe_backend.model.Business;
import pl.villavibe.villavibe_backend.model.Device;
import pl.villavibe.villavibe_backend.model.enums.DeviceCategory;
import pl.villavibe.villavibe_backend.repository.BusinessRepository;
import pl.villavibe.villavibe_backend.repository.DeviceRepository;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceRepository deviceRepository;
    private final BusinessRepository businessRepository;

    @PostMapping("/business/{businessId}")
    public Device addDevice(@PathVariable Long businessId, @RequestBody Device device) {
        Business business = businessRepository.findById(businessId).orElseThrow();
        device.setBusiness(business);
        return deviceRepository.save(device);
    }

    @GetMapping("/business/{businessId}")
    public List<Device> getDevicesByBusiness(@PathVariable Long businessId) {
        return deviceRepository.findByBusinessId(businessId);
    }

    @PutMapping("/{deviceId}")
    public Device updateDevice(@PathVariable Long deviceId, @RequestBody Device updatedDevice) {
        Device existing = deviceRepository.findById(deviceId).orElseThrow();
        existing.setName(updatedDevice.getName());
        existing.setType(updatedDevice.getType());
        existing.setDeviceId(updatedDevice.getDeviceId());
        existing.setCategory(updatedDevice.getCategory());
        existing.setOnline(updatedDevice.getOnline());
        return deviceRepository.save(existing);
    }

    @PatchMapping("/{deviceId}/status")
    public Device updateDeviceStatus(@PathVariable Long deviceId, @RequestParam boolean online) {
        Device device = deviceRepository.findById(deviceId).orElseThrow();
        device.setOnline(online);
        return deviceRepository.save(device);
    }

    @GetMapping("/business/{businessId}/category/{category}")
    public List<Device> getDevicesByBusinessAndCategory(
            @PathVariable Long businessId,
            @PathVariable String category
    ) {
        return deviceRepository.findByBusinessIdAndCategory(businessId, DeviceCategory.valueOf(category.toUpperCase()));
    }

    @DeleteMapping("/{deviceId}")
    public void deleteDevice(@PathVariable Long deviceId) {
        deviceRepository.deleteById(deviceId);
    }
}
