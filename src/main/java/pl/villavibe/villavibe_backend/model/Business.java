package pl.villavibe.villavibe_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "businesses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String address;

    private String nip;

    private String email;

    private String phone;

    @ManyToMany(mappedBy = "businesses")
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL)
    private List<Device> devices = new ArrayList<>();

}
