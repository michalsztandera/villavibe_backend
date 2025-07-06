package pl.villavibe.villavibe_backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import pl.villavibe.villavibe_backend.model.enums.DeviceCategory;

@Entity
@Table(name = "devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String type;
    private String deviceId;

    @Column(name = "uid")
    private String uid; // dodane

    @Enumerated(EnumType.STRING)
    private DeviceCategory category;

    @Column(name = "is_online")
    private Boolean online = false;

    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false)
    @JsonBackReference
    private Business business;
}